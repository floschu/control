# changelog

## [0.10.0] - 2020-05-11

### `ControllerStub`

- `ControllerStub` is removed from `Controller` interface.  
The stub is now accessible via the `Controller.stub()` extension function. Once a `Controller` is stubbed via this extension function, it cannot be un-stubbed.

## [0.9.0] - 2020-05-10

### API dump

- binary compatibility is now verified on each build/publish.

### `StateFlow`

- `ControllerImplemenation` now uses `MutableStateFlow` instead of `ConflatedBroadCastChannel` internally.
- `Controller.state` emissions are now distinct by default (via `StateFlow`).
