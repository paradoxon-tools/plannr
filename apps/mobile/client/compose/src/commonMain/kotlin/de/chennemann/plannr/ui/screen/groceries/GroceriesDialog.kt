package de.chennemann.plannr.ui.screen.groceries

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.Center
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import de.chennemann.plannr.ui.theme.colors

@Composable
fun GroceriesDialog(
    onDismiss: () -> Unit,
) {
    Box(
        Modifier.fillMaxSize(),
        contentAlignment = Center,
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colors.primary)
                .padding(16.dp),
        ) {
            Text(
                text = "This is the Dialog from Groceries",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colors.onPrimaryMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Button(onClick = onDismiss) {
                Text("Close")
            }
        }
    }
}
