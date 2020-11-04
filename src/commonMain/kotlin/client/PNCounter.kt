package client

import client.utils.CObjectUId
import crdtlib.crdt.PNCounter

class PNCounter(
    val attachedCollection: Collection,
    val objectUId: CObjectUId<client.PNCounter>,
    val readOnly: Boolean) :
    CObject<client.PNCounter>(attachedCollection, objectUId, readOnly) {

    // Should be changed in the future
    private val counterCrdt: PNCounter = PNCounter()
        // get() = this.crdt as PNCounter

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
