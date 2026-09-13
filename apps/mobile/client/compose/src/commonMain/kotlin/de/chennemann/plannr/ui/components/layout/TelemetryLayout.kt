package de.chennemann.plannr.ui.components.layout

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import de.chennemann.plannr.ui.theme.colors

@Composable
fun Telemetry(label: String, value: Any) {
    Text("$label:", color = MaterialTheme.colors.onPrimaryMuted, style = MaterialTheme.typography.titleSmall)
    Text(value.toString(), color = MaterialTheme.colors.onPrimary, style = MaterialTheme.typography.bodyLarge)
    Spacer(Modifier.height(8.dp))
}


@Composable
fun TelemetryLayout(
    telemetryData: List<Pair<String, Any>>,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            Box(
                Modifier.shadow(4.dp, RoundedCornerShape(8.dp)).background(MaterialTheme.colorScheme.secondary, RoundedCornerShape(8.dp))
                    .padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 8.dp)
            ) {
                Column {
                    telemetryData.forEach { entry ->
                        Telemetry(entry.first, entry.second)
                    }
                }
            }
        },
        content = {
            content()
        }
    )
}