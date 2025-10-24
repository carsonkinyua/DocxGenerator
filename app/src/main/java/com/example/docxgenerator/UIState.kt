package com.example.docxgenerator

import android.net.Uri

data class UIState(
    val text: String = "",
    val uris: List<Uri> = emptyList()
)
