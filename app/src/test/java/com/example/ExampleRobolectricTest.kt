package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.quran.QuranRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read app name string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Quran Video Editor", appName)
    }

    @Test
    fun `verify Quran repository has all 114 surahs`() {
        val repo = QuranRepository()
        assertEquals(114, repo.allSurahs.size)
        assertEquals("Al-Fatihah", repo.allSurahs[0].transliteration)
        assertEquals("An-Nas", repo.allSurahs[113].transliteration)
        assertNotNull(QuranRepository.BASMALA_ARABIC)
    }
}
