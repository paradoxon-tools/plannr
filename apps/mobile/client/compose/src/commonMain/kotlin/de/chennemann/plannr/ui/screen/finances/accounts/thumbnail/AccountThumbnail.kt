package de.chennemann.plannr.ui.screen.finances.accounts.thumbnail

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import de.chennemann.plannr.data.Account
import de.chennemann.plannr.money.MoneyFormatter
import de.chennemann.plannr.ui.theme.colors

@Composable
fun AccountThumbnail(
    account: Account,
    onAccountClicked: (Account) -> Unit,
) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .clickable { onAccountClicked(account) }
            .background(MaterialTheme.colors.surfaceVariant)
            .padding(16.dp),
    ) {
        Text(
            text = account.accountName,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colors.tertiary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = "Pockets: ${account.pockets.size}",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colors.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = "Balance: ${MoneyFormatter.format(account.totalBalance, "EUR")}",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colors.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
