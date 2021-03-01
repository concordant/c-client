# Concordant Client Library

[![License](https://img.shields.io/badge/license-MIT-green)](https://opensource.org/licenses/MIT)

Concordant client library in Kotlin for the Concordant platform API.

The documentation is available on [GitLab pages](
https://concordant.gitlabpages.inria.fr/software/c-client/c-client/).  
See also the [C-CRDTlib](
https://www.npmjs.com/package/@concordant/c-crdtlib)
and [its documentation](
https://concordant.gitlabpages.inria.fr/software/c-crdtlib/c-crdtlib/)
for CRDTs type-specific usage.

## Getting started

This library is delivered as an [NPM package](
https://www.npmjs.com/package/@concordant/c-client).
A Maven package will be available soon.

Install the package:
``` shell
$ npm i @concordant/c-client
```

Follow [the instructions to start a C-service](
https://www.npmjs.com/package/@concordant/c-client).

### Usage

Here is how a typical session using the Concordant platform looks like:
``` typescript
import * from @concordant/c-client;

// Open a session
let session = Session.Companion.connect(
    "mydatabase",
    "http://url-to-c-service",
    "credentials");

// The Concordant platform provides consistency guarantees
// between objects of a same collection.
// Open a collection in the database, with read/write access.
let collection = session.openCollection("mycollection", false);

// An object must be opened before using it in a transaction.
// Open the PNCounter with objectId "mycounter", with read/write access.
// If it does not exist, it is created in a type-specific default state.
// Note that objects of different types may coexist under a same objectId.
let cntr = collection.open("mycounter", "PNCounter", false);

// Objects values can be accessed/updated in a transaction only.
session.transaction(ConsistencyLevel.None, () => {

    // Use opened objects as any other object
    cntr.increment(10);
    let val = cntr.get();
    console.log(val);

    if (val > 1000){
        // Any unhandled exception aborts the transaction
        throw new Error('Counter overflow');
    }

    // Transaction implicitly commits when reaching the end of its block
});

// Open other objects, run transactions…

// Finally close the collection and session
collection.close();
session.close();
```

## Technical description

**Note:** Some features presented here are not implemented yet.
  This is specified by subsequent **Note**s in the text.

An application stores information in *objects*. In Concordant, an object is an
instance of a CRDT from the C-CRDTlib.
See [its documentation](
https://concordant.gitlabpages.inria.fr/software/c-crdtlib/c-crdtlib/)
for more about objects.

A *collection* is a set of related objects. Concordant provides
consistency guarantees between objects of a same collection.

A *transaction* is a sequence of accesses (reads or updates) to any number of
objects in the same collection.

An application interacts with Concordant within a *session*,
which represents a connection to a C-service & database.

In future versions, Concordant will support opening multiple collections, and
will provide finer control over the pushing, fetching and merging of updates.

We now explain the API in more detail. See also the [code documentation](
https://concordant.gitlabpages.inria.fr/software/c-client/c-client/).

### Session

A session provides the context for client interaction
with the Concordant database engine.
It manages, among other things:
- The connection to the database (URL, database name and credentialss).
- The interest set, i.e., the set of objects currently open by the client.

A client also executes regular, non-Concordant-related code.
The semantics of non-concordant code is not affected by what follows.
In particular, aborting a transaction will *not* revert modifications
to non-concordant objects.

`Session.Companion.connect` requires a valid database, Concordant Service
(C-Service) address and credentials, and is not valid if a session is already
open.

We call *remote* any session or update other than the current session. Local
updates are automatically *pushed*, making them available to remote sessions; a
client can *pull* remote updates into the current session.

### Collection

A *collection* is a set of related Concordant objects, stored in a database.

In the database, an object is identified by its triplet key
`(collectionUId, objectId, type)`.
Note that objects of different types may coexist in a collection
under a same objectId.

**Note:** Currently, only one collection may be open at a time.

Every key always “exists,” in the sense that there are no invalid keys:
when trying to open an object that does not exist or is unavailable,
it is created in a type-specific default state.
Thus, different applications may concurrently create a same object,
whose states will eventually converge (be merged).

Opening an object makes it available to the client
and enables its synchronization with the database.

Collections and objects may be open in read-only or read-write mode.
Attempting to open an object for read-write in a read-only collection
is an error.
Note that “read-only” means that this client is not able to invoke updating
operations; however the objects' state might still change, by pulling remote
updates.

### Object

Concordant manages (CRDT) *objects*,
i.e., it supports and merges updates between concurrent remote sessions.
These objects are provided by the Concordant CRDT library.

An object is of a specific (CRDT) type, with the interface expected by its
semantics (called its *semantic API*). For instance, a
Counter supports the `increment(nb)`, `decrement(nb)` and `get()` methods; an
RGA (a list) supports methods `insertAt(idx, elem)`, `removeAt(idx)` and
`get()`. A complete list of available object types, and their specific semantic
API is provided in the documentation of the C-CRDTlib.

An optional `handler` may be provided to `Collection.open()`,
that will be called when an update is available for this object.

**Note:** handlers are not supported yet.

The client can access an open object, through its semantic API, within a
transaction. Note that:

- Any update function fails if the object is open in read-only mode.
- A semantic function fails outside of a transaction,
  or if the object is not open.
- A semantic function may fail if it would violate the security policy.

### Transaction

A *transaction* is a block of code,
including (read, write, or read-write) accesses to a set of objects.
A transaction can terminate by *committing* or *aborting,*
and has the *all-or-nothing* property: before it terminates, none
of its updates are visible outside of it, and similarly after it aborts; after
it commits, all of its updates are visible.

**Note**: Concordant currently only supports the `None` consistency level:
- Opening an object creates it in its default state
  and asynchronously requests its remote state from the service.
- Remote updates (including initial state) are received asynchronously
  and can be merged in the background at any time.
- Once an update is performed, later reads in the transaction
  observe that update (read-your-writes).
- All-or-nothing is *not* enforced: modifications are immediatly pushed,
  and aborting the enclosing transaction does not revert them.
More types will be added in future versions.

A transaction block is passed as a function to `Session.transaction()`.
It implicitly commits when reaching the end of the block,
or aborts if an unhandled exception is thrown.

Semantic operations on Concordant objects are allowed
inside of a transaction only.
Also Concordant currently only supports non-interactive transactions;
i.e., collection and objects must be opened
before the beginning of a transaction that accesses them.

No concurrency is allowed within a session:
concurrent transactions are not allowed
and concurrent modifications of opened objects have undefined behaviour.
