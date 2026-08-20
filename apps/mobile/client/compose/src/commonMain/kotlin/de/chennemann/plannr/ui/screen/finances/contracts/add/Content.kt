package de.chennemann.plannr.ui.screen.finances.contracts.add

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import de.chennemann.plannr.data.Account
import de.chennemann.plannr.data.Partner
import de.chennemann.plannr.ui.components.button.RippleButton
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddContractScreen(
    component: AddContractComponent,
    onCreated: () -> Unit = {}
) {
    val scope = rememberCoroutineScope()
    var contractName by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }

    val accounts by component.accounts.collectAsState()
    val partners by component.partners.collectAsState()

    var selectedAccount by remember { mutableStateOf<Account?>(null) }
    var selectedPartner by remember { mutableStateOf<Partner?>(null) }
    var isAccountMenuExpanded by remember { mutableStateOf(false) }
    var isPartnerMenuExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(all = 16.dp)
            .padding(top = 56.dp)
    ) {
        Text(
            text = "Create contract",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = contractName,
            onValueChange = {
                contractName = it
            },
            label = { Text("Contract name") },
            placeholder = { Text("e.g. Car Insurance, Gym Membership…") },
            singleLine = true,
        )

        Spacer(Modifier.height(12.dp))

        // Partner selector
        Text(text = "Partner", style = MaterialTheme.typography.labelLarge)
        ExposedDropdownMenuBox(
            expanded = isPartnerMenuExpanded,
            onExpandedChange = { isPartnerMenuExpanded = !isPartnerMenuExpanded }
        ) {



            OutlinedTextField(
                value = selectedPartner?.name ?: "",
                placeholder = { Text(if (partners.isEmpty()) "No partners available" else "Select partner") },
                onValueChange = {},
                readOnly = true,
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = isPartnerMenuExpanded)
                },
                colors = ExposedDropdownMenuDefaults.textFieldColors(),
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
            )

            ExposedDropdownMenu(
                expanded = isPartnerMenuExpanded,
                onDismissRequest = { isPartnerMenuExpanded = false }
            ) {
                partners.forEach { partner ->
                    DropdownMenuItem(
                        text = { Text(partner.name, color = MaterialTheme.colorScheme.onSurface) },
                        onClick = {
                            selectedPartner = partner
                            isPartnerMenuExpanded = false
                        },
                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                    )
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        // Account selector
        Text(text = "Account", style = MaterialTheme.typography.labelLarge)
        ExposedDropdownMenuBox(
            expanded = isAccountMenuExpanded,
            onExpandedChange = { isAccountMenuExpanded = !isAccountMenuExpanded }
        ) {

            TextField(
                value = selectedAccount?.accountName ?: "",
                placeholder = { Text(if (partners.isEmpty()) "No partners available" else "Select partner") },
                onValueChange = {},
                readOnly = true,
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = isAccountMenuExpanded)
                },
                colors = ExposedDropdownMenuDefaults.textFieldColors(),
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
            )

            ExposedDropdownMenu(
                expanded = isAccountMenuExpanded,
                onDismissRequest = { isAccountMenuExpanded = false }
            ) {
                accounts.forEach { account ->
                    DropdownMenuItem(
                        text = { Text(account.accountName, color = MaterialTheme.colorScheme.onSurface) },
                        onClick = {
                            selectedAccount = account
                            isAccountMenuExpanded = false
                        },
                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                    )
                }
            }
        }


        Spacer(Modifier.height(16.dp))

        RippleButton(
            modifier = Modifier.fillMaxWidth(),
            enabled = contractName.isNotBlank() && selectedAccount != null && selectedPartner != null && !isSubmitting && accounts.isNotEmpty() && partners.isNotEmpty(),
            onClick = {
                val trimmed = contractName.trim()
                if (trimmed.isBlank()) {
                    return@RippleButton
                }
                val accId = selectedAccount?.accountId
                val parId = selectedPartner?.partnerId
                if (accId == null || parId == null) {
                    return@RippleButton
                }
                isSubmitting = true
                scope.launch {
                    try {
                        component.addContract(
                            accountId = accId,
                            partnerId = parId,
                            name = trimmed,
                            description = description.trim().ifBlank { null }
                        )
                        contractName = ""
                        description = ""
                        onCreated()
                    } finally {
                        isSubmitting = false
                    }
                }
            }
        ) {
            if (isSubmitting) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.dp
                )
                Spacer(Modifier.height(0.dp))
            }
            Text(if (isSubmitting) "Creating…" else "Create")
        }
    }
}
