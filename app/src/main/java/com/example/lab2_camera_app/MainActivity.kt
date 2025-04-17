package com.example.lab2_camera_app

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import java.io.Console
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date

class MainActivity : AppCompatActivity() {

    private lateinit var selfieImageView: ImageView
    private lateinit var takeSelfieButton: Button
    private lateinit var sendSelfieButton: Button
    private var photoUri: Uri? = null
    private var permissionsGranted = false // Флаг для отслеживания состояния разрешений

    companion object {
        private const val REQUEST_IMAGE_CAPTURE = 1
        private const val REQUEST_PERMISSIONS = 2
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        selfieImageView = findViewById(R.id.selfieImageView)
        takeSelfieButton = findViewById(R.id.takeSelfieButton)
        sendSelfieButton = findViewById(R.id.sendSelfieButton)

        checkAndRequestPermissions()

//        takeSelfieButton.setOnClickListener {
//            if (permissionsGranted) {
//                dispatchTakePictureIntent()
//            } else {
//                checkAndRequestPermissions()
//            }
//        }
//          dorabotochki
        takeSelfieButton.setOnClickListener {
            if (permissionsGranted) {
                launchImageChooser()
            } else {
                checkAndRequestPermissions()
            }
        }
        sendSelfieButton.setOnClickListener {
            if (photoUri != null) {
                sendEmailWithSelfie()
            } else {
                Toast.makeText(this, "Спочатку зробіть селфі!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun checkAndRequestPermissions() {
        val permissionsToRequest = mutableListOf<String>()

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.CAMERA)
        }

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.READ_MEDIA_IMAGES)

            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }

        if (permissionsToRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, permissionsToRequest.toTypedArray(), REQUEST_PERMISSIONS)
        } else {
            permissionsGranted = true
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_PERMISSIONS) {
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                permissionsGranted = true
                Toast.makeText(this, "Дозволи отримані!", Toast.LENGTH_SHORT).show()
            } else {
                permissionsGranted = false
                Toast.makeText(this, "Дозволи потрібні для роботи додатку!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun dispatchTakePictureIntent() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (takePictureIntent.resolveActivity(packageManager) != null) {
            val photoFile: File? = try {
                createImageFile()
            } catch (ex: IOException) {
                Toast.makeText(this, "Помилка створення файлу!", Toast.LENGTH_SHORT).show()
                null
            }

            photoFile?.also {
                photoUri = FileProvider.getUriForFile(
                    this,
                    "com.example.selfieapp.fileprovider",
                    it
                )
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
            }
        } else {
            Toast.makeText(this, "Камера недоступна!", Toast.LENGTH_SHORT).show()
        }
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir: File = getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!
        return File.createTempFile(
            "JPEG_${timeStamp}_",
            ".jpg",
            storageDir
        )
    }
    // Достаем недавние фотки
    private fun launchImageChooser() {
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        galleryIntent.type = "image/*"

        val photoFile = try {
            createImageFile()
        } catch (ex: IOException) {
            Toast.makeText(this, "Помилка створення файлу!", Toast.LENGTH_SHORT).show()
            null
        }

        photoFile?.also {
            photoUri = FileProvider.getUriForFile(
                this,
                "com.example.selfieapp.fileprovider",
                it
            )
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
        }

        val chooser = Intent.createChooser(galleryIntent, "Виберіть джерело фото")
        chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, arrayOf(cameraIntent))
        startActivityForResult(chooser, REQUEST_IMAGE_CAPTURE)
    }


//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
//            photoUri?.let {
//                selfieImageView.setImageURI(it)
//            }
//        }
//    }
    // для вібора фотки
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQUEST_IMAGE_CAPTURE -> {
                    if (data == null || data.data == null) {
                        photoUri?.let {
                            selfieImageView.setImageURI(it)
                        }
                    } else {
                        photoUri = data.data
                        selfieImageView.setImageURI(photoUri)
                    }
                }
            }
        }
    }


    private fun sendEmailWithSelfie() {
        val emailIntent = Intent(Intent.ACTION_SEND)
        emailIntent.type = "message/rfc822"

        emailIntent.putExtra(Intent.EXTRA_EMAIL, arrayOf("hodovychenko@op.edu.ua"))
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "ANDROID [Голованець Єгор]")
        emailIntent.putExtra(Intent.EXTRA_TEXT, "Репо: https://github.com/Holovanets/android_lab2")

        photoUri?.let {
            emailIntent.putExtra(Intent.EXTRA_STREAM, it)
            emailIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        try {
            startActivity(Intent.createChooser(emailIntent, "Відправити лист..."))
        } catch (ex: android.content.ActivityNotFoundException) {
            Toast.makeText(this, "Немає додатку для відправки email!", Toast.LENGTH_SHORT).show()
        }
    }
}