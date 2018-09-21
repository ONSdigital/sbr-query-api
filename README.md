# sbr-query-api
An API that creates frames against SBR data

[![license](https://img.shields.io/github/license/mashape/apistatus.svg)]() [![Dependency Status](https://www.versioneye.com/user/projects/596f195e6725bd0027f25e93/badge.svg?style=flat-square)](https://www.versioneye.com/user/projects/596f195e6725bd0027f25e93)

## API Endpoints

| method | endpoint                                                  | example                                                    |
|--------|-----------------------------------------------------------|------------------------------------------------------------|
| GET    | /v1/enterprise/period/:period/query/:query/filter/:filter | /v1/enterprise/period/201706/query/sic=50014/filter/entref |
| GET    | /v1/download/period/:period/query/:query/filter/:filter   | /v1/download/period/201706/query/sic=50014/filter/entref   |

## Environment Setup

* Java 8 or higher (https://docs.oracle.com/javase/8/docs/technotes/guides/install/mac_jdk.html)
* SBT (http://www.scala-sbt.org/)

```shell
brew install sbt
```

## Running

With the minimal environment setup described above (just Java 8 and SBT)).

To run the `sbr-query-api`, run the following:

``` shell
sbt "api/run -Dsource=h2Local"
```

## Assembly

To assemble the code + all dependencies into a fat .jar, run the following:

```shell
sbt assembly
```

## Deployment

After running the following command:
 
```shell
sbt clean compile "project api" universal:packageBin
```

A .zip file is created here, `/target/universal/sbr-query-api.zip`, which is pushed to CloudFoundry.

## Test

To test all test suites we can use:

```shell
sbt test
```

Testing an individual test suite can be specified by using the `testOnly`.

SBR Api uses its own test configuration settings for integration tests, the details of which can be found on the[ONS Confluence](https://collaborate2.ons.gov.uk/confluence/display/SBR/Scala+Testing​).

To run integration test run:
```shell
sbt it:test
```
See [CONTRIBUTING](CONTRIBUTING.md) for more details on creating tests. 

## Contributing

See [CONTRIBUTING](CONTRIBUTING.md) for details.

## License

Copyright ©‎ 2017, Office for National Statistics (https://www.ons.gov.uk)

Released under MIT license, see [LICENSE](LICENSE) for details.
