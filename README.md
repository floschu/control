![logo](.media/control.png)

[![version](https://img.shields.io/github/v/tag/floschu/control?color=blue&label=version)](https://bintray.com/flosch/control) [![build](https://github.com/floschu/control/workflows/build/badge.svg)](https://github.com/floschu/control/actions) [![issues](https://img.shields.io/github/issues-raw/floschu/control)](https://github.com/floschu/control/issues) [![license](https://img.shields.io/badge/license-Apache%202.0-blue.svg)](LICENSE)


kotlin flow based unidirectional-data-flow architecture

## installation

```groovy
allprojects {
    repositories {
        jcenter()
    }
}

dependencies {
    /**
    * core: kotlin only dependency
    */
    implementation "at.florianschuster.control:control-core:$version"
    
    /**
     * android: AAC viewmodel controller
     */
    implementation "at.florianschuster.control:control-android:$version"
}
```

## what should I know before I try this?

* kotlin
* kotlin coroutines 
* kotlin coroutines flow

## concept

todo

## examples

* [counter](example-counter): most basic example. uses `Controller`.
* [github search](example-github): github repository search. uses `ControllerViewModel`.
* [Playables](https://github.com/floschu/Playables): app with a checklist of games you want to play.


## author

visit my [website](https://florianschuster.at/).
