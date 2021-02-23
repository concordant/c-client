## Private releases

Development versions of this library
are delivered as both Maven packages and NPM packages
in a private [Gitlab Packages registry](
https://gitlab.inria.fr/concordant/software/c-client/-/packages).

To use it, you will need to authenticate to Gitlab, using either:
- a Gitlab [deploy token](
  https://docs.gitlab.com/ee/user/project/deploy_tokens/)
  with at least the `read_package_registry` scope, or
- a Gitlab [personal access token](
  https://docs.gitlab.com/ee/user/profile/personal_access_tokens.html)
  with at least the `read_api` scope.


### Kotlin and Gradle

To setup authentication, add the token to your gradle properties file
`~/.gradle/gradle.properties`:
``` shell
gitlabToken=<deployOrPersonalToken>
```

Then, in your project configuration build file `build.gradle.kts`:
- Ensure `mavenCentral()`
  is listed in the `repositories{}` for usual packages.
- Add Gitlab to repositories:
``` kotlin
repositories {
    maven {
        url = uri("https://gitlab.inria.fr/api/v4/projects/18591/packages/maven")
        credentials(HttpHeaderCredentials::class) {
            // set to "Deploy-Token" here if appropriate
            name = "Private-Token"
            val gitlabToken: String by project
            value = gitlabToken
        }
        authentication {
            create<HttpHeaderAuthentication>("header")
        }
    }
}

```
- Add the c-crdtlib Maven package as a dependency:
``` kotlin
dependencies {
    implementation("concordant:c-crdtlib:x.y.z")
}
```

### JavaScript/TypeScript and NPM

Associate `@concordant` scope with the private registry
and setup authentication:
``` shell
$ npm config set @concordant:registry "https://gitlab.inria.fr/api/v4/packages/npm/"
$ npm config set '//gitlab.inria.fr/api/v4/packages/npm/:_authToken' "<deployOrPersonalToken>"
```

Then install the package:
``` shell
$ npm i @concordant/c-client
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
