package com.segunfrancis.newsfeed.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
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
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: NewsFeedRepository,
    private val bookmarkRepository: BookmarkRepository
) : ViewModel() {

    private val _bookmarkAction = MutableSharedFlow<BookmarkActions>()
    val bookmarkAction: SharedFlow<BookmarkActions> = _bookmarkAction.asSharedFlow()

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        throwable.printStackTrace()
        viewModelScope.launch {
            _bookmarkAction.emit(BookmarkActions.OnError(throwable.handleThrowable()))
        }
    }

    private var _selectedArticle: MutableStateFlow<HomeArticle?> = MutableStateFlow(null)
    val selectedArticle: StateFlow<HomeArticle?> = _selectedArticle.asStateFlow()

    private var selectedCategory: String? = null

    private val newsFlows: Map<String, Flow<PagingData<HomeArticle>>> =
        menuItems.associate { menuItem ->
            menuItem.queryParam to repository
                .getNewsArticles(menuItem.queryParam)
                .map { pagingData -> pagingData.map { it.toHomeArticle() } }
                .cachedIn(viewModelScope)
                .catch { _bookmarkAction.emit(BookmarkActions.OnError(it.handleThrowable())) }
        }

    /**
     * One event channel per category so refresh/retry signals are always
     * delivered to the correct tab's LazyPagingItems — not to every tab.
     */
    private val pagingEvents: Map<String, Channel<PagingEvent>> =
        menuItems.associate { it.queryParam to Channel(Channel.CONFLATED) }

    fun articlesForCategory(category: String): Flow<PagingData<HomeArticle>> =
        newsFlows[category] ?: emptyFlow()

    fun pagingEventsFor(category: String): Flow<PagingEvent> =
        pagingEvents[category]?.receiveAsFlow() ?: emptyFlow()

    /**
     * Full refresh: re-runs RemoteMediator.load(REFRESH).
     * The mediator re-checks staleness and clears the DB if needed before
     * fetching from the network.
     *
     * Call from: pull-to-refresh gesture, a "Refresh" button.
     */
    fun refresh(category: String) {
        viewModelScope.launch { pagingEvents[category]?.send(PagingEvent.Refresh) }
    }

    /**
     * Retry: re-attempts only the last failed load without clearing state.
     * Does NOT re-run the full mediator refresh cycle — it only retries
     * whatever operation (REFRESH / APPEND / PREPEND) previously returned
     * MediatorResult.Error.
     *
     * Call from: an error card's "Try again" button.
     */
    fun retry(category: String) {
        viewModelScope.launch { pagingEvents[category]?.send(PagingEvent.Retry) }
    }

    // Collect once at the screen level — O(1) lookup per card, not N queries.
    private val savedUrls: StateFlow<Set<String>> = bookmarkRepository
        .getBookmarkedUrls()
        .catch { _bookmarkAction.emit(BookmarkActions.OnError(it.handleThrowable())) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = emptySet()
        )

    val isSelectedArticleBookmarked: StateFlow<Boolean> =
        _selectedArticle.flatMapLatest { article ->
            article?.let { bookmarkRepository.isBookmarked(article.url) } ?: flowOf(false)
        }
            .catch { _bookmarkAction.emit(BookmarkActions.OnError(it.handleThrowable())) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = false
            )

    fun toggleBookmark() {
        viewModelScope.launch(exceptionHandler) {
            val category = selectedCategory ?: return@launch
            val article = _selectedArticle.value ?: return@launch
            val isSaved = article.url in savedUrls.value
            if (isSaved) {
                bookmarkRepository.removeBookmark(article.url)
                _bookmarkAction.emit(BookmarkActions.OnRemoveBookmark)
            } else {
                bookmarkRepository.bookmark(article.toDomainSavedArticle(category))
                _bookmarkAction.emit(BookmarkActions.OnAddBookmark)
            }
        }
    }

    fun setSelectedArticle(article: HomeArticle?) {
        _selectedArticle.value = article
    }

    fun setSelectedCategory(category: String) {
        selectedCategory = category
    }
}

sealed interface PagingEvent {
    /** Triggers lazyPagingItems.refresh() — re-runs RemoteMediator.load(REFRESH) */
    object Refresh : PagingEvent

    /** Triggers lazyPagingItems.retry() — retries the last failed load only */
    object Retry : PagingEvent
}

sealed interface BookmarkActions {
    object OnAddBookmark : BookmarkActions
    object OnRemoveBookmark : BookmarkActions
    data class OnError(val message: String) : BookmarkActions
}
