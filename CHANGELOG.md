# changelog

## `[X.X.X]` - Unreleased

- binary compatibility is now verified on each `[build]` & `[publish]`.
- `Controller.stub` is now marked as `@TestOnly`

## `[0.10.0]` - 2020-05-11

- `ControllerStub` is removed from `Controller` interface.  
- `ControllerStub` is now accessible via the `Controller.stub()` extension function. once a `Controller` is stubbed via this extension function, it cannot be un-stubbed.

## `[0.9.0]` - 2020-05-10

- `ControllerImplemenation` now uses `MutableStateFlow` instead of `ConflatedBroadCastChannel` internally.
- `Controller.state` emissions are now distinct by default (via `StateFlow`).
