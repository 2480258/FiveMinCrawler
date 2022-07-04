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

package com.fivemin.core.database

import com.querydsl.core.annotations.QueryEntity
import com.querydsl.sql.Configuration
import com.querydsl.sql.SQLQueryFactory
import com.querydsl.sql.SQLiteTemplates
import org.sqlite.SQLiteDataSource
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test
import org.testng.Assert.*
import java.sql.DriverManager

@QueryEntity
class Persion {

}

class DatabaseTest {
    @Test
    fun dbConnectTest() {
        val dbUrl = "jdbc:sqlite:./deleteMe.db"
        
        val dataSource = SQLiteDataSource()
        dataSource.url = dbUrl
        
        val connection = DriverManager.getConnection(dbUrl)
        val stmt = connection.createStatement()
        stmt.executeUpdate("CREATE TABLE Persons (" +
                "    PersonID int," +
                "    LastName varchar(255)," +
                "    FirstName varchar(255)," +
                "    Address varchar(255)," +
                "    City varchar(255)" +
                ");")
        
        val template = SQLiteTemplates()
        val config = Configuration(template)
        
        val queryFactory = SQLQueryFactory(config, dataSource)
    }
}