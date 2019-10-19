<p align="center"><img alt="flow" width="600" src=".media/control.png"></p>

[![version](https://img.shields.io/github/v/tag/floschu/control?color=f88909&label=version)](https://bintray.com/flosch/control) [![Codacy Badge](https://api.codacy.com/project/badge/Grade/39072347acb94bf79651d7f16bfa63ca)](https://www.codacy.com/manual/floschu/control?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=floschu/control&amp;utm_campaign=Badge_Grade) [![build](https://github.com/floschu/control/workflows/build/badge.svg)](https://github.com/floschu/control/actions) [![license](https://img.shields.io/badge/license-Apache%202.0-blue.svg?color=7b6fe2)](LICENSE)

## installation

``` groovy
repositories {
    jcenter()
}

dependencies {
    // kotlin only modules
    implementation("at.florianschuster.control:control-core:$version")
    testImplementation("at.florianschuster.control:control-test:$version")
}
```

## concept

<p align="center"><img alt="flow" width="500" src=".media/udf.png"></p>

### controller

a [Controller](control-core/src/main/java/at/florianschuster/control/Controller.kt) is an ui-independent class that controls the state of a view. The role of a `Controller` is to separate business logic and control flow away from the view. a `Controller` has no dependency to a view, so it can easily be unit tested.

``` kotlin
class ValueController : Controller<Action, Mutation, State> {

    // action triggered by view
    sealed class Action {
        data class SetValue(val value: Int) : Action()
    }
 
    // mutation that is used to alter the state
    sealed class Mutation {
        data class SetMutatedValue(val mutatedValue: Int) : Mutation()
    }
 
    // immutable state
    data class State(
        val value: Int
    )
 
    // we start with the initial state
    // could also be set from the constructor to enable injection of initial state
    override val initialState = State(value = 0)
    
    // every action is transformed into [0..n] mutations
    override fun mutate(action: Action): Flow<Mutation> = when (action) {
        is Action.SetValue -> flow {
            delay(5000) // some asynchronous action
            emit(Mutation.SetMutatedValue(3))
        }
    }

    // every mutation is used to reduce the previous state to a new state that is then published to the view
    override fun reduce(previousState: State, mutation: Mutation): State = when (mutation) {
        is Mutation.SetMutatedValue -> previousState.copy(value = mutation.mutatedValue)
    }
}
```

### view

``` kotlin
class View {
    private val controller = ValueController()
    
    init {
        // bind view actions to Controller.action
        buttonSetValue.clicks()
            .map { ValueController.Action.SetValue(2) }
            .bind(to = controller.action)
            .launchIn(scope = viewScope)
            
        // bind Controller.state to view
        controller.state.map { it.value }
            .distinctUntilChanged()
            .map { "$it" }
            .bind(to = valueTextView::setText)
            .launchIn(scope = viewScope)
    
        // later at some point
        controller.cancel()
    }
}
```

### test

To test a `Controller` either use `Controller.currentState`:

``` kotlin
@Test
fun testController() {
    // given
    val controller = ValueController().apply { scope = testScope }

    // when
    controller.action(ValueController.Action.SetValue(2))
    advanceTimeBy(5000)

    // then
    assertEquals(3, controller.currentState.value)
}
```

or test with the `control-test` package:

``` kotlin
@Test
fun testController() {
    // given
    val controller = ValueController().apply { scope = testScope }
    val testCollector = controller.state.test(testScope)

    // when
    controller.action(ValueController.Action.SetValue(2))
    advanceTimeBy(5000)

    // then
    testCollector expect noErrors()
    testCollector expect emissionCount(2)
    testCollector expect emissions(
        ValueController.State(0), // initial state
        ValueController.State(3) // after action
    )
}
```

when using delay controls on a `Controller`, set the `Controller.scope` to a `TestCoroutineScope`.

examples:
 *  [CounterControllerTest](example-counter/src/test/java/at/florianschuster/control/counterexample/CounterControllerTest.kt) for [CounterController](example-counter/src/main/java/at/florianschuster/control/counterexample/CounterController.kt)
 *  [GithubControllerTest](example-github/src/test/java/at/florianschuster/control/githubexample/search/GithubControllerTest.kt) for [GithubController](example-github/src/main/java/at/florianschuster/control/githubexample/search/GithubController.kt)

## examples

*   [counter](example-counter): most basic example. uses `Controller`.
*   [github search](example-github): github repository search. uses `Controller` combined with android jetpack AAC `ViewModel`.
*   [Playables](https://github.com/floschu/Playables): app with a checklist of games you want to play.

## author

visit my [website](https://florianschuster.at/).

## reaktor

control is the kotlin coroutines flow alternative of [Reaktor](https://github.com/floschu/Reaktor/) which uses [RxJava2](https://github.com/ReactiveX/RxJava) to create its unidirectional-data-flow architecture.
