# gradle-spoon-plugin [![Build Status](https://travis-ci.org/x2on/gradle-spoon-plugin.png)](https://travis-ci.org/x2on/gradle-spoon-plugin) [![Maven Central](https://maven-badges.herokuapp.com/maven-central/de.felixschulze.gradle/gradle-spoon-plugin/badge.svg)](http://search.maven.org/#search%7Cgav%7C1%7Cg%3A%22de.felixschulze.gradle%22%20AND%20a%3A%22gradle-spoon-plugin%22)
A Gradle plugin for running Android instrumentation tests with [Spoon](http://square.github.io/spoon/).

## Basic usage

Add to your build.gradle

```gradle
buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath 'de.felixschulze.gradle:gradle-spoon-plugin:2.0'
    }
}

apply plugin: 'de.felixschulze.gradle.spoon'
```

## Advanced usage

Add to your build.gradle

```gradle
spoon {
    teamCityLog = true
    debug = true
    failOnFailure = false
    testSizes = ['small', 'medium']
    adbTimeout = 10*60
    failIfNoDeviceConnected = false
}
```

* `teamCityLog`: Add features for [TeamCity](http://www.jetbrains.com/teamcity/)
* `debug`: Activate debug output for spoon
* `failOnFailure`: Deactivate exit code on failure
* `testSizes`: Only run test methods annotated by testSize (small, medium, large)
* `adbTimeout`: ADB timeout in seconds
* `failIfNoDeviceConnected`: Fail if no device is connected

## Running specific test classes or test methods

````
-PspoonTestClass=fully_qualified_test_class_package_name
-PspoonTestMethod=testMethodName
````

## Changelog

[Releases](https://github.com/x2on/gradle-spoon-plugin/releases)

## License

gradle-spoon-plugin is available under the MIT license. See the LICENSE file for more info.
