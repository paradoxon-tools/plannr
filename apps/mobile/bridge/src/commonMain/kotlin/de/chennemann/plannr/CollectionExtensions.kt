package de.chennemann.plannr

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

@OptIn(ExperimentalContracts::class)
fun <T> Collection<T>?.isNeitherNullNorEmpty(): Boolean {
    contract {
        returns(true) implies (this@isNeitherNullNorEmpty != null)
    }
    return !isNullOrEmpty()
}