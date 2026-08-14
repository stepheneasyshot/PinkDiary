package com.stephen.pinkdiary.ui.knowledge

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.stephen.pinkdiary.PinkdiaryApp
import com.stephen.pinkdiary.data.repository.KnowledgeArticle
import com.stephen.pinkdiary.data.repository.KnowledgeRepository
import com.stephen.pinkdiary.ui.mvi.MviViewModel
import kotlinx.coroutines.launch

class KnowledgeViewModel(
    private val knowledgeRepository: KnowledgeRepository
) : MviViewModel<KnowledgeIntent, KnowledgeUiState, KnowledgeEffect>(KnowledgeUiState()) {

    init {
        loadArticles()
    }

    override fun onIntent(intent: KnowledgeIntent) {
        when (intent) {
            is KnowledgeIntent.ArticleSelected -> selectArticle(intent.articleId)
            KnowledgeIntent.BackToListClicked -> showArticleList()
            KnowledgeIntent.RetryClicked -> retry()
        }
    }

    private fun loadArticles() {
        reduce { it.copy(isLoading = true, hasError = false) }
        viewModelScope.launch {
            runCatching { knowledgeRepository.loadArticles() }
                .onSuccess { articles ->
                    reduce {
                        it.copy(
                            isLoading = false,
                            articles = articles,
                            hasError = false
                        )
                    }
                }
                .onFailure {
                    reduce { it.copy(isLoading = false, hasError = true) }
                }
        }
    }

    private fun selectArticle(articleId: String) {
        val article = uiState.value.articles.firstOrNull { it.id == articleId } ?: return
        loadArticle(article)
    }

    private fun loadArticle(article: KnowledgeArticle) {
        reduce {
            it.copy(
                isLoading = true,
                selectedArticle = article,
                markdownContent = "",
                hasError = false
            )
        }
        viewModelScope.launch {
            runCatching { knowledgeRepository.loadArticle(article.id) }
                .onSuccess { content ->
                    reduce { current ->
                        if (current.selectedArticle?.id != article.id) current
                        else current.copy(
                            isLoading = false,
                            markdownContent = content,
                            hasError = false
                        )
                    }
                }
                .onFailure {
                    reduce { current ->
                        if (current.selectedArticle?.id != article.id) current
                        else current.copy(isLoading = false, hasError = true)
                    }
                }
        }
    }

    private fun showArticleList() {
        reduce {
            it.copy(
                isLoading = false,
                selectedArticle = null,
                markdownContent = "",
                hasError = false
            )
        }
    }

    private fun retry() {
        uiState.value.selectedArticle?.let(::loadArticle) ?: loadArticles()
    }

    companion object {
        fun factory(app: PinkdiaryApp): ViewModelProvider.Factory = viewModelFactory {
            initializer { KnowledgeViewModel(app.knowledgeRepository) }
        }
    }
}
