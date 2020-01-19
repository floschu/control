<p align="center"><img alt="flow" width="600" src=".media/control.png"></p>

[![version](https://img.shields.io/github/v/tag/floschu/control?color=f88909&label=version)](https://bintray.com/flosch/control) [![Codacy Badge](https://api.codacy.com/project/badge/Grade/39072347acb94bf79651d7f16bfa63ca)](https://www.codacy.com/manual/floschu/control?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=floschu/control&amp;utm_campaign=Badge_Grade) [![codecov](https://codecov.io/gh/floschu/control/branch/develop/graph/badge.svg)](https://codecov.io/gh/floschu/control) [![build](https://github.com/floschu/control/workflows/build/badge.svg)](https://github.com/floschu/control/actions) [![license](https://img.shields.io/badge/license-Apache%202.0-blue.svg?color=7b6fe2)](LICENSE)

## installation

``` groovy
repositories {
    jcenter()
}

dependencies {
    implementation("at.florianschuster.control:control-core:$version")
}
```

## controller

<p align="center"><img alt="flow" width="500" src=".media/udf.png"></p>

a [Controller](control-core/src/main/kotlin/at/florianschuster/control/Controller.kt) is an ui-independent class that controls the state of a view. The role of a `Controller` is to separate business logic and control flow away from the view. a `Controller` has no dependency to a view, so it can easily be unit tested.

## wiki

visit the wiki for more detailed information:

1. [controller](https://github.com/floschu/control/wiki/1.-controller)
2. [view/consumer](https://github.com/floschu/control/wiki/2.-view-or-consumer)
3. [proxy](https://github.com/floschu/control/wiki/3.-proxy)
4. [transformations](https://github.com/floschu/control/wiki/4.-transformations)
5. [controller testing](https://github.com/floschu/control/wiki/5.-controller-testing)
6. [view/consumer testing](https://github.com/floschu/control/wiki/6.-consumer-or-view-testing)

## examples

*   [counter](example-counter): most basic example. uses `Controller`.
*   [github search](example-github): github repository search. uses `Controller` combined with android jetpack AAC `ViewModel`.
*   [Playables](https://github.com/floschu/Playables): app with a checklist of games you want to play.

## author

visit my [website](https://florianschuster.at/).
