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

package com.fivemin.core

import java.net.URI
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

class ElemIterator<T>(val factory: IteratorElemFactory<T>) {
    val list = mutableListOf<T>()
    val lock = ReentrantLock()
    
    fun gen(): T {
        return lock.withLock {
            var t = factory.getNext()
            //list.add(t)
            
            return t
        }
    }
    
    operator fun get(index: Int): T? {
        return list.get(index)
    }
}

interface IteratorElemFactory<T> {
    fun getNext(): T
}

class UriIterator : IteratorElemFactory<URI> {
    val strFac = StringIterator()
    override fun getNext(): URI {
        
        return URI("http://" + strFac.getNext() + ".com/")
    }
}

class StringIterator : IteratorElemFactory<String> {
    val Str = "abcdefghijklmnopqrstuvwxyz0123456789"
    val reentrantLock = ReentrantLock()
    val len: Int = 8
    var calledCount = 0
    
    override fun getNext(): String {
        return reentrantLock.withLock {
            calledCount++
            return calledCount.toString(36)
        }
    }
}