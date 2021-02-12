package com.suihan74.nedge.models

enum class ClockStyle(
    val pattern : String
) {
    SINGLE_LINE("HH:mm"),
    RECTANGLE("HH\nmm")
}
