package com.example.imagecaputure.activity

import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Rect
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.example.imagecaputure.model.DataModel
import com.example.imagecaputure.R
import com.example.imagecaputure.viewModel.RegViewModel
import com.example.imagecaputure.databinding.ActivityRecognitionBinding
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetector
import com.google.mlkit.vision.face.FaceDetectorOptions
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class RecognitionActivity : AppCompatActivity() {

    private var imageUri: Uri? = null
    private lateinit var binding: ActivityRecognitionBinding
    private lateinit var regViewModel: RegViewModel
    private val dataModel = DataModel()
    private var bitmap : Bitmap ?= null
    private var savedPath :String ?= null
    private var date : List<DataModel> ?= null

    companion object{
        const val TAG = "RecognitionActivity"
    }



    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                openCamera()
            } else {
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
            }
        }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_recognition)

        regViewModel = ViewModelProvider(this@RecognitionActivity)[RegViewModel::class.java]

        regViewModel.getAllRegResponse().observe(this){
            date = it
        }


        binding.cameracardRec.setOnClickListener {
            Log.d("TAG", " cameracard >>>  ")
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.CAMERA
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                // Permission already granted, open camera
                openCamera()
            } else {
                // Permission not granted, request it
                requestCameraPermission()
            }
        }

        detector = FaceDetection.getClient(highAccuracyOpts)

    }

    private fun requestCameraPermission() {
        requestPermissionLauncher.launch(Manifest.permission.CAMERA)
    }

    private fun openCamera() {
        val values = ContentValues()
        values.put(MediaStore.Images.Media.TITLE, "New Picture")
        values.put(MediaStore.Images.Media.DESCRIPTION, "From the Camera")
        imageUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
        cameraActivityResultLauncher.launch(cameraIntent)
    }

    private fun uriToBitmap(selectedFileUri: Uri?): Bitmap? {
        try {
            val parcelFileDescriptor =
                selectedFileUri?.let { contentResolver.openFileDescriptor(it, "r") }
            val fileDescriptor = parcelFileDescriptor?.fileDescriptor
            val image = BitmapFactory.decodeFileDescriptor(fileDescriptor)

            parcelFileDescriptor?.close()
            return image
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }


    @SuppressLint("Range", "Recycle")
    fun rotateBitmap(input: Bitmap?): Bitmap? {
        val orientationColumn = arrayOf(MediaStore.Images.Media.ORIENTATION)
        val cur = imageUri?.let { contentResolver.query(it, orientationColumn, null, null, null) }
        var orientation = -1
        if (cur != null && cur.moveToFirst()) {
            orientation = cur.getInt(cur.getColumnIndex(orientationColumn[0]))
        }
        Log.d("tryOrientation", orientation.toString() + "")
        val rotationMatrix = Matrix()
        rotationMatrix.setRotate(orientation.toFloat())
        val cropped =
            input?.let {
                Bitmap.createBitmap(
                    it,
                    0,
                    0,
                    input.width,
                    input.height,
                    rotationMatrix,
                    true
                )
            }
        return cropped
    }

    private val highAccuracyOpts = FaceDetectorOptions.Builder()
        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
        .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_NONE)
        .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_NONE)
        .build()

    private var detector: FaceDetector? = null


    private var cameraActivityResultLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == RESULT_OK) {
                val inputImage = uriToBitmap(imageUri)
                val rotated = rotateBitmap(inputImage)
                binding.imageViewRec2.setImageBitmap(rotated)
                rotated?.let { performFaceDeletection(it) }
            }
        }


    private fun performFaceDeletection(rotated: Bitmap) {
        val mutableBmp = rotated.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(mutableBmp)
        val image = InputImage.fromBitmap(rotated, 0)
        detector?.process(image)
            ?.addOnSuccessListener { faces ->


                for (face in faces) {
                    val bounds = face.boundingBox
                    val paint = Paint()
                    paint.setColor(Color.RED)
                    paint.style = Paint.Style.STROKE
                    paint.strokeWidth = 15F
                    bitmap = perfromFaceRecognition(bounds, rotated)

                    dataModel.name = "Mani"
                    dataModel.bitMap = savedPath

                    date?.forEach { it1->
                        if (it1.bitMap?.equals(savedPath) == true){
                            Toast.makeText(applicationContext ,applicationContext.getString(R.string.register_successfully) ,Toast.LENGTH_SHORT).show()
                        }else{
                            Toast.makeText(applicationContext ,applicationContext.getString(R.string.go_to_add_the_register) ,Toast.LENGTH_SHORT).show()
                        }
                    }
                    canvas.drawRect(bounds, paint)
                }
            }
            ?.addOnFailureListener {
            }

    }

    private fun perfromFaceRecognition(bounds: Rect, rotated: Bitmap) : Bitmap {
        if (bounds.top < 0) {
            bounds.top = 0
        }
        if (bounds.left < 0) {
            bounds.left = 0
        }
        if (bounds.right > rotated.width) {
            bounds.right = rotated.width - 1
        }
        if (bounds.bottom > rotated.height) {
            bounds.bottom = rotated.height - 1
        }

        val bitmap = Bitmap.createBitmap(rotated, bounds.left, bounds.top, bounds.width(), bounds.height())

        savedPath = saveBitmapAsJpg(bitmap)

        binding.imageViewRec2.setImageBitmap(bitmap)

        return bitmap
    }

    private fun saveBitmapAsJpg(bitmap: Bitmap): String? {
        // Create a directory for your images
        val directory = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "YourDirectory")
        if (!directory.exists()) {
            directory.mkdirs()
        }

        // Create a file to save the image
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "IMG_$timeStamp.jpg"
        val file = File(directory, fileName)

        try {
            // Write the Bitmap to the file
            val fos = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos)
            fos.flush()
            fos.close()
            return file.absolutePath
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return null
    }

}