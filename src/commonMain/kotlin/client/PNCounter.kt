package client

import client.utils.CObjectUId
import crdtlib.crdt.PNCounter

class PNCounter(val oid: CObjectUId<PNCounter>, val readOnly: Boolean) :
    CObject<PNCounter>(oid, readOnly) {

    private val counterCrdt: PNCounter
        get() = this.crdt as PNCounter

    fun get(): Int {
        this.beforeGetter()
        val value = this.counterCrdt.get()
        this.afterGetter()
        return value
    }

    fun increment(amount: Int) {
        val ts = this.beforeUpdate()
        this.counterCrdt.increment(amount, ts)
        this.afterUpdate()
    }

    fun decrement(amount: Int) {
        val ts = this.beforeUpdate()
        this.counterCrdt.decrement(amount, ts)
        this.afterUpdate()
    }
}
