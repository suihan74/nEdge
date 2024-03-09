package com.suihan74.nedge.dataStore

import android.content.Context
import androidx.datastore.dataStore
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.ExperimentalSerializationApi
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@Suppress("NonAsciiCharacters")
class PreferencesDataStoreTest {
    @OptIn(ExperimentalSerializationApi::class)
    private val Context.dataStore by dataStore(
        fileName = "test.ds",
        serializer = PreferencesSerializer()
    )

    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    @Before
    fun init() {
        runBlocking {
            context.dataStore.updateData { Preferences() }
        }
    }

    @Test
    fun 読み込み() =runBlocking {
        val default = Preferences()
        val prefs = context.dataStore.data.first()
        assertEquals(default.lightLevelOff, prefs.lightLevelOff)
        assertEquals(default.lightLevelOn, prefs.lightLevelOn)
        assertEquals(default.silentTimezoneEnd, prefs.silentTimezoneEnd)
    }

    @Test
    fun 値の書き込み() = runBlocking {
        context.dataStore.updateData { prefs ->
            prefs.copy(lightLevelOff = .5f)
        }

        val prefs = context.dataStore.data.first()
        assertEquals(.5f, prefs.lightLevelOff)
    }
}
