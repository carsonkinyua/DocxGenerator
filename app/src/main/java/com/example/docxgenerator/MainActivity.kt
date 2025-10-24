package com.example.docxgenerator

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.BasicTooltipDefaults
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AttachFile
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.rememberAsyncImagePainter
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
        val permissionW =
            ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        if (permissionW != PackageManager.PERMISSION_GRANTED) {
            Log.i("DocumentBuilder", "Permission to write denied")
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                WRITE_REQUEST_CODE
            )
        }

        val permissionR =
            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
        if (permissionR != PackageManager.PERMISSION_GRANTED) {
            Log.i("DocumentBuilder", "Permission to read denied")
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                READ_REQUEST_CODE
            )
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
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
                .toString() + "/${timeStamp}_doc.docx"
        enableEdgeToEdge()
        setContent {
            DocxGeneratorTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    Scaffold { innerPadding ->
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .windowInsetsPadding(WindowInsets.systemBars)
                                .padding(20.dp)
                                .padding(innerPadding)
                        ) {

                            val viewmodel: AppViewmodel = viewModel()
                            val state by viewmodel.state.collectAsState()
                            val text = rememberTextFieldState(state.text)

                            val galleryLauncher =
                                rememberLauncherForActivityResult(
                                    ActivityResultContracts.PickMultipleVisualMedia()
                                ) { uris ->

                                    if (uris.isNotEmpty()) {
                                        viewmodel.onAction(UserActions.OnImageSelect(uris))

                                        uris.forEach { uri ->
                                            doc.addImage(
                                                FileUtils().getPath(this@MainActivity, uri)!!,
                                                100,
                                                100
                                            )
                                        }
                                    }

                                }

                            BasicTextField(
                                state = text,
                                modifier = Modifier.fillMaxWidth(),
                                textStyle = TextStyle(
                                    color = MaterialTheme.colors.onBackground,
                                    fontStyle = FontStyle.Normal,
                                    fontWeight = FontWeight.Normal
                                ),
                                keyboardOptions = KeyboardOptions(
                                    autoCorrect = false,
                                    keyboardType = KeyboardType.Text
                                ),
                                lineLimits = TextFieldLineLimits.MultiLine(
                                    minHeightInLines = 8,
                                    maxHeightInLines = 8
                                ),
                                cursorBrush = SolidColor(MaterialTheme.colors.onBackground),
                                decorator = { innerTextField ->
                                    Box(
                                        modifier = Modifier.border(
                                            width = 2.dp,
                                            color = MaterialTheme.colors.primary,
                                            shape = RoundedCornerShape(16.dp)
                                        )
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(16.dp))
                                                .padding(16.dp)
                                        ) {

                                            innerTextField()

                                            if (state.uris.isNotEmpty()) {
                                                LazyRow(
                                                    modifier = Modifier
                                                        .height(104.dp),
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                ) {
                                                    items(
                                                        items = state.uris,
                                                        key = { it }
                                                    ) { uri ->
                                                        Box(
                                                            modifier = Modifier.size(104.dp)
                                                        ) {
                                                            Image(
                                                                painter = rememberAsyncImagePainter(
                                                                    uri
                                                                ),
                                                                contentDescription = null
                                                            )
                                                            IconButton(
                                                                modifier = Modifier.align(Alignment.TopEnd),
                                                                onClick = {
                                                                    viewmodel.onAction(
                                                                        UserActions.OnImageDelete(
                                                                            uri
                                                                        )
                                                                    )
                                                                }
                                                            ) {
                                                                Icon(
                                                                    imageVector = Icons.Outlined.Cancel,
                                                                    contentDescription = null,
                                                                    modifier = Modifier.size(24.dp)
                                                                )
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                            Row {
                                                IconButton(
                                                    onClick = {
                                                        galleryLauncher.launch(
                                                            PickVisualMediaRequest(
                                                                mediaType = ActivityResultContracts.PickVisualMedia.ImageOnly
                                                            )
                                                        )
                                                    }
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Outlined.AttachFile,
                                                        contentDescription = null,
                                                        modifier = Modifier.size(24.dp),
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            )

                            Button(
                                onClick = {
                                    doc.addText(state.text);
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
    }

    fun openFile(fileName: String) {
        val file = File(fileName)
        val uri: Uri = if (Build.VERSION.SDK_INT < 24) {
            Uri.fromFile(file)
        } else {
            Uri.parse(file.path) // My work-around for SDKs up to 29.
        }
        val viewFile = Intent(Intent.ACTION_VIEW)
        viewFile.setDataAndType(
            uri,
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
        )
        try{
            startActivity(viewFile)
        } catch (e: Exception){
            e.printStackTrace()
            Toast.makeText(this, "Install an application to view .docx files!", Toast.LENGTH_SHORT).show()
        }
    }

    @Deprecated("Should change this")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
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
