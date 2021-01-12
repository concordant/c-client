# Concordant client library

[![License](https://img.shields.io/badge/license-MIT-green)](https://opensource.org/licenses/MIT)

Concordant client library in Kotlin for the Concordant platform API.

## Requirements

- Download and install Gradle from [Gradle website](https://gradle.org/install/);

## Project overview

The code of the version v0 is in the directory *src/*.

The pseudo code of the version v1 is in the directory *pseudo-code-v1/*.

## Usage

This library is delivered as an NPM package and a Maven in [Gitlab Packages](
https://gitlab.inria.fr/concordant/software/c-client/-/packages)
(as a private registry).

### NPM install

Get a deploy token or personal access token from Gitlab,
with at least the read_package_registry scope.  
Then setup authentication:
``` shell
$ npm config set @concordant:registry "https://gitlab.inria.fr/api/v4/packages/npm/"
$ npm config set '//gitlab.inria.fr/api/v4/packages/npm/:_authToken' "<deployOrPersonalToken>"
```

Install the package:
``` shell
$ npm i @concordant/c-client
```

And use it:
``` typescript
import * from @concordant/c-client;
```

## Build project

The build is managed by Gradle.
Kotlin sources (code and tests) are compiled to JVM Bytecode
and to Javascript as a Node.js package.

`gradle assemble`:
- compiles code and tests to JVM Bytecode;
- compiles code and tests to Javascript (Node.js package);
- creates a TypeScript interface
- assembles a NPM package

`gradle allTests`:
- runs JVM test suite;
- runs Node.js test suite in a server like manner;
- a report containing all tests results can be found in the file
  `build/reports/tests/allTests/index.html`.

`gradle build`:
- compiles code and test.

`gradle pack`:
- pack the NPM package

`gradle publish`:
- publish the NPM package to the Gitlab Packages registry
  (requires authentication ; better use it via CI pipelines).

`gradle dokkaHtml`:
- creates the documentation from code comments;
- documentation is accessible at `build/dokka/html/crdtlib/index.html`.
See `gradle tasks` for variants other than HTML.

`gradle clean`:
- cleans the project.

`gradle tasks`:
- show all available tasks with descriptions.
