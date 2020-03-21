package br.com.motoflash.core.ui.util

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Created by lucascordeiro on 27/11/17.
 */

class FileUtils {

    companion object {

        fun getCaptureImageOutputUri(context: Context?): Uri {
            var outputFileUri: Uri? = null
            val getImage = context?.externalCacheDir
            if (getImage != null) {
                outputFileUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                   FileProvider.getUriForFile(
                        context,
                        "br.com.pakman.courier.fileprovider",
                       createImageFile(context)
                    )
                } else {
                    Uri.fromFile(
                        createImageFile(
                            context
                        )
                    )
                }
            }
            return outputFileUri!!
        }

//        fun getCaptureImageOutputUri(context: Context): Uri? {
//            var outputFileUri: Uri? = null
//            val getImage = context.externalCacheDir
//            if (getImage != null) {
//                outputFileUri = Uri.fromFile(File(getImage.path, "pickImageResult.png"))
//            }
//            return outputFileUri
//        }

        fun generateImageName(): String {
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
                return "JPG_$timeStamp.jpg"
        }

        fun createImageFile(context: Context, fileName: String? = null): File {
            val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            return File(storageDir, fileName ?: generateImageName())
        }

        fun getPickImageResultUri(context: Context, data: Intent?): Uri? {
            // val photoURI = FileProvider.getUriForFile(context, context.getApplicationContext().packageName + ".my.package.name.provider", createImageFile())

            var isCamera = true
            if (data != null && data.data != null) {
                val action = data.action
                isCamera = action != null && action == MediaStore.ACTION_IMAGE_CAPTURE
            }
            return if (isCamera) getCaptureImageOutputUri(
                context
            ) else data!!.data
        }

        fun saveBitmap(context: Context, bitmap: Bitmap): File {
            val file = createImageFile(context)
            try {
                val fos = FileOutputStream(file)
                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, fos)
                fos.flush()
                fos.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }

            return file
        }

        @Throws(IOException::class)
        private fun createImageFile(context: Context): File {
            // Create an image file name
            val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!
            return File.createTempFile(
                "JPEG_${timeStamp}_", /* prefix */
                ".jpg", /* suffix */
                storageDir /* directory */
            ).apply {
                // Save a file: path for use with ACTION_VIEW intents
            }
        }
    }
}