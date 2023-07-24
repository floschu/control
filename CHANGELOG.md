# changelog

## `[1.1.0]` - 2023-07-24

- Update Kotlin to `1.9.0`
- Update kotlinx.coroutines to `1.7.2`

## `[1.0.0]` - 2022-04-11

- Remove `Controller.currentState`.
- Remove `Flow.bind` and `Flow.distinctMap` extensions.
- Binary compatibility will now be verified and held up on every release.

## `[0.15.0]` - 2021-10-20

- Refactor `Controller.state` from `Flow<State>` to `StateFlow<State>`.
- Deprecate `Controller.currentState`.
- Internal `BroadcastChannel` implementations changed to `SharedFlow`.

## `[0.14.0]` - 2021-10-17

- Update kotlinx.coroutines to `1.5.2`.
- Remove kotlin as `api` dependency.
- Remove `ControllerLog.default`.
- Lazily start controller when accessing `Controller.effects` field (#26)
- `control-core` will now be deployed to mavenCentral

## `[0.13.1]` - 2020-09-13

- Remove `atomicfu`.

## `[0.13.0]` - 2020-09-01

- Add `EffectController` and `CoroutineScope.createEffectController`.
- Rename `Controller.stub()` to `Controller.toStub()` to better reflect what it is doing.

## `[0.12.0]` - 2020-08-24

- Remove `Mutation` generic type from `Controller` interface.
- Remove `CoroutineScope.createSynchronousController`.
- Remove `ManagedController`.
- Make `ControllerLog` log creation lazy.

## `[0.11.0]` - 2020-05-30

- `CoroutineScope.createController` and `CoroutineScope.createSynchronousController` now accept a custom `ControllerStart` parameter instead of `CoroutineStart`.
- Add `ManagedController`.
- `Controller.stub` is now marked as `@TestOnly`.
- binary compatibility is now checked on each `[build]` & `[publish]`.

## `[0.10.0]` - 2020-05-11

- `ControllerStub` is removed from `Controller` interface.  
- `ControllerStub` is now accessible via the `Controller.stub()` extension function. once a `Controller` is stubbed via this extension function, it cannot be un-stubbed.

## `[0.9.0]` - 2020-05-10

- `ControllerImplementation` now uses `MutableStateFlow` instead of `ConflatedBroadCastChannel` internally.
- `Controller.state` emissions are now distinct by default (via `StateFlow`).
