<p align="center"><img alt="flow" width="600" src=".media/control.png"></p>

<p align=center>
    <a href="https://bintray.com/flosch/control/control-core"><img alt="version" src="https://img.shields.io/bintray/v/flosch/control/control-core?label=core-version&logoColor=f88909" /></a> 
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
    jcenter()
}

dependencies {
    implementation("at.florianschuster.control:control-core:$version")
}
```

## controller

<p align="center"><img alt="flow" width="500" src=".media/udf.png"></p>

A [Controller](control-core/src/main/kotlin/at/florianschuster/control/Controller.kt) is an ui-independent class that controls the state of a view. The role of a `Controller` is to separate business-logic from view-logic. A `Controller` has no dependency to the view, so it can easily be unit tested.

## info & documentation

1. [controller](https://github.com/floschu/control/wiki/controller)
2. [view](https://github.com/floschu/control/wiki/view)
3. [transformations](https://github.com/floschu/control/wiki/transformations)
4. [controller testing](https://github.com/floschu/control/wiki/controller-testing)
5. [view testing](https://github.com/floschu/control/wiki/view-testing)
6. [logging](https://github.com/floschu/control/wiki/logging)

the changelog can be found [here](https://github.com/floschu/control/blob/develop/CHANGELOG.md)

## examples

*   [counter](examples/example-counter): most basic example. uses `Controller`.
*   [github search](examples/example-github): github repository search. uses `Controller` combined with _Android Jetpack AAC_ `ViewModel`.
*   [counter compose](https://github.com/floschu/control/pull/9): like the counter example, but uses _Jetpack Compose_.

## coverage

test coverage is automatically run by github actions on `[push]`

|  | threshold | run | output |
|---:|:---:|---|---|
| instruction/branch | 90% | `./gradlew test jacocoTestReport` | `./control-core/build/reports/jacoco/` |
| mutation | 100% | `./gradlew pitest` | `./control-core/build/reports/pitest/` |



## author

visit my [website](https://florianschuster.at/).
