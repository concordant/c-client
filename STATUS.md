# Status of the Concordant platform, and development directions

4 March 2021

## The Concordant vision

Concordant is a platform to enable applications to share mutable data at the edge or in the cloud.  

It has a *local-first* design, meaning that data is as close as possible to the client application.  The application has fast access to data, because it does not have to cross network layers. Data is always available, even if the local data store is disconnected from all other devices, from the cloud, or from the internet.  

It is based on CRDTs, ensuring that concurrent updates are merged cleanly and with an intuitive semantics.  In particular, updates that are made while disconnected will be merged when connection is restored, no matter how long the disconnection lasted.

Concordant provides the strongest possible consistency guarantees that are compatible with availability.   Its consistency model is called Transactional Causal Consistency Plus (TCC+), i.e.,

-   An application can make any number of updates in a transaction.  If the transaction commits, all its updates take effect (become visible) together.  If it aborts, none of the updates takes effect.
-   Committed transactions become visible in causal order.  If T1 happens before T2, then everyone will observe T1 before T2.  If T3 and T4 are concurrent, then you may observe either T3 before T4, or T4 before T3; however:
-   CRDT updates are convergent: the end result is the same, whether you see T3 before T4 or T4 before T3.
-   Any two devices that are connected to each other eventually receive (and merge) each other’s updates, and thus reach the same state.

## The Concordant API and design

The API and design are documented in 