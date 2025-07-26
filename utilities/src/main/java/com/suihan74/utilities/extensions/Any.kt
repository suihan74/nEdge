@file:Suppress("unused")

package com.suihan74.utilities.extensions

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

/** 値が一致するときだけactionを実行する */
inline fun <T> T.on(expected: T, crossinline action: (T)->Unit) : T {
    if (this == expected) {
        action.invoke(this)
    }
    return this
}

/** 値が一致するときだけactionを実行する */
inline fun <T> T.on(
    expected: T,
    crossinline comparer: (actual: T, expected: T)->Boolean,
    crossinline action: (T)->Unit
) : T {
    if (comparer(this, expected)) {
        action.invoke(this)
    }
    return this
}

// ------ //

/** 値が一致しないときだけactionを実行する */
inline fun <T> T.onNot(expected: T, crossinline action: (T)->Unit) : T {
    if (this != expected) {
        action.invoke(this)
    }
    return this
}

/** 値が一致しないときだけactionを実行する */
inline fun <T> T.onNot(
    expected: T,
    crossinline comparer: (actual: T, expected: T)->Boolean,
    crossinline action: (T)->Unit
) : T {
    if (!comparer(this, expected)) {
        action.invoke(this)
    }
    return this
}

// ------ //

/** 与えられた型のときだけ実行するlet */
@OptIn(ExperimentalContracts::class)
inline fun <reified T, R> Any?.letAs(crossinline block: (T) -> R): R? {
    contract {
        callsInPlace(block, InvocationKind.AT_MOST_ONCE)
    }

    return if (this is T) block(this)
    else null
}

/** 与えられた型のときだけ実行するalso */
@OptIn(ExperimentalContracts::class)
inline fun <reified T> Any?.alsoAs(crossinline block: (T) -> Unit): T? {
    contract {
        callsInPlace(block, InvocationKind.AT_MOST_ONCE)
    }

    return if (this is T) {
        block(this)
        this
    }
    else null
}
