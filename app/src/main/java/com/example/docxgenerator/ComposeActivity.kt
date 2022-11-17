package com.amarula.composeapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.amarula.composeapp.ui.theme.DocxGeneratorTheme

class ComposeActivity: ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DocxGeneratorTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp)
                    ) {
                        Text(
                            text = longText,
                            modifier = Modifier
                                .fillMaxWidth()
                                .fillMaxHeight(0.2F)
                        )
                        Button(
                            onClick = {
                            },
                            modifier = Modifier
                                .align(alignment = Alignment.CenterHorizontally)
                                .padding(top = 20.dp)
                        ) {
                            Text(text = "Add Text")
                        }

                        Button(
                            onClick = {
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 20.dp)
                        ) {
                            Text(text = "Add Image")
                        }

                        Button(
                            onClick = {
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 20.dp)
                        ) {
                            Text(text = "Generate Document")
                        }

                    }

                }
            }
        }
    }
}

const val longText = "Arriving at Changi airport, and after going through the immigration, I went straight to the Jewel Changi, seeing one of the iconic sites you usually would come across whenever you see anything talking about the best airports in the World. All this time, I was enjoying the free wifi so I could immediately update the status :)."
