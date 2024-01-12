package com.segunfrancis.newsfeed.ui.home

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import com.segunfrancis.newsfeed.data.local.NewsItemDatastore
import com.segunfrancis.newsfeed.data.models.Article
import com.segunfrancis.newsfeed.repository.NewsFeedRepository
import com.segunfrancis.newsfeed.ui.home.components.menuItems
import com.segunfrancis.newsfeed.ui.models.HomeArticle
import com.segunfrancis.newsfeed.util.handleThrowable
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: NewsFeedRepository,
    private val datastore: NewsItemDatastore
) : ViewModel() {

    private val _homeState = mutableStateOf(HomeUiState())
    val homeState: State<HomeUiState> = _homeState

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        _homeState.value =
            _homeState.value.copy(errorMessage = throwable.handleThrowable(), isLoading = false)
    }

    private var _newsArticles: Flow<PagingData<HomeArticle>>? = null
    val newsArticles: Flow<PagingData<HomeArticle>>? get() = _newsArticles
    val menuOption = datastore.getMenuOption()

    init {
        initRemote()
        viewModelScope.launch(exceptionHandler) {
            menuOption.collect {
                try {
                    _newsArticles =
                        repository.getNewsArticles(menuItems[it].queryParam).flow.map { pagingData ->
                            pagingData.map { article ->
                                article.toHomeArticle()
                            }
                        }.cachedIn(viewModelScope)
                } catch (t: Throwable) {
                    _homeState.value = _homeState.value.copy(errorMessage = t.handleThrowable())
                }
            }
        }
    }

    fun initRemote() {
        viewModelScope.launch(exceptionHandler) {
            menuOption.collectLatest { index ->
                repository.loadNewsArticlesRemote(menuItems[index].queryParam).collect {
                    _homeState.value = _homeState.value.copy(
                        isLoading = it.networkLoading,
                        errorMessage = it.error?.handleThrowable()
                    )
                }
            }
        }
    }

    private fun Article.toHomeArticle(): HomeArticle {
        return HomeArticle(
            author = author.orEmpty(),
            content = content.orEmpty(),
            description = description.orEmpty(),
            publishedAt = publishedAt,
            title = title,
            url = url,
            urlToImage = urlToImage.orEmpty(),
            onClick = { }
        )
    }

    fun setSelectedMenuOption(optionIndex: Int) {
        viewModelScope.launch {
            datastore.setMenuOption(optionIndex)
        }
    }
}

data class HomeUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)
