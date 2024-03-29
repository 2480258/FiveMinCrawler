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

package com.fivemin.core.engine.session

import com.fivemin.core.engine.UniqueKey


interface UniqueKeyPersister {
    fun persistKey(key: UniqueKey) : Boolean
    
    fun contains(key: UniqueKey) : Boolean
    
    fun finalizeKey(key: UniqueKey) : Boolean
}

class UniqueKeyPersisterImpl (private val databaseAdapter: DatabaseAdapter) : UniqueKeyPersister{
    override fun persistKey(key: UniqueKey): Boolean {
        return databaseAdapter.insertKeyIfNone(key.toString())
    }
    
    override fun contains(key: UniqueKey): Boolean {
        return databaseAdapter.contains(key.toString())
    }
    
    override fun finalizeKey(key: UniqueKey): Boolean {
        return databaseAdapter.finalizeKey(key.toString())
    }
}