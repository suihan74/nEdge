package com.suihan74.nedge.dataStore

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationException
import kotlinx.serialization.StringFormat
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.io.OutputStream

/**
 * アプリ設定`Preferences`保存用データストアのシリアライザ
 */
@OptIn(ExperimentalSerializationApi::class)
class PreferencesSerializer(
    private val stringFormat: StringFormat = Json
) : Serializer<Preferences> {
    override val defaultValue: Preferences
        get() = Preferences()

    override suspend fun writeTo(t: Preferences, output: OutputStream) {
        val str = stringFormat.encodeToString(t)
        val bytes = str.encodeToByteArray()
        withContext(Dispatchers.IO) {
            output.write(bytes)
        }
    }

    override suspend fun readFrom(input: InputStream): Preferences {
        try {
            val bytes = input.readBytes()
            val str = bytes.decodeToString()
            return stringFormat.decodeFromString(str)
        }
        catch (e: SerializationException) {
            throw CorruptionException("failed to read stored data", e)
        }
    }
}
