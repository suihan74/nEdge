@file:Suppress("unused")

package com.suihan74.utilities.extensions

import android.app.Notification

val Notification.title : CharSequence
    get() = extras.run {
       getCharSequence(Notification.EXTRA_TITLE, "")
    }

val Notification.text : CharSequence
    get() = extras.run {
        getCharSequence(Notification.EXTRA_TEXT, "")
    }

val Notification.subText : CharSequence
    get() = extras.run {
        getCharSequence(Notification.EXTRA_SUB_TEXT, "")
    }

val Notification.bigText : CharSequence
    get() = extras.run {
        getCharSequence(Notification.EXTRA_BIG_TEXT, "")
    }

val Notification.summaryText : CharSequence
    get() = extras.run {
        getCharSequence(Notification.EXTRA_SUMMARY_TEXT, "")
    }

val Notification.titleBig : CharSequence
    get() = extras.run {
        getCharSequence(Notification.EXTRA_TITLE_BIG, "")
    }


fun Notification.contains(keyword: CharSequence) : Boolean =
    title.contains(keyword) || text.contains(keyword)
