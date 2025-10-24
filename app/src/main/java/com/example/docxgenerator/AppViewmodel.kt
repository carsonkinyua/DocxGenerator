package com.example.docxgenerator

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class AppViewmodel : ViewModel() {

    private var _state = MutableStateFlow(UIState())
    val state = _state.asStateFlow()

    fun onAction(action: UserActions) {
        when (action) {
            is UserActions.OnImageSelect -> {
                _state.update { it.copy(uris = action.uri) }
            }

            is UserActions.OnTextInput -> {
                _state.update { it.copy(text = action.text) }
            }

            is UserActions.OnImageDelete -> {
                _state.update {
                    it.copy(
                        uris = it.uris.toMutableList().apply {
                            remove(action.uri)
                        }
                    )
                }
            }
        }
    }

}