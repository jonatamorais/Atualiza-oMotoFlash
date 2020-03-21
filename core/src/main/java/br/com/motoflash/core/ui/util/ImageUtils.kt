package br.com.motoflash.core.ui.util

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.media.ExifInterface
import android.net.Uri
import android.os.Parcelable
import android.provider.MediaStore
import android.util.Base64
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.TransformationUtils.rotateImage
import com.bumptech.glide.request.RequestOptions
import timber.log.Timber
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

object ImageUtils {

    fun getRoundedCornerBitmap(bitmap: Bitmap, pixels: Int): Bitmap {
        val output = Bitmap.createBitmap(
            bitmap.width, bitmap
                .height, Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(output)

        val color = -0xbdbdbe
        val paint = Paint()
        val rect = Rect(0, 0, bitmap.width, bitmap.height)
        val rectF = RectF(rect)

        paint.isAntiAlias = true
        canvas.drawARGB(0, 0, 0, 0)
        paint.color = color
        canvas.drawRoundRect(rectF, pixels.toFloat(), pixels.toFloat(), paint)

        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        canvas.drawBitmap(bitmap, rect, rect, paint)

        return output
    }

    fun rotateImageIfRequired(img: Bitmap, context: Context, selectedImage: Uri): Bitmap {

        if (selectedImage.scheme == "content") {
            val projection = arrayOf(MediaStore.Images.ImageColumns.ORIENTATION)
            val c = context.contentResolver.query(selectedImage, projection, null, null, null)
            c?.apply {
                if (c.moveToFirst()) {
                    val rotation = c.getInt(0)
                    c.close()
                    return rotateImage(img, rotation)
                }
            }
            return img
        } else {
            selectedImage.path?.apply {
                val ei = ExifInterface(this)
                val orientation =
                    ei.getAttributeInt(
                        ExifInterface.TAG_ORIENTATION,
                        ExifInterface.ORIENTATION_NORMAL
                    )
                return when (orientation) {
                    ExifInterface.ORIENTATION_ROTATE_90 -> rotateImage(img, 90)
                    ExifInterface.ORIENTATION_ROTATE_180 -> rotateImage(img, 180)
                    ExifInterface.ORIENTATION_ROTATE_270 -> rotateImage(img, 270)
                    else -> img
                }
            }
            return img
        }
    }

    fun getPickImageChooserIntent(context: Context, outputFileUri: Uri?): Intent {

        val allIntents = ArrayList<Intent>()
        val packageManager = context.packageManager

        // collect all camera intents
        val captureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        val listCam = packageManager.queryIntentActivities(captureIntent, 0)
        for (res in listCam) {
            val intent = Intent(captureIntent)
            intent.component = ComponentName(res.activityInfo.packageName, res.activityInfo.name)
            intent.setPackage(res.activityInfo.packageName)
            if (outputFileUri != null) {
                intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri)
            }
            allIntents.add(intent)
        }

        // collect all gallery intents
        val galleryIntent = Intent(Intent.ACTION_GET_CONTENT)
        galleryIntent.type = "image/*"
        val listGallery = packageManager.queryIntentActivities(galleryIntent, 0)
        for (res in listGallery) {
            val intent = Intent(galleryIntent)
            intent.component = ComponentName(res.activityInfo.packageName, res.activityInfo.name)
            intent.setPackage(res.activityInfo.packageName)
            allIntents.add(intent)
        }

        // the main intent is the last in the list (fucking android) so pickup the useless one
        var mainIntent = allIntents[allIntents.size - 1]
        for (intent in allIntents) {
            val component = intent.component
            if (component != null && component.className == "com.android.documentsui.DocumentsActivity") {
                mainIntent = intent
                break
            }
        }
        allIntents.remove(mainIntent)

        // Create a chooser from the main intent
        val chooserIntent =
            Intent.createChooser(mainIntent,"Selecione a origem")

        // Add all other intents
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, allIntents.toTypedArray<Parcelable>())

        return chooserIntent
    }

    fun getPickImageResultUri(context: Context, data: Intent?): Uri? {
        var isCamera = true
        if (data != null && data.data != null) {
            val action = data.action
            isCamera = action != null && action == MediaStore.ACTION_IMAGE_CAPTURE
        }
        return if (isCamera) getCaptureImageOutputUri(context) else data!!.data
    }

    fun getCaptureImageOutputUri(context: Context): Uri? {

        var outputFileUri: Uri? = null
        val getImage = context.externalCacheDir
        if (getImage != null) {
            outputFileUri = Uri.fromFile(File(getImage.path, "${UUID.randomUUID()}.png"))
        }
        return outputFileUri
    }

    fun scaleDown(
        realImage: Bitmap, maxImageSize: Float,
        filter: Boolean
    ): Bitmap {
        val ratio = Math.min(
            maxImageSize / realImage.width,
            maxImageSize / realImage.height
        )
        val width = Math.round(ratio * realImage.width)
        val height = Math.round(ratio * realImage.height)

        return Bitmap.createScaledBitmap(realImage, width, height, filter)
    }


    fun rotateBitmap(source: Bitmap, angle: Float): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(angle)
        return Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
    }


    fun encodeImage(bm: Bitmap): String {
        val baos = ByteArrayOutputStream()
        bm.compress(Bitmap.CompressFormat.JPEG, 50, baos)
        val b = baos.toByteArray()
        return Base64.encodeToString(b, Base64.NO_WRAP)
    }

    fun decodeImage(bm: String?): Bitmap? {
        if (bm != null) {
            val decodedString = Base64.decode(bm, Base64.NO_WRAP)
            return BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
        } else {
            return null
        }
    }

    fun createImageFile(context: Context): File? {
        try {
            // Create an image file name
            @SuppressLint("SimpleDateFormat")
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
            val imageFileName = "PNG_" + timeStamp + "_"
            val storageDir = context.getExternalFilesDir(null)
            val image = File.createTempFile(
                imageFileName, /* prefix */
                ".png", /* suffix */
                storageDir      /* directory */
            )
            image.deleteOnExit()

            return image
        } catch (ex: IOException) {
            Timber.tag("CAMERA - IO").e(ex.message)
            return null
        }
    }

    private fun scaleImage(context: Context, image: Drawable?, scaleFactor: Float): Drawable? {
        var image = image

        if (image == null || image !is BitmapDrawable) {
            return image
        }

        val b = image.bitmap

        val sizeX = Math.round(image.intrinsicWidth * scaleFactor)
        val sizeY = Math.round(image.intrinsicHeight * scaleFactor)

        val bitmapResized = Bitmap.createScaledBitmap(b, sizeX, sizeY, false)

        image = BitmapDrawable(context.resources, bitmapResized)

        return image
    }

    fun loadCircularImage(view: ImageView, url: String? = null, defaultImg: Int) {
        if(url != null) {
            Glide.with(view.context)
                .load(url)
                .apply(
                    RequestOptions.circleCropTransform()
                        .error(defaultImg)
                )
                .into(view)
        } else {
            Glide.with(view.context)
                .load(defaultImg)
                .apply(RequestOptions.circleCropTransform())
                .into(view)
        }
    }
}