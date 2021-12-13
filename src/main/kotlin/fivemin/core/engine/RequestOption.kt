package fivemin.core.engine

import arrow.core.Option
import arrow.core.Some

class RequestOption(val preference: RequesterPreference) {

}

class RequesterPreference(val engine : RequesterEngineInfo, val slot : Option<RequesterSlotInfo>){
    fun withSlot(nslot: RequesterSlotInfo) : RequesterPreference{
        return RequesterPreference(engine, Some(nslot))
    }
}

class RequesterEngineInfo(val name: String){
    override fun equals(other: Any?): Boolean {
        if(other != null && other is RequesterEngineInfo){
            return (other as RequesterEngineInfo).name == name
        }

        return false
    }

    override fun toString(): String {
        return "Engine: $name"
    }

    override fun hashCode(): Int {
        return name.hashCode()
    }
}

class RequesterSlotInfo(val index: Int){
    override fun equals(other: Any?): Boolean {
        if(other != null && other is RequesterSlotInfo){
            return (other as RequesterSlotInfo).index == index
        }

        return false
    }

    override fun toString(): String {
        return "Index: $index"
    }

    override fun hashCode(): Int {
        return index
    }
}