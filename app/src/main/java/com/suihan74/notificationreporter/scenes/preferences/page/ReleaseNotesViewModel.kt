package com.suihan74.notificationreporter.scenes.preferences.page

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.suihan74.notificationreporter.Application
import com.suihan74.notificationreporter.R
import com.suihan74.notificationreporter.models.ReleaseNote
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.threeten.bp.LocalDate

class ReleaseNotesViewModel(
    private val application: Application
) : ViewModel() {
    /** 更新履歴 */
    val items : LiveData<List<ReleaseNote>> by lazy { _items }
    private val _items = MutableLiveData<List<ReleaseNote>>(emptyList())

    // ------ //

    init {
        viewModelScope.launch {
            load()
        }
    }

    // ------ //

    /**
     * 更新履歴をロードする
     */
    private suspend fun load() = withContext(Dispatchers.IO) {
        val items = ArrayList<ReleaseNote>()
        try {
            val titleRegex = Regex("""^\s*\[\s*version\s*:?\s*(\S+)\s*]\s*(\d\d\d\d-\d\d-\d\d)\s*$""")
            application.resources.openRawResource(R.raw.release_notes).bufferedReader().use { reader ->
                reader.useLines {
                    var version = ""
                    var timestamp = LocalDate.MIN
                    var description = ""
                    it.forEach { line ->
                        val matches = titleRegex.matchEntire(line)
                        if (matches == null) {
                            description += if (description.isBlank()) line else "\n$line"
                        }
                        else {
                            if (version.isNotBlank()) {
                                items.add(ReleaseNote(version, description, timestamp))
                            }
                            version = matches.groupValues[1]
                            timestamp = LocalDate.parse(matches.groupValues[2])
                            description = ""
                        }
                    }
                    if (version.isNotBlank()) {
                        items.add(ReleaseNote(version, description, timestamp))
                    }
                }
            }
        }
        catch (e: Throwable) {
            Log.e("ReleaseNotes", Log.getStackTraceString(e))
        }
        _items.postValue(items)
    }
}

