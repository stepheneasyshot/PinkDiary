package com.stephen.pinkdiary.ui.knowledge

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mikepenz.markdown.m3.Markdown
import com.mikepenz.markdown.m3.markdownColor
import com.mikepenz.markdown.m3.markdownTypography
import com.stephen.pinkdiary.R
import com.stephen.pinkdiary.data.repository.KnowledgeArticle

@Composable
fun KnowledgeRoute(viewModel: KnowledgeViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    KnowledgeScreen(state = state, onIntent = viewModel::onIntent)
}

@Composable
fun KnowledgeScreen(
    state: KnowledgeUiState,
    onIntent: (KnowledgeIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(enabled = state.selectedArticle != null) {
        onIntent(KnowledgeIntent.BackToListClicked)
    }

    Surface(modifier = modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        val selectedArticle = state.selectedArticle
        when {
            selectedArticle != null -> ArticleDetail(
                article = selectedArticle,
                markdownContent = state.markdownContent,
                isLoading = state.isLoading,
                hasError = state.hasError,
                onBack = { onIntent(KnowledgeIntent.BackToListClicked) },
                onRetry = { onIntent(KnowledgeIntent.RetryClicked) }
            )
            state.isLoading -> LoadingContent()
            state.hasError -> ErrorContent(
                onRetry = { onIntent(KnowledgeIntent.RetryClicked) }
            )
            else -> ArticleList(
                articles = state.articles,
                onArticleSelected = { onIntent(KnowledgeIntent.ArticleSelected(it)) }
            )
        }
    }
}

@Composable
private fun ArticleList(
    articles: List<KnowledgeArticle>,
    onArticleSelected: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            horizontal = 12.dp,
            vertical = 12.dp
        ),
    ) {
        item {
            Text(
                text = stringResource(R.string.knowledge_title),
                modifier = Modifier.padding(horizontal = 12.dp),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
        item {
            Text(
                text = stringResource(R.string.knowledge_intro),
                modifier = Modifier.padding(
                    start = 12.dp,
                    top = 12.dp,
                    end = 12.dp,
                    bottom = 8.dp
                ),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        items(items = articles, key = { it.id }) { article ->
            ArticleListItem(
                article = article,
                onClick = { onArticleSelected(article.id) }
            )
        }
    }
}

@Composable
private fun ArticleListItem(article: KnowledgeArticle, onClick: () -> Unit) {
    ElevatedCard(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = stringResource(article.titleRes),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = stringResource(article.summaryRes),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ArticleDetail(
    article: KnowledgeArticle,
    markdownContent: String,
    isLoading: Boolean,
    hasError: Boolean,
    onBack: () -> Unit,
    onRetry: () -> Unit
) {
    Column(Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.knowledge_back)
                )
            }
            Text(
                text = stringResource(article.titleRes),
                modifier = Modifier.weight(1f).padding(end = 16.dp),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        when {
            isLoading -> LoadingContent(modifier = Modifier.weight(1f))
            hasError -> ErrorContent(
                onRetry = onRetry,
                modifier = Modifier.weight(1f)
            )
            else -> ArticleMarkdownContent(markdownContent = markdownContent)
        }
    }
}

@Composable
private fun ArticleMarkdownContent(markdownContent: String) {
    PinkMarkdown(
        content = markdownContent,
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 8.dp)
    )
}

@Composable
private fun PinkMarkdown(content: String, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    Markdown(
        content = content,
        modifier = modifier,
        colors = markdownColor(
            text = colors.onSurface,
            codeBackground = colors.primaryContainer,
            inlineCodeBackground = colors.primaryContainer,
            dividerColor = colors.primary.copy(alpha = 0.28f),
            tableBackground = colors.surface
        ),
        typography = markdownTypography(
            h1 = MaterialTheme.typography.headlineMedium.copy(
                color = colors.primary,
                fontWeight = FontWeight.Bold
            ),
            h2 = MaterialTheme.typography.titleLarge.copy(
                color = colors.onPrimaryContainer,
                fontWeight = FontWeight.SemiBold
            ),
            h3 = MaterialTheme.typography.titleMedium.copy(
                color = colors.onPrimaryContainer,
                fontWeight = FontWeight.SemiBold
            ),
            text = MaterialTheme.typography.bodyLarge.copy(color = colors.onSurface),
            paragraph = MaterialTheme.typography.bodyLarge.copy(color = colors.onSurface),
            ordered = MaterialTheme.typography.bodyLarge.copy(color = colors.onSurface),
            bullet = MaterialTheme.typography.bodyLarge.copy(color = colors.onSurface),
            list = MaterialTheme.typography.bodyLarge.copy(color = colors.onSurface),
            quote = MaterialTheme.typography.bodyMedium.copy(color = colors.onSurfaceVariant),
            textLink = TextLinkStyles(
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = colors.primary,
                    fontWeight = FontWeight.SemiBold,
                    textDecoration = TextDecoration.Underline
                ).toSpanStyle()
            )
        )
    )
}

@Composable
private fun LoadingContent(modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
    }
}

@Composable
private fun ErrorContent(
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(R.string.knowledge_load_error),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Button(onClick = onRetry, modifier = Modifier.padding(top = 16.dp)) {
            Text(stringResource(R.string.knowledge_retry))
        }
    }
}
