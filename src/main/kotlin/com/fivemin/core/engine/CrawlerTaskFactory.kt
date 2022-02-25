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

import com.fivemin.core.engine.crawlingTask.DocumentPolicyStorageFactoryCollector

class CrawlerTaskFactory<Document : Request>(val policySet: DocumentPolicyStorageFactoryCollector) {
    inline fun <reified S1 : Transaction<Document>, reified S2 : StrictTransaction<S1, Document>> get1(type: DocumentType): CrawlerTask1<S1, S2, Document, Document> {
        val ps = policySet.getFactory(type).create<Document>()
        return CrawlerTask1(ps.find())
    }

    inline fun <reified S1 : Transaction<Document>, reified S2 : StrictTransaction<S1, Document>, reified S3 : StrictTransaction<S2, Document>> get2(
        type: DocumentType
    ): CrawlerTask2<S1, S2, S3, Document, Document, Document> {
        val ps = policySet.getFactory(type).create<Document>()
        return CrawlerTask2(ps.find(), ps.find())
    }

    inline fun <reified S1 : Transaction<Document>, reified S2 : StrictTransaction<S1, Document>, reified S3 : StrictTransaction<S2, Document>, reified S4 : StrictTransaction<S3, Document>> get3(
        type: DocumentType
    ): CrawlerTask3<S1, S2, S3, S4, Document, Document, Document, Document> {
        val ps = policySet.getFactory(type).create<Document>()
        return CrawlerTask3(ps.find(), ps.find(), ps.find())
    }

    inline fun <reified S1 : Transaction<Document>, reified S2 : StrictTransaction<S1, Document>, reified S3 : StrictTransaction<S2, Document>, reified S4 : StrictTransaction<S3, Document>, reified S5 : StrictTransaction<S4, Document>> get4(
        type: DocumentType
    ): CrawlerTask4<S1, S2, S3, S4, S5, Document, Document, Document, Document, Document> {
        val ps = policySet.getFactory(type).create<Document>()
        return CrawlerTask4(ps.find(), ps.find(), ps.find(), ps.find())
    }
}
