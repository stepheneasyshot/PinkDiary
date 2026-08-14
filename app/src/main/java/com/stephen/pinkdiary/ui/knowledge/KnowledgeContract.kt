package com.stephen.pinkdiary.ui.knowledge

import com.stephen.pinkdiary.data.repository.KnowledgeArticle

data class KnowledgeUiState(
    val isLoading: Boolean = true,
    val articles: List<KnowledgeArticle> = emptyList(),
    val selectedArticle: KnowledgeArticle? = null,
    val markdownContent: String = "",
    val hasError: Boolean = false
)

sealed interface KnowledgeIntent {
    data class ArticleSelected(val articleId: String) : KnowledgeIntent
    data object BackToListClicked : KnowledgeIntent
    data object RetryClicked : KnowledgeIntent
}

sealed interface KnowledgeEffect
