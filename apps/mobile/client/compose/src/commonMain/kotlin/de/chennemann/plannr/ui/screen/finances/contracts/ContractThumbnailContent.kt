package de.chennemann.plannr.ui.screen.finances.contracts

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
import de.chennemann.plannr.data.Contract
import de.chennemann.plannr.ui.theme.colors

@Composable
fun ContractThumbnailContent(
    contract: Contract,
    onContractClicked: (Contract) -> Unit,
) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable { onContractClicked(contract) }
            .background(MaterialTheme.colors.primary)
            .padding(16.dp),
    ) {
        Text(
            text = contract.name,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colors.onPrimaryMuted,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = contract.partner.name,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colors.onPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
