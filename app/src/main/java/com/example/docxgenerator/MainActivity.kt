package com.example.docxgenerator

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.docxgenerator.lib.AndroidDocBuilder
import com.example.docxgenerator.lib.RustLog
import com.example.docxgenerator.ui.theme.DocxGeneratorTheme
import java.io.File
import java.text.SimpleDateFormat
import java.util.*


class MainActivity : ComponentActivity() {
    val WRITE_REQUEST_CODE = 1235
    val READ_REQUEST_CODE = 1236
    val doc = AndroidDocBuilder();
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val permissionW = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        if (permissionW != PackageManager.PERMISSION_GRANTED) {
            Log.i("DocumentBuilder", "Permission to write denied")
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), WRITE_REQUEST_CODE)
        }

        val permissionR = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
        if (permissionR != PackageManager.PERMISSION_GRANTED) {
            Log.i("DocumentBuilder", "Permission to read denied")
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), READ_REQUEST_CODE)
        }

        // Check if the Recorders ("/storage/emulated/0/Recorders/") directory exists, and if not then create it
        val folder = File(Environment.getExternalStorageDirectory().path.toString() + "/WDocuments")
        if (folder.exists()) {
            if (folder.isDirectory) {
                // The Recorders directory exists
            } else {
                // Create the Recorders directory
                folder.mkdir()
            }
        } else {
            // Create the Recorders directory
            folder.mkdir()
        }

        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale("fr")).format(Date())

        var fullPathToFile =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).toString() + "/${timeStamp}_doc.docx"
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
                                      doc.addText(longText);
                            },
                            modifier = Modifier
                                .align(alignment = Alignment.CenterHorizontally)
                                .padding(top = 20.dp)
                        ) {
                            Text(text = "Add Text")
                        }
                        val galleryLauncher = rememberLauncherForActivityResult(
                            ActivityResultContracts.GetContent()) { uri ->
                            uri?.let { uri ->
                                doc.addImage(FileUtils().getPath(this@MainActivity, uri)!!, 100, 100)
                            }
                        }
                        Button(
                            onClick = {
                                galleryLauncher.launch("image/*")
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 20.dp)
                        ) {
                            Text(text = "Add Image")
                        }

                        Button(
                            onClick = {
                                doc.generateDocx(fullPathToFile);
                                openFile(fullPathToFile);
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

    fun openFile(fileName: String) {
        val file = File(fileName)
        val uri: Uri = if (Build.VERSION.SDK_INT < 24) {
            Uri.fromFile(file)
        } else {
            Uri.parse(file.path) // My work-around for SDKs up to 29.
        }
        val viewFile = Intent(Intent.ACTION_VIEW)
        viewFile.setDataAndType(uri, "application/vnd.openxmlformats-officedocument.wordprocessingml.document")
        startActivity(viewFile)
    }

    @Deprecated("Should change this")
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {

            WRITE_REQUEST_CODE -> {
                if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Log.i("DocumentBuilder", "Permission has been denied by user")
                } else {
                    Log.i("DocumentBuilder", "Permission has been granted by user")
                }
            }
            READ_REQUEST_CODE -> {
                if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Log.i("DocumentBuilder", "Permission has been denied by user")
                } else {
                    Log.i("DocumentBuilder", "Permission has been granted by user")
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
    companion object {
        // Used to load the 'audio_lib' library on application startup.
        init {
            System.loadLibrary("docx_lib");
            RustLog.initialiseLogging();
        }
    }
}
const val longText = "Arriving at Changi airport, and after going through the immigration, I went straight to the Jewel Changi, seeing one of the iconic sites you usually would come across whenever you see anything talking about the best airports in the World. All this time, I was enjoying the free wifi so I could immediately update the status :)."
