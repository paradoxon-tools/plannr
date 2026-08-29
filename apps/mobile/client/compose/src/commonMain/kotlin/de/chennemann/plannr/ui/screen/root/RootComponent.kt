package de.chennemann.plannr.ui.screen.root

interface RootComponent {
    class Factory {
        operator fun invoke(): RootComponent =
            DefaultRootComponent()
    }
}

private class DefaultRootComponent : RootComponent
