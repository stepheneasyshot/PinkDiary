package com.stephen.pinkdiary.data.repository

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class KnowledgeCatalogTest {

    @Test
    fun `catalog contains fifteen uniquely identified articles with valid resources`() {
        assertEquals(15, knowledgeArticleCatalog.size)
        assertEquals(
            knowledgeArticleCatalog.size,
            knowledgeArticleCatalog.map(KnowledgeArticle::id).toSet().size
        )
        assertTrue(
            knowledgeArticleCatalog.all { article ->
                article.titleRes != 0 && article.summaryRes != 0 && article.markdownRes != 0
            }
        )
    }
}
