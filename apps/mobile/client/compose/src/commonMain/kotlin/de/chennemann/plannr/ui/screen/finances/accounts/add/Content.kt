package de.chennemann.plannr.ui.screen.finances.accounts.add

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import de.chennemann.plannr.ui.components.button.RippleButton
import kotlinx.coroutines.launch

@Composable
fun AddAccountScreen(
    component: AddAccountComponent,
    onCreated: () -> Unit = {}
) {
    val scope = rememberCoroutineScope()
    var accountName by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }
    var errorText by remember { mutableStateOf<String?>(null) }
    val focusManager = LocalFocusManager.current

    val maxLength = 40

    Column(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Create account",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(Modifier.height(12.dp))

                TextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = accountName,
                    onValueChange = {
                        if (it.length <= maxLength) {
                            accountName = it
                            errorText = null
                        }
                    },
                    label = { Text("Account name") },
                    placeholder = { Text("e.g. Savings, Daily, Travel…") },
                    isError = errorText != null,
                    supportingText = {
                        val helper = errorText ?: "${'$'}{accountName.length}/${'$'}maxLength"
                        Text(helper)
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            val trimmed = accountName.trim()
                            if (trimmed.isBlank()) {
                                errorText = "Please enter a name"
                                return@KeyboardActions
                            }
                            if (!isSubmitting) {
                                focusManager.clearFocus()
                                isSubmitting = true
                                scope.launch {
                                    try {
                                        component.addAccount(trimmed)
                                        accountName = ""
                                        errorText = null
                                        onCreated()
                                    } finally {
                                        isSubmitting = false
                                    }
                                }
                            }
                        }
                    )
                )

                Spacer(Modifier.height(16.dp))

                RippleButton(
                    modifier = Modifier.fillMaxWidth(),
                    enabled = accountName.isNotBlank() && !isSubmitting,
                    onClick = {
                        val trimmed = accountName.trim()
                        if (trimmed.isBlank()) {
                            errorText = "Please enter a name"
                            return@RippleButton
                        }
                        focusManager.clearFocus()
                        isSubmitting = true
                        scope.launch {
                            try {
                                component.addAccount(trimmed)
                                accountName = ""
                                errorText = null
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
    }
}
