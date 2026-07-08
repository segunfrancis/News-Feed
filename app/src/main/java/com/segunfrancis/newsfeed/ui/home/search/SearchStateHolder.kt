package com.segunfrancis.newsfeed.ui.home.search

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.segunfrancis.newsfeed.ui.SearchUiState
import com.segunfrancis.newsfeed.ui.models.HomeArticle
import com.segunfrancis.newsfeed.util.handleThrowable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

class SearchStateHolder(
    private val onSearch: suspend (String) -> List<HomeArticle>,
    private val scope: CoroutineScope
) {
    var uiState by mutableStateOf<SearchUiState>(SearchUiState.Idle)
        private set
    private var lastSearchQuery = ""
    private var searchJob: Job? = null

    fun search(query: String) {
        val trimmed = query.trim()
        if (trimmed.isBlank()) return
        if (trimmed.equals(lastSearchQuery, ignoreCase = true)) return

        searchJob?.cancel()
        searchJob = scope.launch {
            uiState = SearchUiState.Loading
            try {
                lastSearchQuery = trimmed
                val results = onSearch(trimmed)
                uiState = if (results.isNotEmpty()) {
                    SearchUiState.Success(results)
                } else {
                    SearchUiState.Empty(trimmed)
                }
            } catch (c: CancellationException) {
                throw c       // must re-throw — structured concurrency depends on it
            } catch (t: Throwable) {
                uiState = SearchUiState.Error(t.handleThrowable())
            }
        }
    }

    companion object {
        /**
         * Persists only lastSearchQuery — a plain String that fits in a Bundle.
         * onSearch and scope cannot be serialized; they are re-supplied by the
         * composable at restore time, which is fine because they come from
         * rememberCoroutineScope() and the ViewModel respectively — both of
         * which are reconstructed fresh after a configuration change anyway.
         *
         * On restore, lastSearchQuery on the new holder is "" so the
         * deduplication check in search() passes and the previous query
         * re-runs automatically without any extra code.
         */
        fun Saver(
            onSearch: suspend (String) -> List<HomeArticle>,
            scope: CoroutineScope,
        ): Saver<SearchStateHolder, String> = Saver(
            save = { holder ->
                holder.lastSearchQuery   // private but accessible from companion
            },
            restore = { savedQuery ->
                SearchStateHolder(onSearch = onSearch, scope = scope).also { holder ->
                    if (savedQuery.isNotBlank()) holder.search(savedQuery)
                }
            },
        )
    }
}

@Composable
fun rememberSearchStateHolder(onSearch: suspend (String) -> List<HomeArticle>): SearchStateHolder {
    val scope = rememberCoroutineScope()
    return rememberSaveable(
        // scope is not a key — a new scope after config change doesn't mean
        // we want a new holder; we want the restored one with its saved query.
        saver = SearchStateHolder.Saver(onSearch = onSearch, scope = scope),
    ) {
        SearchStateHolder(onSearch = onSearch, scope = scope)
    }
}
