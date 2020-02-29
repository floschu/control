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