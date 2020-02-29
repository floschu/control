package at.florianschuster.control

import kotlinx.coroutines.flow.Flow

/**
 * Base type for [Mutator] and [ComplexMutator].
 */
typealias MutatorType<Action, Mutation, State> = (
    action: Action,
    stateAccessor: () -> State,
    actionFlow: Flow<Action>
) -> Flow<Mutation>

/**
 * Base type for [Reducer].
 */
typealias ReducerType<Mutation, State> = (previousState: State, mutation: Mutation) -> State

/**
 * Base type for [Transformer].
 */
typealias TransformerType<Emission> = (emissions: Flow<Emission>) -> Flow<Emission>