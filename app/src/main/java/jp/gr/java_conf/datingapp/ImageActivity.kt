package jp.gr.java_conf.datingapp

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.yalantis.ucrop.UCrop
import jp.gr.java_conf.datingapp.enums.ImageEnum
import jp.gr.java_conf.datingapp.listener.IImagePickerLister
import jp.gr.java_conf.datingapp.utility.UiHelper
import java.io.File
import java.io.IOException

class ImageActivity : AppCompatActivity(), IImagePickerLister {
    private var currentPhotoPath = ""
    private val uiHelper = UiHelper()
    private var isValidFlow = true
    private var isFromChat = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image)
        isValidFlow = true
        val intent = intent
        isFromChat = intent.getBooleanExtra("fromChatActivity", false)
        if (!isFromChat) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) if (uiHelper.checkSelfPermissions(this)) uiHelper.showImagePickerDialog(this, this)
        } else {
            if (intent.getIntExtra("requestCode", 0) == 100) {
                openCamera()
            } else if (intent.getIntExtra("requestCode", 0) == 200) {
                openImagesDocument()
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_STORAGE_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) uiHelper.showImagePickerDialog(this, this) else if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_DENIED) {
                Toast.makeText(this, resources.getString(R.string.storage_denied), Toast.LENGTH_LONG).show()
                finish()
            } else if (grantResults[0] == PackageManager.PERMISSION_DENIED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, resources.getString(R.string.camera_denied), Toast.LENGTH_LONG).show()
                finish()
            } else if (grantResults[0] == PackageManager.PERMISSION_DENIED && grantResults[1] == PackageManager.PERMISSION_DENIED) {
                Toast.makeText(this, resources.getString(R.string.both_denied), Toast.LENGTH_LONG).show()
                finish()
            }
        } else if (requestCode == ONLY_CAMERA_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) uiHelper.showImagePickerDialog(this, this) else {
                Toast.makeText(this, resources.getString(R.string.camera_denied), Toast.LENGTH_LONG).show()
                finish()
            }
        } else if (requestCode == ONLY_STORAGE_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) uiHelper.showImagePickerDialog(this, this) else {
                Toast.makeText(this, resources.getString(R.string.storage_denied), Toast.LENGTH_LONG).show()
                finish()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CAMERA_ACTION_PICK_REQUEST_CODE && resultCode == RESULT_OK) {
            val uri = Uri.parse(currentPhotoPath)
            isValidFlow = true
            if (isFromChat) {
                val resultIntent = Intent()
                resultIntent.putExtra("image_uri", uri.toString())
                setResult(RESULT_OK, resultIntent)
                finish()
            } else {
                openCropActivity(uri, uri)
            }
        } else if (requestCode == UCrop.REQUEST_CROP && resultCode == RESULT_OK) {
            if (data != null) {
                val uri = UCrop.getOutput(data)
                val resultIntent = Intent()
                resultIntent.putExtra("image_uri", uri.toString())
                setResult(RESULT_OK, resultIntent)
                isValidFlow = true
                finish()
            }
        } else if (requestCode == PICK_IMAGE_GALLERY_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            try {
                isValidFlow = true
                val sourceUri = data.data
                if (isFromChat) {
                    if (sourceUri != null) {
                        val resultIntent = Intent()
                        resultIntent.putExtra("image_uri", sourceUri.toString())
                        setResult(RESULT_OK, resultIntent)
                        finish()
                    }
                } else {
                    val file = imageFile
                    val destinationUri = Uri.fromFile(file)
                    openCropActivity(sourceUri, destinationUri)
                }
            } catch (e: Exception) {
                println(e.message)
                Toast.makeText(this, resources.getString(R.string.another_image), Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun openImagesDocument() {
        val pictureIntent = Intent(Intent.ACTION_GET_CONTENT)
        pictureIntent.type = "image/*"
        pictureIntent.addCategory(Intent.CATEGORY_OPENABLE)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            val mimeTypes = arrayOf("image/jpeg", "image/png")
            pictureIntent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
        }
        startActivityForResult(Intent.createChooser(pictureIntent, resources.getString(R.string.select_image)), PICK_IMAGE_GALLERY_REQUEST_CODE)
    }

    @get:Throws(IOException::class)
    private val imageFile: File
        private get() {
            val imageFileName = "JPEG_" + System.currentTimeMillis() + "_"
            val storageDir = File(
                    Environment.getExternalStoragePublicDirectory(
                            Environment.DIRECTORY_DCIM
                    ), "Camera"
            )
            if (storageDir.exists()) println("ファイルあり") else println("ファイルなし")
            storageDir.mkdirs()
            val file = File.createTempFile(
                    imageFileName, ".jpg", storageDir
            )
            println(!file.exists())
            if (!file.exists()) {
                println("存在しません！")
            }
            currentPhotoPath = "file:" + file.absolutePath
            return file
        }

    private fun openCropActivity(sourceUri: Uri?, destinationUri: Uri) {
        val options = UCrop.Options()
        options.setToolbarTitle(resources.getString(R.string.crop_image))
        options.setHideBottomControls(true)
        UCrop.of(sourceUri!!, destinationUri)
                .withOptions(options)
                .withAspectRatio(80f, 100f)
                .start(this)
    }

    override fun onOptionSelected(imageEnum: ImageEnum) {
        if (imageEnum == ImageEnum.FROM_CAMERA) openCamera() else if (imageEnum == ImageEnum.FROM_GALLERY) openImagesDocument()
    }

    private fun openCamera() {
        val pictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        val file: File
        file = try {
            imageFile
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, resources.getString(R.string.another_image), Toast.LENGTH_LONG).show()
            return
        }
        val uri: Uri
        uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".provider", file)
        } else Uri.fromFile(file)
        isValidFlow = true
        pictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri)
        startActivityForResult(pictureIntent, CAMERA_ACTION_PICK_REQUEST_CODE)
    }

    override fun onResume() {
        super.onResume()
        println("onResume")
        if (!isValidFlow) {
            finish()
        }
    }

    override fun onPause() {
        super.onPause()
        println("onPause")
        isValidFlow = false
    }

    companion object {
        private const val CAMERA_ACTION_PICK_REQUEST_CODE = 610
        private const val PICK_IMAGE_GALLERY_REQUEST_CODE = 609
        const val CAMERA_STORAGE_REQUEST_CODE = 611
        const val ONLY_CAMERA_REQUEST_CODE = 612
        const val ONLY_STORAGE_REQUEST_CODE = 613
    }
}