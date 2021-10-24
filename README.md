<p align="center"><img alt="flow" width="600" src=".media/control.png"></p>

<p align=center>
    <a href="https://search.maven.org/artifact/at.florianschuster.control/control-core"><img alt="version" src="https://img.shields.io/maven-central/v/at.florianschuster.control/control-core?label=core-version&logoColor=f88909" /></a>
    <a href="LICENSE"><img alt="license" src="https://img.shields.io/badge/license-Apache%202.0-blue.svg?color=7b6fe2" /></a>
</p>

<p align=center>
    <a href="https://github.com/floschu/control/"><img alt="last commit" src="https://img.shields.io/github/last-commit/floschu/control?logoColor=ffffff" /></a>
    <a href="https://www.codacy.com/manual/floschu/control?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=floschu/control&amp;utm_campaign=Badge_Grade"><img alt="code quality" src="https://api.codacy.com/project/badge/Grade/39072347acb94bf79651d7f16bfa63ca" /></a>
    <a href="https://codecov.io/gh/floschu/control"><img alt="coverage" src="https://codecov.io/gh/floschu/control/branch/develop/graph/badge.svg" /></a>
    <a href="https://github.com/floschu/control/actions"><img alt="build" src="https://github.com/floschu/control/workflows/build/badge.svg" /></a>
</p>

## installation

``` groovy
repositories {
    mavenCentral()
}

dependencies {
    implementation("at.florianschuster.control:control-core:$version")
}
```

see [changelog](https://github.com/floschu/control/blob/develop/CHANGELOG.md)  for versions

## controller

<p align="center"><img alt="flow" width="500" src=".media/udf.png"></p>

A [Controller](control-core/src/main/kotlin/at/florianschuster/control/Controller.kt) is an ui-independent class that controls the state of a view. The role of a `Controller` is to separate business-logic from view-logic. A `Controller` has no dependency to the view, so it can easily be unit tested.

## info & documentation

1. [controller](https://github.com/floschu/control/wiki/controller)
2. [view](https://github.com/floschu/control/wiki/view)
3. [transformations](https://github.com/floschu/control/wiki/transformations)
4. [effects](https://github.com/floschu/control/wiki/effects)
5. [controller testing](https://github.com/floschu/control/wiki/controller-testing)
6. [view testing](https://github.com/floschu/control/wiki/view-testing)
7. [logging](https://github.com/floschu/control/wiki/logging)

## examples

*   [kotlin-counter](examples/kotlin-counter): most basic kotlin example. uses `Controller`.
*   [android-counter](examples/android-counter): most basic android example. uses `Controller` from kotlin-counter.
*   [android-github-search](examples/android-github): android github repository search. uses `Controller` combined with _Android Jetpack AAC_ `ViewModel`.
*   [android-counter-compose](examples/android-compose): android counter example built with [jetpack compose](https://developer.android.com/jetpack/compose).

## author

visit my [website](https://florianschuster.at/).
