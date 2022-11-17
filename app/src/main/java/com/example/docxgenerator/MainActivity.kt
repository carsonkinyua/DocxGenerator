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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
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
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    Row(
                        modifier = Modifier,
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.Start
                    ) {
                            AddButton(doc)
                    }
                    Row(
                        modifier = Modifier,
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.End
                    ) {
                        val galleryLauncher = rememberLauncherForActivityResult(
                            ActivityResultContracts.GetContent()) {
                            doc.addImage(FileUtils().getPath(this@MainActivity, it)!!, 100, 100)
                        }
                        Button(onClick = {
                            galleryLauncher.launch("image/*")
                        }) {
                            Text(text = "Add Image")
                        }
                    }
                    Row(
                        modifier = Modifier,
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Button(onClick = {
                            doc.generateDocx(fullPathToFile);
                            openFile(fullPathToFile);
                        }) {
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

@Composable
fun Greeting(name: String) {
    Text(text = "Hello $name!")
}

@Composable
fun AddButton(doc: AndroidDocBuilder) {
    Button(onClick = {
        doc.addText("Text 1")
    }) {
        Text(text = "Add Text")
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    DocxGeneratorTheme {
        Greeting("Android")
        AddButton(AndroidDocBuilder())
    }
}