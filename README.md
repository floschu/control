![logo](.media/control.png)

![control](https://img.shields.io/github/v/tag/floschu/control?color=blue&label=version) ![issues](https://img.shields.io/github/issues-raw/floschu/control) ![license](https://img.shields.io/badge/license-Apache%202.0-blue.svg)
 
[![build](https://github.com/floschu/control/workflows/.github/workflows/build.yml/badge.svg)](https://github.com/floschu/control/actions)


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
    * core: kotlin only dependency.
    */
    implementation "at.florianschuster.control:control-core:$version"
    
    /**
     * android: AAC viewmodel controller.
     */
    implementation "at.florianschuster.control:control-android:$version"
}
```

## what should I know before I try this?

* kotlin
* Kotlin coroutines & kotlin flow

## concept

TODO

## examples

* [counter](.example-counter): most basic example. uses `Controller`.
* [github search](.example-github): github repository search. uses `ControllerViewModel`.
* [Playables](https://github.com/floschu/Playables): app with a checklist of games you want to play.


## author

visit my [website](https://florianschuster.at/).

## license

```
Copyright 2019 Florian Schuster.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
