# Status of the Concordant platform, and development directions

16 March 2021

## The Concordant vision

Concordant is a platform to enable applications to share mutable data at the edge or in the cloud.  

It has a *local-first* design, meaning that data is as close as possible to the client application. The application has fast access to data, because it does not have to cross network layers. Data is always available, even if the local data store is disconnected from all other devices, from the cloud, or from the internet.

It is based on CRDTs, ensuring that concurrent updates are merged cleanly and with an intuitive semantics. In particular, updates that are made while disconnected will be merged when connection is restored, no matter how long the disconnection lasted. Concordant provides *delta CRDTs* in order to minimise network traffic, in particular after a long disconnection.

Concordant provides the strongest possible consistency guarantees that are compatible with availability. Its consistency model is called Transactional Causal Consistency Plus (TCC+), i.e.,

- An application can invoke any number of operations, on any number of objects, in a transaction.  If the transaction commits, all its updates take effect (become visible) together.  If it aborts, none of the updates takes effect.   This is called *atomicity*.
- Committed transactions become visible in causal order.  If T1 happens before T2, then everyone will observe T1 before T2.  If T3 and T4 are concurrent, then you may observe either T3 before T4, or T4 before T3; however:
- CRDT updates are convergent: the end result is the same, whether you see T3 before T4 or T4 before T3.
- Any two devices that are connected to each other eventually receive (and merge) each other’s updates, and thus reach the same state.

Eventually, we expect to add the option of stronger guarantees when required by the application.

## The Concordant design and API

We have a [tutorial](https://concordant.io/getting-started) that illustrates how to develop an application using the c-client API.  The design and API is documented in the [c-client README](https://gitlab.inria.fr/concordant/software/c-client/-/blob/master/README.md). Let‘s summarise briefly.

A Concordant database consists of a set of *collections*, each containing any number of CRDT *objects*. The supported object types are those in our [c-CRDTlib](https://gitlab.inria.fr/concordant/software/c-crdtlib).  (You are free to use other kinds of objects in your code, but they cannot be stored in a collection.).

An application starts by connecting through a *session*.  To access an object, open its collection, then open the object in the collection.  (Remember to close them when done.)

The “open” call has a number of options, e.g., read-only vs. read-write, and arguments such as a security credentials. A handler argument enables be notified when remote updates to the object are available; later the application can pull these updates, merging them into the local state.

Opening an object returns a standard language-level object reference.  The application can invoke the standard object methods, *but only within a transaction*. Invoking an object outside of a transaction is an error; similarly, invoking it after closing it is an error.

Transactions also have options, in particular the level of consistency guarantees.

Note an important caveat with respect to the vision. The Concordant consistency guarantees apply only within a collection. We do plan to support consistency across collections in the future, but this is further along.

## Current implementation and limitations

We focused on making the platform and API available as soon as possible, so that *you* can experiment and start developing proof-of-concept applications quickly.

The good news is that the C-Client API is to remain stable. As we improve the platform, we will provide more and better features, but your existing code will not have to change.

The current Concordant platform is an alpha version. We made a number of implementation shortcuts and restrictions. We strove to develop clean, modular, extensible and correct code; however, performance and features have been a secondary consideration, in order to deliver the platform quickly.

The main limitations are as follows:

- Transactions can run only under `ConsistencyLevel.None`, which provides no firm guarantees. Such a transaction cannot abort, its updates are *not* guaranteed to be atomic, and transactions are *not* guaranteed to be visible in causal order.
- Data is stored on a server, running on a designated device. If this device is not reachable, then the application will be non-responsive. Disconnected operation is *not* supported.
- Notifications are not implemented. The application must poll to receive remote updates.
- The object interface is ad-hoc and does not conform to developers’ expectations.
- Performance is poor, in particular because we do not leverage the delta capability and because of polling. This is particularly apparent in the C-Markdown-Editor application.
- There is no security layer.

## Short-term evolution

We plan a new releases, fixing the most egregious issues, as follows:

- Provide an on-device object cache, backed up by a Service Worker with PouchDB.  This will enable atomic transactions and disconnected operation.
- Provide basic user authentication and security.
- Fix the interface of CRDT objects, to conform to the corresponding Kotlin/TypeScript standard types.
- Provide notifications.

## Medium-term evolution

The medium-term objectives are improved performance and availability. We will provide more advanced CRDTs (e.g., Reference, Tree or Split-RGA) and upgrade the applications, notably C-Markdown-Editor, based on [crdt-md-editor](https://github.com/ilyasToumlilt/crdt-md-editor).

The next major release will implement a true peer-to-peer edge system, ensuring the TCC+ guarantees (per collection). It will be based on shared append-only journals of deltas. This will improve consistency, scalability, and performance.

This release will add the concept of a *lateral group*, a set of co-located devices that collaborate directly. A group can ensure stronger consistency guarantees among its members, and can operate disconnected from other groups.

We expect that the C-Client APIs will remain backwards-compatible during this evolution. Applications will be able to take advantage of the improvements, but those that do not care will not have to refactor.

## Longer term evolution

The longer-term plans are (of course) to evolve the platform to fully implement the “vision” model.

The group concept will enable a Concordant platform to span multiple geographical locations, to persist data in the cloud, and to share over long-distance internet connections.
