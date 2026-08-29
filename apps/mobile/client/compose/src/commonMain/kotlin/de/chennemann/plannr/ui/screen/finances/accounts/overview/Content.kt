package de.chennemann.plannr.ui.screen.finances.accounts.overview

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cheonjaeung.compose.grid.SimpleGridCells
import com.cheonjaeung.compose.grid.VerticalGrid
import de.chennemann.plannr.resources.Res
import de.chennemann.plannr.ui.components.button.RippleButton
import de.chennemann.plannr.ui.screen.finances.accounts.thumbnail.AccountThumbnail
import de.chennemann.plannr.ui.theme.colors
import dev.icerock.moko.resources.compose.stringResource
import kotlinx.coroutines.launch

@Composable
fun AccountOverviewContent(
    component: AccountOverviewComponent,
) {
    val accounts = component.accounts.collectAsState().value
    val scope = rememberCoroutineScope()

    Text(
        text = stringResource(Res.strings.finances_accounts_section_label),
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colors.onBackground
    )

    Spacer(Modifier.height(16.dp))

    VerticalGrid(
        columns = SimpleGridCells.Fixed(2),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        accounts.forEach { account ->
            AccountThumbnail(account = account, onAccountClicked = component::onAccountClicked)
        }
    }

    Spacer(Modifier.height(24.dp))
    RippleButton(
        modifier = Modifier,
        onClick = { scope.launch { component.addAccount() } }
    ) {
        Text("Add Account")
    }
}
