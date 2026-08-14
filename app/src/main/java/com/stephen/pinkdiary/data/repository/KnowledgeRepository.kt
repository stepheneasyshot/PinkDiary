package com.stephen.pinkdiary.data.repository

import android.content.res.Resources
import androidx.annotation.RawRes
import androidx.annotation.StringRes
import com.stephen.pinkdiary.R
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class KnowledgeArticle(
    val id: String,
    @param:StringRes val titleRes: Int,
    @param:StringRes val summaryRes: Int,
    @param:RawRes val markdownRes: Int
)

interface KnowledgeRepository {
    suspend fun loadArticles(): List<KnowledgeArticle>
    suspend fun loadArticle(articleId: String): String
}

internal val knowledgeArticleCatalog = listOf(
    KnowledgeArticle(
        id = "menstrual_cycle_basics",
        titleRes = R.string.knowledge_article_cycle_title,
        summaryRes = R.string.knowledge_article_cycle_summary,
        markdownRes = R.raw.menstrual_cycle_basics
    ),
    KnowledgeArticle(
        id = "cycle_phases_and_ovulation",
        titleRes = R.string.knowledge_article_phases_title,
        summaryRes = R.string.knowledge_article_phases_summary,
        markdownRes = R.raw.cycle_phases_and_ovulation
    ),
    KnowledgeArticle(
        id = "first_period_guide",
        titleRes = R.string.knowledge_article_first_period_title,
        summaryRes = R.string.knowledge_article_first_period_summary,
        markdownRes = R.raw.first_period_guide
    ),
    KnowledgeArticle(
        id = "menstrual_tracking",
        titleRes = R.string.knowledge_article_tracking_title,
        summaryRes = R.string.knowledge_article_tracking_summary,
        markdownRes = R.raw.menstrual_tracking
    ),
    KnowledgeArticle(
        id = "menstrual_products_and_hygiene",
        titleRes = R.string.knowledge_article_products_title,
        summaryRes = R.string.knowledge_article_products_summary,
        markdownRes = R.raw.menstrual_products_and_hygiene
    ),
    KnowledgeArticle(
        id = "period_pain",
        titleRes = R.string.knowledge_article_pain_title,
        summaryRes = R.string.knowledge_article_pain_summary,
        markdownRes = R.raw.period_pain
    ),
    KnowledgeArticle(
        id = "heavy_periods",
        titleRes = R.string.knowledge_article_heavy_title,
        summaryRes = R.string.knowledge_article_heavy_summary,
        markdownRes = R.raw.heavy_periods
    ),
    KnowledgeArticle(
        id = "irregular_periods",
        titleRes = R.string.knowledge_article_irregular_title,
        summaryRes = R.string.knowledge_article_irregular_summary,
        markdownRes = R.raw.irregular_periods
    ),
    KnowledgeArticle(
        id = "missed_periods_amenorrhea",
        titleRes = R.string.knowledge_article_amenorrhea_title,
        summaryRes = R.string.knowledge_article_amenorrhea_summary,
        markdownRes = R.raw.missed_periods_amenorrhea
    ),
    KnowledgeArticle(
        id = "pms_and_pmdd",
        titleRes = R.string.knowledge_article_pms_title,
        summaryRes = R.string.knowledge_article_pms_summary,
        markdownRes = R.raw.pms_and_pmdd
    ),
    KnowledgeArticle(
        id = "exercise_and_menstruation",
        titleRes = R.string.knowledge_article_exercise_title,
        summaryRes = R.string.knowledge_article_exercise_summary,
        markdownRes = R.raw.exercise_and_menstruation
    ),
    KnowledgeArticle(
        id = "iron_deficiency_and_periods",
        titleRes = R.string.knowledge_article_iron_title,
        summaryRes = R.string.knowledge_article_iron_summary,
        markdownRes = R.raw.iron_deficiency_and_periods
    ),
    KnowledgeArticle(
        id = "endometriosis",
        titleRes = R.string.knowledge_article_endometriosis_title,
        summaryRes = R.string.knowledge_article_endometriosis_summary,
        markdownRes = R.raw.endometriosis
    ),
    KnowledgeArticle(
        id = "pcos_and_periods",
        titleRes = R.string.knowledge_article_pcos_title,
        summaryRes = R.string.knowledge_article_pcos_summary,
        markdownRes = R.raw.pcos_and_periods
    ),
    KnowledgeArticle(
        id = "perimenopause_and_periods",
        titleRes = R.string.knowledge_article_perimenopause_title,
        summaryRes = R.string.knowledge_article_perimenopause_summary,
        markdownRes = R.raw.perimenopause_and_periods
    )
)

class RawKnowledgeRepository(
    private val resources: Resources,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : KnowledgeRepository {

    private val articles = knowledgeArticleCatalog

    override suspend fun loadArticles(): List<KnowledgeArticle> = articles

    override suspend fun loadArticle(articleId: String): String = withContext(ioDispatcher) {
        val article = articles.firstOrNull { it.id == articleId }
            ?: throw NoSuchElementException("Unknown knowledge article: $articleId")
        resources.openRawResource(article.markdownRes).bufferedReader().use { it.readText() }
    }
}
