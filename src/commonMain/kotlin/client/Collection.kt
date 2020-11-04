/*
* Copyright © 2020, Concordant and contributors.
*
* Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
* associated documentation files (the "Software"), to deal in the Software without restriction,
* including without limitation the rights to use, copy, modify, merge, publish, distribute,
* sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
* furnished to do so, subject to the following conditions:
*
* The above copyright notice and this permission notice shall be included in all copies or
* substantial portions of the Software.
*
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
* NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
* NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
* DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
* OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/

package client

import client.utils.CObjectUId
import client.utils.CollectionUId
import client.utils.NotificationHandler

/**
* This class represents a collection of objects.
* @property id the collection unique identifier.
* @property readOnly is the collection open in read-only mode.
*/
class Collection(private val id: CollectionUId, private val readOnly: Boolean) {

    /**
     * Opens an object of the collection.
     * @param objectId the name of the object.
     * @param readOnly is the object open in read-only mode.
     * @param handler currently not used.
     */
    fun <T> open(objectId: String, readOnly: Boolean, handler: NotificationHandler<T>): T {
        if (this.readOnly && !readOnly) throw RuntimeException("Collection has been opened in read-only mode.")

        val objectUId = CObjectUId<T>(this.id, objectId)
        return CObject<T>(objectUId, readOnly) as T
    }

    /**
     * Closes this collection.
     */
    fun close() { }
}
