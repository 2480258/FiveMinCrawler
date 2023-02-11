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

package com.fivemin.core.logger

import org.testng.Assert.*
import org.testng.annotations.Test
import kotlin.reflect.KClassifier
import kotlin.reflect.KProperty1
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.starProjectedType

class PropertyBFSTest {
    
    open class AOPTestA(val k: String)
    
    class AOPTestD(val a: AOPTestA, val f: AOPTestF?)
    
    class AOPTestF(val d: AOPTestD)
    
    class AOPTestB(val k: List<String>)
    
    class AOPTestC(val g: String) : AOPTestA(g)
    
    
    
    @Test
    fun testSameProperty() {
        val a = AOPTestA("a")
        val b = AOPTestA("b")
        
        assertEquals(a::class.memberProperties.first(), b::class.memberProperties.first())
    }
    
    @Test
    fun testGenericTypeIsSame() {
        val a = listOf<String>("a")::class
        val b = listOf<Int>(1)::class
    
        assertEquals(a, b)
    }
    
    @Test
    fun testRecursiveTypeWithAnswer() {
        val p = BFSUtil()
        
        val a = AOPTestD(AOPTestA("a"), AOPTestF(AOPTestD(AOPTestA("b"), null)))
        
        val ret = p.find(a, String::class)
        
        assertEquals(ret, "a")
    }
    
    @Test
    fun testRecursiveTypeWithoutAnswer() {
        val p = BFSUtil()
        
        val a = AOPTestD(AOPTestA("a"), AOPTestF(AOPTestD(AOPTestA("b"), null)))
        
        val ret = p.find(a, Int::class)
        
        assertEquals(ret, null)
    }
    
    @Test
    fun testSearchMultipleTimes() {
        val p = BFSUtil()
        
        val a = AOPTestD(AOPTestA("a"), AOPTestF(AOPTestD(AOPTestA("b"), null)))
        
        val ret1 = p.find(a, String::class)
        val ret2 = p.find(a, String::class)
        assertEquals(ret2, "a")
    }
    
    @Test
    fun testNoSearchForwardOtherPackages() {
        val p = BFSUtil()
        
        val a = AOPTestB(listOf("a"))
        
        val ret = p.find(a, String::class)
        assertEquals(ret, null)
    }
    
    @Test
    fun testInheritedClass() {
        val p = BFSUtil()
        
        val a = AOPTestD(AOPTestC("a"), null)
        
        val ret = p.find(a, AOPTestA::class)?.k
        assertEquals(ret, "a")
    }
    
    @Test
    fun testInheritedClassReversed() {
        val p = BFSUtil()
        
        val a = AOPTestD(AOPTestA("a"), null)
        
        val ret = p.find(a, AOPTestC::class)?.k
        assertEquals(ret, null)
    }
}