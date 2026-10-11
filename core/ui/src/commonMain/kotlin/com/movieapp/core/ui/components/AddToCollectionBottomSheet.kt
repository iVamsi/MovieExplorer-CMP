package com.movieapp.core.ui.components

import com.movieapp.core.ui.resources.Res
import com.movieapp.core.ui.resources.action_create
import com.movieapp.core.ui.resources.add_to_collection_create_hint
import com.movieapp.core.ui.resources.add_to_collection_empty
import com.movieapp.core.ui.resources.add_to_collection_title
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.movieapp.core.model.UserCollection
import com.movieapp.core.ui.designsystem.MovieAppSpacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddToCollectionBottomSheet(
    collections: List<UserCollection>,
    isSubmitting: Boolean,
    onDismiss: () -> Unit,
    onSelectCollection: (UserCollection) -> Unit,
    onCreateAndAdd: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var newName by remember { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        modifier = modifier,
        shape = MaterialTheme.shapes.extraLarge,
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
    ) {
        Column(
            modifier =
                Modifier.fillMaxWidth().padding(horizontal = MovieAppSpacing.screenHorizontal),
            verticalArrangement = Arrangement.spacedBy(MovieAppSpacing.small),
        ) {
            Text(
                text = stringResource(Res.string.add_to_collection_title),
                style = MaterialTheme.typography.titleLarge,
            )
            if (collections.isEmpty()) {
                Text(
                    text = stringResource(Res.string.add_to_collection_empty),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().heightIn(max = 240.dp),
                    verticalArrangement = Arrangement.spacedBy(MovieAppSpacing.itemGapCompact),
                ) {
                    items(collections, key = { it.id }) { collection ->
                        Row(
                            modifier =
                                Modifier.fillMaxWidth()
                                    .clickable(
                                        enabled = !isSubmitting,
                                        role = Role.Button,
                                        onClick = { onSelectCollection(collection) },
                                    )
                                    .padding(vertical = MovieAppSpacing.small),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = collection.name,
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                }
            }
            OutlinedTextField(
                value = newName,
                onValueChange = { newName = it },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isSubmitting,
                label = { Text(stringResource(Res.string.add_to_collection_create_hint)) },
                singleLine = true,
            )
            Button(
                onClick = { onCreateAndAdd(newName.trim()) },
                enabled = !isSubmitting && newName.isNotBlank(),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(Res.string.action_create))
            }
            Spacer(modifier = Modifier.height(MovieAppSpacing.medium))
        }
    }
}
