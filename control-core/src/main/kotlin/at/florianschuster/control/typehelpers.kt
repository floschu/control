@file:Suppress("FunctionName")

package at.florianschuster.control

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

/**
 * Use to create a [Mutator].
 */
fun <Action, Mutation, State> Mutator(
    mutate: (action: Action) -> Flow<Mutation> = { _ -> emptyFlow() }
): Mutator<Action, Mutation, State> = { action, _, _ -> mutate(action) }

/**
 * Use to create a complex [Mutator].
 */
fun <Action, Mutation, State> ComplexMutator(
    mutate: (
        action: Action,
        stateAccessor: () -> State,
        transformedActionFlow: Flow<Action>
    ) -> Flow<Mutation> = { _, _, _ -> emptyFlow() }
): Mutator<Action, Mutation, State> = { action, stateAccessor, transformedActionFlow ->
    mutate(action, stateAccessor, transformedActionFlow)
}

/**
 * Use to create a [Reducer].
 */
fun <Mutation, State> Reducer(
    reduce: (mutation: Mutation, previousState: State) -> State = { _, previousState -> previousState }
): Reducer<Mutation, State> = { mutation, previousState -> reduce(mutation, previousState) }

/**
 * User to create a [Transformer].
 */
fun <Emission> Transformer(
    transform: (emissions: Flow<Emission>) -> Flow<Emission> = { it }
): Transformer<Emission> = { emissions -> transform(emissions) }