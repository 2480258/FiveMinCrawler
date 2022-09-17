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
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.sqlite.javax.SQLiteConnectionPoolDataSource
import java.sql.Connection
import java.sql.DriverManager
import java.sql.PreparedStatement
import java.sql.Statement
import javax.sql.DataSource


class DatabaseAdapterFactoryImpl(private val jdbcUrl: String) {
    private val dataSource: HikariDataSource
    
    init {
        val config = HikariConfig()
        config.jdbcUrl = jdbcUrl
        config.connectionTimeout = 500000
        config.maximumPoolSize = 1
        dataSource = HikariDataSource(config)
        
        initializeTable()
    }
    
    private fun initializeTable() {
        var con : Connection? = null
        
        try {
            con = dataSource.getConnection()
            val statement = con.createStatement()
            
            statement.executeUpdate("create table if not exists keys (id string primary key, finalized integer)")
            statement.executeUpdate("create unique index if not exists keyindex on keys(id)")
            
        } finally {
            con?.close()
        }
    }
    
    fun get() : DatabaseAdapter {
        return DatabaseAdapterImpl(dataSource)
    }
}

class DatabaseAdapterImpl(private val dataSource: DataSource) : DatabaseAdapter {
    
    private fun <T> ensureConnection(func: (Connection) -> T) : T {
        var con : Connection? = null
    
        try {
            con = dataSource.getConnection()
            return func(con)
        } finally {
            con?.close()
        }
    }
    
    override fun insertKeyIfNone(key: String): Boolean {
        return ensureConnection {
            var insertPrepared : PreparedStatement? = null
            
            try {
                insertPrepared = it.prepareStatement("insert or ignore into keys values(?, ?)")
    
                insertPrepared.setString(1, key)
                insertPrepared.setInt(2, 0)
                val result = insertPrepared.executeUpdate()
    
                result >= 1
            } finally {
                insertPrepared?.close()
            }
        }
    }
    
    override fun contains(key: String): Boolean {
        return ensureConnection {
            var containsPrepared : PreparedStatement? = null
            
            try {
                containsPrepared = it.prepareStatement("select * from keys where id = ?")
                
                containsPrepared.setString(1, key)
                val result = containsPrepared.executeQuery()
    
                result.next()
            } finally {
                containsPrepared?.close()
            }
        }
    }
    
    override fun finalizeKey(key: String): Boolean {
        return ensureConnection {
            var updatePrepared : PreparedStatement? = null
            
            try {
                updatePrepared = it.prepareStatement("update keys set finalized = 1 where id = ? and finalized = 0")
                
                updatePrepared.setString(1, key)
                val result = updatePrepared.executeUpdate()
    
                result >= 1
            } finally {
                updatePrepared?.close()
            }
        }
    }
    
    override fun removeNotFinalized(): Boolean {
        return ensureConnection {
            var removePrepared : PreparedStatement? = null
            
            try {
                removePrepared = it.prepareStatement("delete from keys where finalized = 0")
                
                val result = removePrepared.executeUpdate()
                
                result >= 1
            } finally {
                removePrepared?.close()
            }
        }
    }
    
    
}