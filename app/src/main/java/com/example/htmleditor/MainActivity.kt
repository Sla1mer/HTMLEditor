package com.example.htmleditor

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.core.content.ContextCompat
import java.io.*
import android.Manifest
import android.net.Uri


class MainActivity : AppCompatActivity() {

    private lateinit var textEditor: EditText
    private val PERMISSIONS_REQUEST_CODE = 101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Проверяем разрешения
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val permissions = arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )

            if (!checkPermissions(permissions)) {
                requestPermissions(permissions, PERMISSIONS_REQUEST_CODE)
            }
        }

        textEditor = findViewById(R.id.textEditor)
        val loadButton: Button = findViewById(R.id.loadButton)
        val saveTextButton: Button = findViewById(R.id.saveTextButton)
        val saveHtmlButton: Button = findViewById(R.id.saveHtmlButton)
        val exitButton: Button = findViewById(R.id.exitButton)

        loadButton.setOnClickListener {
            openFile()
        }

        saveTextButton.setOnClickListener {
            saveFileAsText()
        }

        saveHtmlButton.setOnClickListener {
            saveFileAsHtml()
        }

        exitButton.setOnClickListener {
            finish()
        }
    }

    private fun checkPermissions(permissions: Array<String>): Boolean {
        for (permission in permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false
            }
        }
        return true
    }

    private fun openFile() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "*/*"
        startActivityForResult(intent, FILE_PICKER_REQUEST)
    }

    private fun saveFileAsText() {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "text/plain"
        intent.putExtra(Intent.EXTRA_TITLE, "textfile.txt")
        startActivityForResult(intent, CREATE_TEXT_DOCUMENT_REQUEST)
    }

    private fun saveFileAsHtml() {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "text/html"
        intent.putExtra(Intent.EXTRA_TITLE, "htmlfile.html")
        startActivityForResult(intent, CREATE_HTML_DOCUMENT_REQUEST)
    }

    companion object {
        private const val FILE_PICKER_REQUEST = 1
        private const val CREATE_TEXT_DOCUMENT_REQUEST = 2
        private const val CREATE_HTML_DOCUMENT_REQUEST = 3
    }

    private fun saveContentToDocument(documentUri: Uri, content: String) {
        try {
            val outputStream = contentResolver.openOutputStream(documentUri)
            outputStream?.bufferedWriter().use { it?.write(content) }
            Toast.makeText(this, "Файл успешно сохранен", Toast.LENGTH_SHORT).show()
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(this, "Ошибка при сохранении файла", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == FILE_PICKER_REQUEST && resultCode == Activity.RESULT_OK) {
            val selectedFileUri = data?.data
            if (selectedFileUri != null) {
                try {
                    val inputStream = contentResolver.openInputStream(selectedFileUri)
                    val reader = BufferedReader(InputStreamReader(inputStream))
                    val stringBuilder = StringBuilder()
                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        stringBuilder.append(line)
                        stringBuilder.append('\n')
                    }
                    inputStream?.close()
                    reader.close()
                    textEditor.setText(stringBuilder.toString())
                } catch (e: IOException) {
                    e.printStackTrace()
                    Toast.makeText(this, "Ошибка при открытии файла", Toast.LENGTH_SHORT).show()
                }
            }
        }  else if (requestCode == CREATE_TEXT_DOCUMENT_REQUEST && resultCode == Activity.RESULT_OK) {
            val documentUri = data?.data
            if (documentUri != null) {
                val content = textEditor.text.toString()
                saveContentToDocument(documentUri, content)
            }
        } else if (requestCode == CREATE_HTML_DOCUMENT_REQUEST && resultCode == Activity.RESULT_OK) {
            val documentUri = data?.data

            if (documentUri != null) {
                val content = textEditor.text.toString()
                val htmlText = content
                    .replace(" ", "&nbsp:")
                    .replace("\n", "<br>")
                    .replace("<", "&lt:")
                    .replace(">", "&gt:")
                    .replace("&", "&amp:")
                    .replace("\"", "&quot:")

                saveContentToDocument(documentUri, htmlText)
            }
        }
    }

}