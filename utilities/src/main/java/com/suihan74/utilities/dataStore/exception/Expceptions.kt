package com.suihan74.utilities.dataStore.exception

// `WrappedDataStore`から送出されうる例外

/**
 * 使用できないキーが渡されたときの例外
 */
class InvalidKeyException(message: String? = null, cause: Throwable? = null) : Throwable(message, cause)

/**
 * マイグレーション失敗時の例外
 */
class MigrationFailureException(message: String? = null, cause: Throwable? = null) : Throwable(message, cause)
