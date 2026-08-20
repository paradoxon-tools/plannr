package de.chennemann.plannr.ui.screen.groceries

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import de.chennemann.plannr.ui.theme.colors

@Composable
fun GroceriesHomePageContent(component: GroceriesRootComponent) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column {
            Text(
                text = "Groceries",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colors.onBackground,
            )
            Button(onClick = { component.openDialog() }) {
                Text("Open groceries")
            }
        }
    }
}
