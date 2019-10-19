![logo](.media/control.png)

[![version](https://img.shields.io/github/v/tag/floschu/control?color=blue&label=version)](https://bintray.com/flosch/control) [![Codacy Badge](https://api.codacy.com/project/badge/Grade/39072347acb94bf79651d7f16bfa63ca)](https://www.codacy.com/manual/floschu/control?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=floschu/control&amp;utm_campaign=Badge_Grade) [![build](https://github.com/floschu/control/workflows/build/badge.svg)](https://github.com/floschu/control/actions) [![license](https://img.shields.io/badge/license-Apache%202.0-blue.svg)](LICENSE)

coroutines flow based unidirectional-data-flow architecture

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

### controller

``` kotlin
class ValueController : Controller<ValueController.Action, ValueController.Mutation, ValueController.State> {
    sealed class Action {
        data class SetValue(val value: Int) : Action()
    }
 
    sealed class Mutation {
        data class SetMutatedValue(val mutatedValue: Int) : Mutation()
    }
 
    data class State(
        val value: Int
    )
 
    override val initialState = State(value = 0)
    
    override fun mutate(action: Action): Flow<Mutation> = when (action) {
        is Action.SetValue -> flow {
            delay(5000) // some asynchronous action
            emit(Mutation.SetMutatedValue(3))
        }
    }

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
        // action
        buttonSetValue.clicks()
            .map { ValueController.Action.SetValue(2) }
            .bind(to = controller.action)
            .launchIn(scope = /*some scope*/)
            
        // state
        controller.state.map { it.value }
            .distinctUntilChanged()
            .map { "$it" }
            .bind(to = valueTextView::setText)
            .launchIn(scope = /*some scope*/)
    
        // later at some point
        controller.cancel()
    }
}
```

### test

either use `Controller.currentState`

``` koltlin
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

or test with the `control-test` package

``` koltlin
@Test
fun testController() {
    // given
    val controller = ValueController().apply { scope = testScope }
    val testCollector = controller.state.test(testScope)
    
    // when
    controller.action(ValueController.Action.SetValue(2))
    advanceTimeBy(5000)
    
    // then
    with(testCollector) {
        hasNoErrors()
        hasEmissionCount(2)
        hasEmission(
            ValueController.State(0),
            ValueController.State(3)
        )
    }
}
```

## examples

*   [counter](example-counter): most basic example. uses `Controller`.
*   [github search](example-github): github repository search. uses `Controller` combined with android jetpack AAC `ViewModel`.
*   [Playables](https://github.com/floschu/Playables): app with a checklist of games you want to play.

## author

visit my [website](https://florianschuster.at/).

## reaktor

control is the kotlin coroutines flow alternative of [Reaktor](https://github.com/floschu/Reaktor/) which uses [RxJava2](https://github.com/ReactiveX/RxJava) to create its unidirectional-data-flow architecture.
