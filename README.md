# Concordant Client Library

[![License](https://img.shields.io/badge/license-MIT-green)](https://opensource.org/licenses/MIT)

Concordant client library in Kotlin for the Concordant platform API.

This library is delivered as an NPM package and a Maven in [Gitlab Packages](
https://gitlab.inria.fr/concordant/software/c-client/-/packages)
(as a private registry).

## Requirements

- Download and install Gradle from [Gradle website](https://gradle.org/install/);

## Install

This library is delivered as an
[NPM package](https://www.npmjs.com/package/@concordant/c-client).

### JavaScript/TypeScript and NPM

Install the package:
``` shell
$ npm i @concordant/c-client
```

Import it in your code:
``` typescript
import * from @concordant/c-client;
```

## Concondant API

An application stores information in *objects*. In Concordant, an object is an
instance of a CRDT, and is managed by C-CRDTlib (see its documentation for more
about objects). A *collection* is a set of related objects. Concordant provides
consistency guarantees between the objects of a same collection, and weaker
guarantees between collections.

A *transaction* is a sequence of accesses (reads or updates) to any number of
objects in the same collection. A transaction can terminate by *committing* or
*aborting,* and has the *all-or-nothing* property: before it terminates, none
of its updates are visible outside of it, and similarly after it aborts; after
it commits, all of its updates are visible. (Note: currently, Concordant
supports only the `None` consistency level, which does *not* enforce
all-or-nothing).

An application interacts with Concordant within a *session*. Thus the standard
application execution lifecycle is as follows:

- Begin a session.
- Open a collection; open objects within this collection.
- Start a transaction; read/write any number of open objects, zero or more
  times; commit the transaction. This pushes the transaction’s updates to the
  distributed Concordant database.
- Start another transaction; this pulls and merges remote updates. Read, write;
  commit or abort.
- and so on.
- Finally, close the objects, the collection, and the session; terminate the
  application.

In future versions, Concordant will support opening multiple collections, and
will provide finer control over the pulling, pushing and merging of updates.

We now explain the API in more detail.

### Session

An instance of a client application (or just *client* in the following)
interacts with Concordant in the context of a session. A given client can have
only a single session open at a time. A session provides the context for client
interaction with the Concordant database engine. It manages, among other
things, the interest set, i.e., the set of objects currently open by the
client.

A client also contains non-Concordant-related code, such as access to local
variables, in-memory objects or conditionals. The semantics of non-concordant
code is not affected by what follows.

The `Session` API is the following:

``` typescript
let session = Session.Companion.connect(database, addressToCService, credentials)
...
session.close()
```

`Session.Companion.connect` requires a valid database, Concordant Service
(C-Service) address and credentials, and is not valid if a session is already
open.

We call *remote* any session or update other than the current session. Local
updates are automatically *pushed*, making them available to remote sessions; a
client can *pull* remote updates into the current session.

### Collection

A *collection* is a set of related Concordant database objects. Currently, only
one collection may be open at a time.

The *interest set* is the set of objects that are accessed by transactions
in the current session. We distinguish read-only (immutable) from read-write
(mutable) access.

The client adds (resp. removes) an object from the interest set by `open`ing
(resp. `close`ing) it. We distinguish read-only vs. mutable `open`. To open an
object, its type must be specified.  

The `Collection` API is the following:

``` typescript
let collection = session.openCollection(collectionUId, readOnly)
...
// readOnly can be true only if the collection is mutable
let object = collection.open(objectId, type, readOnly, handler)
...
collection.close()
```

Above, `type` is the type of the object (e.g., `MVMap`, `PNCounter`), and
`handler` is an app-provided notification handler function (currently not
used). If a collection is opened with the boolean `readOnly` true, the objects
it contains are immutable (and `collection.open` in writable mode is invalid).
Note that “mutable” means that this client is not able to invoke updating
operations; however the objects' state might still change, by pulling remote
updates.

`session.openCollection` and `collection.open` require a session, and are
not valid inside a transaction. Either can fail if access would violate
the security policy.

A Concordant object’s “semantic" API can only be called within a transaction
block. For instance, for a counter, its methods `increment` and `decrement`
cannot be called outside of a transaction block; similarly, for an `RGA` and
its methods `insertAt`, `removeAt` and `get`.

Notifications are not yet implemented; therefore, the notification handler
should look like this:

``` typescript
// Declaring an handler function
function handler () {
    return;
}

// Or directly using an anonymous function
function () { return }
```

### Transaction

A *transaction* is a block of (read, write, or read-write) accesses to a set of
objects. A client executes a sequence of transactions within a session (there
is no concurrency allowed within a session). A transaction block that
terminates normally commits.

A transaction block is a sequence of zero or more concordant-object operations
wrapped in a function and passed as an argument to `session.transaction`.

Currently, Concordant supports only a single transaction type with the
following guarantees:

-`None`: once the transaction performs an update, later reads in the
  transaction observe that update. The all-or-nothing property is not enforced.
  Push and pull are best-effort.

The `Transaction` API for type `None` is the following:

``` typescript
... // open the resources that will be required in the transaction...

session.transaction (ConsistencyLevel.None, () => {
  ... // transaction block: client code of the transaction
  ... // access (read, write or read-write) Concordant objects
});
```

“Semantic” operations on Concordant objects are allowed inside of a
transaction, and not allowed outside.

Currently, Concordant supports only non-interactive transactions; i.e., the
session and interest set must be opened before the beginning of the
transaction, and may not be changed within the transaction block.

Concordant currently supports only the `None` transaction type. More types will
be added in future versions.

### Object

Concordant manages (CRDT) objects, i.e., it supports and merges updates between
concurrent remote sessions. These objects are provided by the Concordant CRDT
library.

An object is of a specific (CRDT) type, with the interface expected by its
semantics (called its *semantic API*). For instance, the semantic API of a
Counter supports the `increment(nb)`, `decrement(nb)` and `get()` methods; an
RGA (a list) supports methods `insertAt(idx, elem)`, `removeAt(idx)` and
`get()`. A complete list of available object types, and their specific semantic
API is provided in the documentation of the C-CRDTlib.

The client can access an open object, through its semantic API, within a
transaction. Note that:

* Any update function fails if the object is open in read-only mode.
* A semantic function fails outside of a transaction, or if the object is not open.
* A semantic function may fail if it would violate the security policy.  

All object types support the following semantic methods:

* `toJson` serialises the object into a JSON string representing its current
  state.
* `DeltaCRDT.fromJson` takes a JSON string and converts it to to an object
  state of its corresponding type (and fails if the JSON does not parse
  properly).

An object is stored in a collection, which itself is stored in a database. In
the database, an object is identified by its key, which is the triple
$(collectionUId, objectID, type)$. Opening an object makes it available to the
client and adds it to its interest set. 

Every key always “exists,” in the sense that there are no invalid keys. Every
key starts in the  uninitialised state, until the app modifies it otherwise. 
