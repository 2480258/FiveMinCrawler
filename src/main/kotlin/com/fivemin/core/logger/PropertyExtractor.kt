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

package com.fivemin.core.logger

import arrow.core.memoize
import java.util.LinkedList
import kotlin.reflect.*
import kotlin.reflect.full.*
import kotlin.reflect.jvm.jvmErasure

class PropertyExtractor {
    val bfsTool_private = PropertyBFS()
    
    inline fun <reified T : Any> find(thisObj: Any, wantType: KClass<T>, genericTypes: List<KTypeProjection> = listOf()) : T? {
        try {
            if(thisObj is T)
                return thisObj
            
            val props = thisObj::class.memberProperties.map {
                it as KProperty1<Any, Any>
            }.toList()
    
            var retObj: List<KProperty1<Any, Any>>? = null
            var count = 0
            
            while((retObj == null) && (count < props.size)) {
                retObj = bfsTool_private.find(props[count], wantType.createType(genericTypes))
                count++
            }
            
            val reconstructed = reconstruct_private(retObj!!, thisObj)
            
            return if(reconstructed is T) reconstructed else null
        }
        catch(e: Exception) {
            return null
        }
    }
    
    fun reconstruct_private(keys : List<KProperty1<Any, Any>>, thisObj: Any) : Any {
        var curObj = thisObj
        
        for(i in 0 until keys.size) {
            curObj = keys[i].get(curObj)
        }
        
        return curObj
    }
    
    class PropertyBFS : BFS<KProperty1<Any, Any>, KType>() {
        override fun getKeysFromKey(key: KProperty1<Any, Any>): Iterable<KProperty1<Any, Any>> {
            if(key.returnType.jvmErasure.qualifiedName?.contains("fivemin") != true && key.returnType.jvmErasure.qualifiedName?.contains("arrow") != true) {
                return listOf()
            }
            
            val props = key.returnType.jvmErasure.memberProperties.map {
                it as KProperty1<Any, Any>
            }
            
            return props
        }
        
        // equalKeys(a, b) => 두 타입이 서로 관련없을 경우 False
        // a is b 일 경우 => a가 b를 상속받음 => False
        // b is a 일 경우 => b가 a를 상속받음 => True (현재 추상타입밖에 없으므로 구체타입을 추가적으로 탐색해야 함)
        // 서로가 서로를 상속하지만 같지 않은 경우? 없음
        override fun equalsTarget(whatIHave: KType, whatIWant: KType) : Boolean{
            val nullableWhatIHave = whatIHave.withNullability(true)
            val nullableWhatIWant = whatIWant.withNullability(true)
            
            if(nullableWhatIHave == nullableWhatIWant) {
                return true
            }
            
            if(nullableWhatIHave.isSubtypeOf(nullableWhatIWant)) { //변경
                return true
            }
            
            if(nullableWhatIHave.classifier is KTypeParameter) { // In case that I want a generic parameter which has upper bound
                return (nullableWhatIHave.classifier as KTypeParameter).upperBounds.any {
                    equalsTarget(it, nullableWhatIWant)
                }
            }
            
            return false
        }
        
        override fun convertToTarget(item: KProperty1<Any, Any>): KType {
            return item.returnType
        }
    }
}



abstract class BFS<Key, Target> {
    
    protected abstract fun getKeysFromKey(key: Key): Iterable<Key>
    
    protected abstract fun convertToTarget(item: Key): Target
    
    protected abstract fun equalsTarget(whatIHave: Target, whatIWant: Target): Boolean
    
    
    val find = ::findInternal.memoize() // memoize can be applied.
    
    private fun findInternal(src: Key, dest: Target) : List<Key>? {
        
        val route = bfs(src, dest)
        
        if(route != null) {
            return backtrace(route, src, dest, mutableSetOf())?.reversed()
        }
        
        return null
    }

    private fun bfs(src : Key, dest: Target) : Map<Key, Key>? {
        val route = mutableMapOf<Key, Key>() //src, dest
        val que = LinkedList<Key>()
        que.add(src)
        
        while(que.size != 0) {
            val item = que.removeFirst()
            
            if(equalsTarget(convertToTarget(item), dest)) { // find the object or the route to the object.
                return route
            }
    
            if(hasItemAlready(route, item, que)) {
                continue
            }
            
            val keys = getKeysFromKey(item)
            
            keys.forEach {
                route[item] = it
                que.addLast(it)
            }
        }
        
        return null
    }
    
    private fun backtrace(route: Map<Key, Key>, nextKey: Key, finalKey: Target, visited : MutableSet<Key>) : LinkedList<Key>? {
        if(equalsTarget(convertToTarget(nextKey), finalKey)) { // A가 더 구체적인 타입일 경우 False, B가 더 구체적인 타입일 경우 True //변경
            val ret = LinkedList<Key>()
            ret.addLast(nextKey)
            
            return ret
        }
        
        if(visited.contains(nextKey)) {
            return null
        } else {
            visited.add(nextKey)
        }
        
        val keys = route.filter {
            equalsTarget(convertToTarget(nextKey), convertToTarget(it.key))
        }.toList()
        
        var rr : LinkedList<Key>? = null
        var count = 0
        
        while((rr == null) && (count < keys.size)) {
            rr = backtrace(route, keys[count].second, finalKey, visited)
            rr?.addLast(nextKey)
            count++
        }
        
        return rr
    }
    
    private fun hasItemAlready(
        route: Map<Key, Key>,
        item: Key,
        que: LinkedList<Key>
    ) : Boolean {
        val r = route.any {
            equalsTarget(convertToTarget(item), convertToTarget(it.key)) // what I have, what I want to have
        }
    
        val q = que.any {
            equalsTarget(convertToTarget(it), convertToTarget(item))
        }
        
        return r or q
    }
}