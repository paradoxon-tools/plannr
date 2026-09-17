package de.chennemann.plannr.ui.screen.finances.management

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import de.chennemann.plannr.resources.Res
import de.chennemann.plannr.ui.screen.finances.partners.PartnerLogo
import de.chennemann.plannr.ui.screen.finances.partners.PartnerLogoEditor
import dev.icerock.moko.resources.compose.painterResource
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun ManagementPage(
    title: String,
    onBack: () -> Unit,
    actions: @Composable RowScope.() -> Unit = {},
    content: @Composable (PaddingValues) -> Unit,
) {
    Surface(color = MaterialTheme.colorScheme.background, contentColor = MaterialTheme.colorScheme.onBackground) {
        Scaffold(
            topBar = {
                TopAppBar(title = { Text(title) }, navigationIcon = {
                    IconButton(onClick = onBack) { Icon(painterResource(Res.images.arrow_back), "Back") }
                }, actions = actions)
            },
            content = content,
        )
    }
}

@Composable
fun WalletManagementScreen(onBack: () -> Unit, onPartners: () -> Unit) {
    ManagementPage("Wallet management", onBack) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Manage the information used throughout your wallet.", style = MaterialTheme.typography.bodyLarge)
            Card(onClick = onPartners, modifier = Modifier.fillMaxWidth()) {
                ListItem(
                    headlineContent = { Text("Partners", fontWeight = FontWeight.Bold) },
                    supportingContent = { Text("Names, descriptions and logos") },
                    trailingContent = { Icon(painterResource(Res.images.chevron_right), null) },
                )
            }
            Text("More management tools", Modifier.padding(top = 16.dp), style = MaterialTheme.typography.titleSmall)
            listOf("Accounts" to "Manage your bank accounts", "Pockets" to "Organize pockets within accounts").forEach { (title, subtitle) ->
                ListItem(
                    headlineContent = { Text(title) },
                    supportingContent = { Text(subtitle) },
                    trailingContent = { Text("Coming later", style = MaterialTheme.typography.labelSmall) },
                )
            }
        }
    }
}

@Composable
fun PartnerManagementScreen(component: PartnerManagementComponent, onBack: () -> Unit, onPartner: (Long) -> Unit) {
    val partners by component.partners.collectAsState()
    val loading by component.loading.collectAsState()
    val loadError by component.error.collectAsState()
    var query by rememberSaveable { mutableStateOf("") }
    var showArchived by rememberSaveable { mutableStateOf(false) }
    var adding by rememberSaveable { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    LaunchedEffect(component) { component.refresh() }
    val visible = partners.filter { it.isArchived == showArchived }
        .filter { it.name.contains(query, true) || it.description.orEmpty().contains(query, true) }
        .sortedBy { it.name.lowercase() }

    ManagementPage("Partners", onBack, actions = {
        IconButton(onClick = { adding = true }) { Icon(painterResource(Res.images.add), "Add partner") }
    }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            OutlinedTextField(query, { query = it }, Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                singleLine = true, label = { Text("Search partners") })
            Row(Modifier.padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(selected = !showArchived, onClick = { showArchived = false }, label = { Text("Active") })
                FilterChip(selected = showArchived, onClick = { showArchived = true }, label = { Text("Archived") })
            }
            if (loading) LinearProgressIndicator(Modifier.fillMaxWidth())
            loadError?.let {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(it, Modifier.weight(1f), color = MaterialTheme.colorScheme.error)
                    TextButton(onClick = { scope.launch { component.refresh() } }) { Text("Retry") }
                }
            }
            LazyColumn(Modifier.weight(1f), contentPadding = PaddingValues(bottom = 24.dp)) {
                items(visible, key = { it.partnerId }) { partner ->
                    ListItem(
                        modifier = Modifier.clickable { onPartner(partner.partnerId) },
                        leadingContent = { PartnerLogo(partner) },
                        headlineContent = { Text(partner.name) },
                        supportingContent = partner.description?.let { description -> { Text(description, maxLines = 2) } },
                        trailingContent = { Icon(painterResource(Res.images.chevron_right), "Edit partner") },
                    )
                }
                if (visible.isEmpty() && !loading && loadError == null) item {
                    Text(if (query.isNotBlank()) "No matching partners." else if (showArchived) "No archived partners." else "No partners yet. Tap + to add one.",
                        Modifier.padding(24.dp))
                }
            }
        }
    }
    if (adding) AddPartnerDialog(component, onDismiss = { adding = false }, onCreated = {
        adding = false
        onPartner(it)
    })
}

@Composable
private fun AddPartnerDialog(component: PartnerManagementComponent, onDismiss: () -> Unit, onCreated: (Long) -> Unit) {
    var name by rememberSaveable { mutableStateOf("") }
    var description by rememberSaveable { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    AlertDialog(onDismissRequest = { if (!busy) onDismiss() }, title = { Text("New partner") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(name, { name = it }, enabled = !busy, label = { Text("Name") }, singleLine = true)
                OutlinedTextField(description, { description = it }, enabled = !busy, label = { Text("Description") })
                error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            }
        },
        confirmButton = {
            TextButton(enabled = name.isNotBlank() && name.trim().length <= 255 && !busy, onClick = {
                busy = true
                error = null
                scope.launch {
                    try { onCreated(component.create(name, description)) }
                    catch (cancelled: CancellationException) { throw cancelled }
                    catch (_: Exception) { error = "Could not create partner. Please try again." }
                    finally { busy = false }
                }
            }) { Text(if (busy) "Creating…" else "Create partner") }
        },
        dismissButton = { TextButton(onClick = onDismiss, enabled = !busy) { Text("Cancel") } },
    )
}

@Composable
fun PartnerEditorScreen(component: PartnerManagementComponent, partnerId: Long, onBack: () -> Unit) {
    val partners by component.partners.collectAsState()
    val partner = partners.firstOrNull { it.partnerId == partnerId }
    LaunchedEffect(partnerId) { if (partner == null) component.refresh() }
    if (partner == null) {
        val loading by component.loading.collectAsState()
        val error by component.error.collectAsState()
        val scope = rememberCoroutineScope()
        ManagementPage("Partner", onBack) { padding ->
            Column(Modifier.padding(padding).padding(16.dp)) {
                if (loading) CircularProgressIndicator()
                else {
                    Text(error ?: "This partner is no longer available.")
                    TextButton(onClick = { scope.launch { component.refresh() } }) { Text("Retry") }
                }
            }
        }
        return
    }
    var name by rememberSaveable(partnerId) { mutableStateOf(partner.name) }
    var description by rememberSaveable(partnerId) { mutableStateOf(partner.description.orEmpty()) }
    var busy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var saved by remember { mutableStateOf(false) }
    var confirmArchive by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val dirty = name.trim() != partner.name || description.trim().ifBlank { null } != partner.description
    ManagementPage("Edit partner", onBack) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).imePadding().verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)) {
            OutlinedTextField(name, { name = it; saved = false }, Modifier.fillMaxWidth(), enabled = !busy,
                label = { Text("Name") }, singleLine = true, isError = name.isBlank() || name.trim().length > 255)
            OutlinedTextField(description, { description = it; saved = false }, Modifier.fillMaxWidth(), enabled = !busy,
                label = { Text("Description") }, minLines = 3)
            error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            Button(enabled = dirty && name.isNotBlank() && name.trim().length <= 255 && !busy, onClick = {
                busy = true
                error = null
                scope.launch {
                    try {
                        component.save(partnerId, name, description)
                        name = name.trim()
                        description = description.trim()
                        saved = true
                    } catch (cancelled: CancellationException) { throw cancelled }
                    catch (_: Exception) { error = "Could not save the partner. Please try again." }
                    finally { busy = false }
                }
            }) { Text(if (busy) "Saving…" else "Save changes") }
            if (saved) Text("Partner details saved.", style = MaterialTheme.typography.bodySmall)
            HorizontalDivider()
            PartnerLogoEditor(partner, component::previewLogo, component::saveLogo, component::removeLogo)
            HorizontalDivider()
            Text(if (partner.isArchived) "This partner is archived." else "Archive partners you no longer use. Existing contracts keep their partner.",
                style = MaterialTheme.typography.bodySmall)
            OutlinedButton(onClick = { confirmArchive = true }, enabled = !busy && !dirty) {
                Text(if (partner.isArchived) "Restore partner" else "Archive partner")
            }
            if (dirty) Text("Save your changes before archiving or restoring.", style = MaterialTheme.typography.bodySmall)
        }
    }
    if (confirmArchive) AlertDialog(
        onDismissRequest = { if (!busy) confirmArchive = false },
        title = { Text(if (partner.isArchived) "Restore partner?" else "Archive partner?") },
        text = { Text(partner.name) },
        confirmButton = {
            TextButton(enabled = !busy, onClick = {
                busy = true
                scope.launch {
                    try { component.setArchived(partnerId, !partner.isArchived); confirmArchive = false; onBack() }
                    catch (cancelled: CancellationException) { throw cancelled }
                    catch (_: Exception) { confirmArchive = false; error = "Could not change the partner status." }
                    finally { busy = false }
                }
            }) { Text(if (partner.isArchived) "Restore" else "Archive") }
        },
        dismissButton = { TextButton(enabled = !busy, onClick = { confirmArchive = false }) { Text("Cancel") } },
    )
}
