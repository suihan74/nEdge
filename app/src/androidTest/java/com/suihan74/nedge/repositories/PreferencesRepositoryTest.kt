package com.suihan74.nedge.repositories

import android.app.Notification
import android.os.Bundle
import android.service.notification.StatusBarNotification
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import com.suihan74.nedge.models.KeywordMatchingType
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PreferencesRepositoryTest {
    @Test
    fun キーワードマッチング() {
        val repo = PreferencesRepository(
            dataStore = mock(),
            notificationDao = mock()
        )

        val isMatchKeyword = PreferencesRepository::class.java.getDeclaredMethod(
            "isMatchKeyword",
            StatusBarNotification::class.java,
            String::class.java,
            KeywordMatchingType::class.java,
        ).apply {
            isAccessible = true
        }

        val mockNotification = mock<StatusBarNotification>().also {
            whenever(it.notification) doReturn Notification().apply {
                extras = Bundle().apply {
                    putCharSequence(Notification.EXTRA_TITLE, "title")
                    putCharSequence(Notification.EXTRA_TEXT, "text")
                }
            }
        }

        // タイトル一致
        assertEquals(true, isMatchKeyword.invoke(repo, mockNotification, "title", KeywordMatchingType.INCLUDE))
        // テキスト一致
        assertEquals(true, isMatchKeyword.invoke(repo, mockNotification, "text", KeywordMatchingType.INCLUDE))
        // タイトル一致(失敗)
        assertEquals(false, isMatchKeyword.invoke(repo, mockNotification, "タイトル", KeywordMatchingType.INCLUDE))
        // テキスト一致(失敗)
        assertEquals(false, isMatchKeyword.invoke(repo, mockNotification, "テキスト", KeywordMatchingType.INCLUDE))

        // タイトル不一致
        assertEquals(true, isMatchKeyword.invoke(repo, mockNotification, "タイトル", KeywordMatchingType.EXCLUDE))
        // テキスト不一致
        assertEquals(true, isMatchKeyword.invoke(repo, mockNotification, "テキスト", KeywordMatchingType.EXCLUDE))
        // タイトル不一致(失敗)
        assertEquals(false, isMatchKeyword.invoke(repo, mockNotification, "title", KeywordMatchingType.EXCLUDE))
        // テキスト不一致(失敗)
        assertEquals(false, isMatchKeyword.invoke(repo, mockNotification, "text", KeywordMatchingType.EXCLUDE))

        // タイトル部分一致
        assertEquals(true, isMatchKeyword.invoke(repo, mockNotification, "tit", KeywordMatchingType.INCLUDE))
        // テキスト部分一致
        assertEquals(true, isMatchKeyword.invoke(repo, mockNotification, "xt", KeywordMatchingType.INCLUDE))

        // タイトル部分含まず(失敗)
        assertEquals(false, isMatchKeyword.invoke(repo, mockNotification, "tit", KeywordMatchingType.EXCLUDE))
        // テキスト部分含まず(失敗)
        assertEquals(false, isMatchKeyword.invoke(repo, mockNotification, "xt", KeywordMatchingType.EXCLUDE))

        // 複数キーワード両方一致
        assertEquals(true, isMatchKeyword.invoke(repo, mockNotification, "title text", KeywordMatchingType.INCLUDE))
        // 複数キーワードでひとつは一致する
        assertEquals(true, isMatchKeyword.invoke(repo, mockNotification, "タイトル text", KeywordMatchingType.INCLUDE))
        // 複数キーワードでひとつは一致する
        assertEquals(true, isMatchKeyword.invoke(repo, mockNotification, "タイトル テキスト text", KeywordMatchingType.INCLUDE))
        // 複数キーワードでひとつも一致しない
        assertEquals(false, isMatchKeyword.invoke(repo, mockNotification, "タイトル テキスト", KeywordMatchingType.INCLUDE))

    }
}
