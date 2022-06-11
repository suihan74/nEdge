package com.suihan74.nedge

import org.junit.Assert.assertNotEquals
import org.junit.Test
import java.time.Duration
import java.time.LocalTime

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun duration_between() {
        val start = LocalTime.of(7, 0)
        val end = LocalTime.of(8, 0)
        val btw1 = Duration.between(start, end)
        val btw2 = Duration.between(end, start)
        assertNotEquals(btw1.toMillis(), btw2.toMillis())
    }
}
