package de.chennemann.plannr.ui.screen.finances.transactions

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import de.chennemann.plannr.data.Transaction
import de.chennemann.plannr.money.MoneyFormatter
import de.chennemann.plannr.ui.theme.colors

@Composable
fun TransactionThumbnailContent(
    transaction: Transaction,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colors.primary)
            .padding(16.dp),
    ) {
        Text(
            text = transaction.title + " - " + transaction.description,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colors.onPrimaryMuted,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = transaction.date.toString(),
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colors.onPrimaryMuted,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = MoneyFormatter.format(transaction.amount),
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colors.onPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
