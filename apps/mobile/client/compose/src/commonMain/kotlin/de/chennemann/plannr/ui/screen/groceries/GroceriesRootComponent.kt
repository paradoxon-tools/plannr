package de.chennemann.plannr.ui.screen.groceries

interface GroceriesRootComponent {
    fun openDialog()

    class Factory {
        operator fun invoke(onOpenDialogRequested: () -> Unit): GroceriesRootComponent =
            DefaultGroceriesRootComponent(onOpenDialogRequested)
    }
}

private class DefaultGroceriesRootComponent(
    private val onOpenDialogRequested: () -> Unit,
) : GroceriesRootComponent {

    override fun openDialog() {
        onOpenDialogRequested()
    }
}
