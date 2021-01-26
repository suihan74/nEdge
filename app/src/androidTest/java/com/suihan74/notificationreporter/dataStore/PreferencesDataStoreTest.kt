package com.suihan74.notificationreporter.dataStore

import androidx.datastore.createDataStore
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@Suppress("NonAsciiCharacters")
class PreferencesDataStoreTest {
    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private val dataStore = context.createDataStore(
        fileName = "test.ds",
        serializer = PreferencesSerializer()
    )

    @Before
    fun init() {
        runBlocking {
            dataStore.updateData { Preferences() }
        }
    }

    @Test
    fun 読み込み() =runBlocking {
        val default = Preferences()
        val prefs = dataStore.data.first()
        assertEquals(default.lightLevelOff, prefs.lightLevelOff)
        assertEquals(default.lightLevelOn, prefs.lightLevelOn)
        assertEquals(default.silentTimezoneEnd, prefs.silentTimezoneEnd)
    }

    @Test
    fun 値の書き込み() = runBlocking {
        dataStore.updateData { prefs ->
            prefs.copy(lightLevelOff = .5f)
        }

        val prefs = dataStore.data.first()
        assertEquals(.5f, prefs.lightLevelOff)
    }
}
