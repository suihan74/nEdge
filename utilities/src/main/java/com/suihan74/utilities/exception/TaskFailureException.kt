package com.suihan74.utilities.exception

/**
 * 処理失敗時の汎用的な例外
 */
class TaskFailureException(message: String? = null, cause: Throwable? = null) : Throwable(message, cause)
