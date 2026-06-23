package com.segunfrancis.newsfeed.ui.favourite

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.segunfrancis.newsfeed.domain.BookmarkRepository
import com.segunfrancis.newsfeed.util.handleThrowable
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FavouriteViewModel @Inject constructor(private val repository: BookmarkRepository) : ViewModel() {

    private val _actions = MutableSharedFlow<FavouriteActions>()
    val actions: SharedFlow<FavouriteActions> = _actions.asSharedFlow()

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        throwable.printStackTrace()
        viewModelScope.launch {
            _actions.emit(FavouriteActions.OnError(throwable.handleThrowable()))
        }
    }

    private val _selectedArticle = MutableStateFlow<SavedArticle?>(null)
    val selectedArticle: StateFlow<SavedArticle?> = _selectedArticle.asStateFlow()

    val favouriteArticles = repository.getAllBookmarks()
        .map { domainSavedArticles -> domainSavedArticles.map { it.toSavedArticle() } }
        .catch {
            it.printStackTrace()
            _actions.emit(FavouriteActions.OnError(it.handleThrowable()))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    fun removeBookmark() {
        val article = selectedArticle.value ?: return
        viewModelScope.launch(exceptionHandler) {
            repository.removeBookmark(article.url)
            _actions.emit(FavouriteActions.OnRemoveBookmark)
        }
    }

    fun updateSelectedArticle(article: SavedArticle?) {
        _selectedArticle.value = article
    }
}

sealed interface FavouriteActions {
    data class OnError(val message: String) : FavouriteActions
    object OnRemoveBookmark : FavouriteActions
}
