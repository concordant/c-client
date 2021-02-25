# Concordant client library

[![License](https://img.shields.io/badge/license-MIT-green)](https://opensource.org/licenses/MIT)

Concordant client library in Kotlin for the Concordant platform API.

This library is delivered as an NPM package and a Maven in [Gitlab Packages](
https://gitlab.inria.fr/concordant/software/c-client/-/packages)
(as a private registry).

## Requirements

- Download and install Gradle from [Gradle website](https://gradle.org/install/);

## JavaScript/TypeScript and NPM

This library is delivered as
an [NPM package](https://www.npmjs.com/package/@concordant/c-client).

### Install

Install the package:
``` shell
$ npm i @concordant/c-client
```

### Usage

```typescript
import * from @concordant/c-client;

// Open a session
let session = client.Session.Companion.connect("mydatabase", "http://url-to-c-service", "credentials");

// Open a collection of objects
let collection = session.openCollection("mycollection", false);

// Open an object within the collection
let cntr = collection.open("mycounter", "PNCounter", false, function () {return});

// Compute with one or more objects within atomic transactions
this.props.session.transaction(client.utils.ConsistencyLevel.None, () => {
    // Access objects here
    cntr.increment(10);
    let val = cntr.get();
});
```
