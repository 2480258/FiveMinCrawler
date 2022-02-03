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

import arrow.core.Option
import arrow.core.none

interface TagRepository : Iterable<Tag> {
    operator fun get(key: String): Tag
    fun contains(key: String): Boolean
}

class TagRepositoryImpl(
    private val src: Option<Iterable<Tag>> = none(),
    private val connect: Option<TagRepository> = none()
) :
    TagRepository {

    private val set: Set<Tag>

    init {
        set = src.fold({ emptySet() }, { safe_src ->
            if (safe_src.count { x -> x.isUnique } > 1) {
                throw IllegalArgumentException("dup uniquekey")
            }

            connect.map { safe_conn ->
                if (safe_conn.any { x -> x.isUnique } && safe_src.any { x -> x.isUnique }) {
                    throw IllegalArgumentException("dup uniquekey with given target")
                }
            }

            if (safe_src.distinct().count() != safe_src.count()) {
                throw IllegalArgumentException("name dup")
            }

            safe_src.toSet()
        })
    }

    override fun get(key: String): Tag {
        if (set.any {
            it.name == key
        }
        ) {
            return set.single {
                it.name == key
            }
        }

        connect.map {
            if (it.contains(key)) {
                return@get it.get(key)
            }
        }

        throw IllegalArgumentException()
    }

    override fun contains(key: String): Boolean {
        return set.any { x -> x.name == key } || connect.fold({ false }, {
            it.contains(key)
        })
    }

    override fun iterator(): Iterator<Tag> {
        return connect.fold({ listOf() }) {
            it.filter {
                !set.map {
                    it.name
                }.contains(it.name)
            }
        }.plus(set).iterator()
    }
}
