package de.chennemann.plannr.ui.screen.finances.contracts

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import de.chennemann.plannr.data.Contract
import de.chennemann.plannr.data.Partner
import de.chennemann.plannr.money.MoneyFormatter
import de.chennemann.plannr.ui.components.layout.twopane.Pane
import de.chennemann.plannr.ui.components.layout.twopane.TwoPaneLayout
import de.chennemann.plannr.ui.components.layout.twopane.rememberTwoPanePagerState
import de.chennemann.plannr.ui.screen.finances.transactions.TransactionOverviewContent
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate

@Composable
fun ContractDetailsScreen(component: ContractDetailsComponent) {
    Surface(color = MaterialTheme.colorScheme.background, contentColor = MaterialTheme.colorScheme.onBackground) {
        ContractDetailsContent(component)
    }
}

@Composable
private fun ContractDetailsContent(component: ContractDetailsComponent) {
    val contract by component.contract.collectAsState()
    val partners by component.partners.collectAsState()
    val value = contract ?: return
    var editing by rememberSaveable(value.contractId.contractId) { mutableStateOf(false) }
    val detailsScrollState = rememberScrollState()
    val transactionsScrollState = rememberLazyListState()
    val pagerState = rememberTwoPanePagerState(
        mainScrollableState = detailsScrollState,
        secondaryScrollableState = transactionsScrollState,
    )
    val transitioning by pagerState.isPageTransitionInProgress.collectAsState()

    if (editing) {
        Column(
            Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState()).padding(16.dp).padding(top = 56.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            ContractEditor(value, partners, component::save) { editing = false }
        }
        return
    }

    TwoPaneLayout(
        pagerState = pagerState,
        mainPageContent = {
            Column(
                Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)
                    .verticalScroll(detailsScrollState, enabled = !transitioning)
                    .padding(16.dp).padding(top = 56.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
            Text("Contract details", style = MaterialTheme.typography.headlineSmall)
            Box(Modifier.size(32.dp).background(contractColor(value.color), CircleShape))
            Text(value.name, style = MaterialTheme.typography.titleLarge)
            Text(value.partner.name.takeUnless { value.partner.partnerId < 0 } ?: "No partner")
            Text("Balance: ${MoneyFormatter.format(value.balance, "EUR")}", style = MaterialTheme.typography.titleMedium)
            Text(value.description ?: "No description")
            Text("Signing date: ${value.signingDate ?: "Not set"}")
            Text("Expiration date: ${value.expirationDate ?: "Not set"}")
            Text("Last cancellation date: ${value.lastCancellationDate ?: "Not set"}")
            Button(onClick = { editing = true }) { Text("Edit contract") }
            TextButton(
                onClick = { pagerState.animateToPane(Pane.Secondary) },
                modifier = Modifier.fillMaxWidth(),
            ) { Text("Scroll up for Transactions ↑") }
            }
        },
        secondaryPageContent = {
            TransactionOverviewContent(
                component = component.transactionOverview,
                secondaryListState = transactionsScrollState,
                pagerState = pagerState,
            )
        },
    )
}

internal fun contractColor(color: Int): Color = Color(color or 0xFF000000.toInt())

@Composable
private fun ContractEditor(
    contract: Contract,
    partners: List<Partner>,
    save: suspend (Contract) -> Unit,
    onClose: () -> Unit,
) {
    var name by rememberSaveable { mutableStateOf(contract.name) }
    var description by rememberSaveable { mutableStateOf(contract.description.orEmpty()) }
    var partnerId by rememberSaveable { mutableStateOf(contract.partner.partnerId) }
    var color by rememberSaveable { mutableStateOf((contract.color and 0xFFFFFF).toString(16).padStart(6, '0').uppercase()) }
    var signing by rememberSaveable { mutableStateOf(contract.signingDate.orEmpty()) }
    var expiration by rememberSaveable { mutableStateOf(contract.expirationDate.orEmpty()) }
    var cancellation by rememberSaveable { mutableStateOf(contract.lastCancellationDate.orEmpty()) }
    var saving by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var partnerMenu by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val parsedColor = color.takeIf { it.length == 6 && it.all { char -> char in "0123456789abcdefABCDEF" } }?.toIntOrNull(16)
    fun validDate(text: String) = text.isBlank() || runCatching { LocalDate.parse(text) }.isSuccess
    val valid = name.isNotBlank() && parsedColor != null && listOf(signing, expiration, cancellation).all(::validDate)
    Text("Edit contract", style = MaterialTheme.typography.headlineSmall)
    OutlinedTextField(name, { name = it }, Modifier.fillMaxWidth(), enabled = !saving,
        label = { Text("Name") }, singleLine = true, isError = name.isBlank())
    OutlinedTextField(description, { description = it }, Modifier.fillMaxWidth(), enabled = !saving,
        label = { Text("Description") }, minLines = 2)
    Box {
        OutlinedButton(onClick = { partnerMenu = true }, enabled = !saving) {
            Text("Partner: " + (partners.firstOrNull { it.partnerId == partnerId }?.name
                ?: contract.partner.takeIf { it.partnerId == partnerId && partnerId >= 0 }?.name ?: "No partner"))
        }
        DropdownMenu(partnerMenu, onDismissRequest = { partnerMenu = false }) {
            DropdownMenuItem(text = { Text("No partner") }, onClick = { partnerId = -1; partnerMenu = false })
            partners.forEach { partner ->
                DropdownMenuItem(text = { Text(partner.name) }, onClick = { partnerId = partner.partnerId; partnerMenu = false })
            }
        }
    }
    Text("Color", style = MaterialTheme.typography.titleMedium)
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        listOf(0x00A896, 0x3F51B5, 0xC62828, 0xEF6C00, 0x7B1FA2).forEach { swatch ->
            val hex = swatch.toString(16).padStart(6, '0').uppercase()
            Button(
                onClick = { color = hex }, enabled = !saving,
                modifier = Modifier.weight(1f).height(48.dp),
                contentPadding = PaddingValues(0.dp),
                colors = ButtonDefaults.buttonColors(containerColor = contractColor(swatch), contentColor = Color.White),
            ) { Text(if (color.equals(hex, ignoreCase = true)) "✓" else "#") }
        }
    }
    OutlinedTextField(color, { color = it.removePrefix("#") }, Modifier.fillMaxWidth(), enabled = !saving,
        label = { Text("Color hex (RRGGBB)") }, singleLine = true, isError = parsedColor == null,
        leadingIcon = { Box(Modifier.size(24.dp).background(contractColor(parsedColor ?: 0), CircleShape)) })
    listOf(
        Triple("Signing date", signing, { text: String -> signing = text }),
        Triple("Expiration date", expiration, { text: String -> expiration = text }),
        Triple("Last cancellation date", cancellation, { text: String -> cancellation = text }),
    ).forEach { (label, text, update) ->
        OutlinedTextField(text, update, Modifier.fillMaxWidth(), enabled = !saving,
            label = { Text(label) }, placeholder = { Text("YYYY-MM-DD") },
            supportingText = { Text("YYYY-MM-DD, or leave empty") },
            singleLine = true, isError = !validDate(text))
    }
    error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedButton(onClick = onClose, enabled = !saving) { Text("Cancel") }
        Button(enabled = valid && !saving, onClick = {
            saving = true
            error = null
            scope.launch {
                try {
                    save(contract.copy(
                        name = name.trim(), description = description.trim().ifBlank { null },
                        partner = partners.firstOrNull { it.partnerId == partnerId }
                            ?: contract.partner.takeIf { it.partnerId == partnerId }
                            ?: Partner(-1, "No partner"),
                        color = requireNotNull(parsedColor),
                        signingDate = signing.ifBlank { null }, expirationDate = expiration.ifBlank { null },
                        lastCancellationDate = cancellation.ifBlank { null },
                    ))
                    onClose()
                } catch (cancelled: CancellationException) {
                    throw cancelled
                } catch (failure: Exception) {
                    error = "Could not save the contract. Please try again."
                } finally {
                    saving = false
                }
            }
        }) { Text(if (saving) "Saving…" else "Save changes") }
    }
}
