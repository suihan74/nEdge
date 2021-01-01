package com.suihan74.notificationreporter.models

enum class ClockStyle(
    val pattern : String
) {
    NORMAL("HH:mm"),
    RECTANGLE("HH\nmm")
}
