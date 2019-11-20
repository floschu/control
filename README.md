<p align="center"><img alt="flow" width="600" src=".media/control.png"></p>

[![version](https://img.shields.io/github/v/tag/floschu/control?color=f88909&label=version)](https://bintray.com/flosch/control) [![Codacy Badge](https://api.codacy.com/project/badge/Grade/39072347acb94bf79651d7f16bfa63ca)](https://www.codacy.com/manual/floschu/control?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=floschu/control&amp;utm_campaign=Badge_Grade) [![build](https://github.com/floschu/control/workflows/build/badge.svg)](https://github.com/floschu/control/actions) [![license](https://img.shields.io/badge/license-Apache%202.0-blue.svg?color=7b6fe2)](LICENSE)

## installation

``` groovy
repositories {
    jcenter()
}

dependencies {
    implementation("at.florianschuster.control:control-core:$version")
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
 
    // we start with the initial state - could also be set via 
    // the constructor to enable injection
    override val initialState = State(value = 0)
    
    // every action is transformed into [0..n] mutations
    override fun mutate(action: Action): Flow<Mutation> = when (action) {
        is Action.SetValue -> flow {
            delay(5000) // some asynchronous action
            emit(Mutation.SetMutatedValue(action.value))
        }
    }

    // every mutation is used to reduce the previous state to a 
    // new state that is then published to the view
    override fun reduce(previousState: State, mutation: Mutation): State = when (mutation) {
        is Mutation.SetMutatedValue -> previousState.copy(value = mutation.mutatedValue)
    }
}
```

### view

*   binds its interactions to the `Controller.action`
*   binds the `Controller.state` to its components

in this example a literal view with a `Button` and a `TextView` is implemented. however that does not mean that only a view can have a `Controller` - there could also be a feature wide or a global `Controller` that controls the state of the corresponding system or app.

``` kotlin
class View(
    private val controller : ValueController = ValueController()
) {
    
    init {
        // bind view actions to Controller.action
        setValueButton.clicks()
            .map { ValueController.Action.SetValue(value = 3) }
            .bind(to = controller.action)
            .launchIn(scope = viewScope)
            
        // bind Controller.state to view
        controller.state.map { it.value }
            .distinctUntilChanged()
            .map { "$it" }
            .bind(to = valueTextView::setText)
            .launchIn(scope = viewScope)
    }
    
    fun onDestroy() {
        controller.cancel()
    }
}
```
here [FlowBinding](https://github.com/ReactiveCircus/FlowBinding) is used to transform android view events into a `Flow`

### test

#### controller testing

to test a `Controller` either use `Controller.currentState`:

``` kotlin
@Test
fun testController() = testScope.runBlockingTest {
    // given
    val controller = ValueController().apply { scope = testScope }

    // when
    controller.action(ValueController.Action.SetValue(3))
    advanceTimeBy(5000)

    // then
    assertEquals(3, controller.currentState.value)
}
```

or test with a TestFlow ([flow-test-extensions](https://github.com/floschu/flow-test-extensions)):

``` kotlin
@Test
fun testController() = testScope.runBlockingTest {
    // given
    val controller = ValueController().apply { scope = testScope }
    val testFlow = controller.state.testIn(testScope)

    // when
    controller.action(ValueController.Action.SetValue(3))
    advanceTimeBy(5000)

    // then
    testFlow expect noErrors()
    testFlow expect emissionCount(2)
    testFlow expect emissions(
        ValueController.State(0), // initial state
        ValueController.State(3) // after action
    )
}
```

when using delay controls on a `Controller`, set the `Controller.scope` to a `TestCoroutineScope`.

examples:
*   [CounterControllerTest](example-counter/src/test/kotlin/at/florianschuster/control/counterexample/CounterControllerTest.kt) for [CounterController](example-counter/src/main/kotlin/at/florianschuster/control/counterexample/CounterController.kt)
*   [GithubControllerTest](example-github/src/test/kotlin/at/florianschuster/control/githubexample/search/GithubControllerTest.kt) for [GithubController](example-github/src/main/kotlin/at/florianschuster/control/githubexample/search/GithubController.kt)

#### consumer/view testing

a consumer or view that binds its ui to a controller, can be tested by setting the `Controller.stubEnabled` to true and then using `Controller.stub` for stubbing.

if the stub is enabled, `Controller.mutate()` and `Controller.reduce()` are not executed.

to check if actions are correctly triggered by a consumer use `Controler.stub.actions` to get an ordered `List<Action>`. to check if state is correctly rendered use `Controller.stub.state(State)` to instantly trigger a `Controller.state` emission.

``` kotlin
@Test
fun valueButtonClickTriggersCorrectAction() {
    // given
    val controller = ValueController().apply { stubEnabled = true }
    val view = View(controller)
    
    
    // when
    onView(withId(R.id.setValueButton)).perform(click())
    
    // then
    assertEquals(
        ValueController.Action.SetValue(value = 3), 
        controller.stub.actions.last()
    )
}

@Test
fun stubbedStateUpdatesTextViewText() {
    // given
    val testValue = 42
    val controller = ValueController().apply { stubEnabled = true }
    val view = View(controller)
    
    // when
    controller.stub.state(ValueController.State(value = testValue))
    
    // then
    onView(withId(R.id.valueTextView))
        .check(matches(withText("$testValue")))
}
```

examples:
*   [CounterActivityTest](example-counter/src/androidTest/kotlin/at/florianschuster/control/counterexample/CounterViewTest.kt) for [CounterActivity](example-counter/src/main/kotlin/at/florianschuster/control/counterexample/CounterView.kt)
*   [GithubActivityTest](example-github/src/androidTest/kotlin/at/florianschuster/control/githubexample/search/GithubViewTest.kt) for [GithubActivity](example-github/src/main/kotlin/at/florianschuster/control/githubexample/search/GithubView.kt)

### transform

transform functions are called once for corresponding `Flow`'s in a `Controller`.

an initial action can be implemented via `transformAction`:

``` kotlin
override fun transformAction(action: Flow<Action>): Flow<Action> {
    return action.onStart { emit(Action.InitialLoad) }
}
```

a global state can be merged with the `Controller.state` via `transformMutation`:

``` kotlin
val userSession: Flow<Session>

// in Controller
override fun transformMutation(mutation: Flow<Mutation>): Flow<Mutation> {
    return flowOf(mutation, userSession.map { Mutation.SetSession(it) }).flattenMerge()
}
```

everytime `userSession` emits a new session, its gets reduced into the `Controller.state` via `Mutation.SetSession`

## examples

*   [counter](example-counter): most basic example. uses `Controller`.
*   [github search](example-github): github repository search. uses `Controller` combined with android jetpack AAC `ViewModel`.
*   [Playables](https://github.com/floschu/Playables): app with a checklist of games you want to play.

## author

visit my [website](https://florianschuster.at/).

## reaktor

control is the kotlin coroutines flow alternative of [Reaktor](https://github.com/floschu/Reaktor/) which uses [RxJava2](https://github.com/ReactiveX/RxJava) to create its unidirectional-data-flow architecture.
