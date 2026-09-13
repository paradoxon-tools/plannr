package de.chennemann.plannr.ui.screen.finances.contracts

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
import de.chennemann.plannr.data.Contract
import de.chennemann.plannr.database.repository.ContractRepository
import de.chennemann.plannr.resources.Res
import de.chennemann.plannr.ui.components.button.RippleButton
import de.chennemann.plannr.ui.theme.colors
import dev.icerock.moko.resources.compose.stringResource
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

interface ContractOverviewComponent {
    val contracts: StateFlow<List<Contract>>

    suspend fun addContract()
    suspend fun updateContractName(contractId: Long, name: String)
    suspend fun updateContractDescription(contractId: Long, description: String)
    fun onContractClicked(contract: Contract)

    class Factory(
        private val contractRepository: ContractRepository,
    ) {
        operator fun invoke(
            onContractClicked: (Contract) -> Unit,
            onAddContractRequested: () -> Unit,
        ): ContractOverviewComponent =
            DefaultContractOverviewComponent(
                contractRepository = contractRepository,
                onContractClicked = onContractClicked,
                onAddContractRequested = onAddContractRequested,
            )
    }
}

private class DefaultContractOverviewComponent(
    private val contractRepository: ContractRepository,
    private val onContractClicked: (Contract) -> Unit,
    private val onAddContractRequested: () -> Unit,
) : ContractOverviewComponent {
    override val contracts = contractRepository.contracts

    override suspend fun addContract() {
        onAddContractRequested()
    }

    override suspend fun updateContractName(contractId: Long, name: String) {
        contractRepository.updateContractName(contractId, name)
    }

    override suspend fun updateContractDescription(contractId: Long, description: String) {
        contractRepository.updateContractDescription(contractId, description)
    }

    override fun onContractClicked(contract: Contract) {
        onContractClicked.invoke(contract)
    }
}

@Composable
fun ContractOverviewContent(
    component: ContractOverviewComponent,
) {
    val contracts = component.contracts.collectAsState().value
    val scope = rememberCoroutineScope()

    Text(
        text = stringResource(Res.strings.finances_contracts_section_label),
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colors.onBackground,
    )

    Spacer(Modifier.height(16.dp))

    VerticalGrid(
        columns = SimpleGridCells.Fixed(2),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        contracts.forEach { contract ->
            ContractThumbnailContent(
                contract = contract,
                onContractClicked = component::onContractClicked,
            )
        }
    }

    Spacer(Modifier.height(24.dp))
    RippleButton(
        modifier = Modifier,
        onClick = { scope.launch { component.addContract() } },
    ) {
        Text("Add Contract")
    }
}
