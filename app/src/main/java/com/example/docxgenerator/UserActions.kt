package com.example.docxgenerator

import android.net.Uri

sealed interface UserActions {
    data class OnTextInput(val text: String): UserActions
    data class OnImageSelect(val uri: List<Uri>): UserActions
    data class OnImageDelete(val uri: Uri): UserActions
}