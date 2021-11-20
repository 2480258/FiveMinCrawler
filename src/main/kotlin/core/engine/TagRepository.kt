package core.engine

import arrow.core.Option

interface TagRepository : Iterable<Tag> {
    operator fun get(key : String) : Tag
    fun contains(key : String) : Boolean
}

class TagRepositoryImpl : TagRepository{ constructor(
    src : Option<Iterable<Tag>>,
    connect : Option<TagRepository>){

    set = src.fold({ emptySet() }, { safe_src->
        if(safe_src.count { x -> x.isUnique } > 1){
            throw IllegalArgumentException("dup uniquekey")
        }

        connect.map { safe_conn ->
            if(safe_conn.any { x -> x.isUnique } && safe_src.any { x -> x.isUnique }){
                throw IllegalArgumentException("dup uniquekey with given target")
            }
        }

        if(safe_src.distinct().count() != safe_src.count()){
            throw IllegalArgumentException("name dup")
        }

        safe_src.toSet()
    })}

    private val set : Set<Tag>

    override fun get(key: String) : Tag {
        return set.first { x -> x.name == key }
    }

    override fun contains(key: String): Boolean {
        return set.any { x -> x.name == key }
    }

    override fun iterator(): Iterator<Tag> {
        return set.iterator()
    }
}