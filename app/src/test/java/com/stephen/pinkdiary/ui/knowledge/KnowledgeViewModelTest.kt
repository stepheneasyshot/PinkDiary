package com.stephen.pinkdiary.ui.knowledge

import com.stephen.pinkdiary.R
import com.stephen.pinkdiary.data.repository.KnowledgeArticle
import com.stephen.pinkdiary.data.repository.KnowledgeRepository
import com.stephen.pinkdiary.testutil.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class KnowledgeViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `articles load as list then selection loads markdown detail`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val repository = FakeKnowledgeRepository("## 周期计算")
            val viewModel = KnowledgeViewModel(repository)
            advanceUntilIdle()

            assertEquals(listOf(repository.article), viewModel.uiState.value.articles)
            assertNull(viewModel.uiState.value.selectedArticle)

            viewModel.onIntent(KnowledgeIntent.ArticleSelected(repository.article.id))
            advanceUntilIdle()

            assertEquals(repository.article, viewModel.uiState.value.selectedArticle)
            assertEquals("## 周期计算", viewModel.uiState.value.markdownContent)
            assertFalse(viewModel.uiState.value.isLoading)
        }

    @Test
    fun `retry intent reloads selected article after failure`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val repository = FakeKnowledgeRepository("## 恢复成功", articleFailuresRemaining = 1)
            val viewModel = KnowledgeViewModel(repository)
            advanceUntilIdle()

            viewModel.onIntent(KnowledgeIntent.ArticleSelected(repository.article.id))
            advanceUntilIdle()
            assertTrue(viewModel.uiState.value.hasError)

            viewModel.onIntent(KnowledgeIntent.RetryClicked)
            advanceUntilIdle()

            assertFalse(viewModel.uiState.value.hasError)
            assertEquals("## 恢复成功", viewModel.uiState.value.markdownContent)
            assertEquals(2, repository.articleLoadCount)
        }

    @Test
    fun `back intent clears detail and restores list state`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val repository = FakeKnowledgeRepository("## 正文")
            val viewModel = KnowledgeViewModel(repository)
            advanceUntilIdle()
            viewModel.onIntent(KnowledgeIntent.ArticleSelected(repository.article.id))
            advanceUntilIdle()

            viewModel.onIntent(KnowledgeIntent.BackToListClicked)

            assertNull(viewModel.uiState.value.selectedArticle)
            assertEquals("", viewModel.uiState.value.markdownContent)
            assertFalse(viewModel.uiState.value.hasError)
        }
}

private class FakeKnowledgeRepository(
    private val content: String,
    private var articleFailuresRemaining: Int = 0
) : KnowledgeRepository {
    val article = KnowledgeArticle(
        id = "cycle",
        titleRes = R.string.knowledge_article_cycle_title,
        summaryRes = R.string.knowledge_article_cycle_summary,
        markdownRes = R.raw.menstrual_cycle_basics
    )

    var articleLoadCount: Int = 0
        private set

    override suspend fun loadArticles(): List<KnowledgeArticle> = listOf(article)

    override suspend fun loadArticle(articleId: String): String {
        articleLoadCount += 1
        if (articleFailuresRemaining > 0) {
            articleFailuresRemaining -= 1
            error("load failed")
        }
        return content
    }
}
