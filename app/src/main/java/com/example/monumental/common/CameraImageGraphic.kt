package com.example.monumental.common

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Rect
import com.example.monumental.common.GraphicOverlay.Graphic

/** Draw camera image to background.  */
abstract class CameraImageGraphic(overlay: GraphicOverlay?, private val bitmap: Bitmap) : Graphic(overlay) {
    override fun draw(canvas: Canvas?) {
        canvas?.drawBitmap(
            bitmap,
            null,
            Rect(0, 0, canvas.width, canvas.height),
            null
        )
    }

}