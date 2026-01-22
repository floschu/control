---
name: floschu-control
description: Implement, debug, and test floschu/control - a unidirectional data flow state management kmp library with coroutines
license: Apache-2.0
compatibility: opencode
metadata:
  language: kotlin
  framework: kotlin-multiplatform
---


# Control Library Skill

A skill for working with [control](https://github.com/floschu/control) - a Kotlin Multiplatform unidirectional data flow (UDF) library.

## Overview

Control is a UI-independent state management library that separates business logic from view logic using the UDF pattern. Controllers have no dependency on views, making them easy to unit test.

### Core Architecture

```
Action -> Mutator -> [0..n] Mutations -> Reducer -> New State
```

```
                          Action
      ┏━━━━━━━━━━━━━━━━━━━━━│━━━━━━━━━━━━━━━━━┓
      ┃                     │                 ┃
      ┃               ┏━━━━━▼━━━━━┓           ┃  side effect ┏━━━━━━━━━━━━━━━━━━━━┓
      ┃               ┃  mutator ◀────────────────────────────▶  service/usecase  ┃
      ┃               ┗━━━━━━━━━━━┛           ┃              ┗━━━━━━━━━━━━━━━━━━━━┛
      ┃                     │                 ┃
      ┃                     │ 0..n mutations  ┃
      ┃                     │                 ┃
      ┃               ┏━━━━━▼━━━━━┓           ┃
      ┃  ┌───────────▶┃  reducer  ┃           ┃
      ┃  │            ┗━━━━━━━━━━━┛           ┃
      ┃  │ previous         │                 ┃
      ┃  │ state            │ new state       ┃
      ┃  │                  │                 ┃
      ┃  │            ┏━━━━━▼━━━━━┓           ┃
      ┃  └────────────┃   state   ┃           ┃
      ┃               ┗━━━━━━━━━━━┛           ┃
      ┃                     │                 ┃
      ┗━━━━━━━━━━━━━━━━━━━━━│━━━━━━━━━━━━━━━━━┛
                            ▼
                          state
```

## Installation

Add the dependency to your Kotlin Multiplatform project:

```groovy
repositories {
    mavenCentral()
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation("at.florianschuster.control:control-core:$version")
            }
        }
    }
}
```

## Creating a Basic Controller

### Step 1: Define Actions

Actions represent user intents or events that trigger state changes. Define them as a sealed interface:

```kotlin
sealed interface CounterAction {
    data object Increment : CounterAction
    data object Decrement : CounterAction
}
```

### Step 2: Define Mutations (Private)

Mutations are internal state change descriptors. Keep them private to the controller:

```kotlin
private sealed interface CounterMutation {
    data object IncreaseValue : CounterMutation
    data object DecreaseValue : CounterMutation
    data class SetLoading(val loading: Boolean) : CounterMutation
}
```

### Step 3: Define State

State is an immutable data class representing the current state:

```kotlin
data class CounterState(
    val value: Int = 0,
    val loading: Boolean = false
)
```

### Step 4: Create the Controller

Use `CoroutineScope.createController()` to build the controller:

```kotlin
typealias CounterController = Controller<CounterAction, CounterState>

fun CoroutineScope.createCounterController(
    initialValue: Int = 0
): CounterController = createController(
    initialState = CounterState(value = initialValue),
    
    mutator = { action ->
        when (action) {
            is CounterAction.Increment -> flow {
                emit(CounterMutation.SetLoading(true))
                delay(500.milliseconds)
                emit(CounterMutation.IncreaseValue)
                emit(CounterMutation.SetLoading(false))
            }
            is CounterAction.Decrement -> flow {
                emit(CounterMutation.SetLoading(true))
                delay(500.milliseconds)
                emit(CounterMutation.DecreaseValue)
                emit(CounterMutation.SetLoading(false))
            }
        }
    },
    
    reducer = { mutation, previousState ->
        when (mutation) {
            is CounterMutation.IncreaseValue -> previousState.copy(value = previousState.value + 1)
            is CounterMutation.DecreaseValue -> previousState.copy(value = previousState.value - 1)
            is CounterMutation.SetLoading -> previousState.copy(loading = mutation.loading)
        }
    }
)
```

## Key Components

### Controller Interface

The core interface with two members:

```kotlin
interface Controller<Action, State> {
    fun dispatch(action: Action)  // Send actions to be processed
    val state: StateFlow<State>   // Observe state changes
}
```

### Mutator

Transforms actions into a Flow of mutations. Has access to `MutatorContext`:

```kotlin
typealias Mutator<Action, Mutation, State> = 
    MutatorContext<Action, State>.(action: Action) -> Flow<Mutation>

interface MutatorContext<Action, State> {
    val currentState: State      // Access current state
    val actions: Flow<Action>    // Access actions flow for combining
}
```

**Mutator patterns:**

```kotlin
mutator = { action ->
    when(action) {
        // Emit no mutations
        is Action.NoOp -> emptyFlow()
        
        // Emit single mutation
        is Action.Simple -> flowOf(Mutation.DoSomething)
        
        // Emit multiple mutations (async operations)
        is Action.LoadData -> flow {
            emit(Mutation.SetLoading(true))
            val data = repository.fetchData()  // Suspend call
            emit(Mutation.SetData(data))
            emit(Mutation.SetLoading(false))
        }
        
        // Access current state
        is Action.Toggle -> flowOf(
            Mutation.SetEnabled(!currentState.isEnabled)
        )
    }
}
```

### Reducer

Synchronously transforms mutations into new state:

```kotlin
typealias Reducer<Mutation, State> = 
    ReducerContext.(mutation: Mutation, previousState: State) -> State

reducer = { mutation, previousState ->
    when(mutation) {
        is Mutation.SetLoading -> previousState.copy(loading = mutation.loading)
        is Mutation.SetData -> previousState.copy(data = mutation.data)
        is Mutation.SetEnabled -> previousState.copy(isEnabled = mutation.enabled)
    }
}
```

### Transformers

Transform flows of actions, mutations, or states:

```kotlin
// Initial action on start
actionsTransformer = { actions ->
    actions.onStart { emit(Action.InitialLoad) }
}

// Merge global streams
mutationsTransformer = { mutations ->
    merge(mutations, userSession.map { Mutation.SetSession(it) })
}

// Logging state changes
statesTransformer = { states ->
    states.onEach { println("New State: $it") }
}
```

## EffectController

For one-off side effects (toasts, navigation, snackbars):

```kotlin
interface EffectController<Action, State, Effect> : Controller<Action, State> {
    val effects: Flow<Effect>  // Fan-out delivery (one emission per collector)
}
```

### Creating an EffectController

```kotlin
sealed interface MyEffect {
    data class ShowToast(val message: String) : MyEffect
    data object NavigateBack : MyEffect
}

fun CoroutineScope.createMyController(): EffectController<MyAction, MyState, MyEffect> =
    createEffectController(
        initialState = MyState(),
        
        mutator = { action ->
            when (action) {
                is MyAction.Save -> flow {
                    emit(Mutation.SetLoading(true))
                    try {
                        repository.save(currentState.data)
                        emitEffect(MyEffect.ShowToast("Saved!"))
                        emitEffect(MyEffect.NavigateBack)
                    } catch (e: Exception) {
                        emitEffect(MyEffect.ShowToast("Error: ${e.message}"))
                    }
                    emit(Mutation.SetLoading(false))
                }
            }
        },
        
        reducer = { mutation, previousState ->
            // Can also emit effects in reducer
            when (mutation) {
                is Mutation.SetError -> {
                    emitEffect(MyEffect.ShowToast(mutation.error))
                    previousState.copy(error = mutation.error)
                }
                else -> previousState
            }
        }
    )
```

## Configuration Options

### ControllerLog

Configure logging for debugging:

```kotlin
createController(
    // ...
    controllerLog = ControllerLog.None,      // No logging (default)
    controllerLog = ControllerLog.Println,   // Print to console
    controllerLog = ControllerLog.Custom { message ->
        Timber.d(message)  // Custom logger
    }
)
```

### ControllerStart

Control when the state machine starts:

```kotlin
createController(
    // ...
    controllerStart = ControllerStart.Lazy,        // Start on first access (default)
    controllerStart = ControllerStart.Immediately  // Start immediately on creation
)
```

### Custom Dispatcher

Override the coroutine dispatcher:

```kotlin
createController(
    // ...
    dispatcher = Dispatchers.Default  // Or any custom dispatcher
)
```

## Testing

### Controller Testing

Test controllers directly by dispatching actions and asserting state:

```kotlin
class CounterControllerTest {
    
    @Test
    fun `increment increases value`() = runTest {
        val controller = createCounterController(initialValue = 0)
        
        controller.dispatch(CounterAction.Increment)
        advanceUntilIdle()
        
        assertEquals(1, controller.state.value.value)
    }
}
```

### View Testing with Stubs

Use `ControllerStub` to test views in isolation:

```kotlin
@OptIn(TestOnlyStub::class)
class CounterViewTest {
    
    @Test
    fun `view displays correct state`() {
        val controller = scope.createCounterController().toStub()
        
        // Emit test state
        controller.emitState(CounterState(value = 42, loading = false))
        
        // Assert view displays "42"
    }
    
    @Test
    fun `button dispatches increment action`() {
        val controller = scope.createCounterController().toStub()
        
        // Simulate button click
        incrementButton.performClick()
        
        // Verify action was dispatched
        assertEquals(
            listOf(CounterAction.Increment),
            controller.dispatchedActions
        )
    }
}
```

### EffectController Stub

```kotlin
@OptIn(TestOnlyStub::class)
class MyViewTest {
    
    @Test
    fun `shows toast on effect`() {
        val controller = scope.createMyController().toStub()
        
        // Emit test effect
        controller.emitEffect(MyEffect.ShowToast("Test message"))
        
        // Assert toast is shown
    }
}
```

## View Integration

### Jetpack Compose (Android)

```kotlin
@Composable
fun CounterScreen(
    controller: CounterController = viewModelScope.createCounterController()
) {
    val state by controller.state.collectAsState()
    
    Column {
        Text(text = "Count: ${state.value}")
        
        Button(
            onClick = { controller.dispatch(CounterAction.Increment) },
            enabled = !state.loading
        ) {
            Text("Increment")
        }
        
        Button(
            onClick = { controller.dispatch(CounterAction.Decrement) },
            enabled = !state.loading
        ) {
            Text("Decrement")
        }
        
        if (state.loading) {
            CircularProgressIndicator()
        }
    }
}
```

### Collecting Effects

```kotlin
@Composable
fun MyScreen(controller: EffectController<MyAction, MyState, MyEffect>) {
    val context = LocalContext.current
    
    LaunchedEffect(controller) {
        controller.effects.collect { effect ->
            when (effect) {
                is MyEffect.ShowToast -> {
                    Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
                }
                is MyEffect.NavigateBack -> {
                    // Handle navigation
                }
            }
        }
    }
    
    // Rest of UI...
}
```

## Best Practices

1. **Keep Mutations private**: Mutations are implementation details of the controller
2. **Use immutable State**: Always use `data class` with `copy()` for state updates
3. **Single source of truth**: State should be the only source of truth for the view
4. **Side effects in mutator**: Perform async operations (API calls, DB access) in the mutator
5. **Pure reducers**: Reducers should be pure functions with no side effects
6. **Use EffectController for one-off events**: Navigation, toasts, and snackbars should use effects
7. **Test controllers independently**: Controllers have no view dependency, test them in isolation

## Common Patterns

### Loading/Error/Success Pattern

```kotlin
data class DataState(
    val data: List<Item> = emptyList(),
    val loading: Boolean = false,
    val error: String? = null
)

private sealed interface DataMutation {
    data object SetLoading : DataMutation
    data class SetData(val data: List<Item>) : DataMutation
    data class SetError(val error: String) : DataMutation
}

mutator = { action ->
    when (action) {
        is DataAction.Load -> flow {
            emit(DataMutation.SetLoading)
            try {
                val data = repository.loadData()
                emit(DataMutation.SetData(data))
            } catch (e: Exception) {
                emit(DataMutation.SetError(e.message ?: "Unknown error"))
            }
        }
    }
}

reducer = { mutation, previousState ->
    when (mutation) {
        is DataMutation.SetLoading -> previousState.copy(loading = true, error = null)
        is DataMutation.SetData -> previousState.copy(data = mutation.data, loading = false)
        is DataMutation.SetError -> previousState.copy(error = mutation.error, loading = false)
    }
}
```

### Debounce Search Pattern

```kotlin
actionsTransformer = { actions ->
    actions.debounce { action ->
        if (action is SearchAction.Query) 300.milliseconds else Duration.ZERO
    }
}
```

### Cancelling Previous Operations

```kotlin
mutator = { action ->
    when (action) {
        is SearchAction.Query -> flow {
            emit(SearchMutation.SetLoading(true))
            val results = searchService.search(action.query)
            emit(SearchMutation.SetResults(results))
            emit(SearchMutation.SetLoading(false))
        }.takeUntil(actions.filterIsInstance<SearchAction.Query>())
    }
}
```

## changelog

See the [changelog](https://github.com/floschu/control/blob/develop/CHANGELOG.md) for versions.