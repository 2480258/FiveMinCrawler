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

package com.fivemin.core.engine.session.database

import com.fivemin.core.engine.session.DatabaseAdapter
import java.sql.*

class DatabaseAdapterImpl(private val jdbcUrl: String) : DatabaseAdapter {
    
    val connection: Connection
    val insertPrepared: PreparedStatement
    val containsPrepared: PreparedStatement
    
    init {
        connection = DriverManager.getConnection(jdbcUrl)
        connection.autoCommit = true
    
        val statement: Statement = connection.createStatement()
        statement.queryTimeout = 30
        statement.executeUpdate("drop table if exists person")
        statement.executeUpdate("create table person (id string primary key)")
        statement.executeUpdate("create unique index keyindex on person(id)")
        
        insertPrepared = connection.prepareStatement("insert or ignore into person values(?)")
        containsPrepared = connection.prepareStatement("select * from person where id = ?")
    }
    
    override fun insertKeyIfNone(key: String): Boolean {
        insertPrepared.setString(1, key)
        val result = insertPrepared.executeUpdate()
        
        return result >= 1
    }
    
    override fun contains(key: String): Boolean {
        containsPrepared.setString(1, key)
        val result = containsPrepared.executeQuery()
        
        return result.next()
    }
}