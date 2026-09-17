package de.chennemann.plannr.ui.screen.finances.partners

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import de.chennemann.plannr.data.Partner
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.readBytes
import io.github.vinceglb.filekit.size
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import kotlin.io.encoding.Base64

@Composable
fun PartnerLogoEditor(
    partner: Partner,
    preview: suspend (String) -> String,
    save: suspend (Long, String) -> Unit,
    remove: suspend (Long) -> Unit,
) {
    key(partner.partnerId) {
        var website by remember { mutableStateOf("") }
        var websiteOpen by remember { mutableStateOf(false) }
        var candidate by remember { mutableStateOf<ByteArray?>(null) }
        var busy by remember { mutableStateOf(false) }
        var error by remember { mutableStateOf<String?>(null) }
        var saved by remember { mutableStateOf(false) }
        val scope = rememberCoroutineScope()
        fun runAction(action: suspend () -> Unit) {
            busy = true
            error = null
            saved = false
            scope.launch {
                try { action() }
                catch (cancelled: CancellationException) { throw cancelled }
                catch (_: Exception) { error = "Could not update the logo. Check the image or website and your server connection." }
                finally { busy = false }
            }
        }
        val picker = rememberFilePickerLauncher(
            type = FileKitType.File(extensions = listOf("png", "jpg", "jpeg", "gif", "ico")),
            onError = { error = "Could not open the image picker." },
            onResult = { file ->
                if (file != null) runAction {
                    if (file.size() > 1_048_576) {
                        error = "Choose an image smaller than 1 MB."
                    } else {
                        val bytes = file.readBytes()
                        if (bytes.size > 1_048_576) error = "Choose an image smaller than 1 MB."
                        else candidate = bytes
                    }
                }
            },
        )
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Partner logo", style = MaterialTheme.typography.titleMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                PartnerLogo(partner)
                Text(partner.name)
            }
            Text("Used wherever this partner appears. Logo changes are saved immediately.",
                style = MaterialTheme.typography.bodySmall)
            TextButton(onClick = { websiteOpen = !websiteOpen }, enabled = !busy) { Text("Load logo from website") }
            AnimatedVisibility(websiteOpen) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(website, { website = it }, Modifier.fillMaxWidth(), singleLine = true,
                        enabled = !busy, label = { Text("Website or image URL") },
                        supportingText = { Text("Used only for this lookup; the address is not saved.") })
                    Button(enabled = website.isNotBlank() && !busy, onClick = {
                        val address = website
                        candidate = null
                        runAction {
                            candidate = Base64.decode(preview(address))
                            website = ""
                            websiteOpen = false
                        }
                    }) { Text("Find logo") }
                }
            }
            OutlinedButton(onClick = { picker.launch() }, enabled = !busy) { Text("Upload image") }
            candidate?.let { bytes ->
                Surface(color = androidx.compose.ui.graphics.Color(0xFFF5F5F5),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)) {
                    AsyncImage(bytes, "New partner logo preview", Modifier.size(96.dp).padding(8.dp), contentScale = ContentScale.Fit)
                }
                Text("Check that this is the correct partner logo before saving.", style = MaterialTheme.typography.bodySmall)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(enabled = !busy, onClick = {
                        runAction {
                            save(partner.partnerId, Base64.encode(bytes))
                            candidate = null
                            saved = true
                        }
                    }) { Text("Use this logo") }
                    TextButton(enabled = !busy, onClick = { candidate = null }) { Text("Discard") }
                }
            }
            if (partner.logoUrl != null) {
                TextButton(enabled = !busy, onClick = {
                    runAction { remove(partner.partnerId); candidate = null; saved = true }
                }) { Text("Remove logo") }
            }
            if (busy) LinearProgressIndicator(Modifier.fillMaxWidth())
            if (saved) Text("Partner logo saved.", style = MaterialTheme.typography.bodySmall)
            error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        }
    }
}
