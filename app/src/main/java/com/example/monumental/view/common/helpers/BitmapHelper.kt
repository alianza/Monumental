package com.example.monumental.view.common.helpers

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import kotlin.math.max

/**
 * Class for helping with Bitmap related operations
 *
 */
class BitmapHelper {

    /**
     * Gets a scaled bitmap according to the ImageHelper dimensions
     *
     * @param contentResolver
     * @param imageUri
     * @param imageHelper
     * @return Bitmap
     */
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

    /**
     * Gets a Bitmap from the device storage
     *
     * @param contentResolver ContentResolver class provides applications access to the content model
     * @param imageUri Uri image to retrieve
     * @return Bitmap that's retrieved
     */
    fun getBitmap(contentResolver: ContentResolver, imageUri: Uri): Bitmap? {
        return if (Build.VERSION.SDK_INT < 29) { // Conditional compatibility statement API based
            MediaStore.Images.Media.getBitmap(contentResolver, imageUri)
        } else {
            val source = ImageDecoder.createSource(contentResolver, imageUri)
            ImageDecoder.decodeBitmap(source)
        }
    }
}