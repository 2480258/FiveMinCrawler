package fivemin.core.engine

import arrow.core.Option
import arrow.core.none

interface TagRepository : Iterable<Tag> {
    operator fun get(key: String): Tag
    fun contains(key: String): Boolean
}

class TagRepositoryImpl(private val src: Option<Iterable<Tag>> = none(), private val connect: Option<TagRepository> = none()) :
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
        return set.first { x -> x.name == key }
    }

    override fun contains(key: String): Boolean {
        return set.any { x -> x.name == key }
    }

    override fun iterator(): Iterator<Tag> {
        return set.iterator()
    }
}