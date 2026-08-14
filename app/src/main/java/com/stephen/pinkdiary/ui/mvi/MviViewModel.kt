package com.stephen.pinkdiary.ui.mvi

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update

/**
 * 项目统一的 MVI 状态容器：公开只读状态与非重放 Effect，所有输入通过 [onIntent]。
 */
abstract class MviViewModel<Intent, State, Effect>(initialState: State) : ViewModel() {

    private val mutableUiState = MutableStateFlow(initialState)
    val uiState: StateFlow<State> = mutableUiState.asStateFlow()

    private val effectChannel = Channel<Effect>(capacity = Channel.BUFFERED)
    val effects: Flow<Effect> = effectChannel.receiveAsFlow()

    abstract fun onIntent(intent: Intent)

    protected fun reduce(transform: (State) -> State) {
        mutableUiState.update(transform)
    }

    protected suspend fun emitEffect(effect: Effect) {
        effectChannel.send(effect)
    }
}
