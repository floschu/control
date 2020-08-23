# changelog

## `[1.0.0]` - Unreleased

- binary compatibility will now be verified and held up on every release.

## `[0.12.0]` - 2020-08-23

- Remove `Mutation` generic type from `Controller` interface.
- Remove `CoroutineScope.createSynchronousController`.
- Remove `ManagedController`.
- Add `ManagedController`.
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
