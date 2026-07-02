package com.segunfrancis.newsfeed.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.segunfrancis.newsfeed.domain.BookmarkRepository
import com.segunfrancis.newsfeed.domain.NewsFeedRepository
import com.segunfrancis.newsfeed.ui.components.menuItems
import com.segunfrancis.newsfeed.ui.models.HomeArticle
import com.segunfrancis.newsfeed.ui.models.toDomainSavedArticle
import com.segunfrancis.newsfeed.ui.models.toHomeArticle
import com.segunfrancis.newsfeed.util.handleThrowable
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: NewsFeedRepository,
    private val bookmarkRepository: BookmarkRepository
) : ViewModel() {

    private val _bookmarkAction = MutableSharedFlow<SearchBookmarkActions>()
    val bookmarkAction: SharedFlow<SearchBookmarkActions> = _bookmarkAction.asSharedFlow()

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        throwable.printStackTrace()
        viewModelScope.launch {
            _bookmarkAction.emit(SearchBookmarkActions.OnError(throwable.handleThrowable()))
        }
    }

    private val _pendingSearchQuery = MutableStateFlow("")
    val pendingSearchQuery: StateFlow<String> = _pendingSearchQuery.asStateFlow()

    /**
     * Exposed to the state holder as a plain suspend function.
     * The ViewModel provides the work; the state holder owns the lifetime.
     * Hilt injects the repository here — the state holder never touches DI.
     */
    val searchOperation: suspend (String) -> List<HomeArticle> = { query ->
        repository.searchNews(query).map { it.toHomeArticle() }
    }

    private var _selectedArticle: MutableStateFlow<HomeArticle?> = MutableStateFlow(null)
    val selectedArticle: StateFlow<HomeArticle?> = _selectedArticle.asStateFlow()

    private var selectedCategory: String = menuItems.first().queryParam

    fun submitSearchQuery(query: String) {
        _pendingSearchQuery.value = query
    }

    fun clearPendingQuery() {
        _pendingSearchQuery.value = ""
    }

    private val savedUrls: StateFlow<Set<String>> = bookmarkRepository
        .getBookmarkedUrls()
        .catch { _bookmarkAction.emit(SearchBookmarkActions.OnError(it.handleThrowable())) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = emptySet()
        )

    val isSelectedArticleBookmarked: StateFlow<Boolean> =
        _selectedArticle.flatMapLatest { article ->
            article?.let { bookmarkRepository.isBookmarked(article.url) } ?: flowOf(false)
        }
            .catch { _bookmarkAction.emit(SearchBookmarkActions.OnError(it.handleThrowable())) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = false
            )

    fun toggleBookmark() {
        viewModelScope.launch(exceptionHandler) {
            val article = _selectedArticle.value ?: return@launch
            val isSaved = article.url in savedUrls.value
            if (isSaved) {
                bookmarkRepository.removeBookmark(article.url)
                _bookmarkAction.emit(SearchBookmarkActions.OnRemoveBookmark)
            } else {
                bookmarkRepository.bookmark(article.toDomainSavedArticle(selectedCategory))
                _bookmarkAction.emit(SearchBookmarkActions.OnAddBookmark)
            }
        }
    }

    fun setSelectedArticle(article: HomeArticle?) {
        _selectedArticle.value = article
    }
}

sealed interface SearchUiState {
    data object Idle : SearchUiState
    data object Loading : SearchUiState
    data class Error(val message: String) : SearchUiState
    data class Success(val articles: List<HomeArticle>) : SearchUiState
    data object Empty : SearchUiState
}

sealed interface SearchBookmarkActions {
    object OnAddBookmark : SearchBookmarkActions
    object OnRemoveBookmark : SearchBookmarkActions
    data class OnError(val message: String) : SearchBookmarkActions
}
