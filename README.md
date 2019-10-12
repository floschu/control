![logo](.media/control.png)

[![version](https://img.shields.io/github/v/tag/floschu/control?color=blue&label=version)](https://bintray.com/flosch/control) [![build](https://github.com/floschu/control/workflows/build/badge.svg)](https://github.com/floschu/control/actions) [![codecov](https://codecov.io/gh/floschu/control/branch/develop/graph/badge.svg)](https://codecov.io/gh/floschu/control) [![issues](https://img.shields.io/github/issues-raw/floschu/control)](https://github.com/floschu/control/issues) [![license](https://img.shields.io/badge/license-Apache%202.0-blue.svg)](LICENSE)


kotlin flow based unidirectional-data-flow architecture

## installation

``` groovy
allprojects {
    repositories {
        jcenter()
    }
}

dependencies {
    // kotlin module
    implementation("at.florianschuster.control:control-core:$version")
    
    // android module > also provides control-core
    implementation("at.florianschuster.control:control-android:$version")
    
    // kotlin module
    implementation("at.florianschuster.control:control-data:$version")
}
```

## concept

todo

## examples

* [counter](example-counter): most basic example. uses `Controller`.
* [github search](example-github): github repository search. uses `ControllerViewModel`.
* [Playables](https://github.com/floschu/Playables): app with a checklist of games you want to play.

## author

visit my [website](https://florianschuster.at/).

## reaktor

control is the kotlin coroutines flow alternative of [Reaktor](https://github.com/floschu/Reaktor/) which uses [RxJava2](https://github.com/ReactiveX/RxJava) to create its unidirectional-data-flow architecture.