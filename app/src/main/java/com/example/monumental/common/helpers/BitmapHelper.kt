package com.example.monumental.common.helpers

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import kotlin.math.max

class BitmapHelper {

    fun getScaledBitmap(contentResolver: ContentResolver, imageUri: Uri, imageHelper: ImageHelper): Bitmap? {
        val imageBitmap = getBitmap(contentResolver, imageUri)
        // Get the dimensions of the View
        val targetedSize = imageHelper.getTargetedWidthHeight()

        val targetWidth = targetedSize.first
        val maxHeight = targetedSize.second

        // Determine how much to scale down the image
        val scaleFactor = max(
            imageBitmap!!.width.toFloat() / targetWidth.toFloat(),
            imageBitmap.height.toFloat() / maxHeight.toFloat()
        )

        return Bitmap.createScaledBitmap(
            imageBitmap,
            (imageBitmap.width / scaleFactor).toInt(),
            (imageBitmap.height / scaleFactor).toInt(),
            true
        )
    }

    private fun getBitmap(contentResolver: ContentResolver, imageUri: Uri): Bitmap? {
        return if (Build.VERSION.SDK_INT < 29) {
            MediaStore.Images.Media.getBitmap(contentResolver, imageUri)
        } else {
            val source = ImageDecoder.createSource(contentResolver, imageUri)
            ImageDecoder.decodeBitmap(source)
        }
    }
}