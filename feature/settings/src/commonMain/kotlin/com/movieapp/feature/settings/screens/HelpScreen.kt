@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.movieapp.feature.settings.screens

import com.movieapp.core.ui.resources.Res as CoreRes
import com.movieapp.core.ui.resources.cd_collapse
import com.movieapp.core.ui.resources.cd_expand
import com.movieapp.core.ui.resources.settings_help
import com.movieapp.feature.settings.resources.Res
import com.movieapp.feature.settings.resources.help_faq_account_a
import com.movieapp.feature.settings.resources.help_faq_account_q
import com.movieapp.feature.settings.resources.help_faq_collection_a
import com.movieapp.feature.settings.resources.help_faq_collection_q
import com.movieapp.feature.settings.resources.help_faq_heading
import com.movieapp.feature.settings.resources.help_faq_report_a
import com.movieapp.feature.settings.resources.help_faq_report_q
import com.movieapp.feature.settings.resources.help_faq_theme_a
import com.movieapp.feature.settings.resources.help_faq_theme_q
import com.movieapp.feature.settings.resources.help_faq_tmdb_a
import com.movieapp.feature.settings.resources.help_faq_tmdb_q
import com.movieapp.feature.settings.resources.help_faq_watchlist_a
import com.movieapp.feature.settings.resources.help_faq_watchlist_q
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import com.movieapp.core.ui.designsystem.MovieAppSpacing

private data class FaqItem(val question: String, val answer: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpScreen(onBackClick: () -> Unit) {
    val faqItems =
        listOf(
            FaqItem(
                question = stringResource(Res.string.help_faq_watchlist_q),
                answer = stringResource(Res.string.help_faq_watchlist_a),
            ),
            FaqItem(
                question = stringResource(Res.string.help_faq_collection_q),
                answer = stringResource(Res.string.help_faq_collection_a),
            ),
            FaqItem(
                question = stringResource(Res.string.help_faq_tmdb_q),
                answer = stringResource(Res.string.help_faq_tmdb_a),
            ),
            FaqItem(
                question = stringResource(Res.string.help_faq_account_q),
                answer = stringResource(Res.string.help_faq_account_a),
            ),
            FaqItem(
                question = stringResource(Res.string.help_faq_theme_q),
                answer = stringResource(Res.string.help_faq_theme_a),
            ),
            FaqItem(
                question = stringResource(Res.string.help_faq_report_q),
                answer = stringResource(Res.string.help_faq_report_a),
            ),
        )
    Scaffold(
        topBar = {
            SettingsTopBar(
                title = stringResource(CoreRes.string.settings_help),
                onBackClick = onBackClick,
            )
        }
    ) { padding ->
        LazyColumn(
            modifier =
                Modifier.fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = MovieAppSpacing.screenHorizontal)
        ) {
            item {
                Spacer(modifier = Modifier.height(MovieAppSpacing.small))
                Text(
                    text = stringResource(Res.string.help_faq_heading),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(MovieAppSpacing.small))
            }
            items(faqItems, key = { it.question }) { item ->
                FaqAccordion(item)
                HorizontalDivider()
            }
        }
    }
}

@Composable
private fun FaqAccordion(item: FaqItem) {
    var expanded by remember(item.question) { mutableStateOf(false) }
    val collapseCd = stringResource(CoreRes.string.cd_collapse)
    val expandCd = stringResource(CoreRes.string.cd_expand)
    Column(
        modifier =
            Modifier.fillMaxWidth()
                .clickable(
                    role = Role.Button,
                    onClick = { expanded = !expanded },
                )
                .semantics {
                    stateDescription = if (expanded) collapseCd else expandCd
                }
                .padding(vertical = MovieAppSpacing.sectionVertical)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = item.question,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f),
            )
            Icon(
                imageVector = if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                contentDescription = if (expanded) collapseCd else expandCd,
            )
        }
        AnimatedVisibility(
            visible = expanded,
            enter =
                fadeIn(animationSpec = MaterialTheme.motionScheme.defaultEffectsSpec()) +
                    expandVertically(
                        animationSpec = MaterialTheme.motionScheme.defaultSpatialSpec()
                    ),
            exit =
                fadeOut(animationSpec = MaterialTheme.motionScheme.defaultEffectsSpec()) +
                    shrinkVertically(
                        animationSpec = MaterialTheme.motionScheme.defaultSpatialSpec()
                    ),
        ) {
            Text(
                text = item.answer,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = MovieAppSpacing.small),
            )
        }
    }
}
