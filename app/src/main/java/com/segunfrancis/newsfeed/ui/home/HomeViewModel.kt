package com.segunfrancis.newsfeed.ui.home

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.segunfrancis.newsfeed.data.models.Article
import com.segunfrancis.newsfeed.ui.models.HomeArticle
import com.segunfrancis.newsfeed.usecase.HomeUseCase
import com.segunfrancis.newsfeed.util.handleThrowable
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(private val useCase: HomeUseCase) : ViewModel() {

    private val _homeState = mutableStateOf(HomeUiState())
    val homeState: State<HomeUiState> = _homeState

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        _homeState.value =
            _homeState.value.copy(errorMessage = throwable.handleThrowable(), isLoading = false)
    }

    init {
        getHomeData()
    }

    fun getHomeData() {
        _homeState.value = _homeState.value.copy(isLoading = true)
        viewModelScope.launch(exceptionHandler) {
            useCase().collect { response ->
                _homeState.value = _homeState.value.copy(
                    articles = response.articles.map { it.toHomeArticle() },
                    errorMessage = response.error?.handleThrowable(),
                    isLoading = response.networkLoading
                )
            }
        }
    }

    private fun Article.toHomeArticle(): HomeArticle {
        return HomeArticle(
            author = author ?: "",
            content = content ?: "",
            description = description ?: "",
            publishedAt = publishedAt,
            title = title,
            url = url,
            urlToImage = urlToImage ?: "",
            onClick = { }
        )
    }
}

data class HomeUiState(
    val articles: List<HomeArticle> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)
