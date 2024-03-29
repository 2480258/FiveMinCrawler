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

package com.fivemin.core.engine.crawlingTask

import com.fivemin.core.engine.CrawlerTaskFactory
import com.fivemin.core.engine.CrawlerTaskFactoryFactory
import com.fivemin.core.engine.Request

/**
 * Factory Method for CrawlerTaskFactory.
 */
class CrawlerTaskFactoryFactoryImpl(private val storage: DocumentPolicyStorageFactoryCollector<Request>) : CrawlerTaskFactoryFactory<Request> {
    override fun getFactory(): CrawlerTaskFactory<Request> {
        return CrawlerTaskFactory(storage)
    }
}
