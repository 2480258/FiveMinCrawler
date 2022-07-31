/*
 *
 *     FiveMinCrawler
 *     Copyright (C) 2022  2480258
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 */

package com.fivemin.core.engine

import arrow.core.none
import io.mockk.mockk
import org.testng.annotations.Test

import org.testng.Assert.*
import java.nio.charset.Charset

class StringFilterImplTest {
    
    val bomCharsets: Map<Charset, ByteArray> = mapOf(
        Charsets.UTF_8 to byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte()),
        Charsets.UTF_32BE to byteArrayOf(0x00.toByte(), 0x00.toByte(), 0xFE.toByte(), 0xFF.toByte()),
        Charsets.UTF_32LE to byteArrayOf(0xFF.toByte(), 0xFE.toByte(), 0x00.toByte(), 0x00.toByte()),
        Charsets.UTF_16BE to byteArrayOf(0xFE.toByte(), 0xFF.toByte()),
        Charsets.UTF_16LE to byteArrayOf(0xFF.toByte(), 0xFE.toByte())
    )
    
    @Test
    fun testWrite_JudgeEncoding_UTF8() {
        val cs = Charsets.UTF_8
        
        val stringFilter = StringFilterImpl(mockk(relaxed = true), none())
        
        stringFilter.write(bomCharsets[cs]!!, 0, bomCharsets[cs]!!.size)
        
        assertEquals(stringFilter.encoding, cs)
    }
    
    
    @Test
    fun testWrite_JudgeEncoding_UTF16LE() {
        val cs = Charsets.UTF_16LE
        
        val stringFilter = StringFilterImpl(mockk(relaxed = true), none())
        
        stringFilter.write(bomCharsets[cs]!!, 0, bomCharsets[cs]!!.size)
        
        assertEquals(stringFilter.encoding, cs)
    }
    
    
    @Test
    fun testWrite_JudgeEncoding_UTF16BE() {
        val cs = Charsets.UTF_16BE
        
        val stringFilter = StringFilterImpl(mockk(relaxed = true), none())
        
        stringFilter.write(bomCharsets[cs]!!, 0, bomCharsets[cs]!!.size)
        
        assertEquals(stringFilter.encoding, cs)
    }
    
    
    @Test
    fun testWrite_JudgeEncoding_UTF32LE() {
        val cs = Charsets.UTF_32LE
        
        val stringFilter = StringFilterImpl(mockk(relaxed = true), none())
        
        stringFilter.write(bomCharsets[cs]!!, 0, bomCharsets[cs]!!.size)
        
        assertEquals(stringFilter.encoding, cs)
    }
    
    
    @Test
    fun testWrite_JudgeEncoding_UTF32BE() {
        val cs = Charsets.UTF_32BE
        
        val stringFilter = StringFilterImpl(mockk(relaxed = true), none())
        
        stringFilter.write(bomCharsets[cs]!!, 0, bomCharsets[cs]!!.size)
        
        assertEquals(stringFilter.encoding, cs)
    }
}