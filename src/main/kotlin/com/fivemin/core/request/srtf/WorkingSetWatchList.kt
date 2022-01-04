package com.fivemin.core.request.srtf

import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

class WorkingSetWatchList {
    private val blocks: MutableMap<SRTFPageBlock, Counter> = mutableMapOf()
    private val lock = ReentrantLock()

    val count: Int
        get() {
            lock.withLock {
                return blocks.count()
            }
        }

    fun add(block: SRTFPageBlock) {
        lock.withLock {
            if (!blocks.containsKey(block)) {
                blocks[block] = Counter()
            }

            blocks[block]!!.increase()
        }
    }

    fun get(): Iterable<Map.Entry<SRTFPageBlock, Counter>> {
        lock.withLock {
            return blocks.asIterable().toList()
        }
    }

    fun remove(block: SRTFPageBlock) {
        lock.withLock {
            if (!blocks.containsKey(block)) {
                return
            }

            blocks[block]!!.decrease()

            if (blocks[block]!!.count == 0) {
                blocks.remove(block)
            }
        }
    }
}
