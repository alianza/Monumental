package com.example.monumental.cloudlandmarkrecognition

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.text.TextPaint
import com.example.monumental.common.GraphicOverlay
import com.google.firebase.ml.vision.cloud.landmark.FirebaseVisionCloudLandmark

/** Graphic instance for rendering detected landmark.  */
abstract class CloudLandmarkGraphic(overlay: GraphicOverlay, private val landmark: FirebaseVisionCloudLandmark) :
    GraphicOverlay.Graphic(overlay) {

    private val rectPaint = Paint().apply {
        color = TEXT_COLOR
        style = Paint.Style.STROKE
        strokeWidth = STROKE_WIDTH
        setShadowLayer(2F, 0F, 0F, SHADOW_COLOR)
    }

    private val landmarkPaint = TextPaint(TextPaint.ANTI_ALIAS_FLAG).apply {
        color = TEXT_COLOR
        textSize = TEXT_SIZE
        setShadowLayer(16F, 0F, 0F, SHADOW_COLOR)
        setShadowLayer(2F, 0F, 0F, SHADOW_COLOR)
    }

    /**
     * Draws the landmark block annotations for position, size, and raw value on the supplied canvas.
     */
    override fun draw(canvas: Canvas?) {
        landmark.landmark.let { lm ->
            landmark.boundingBox?.let { boundingBox ->
                // Draws the bounding box around the LandmarkBlock.
                val rect = RectF(boundingBox)
                with(rect) {
                    left = translateX(left)
                    top = translateY(top)
                    right = translateX(right)
                    bottom = translateY(bottom)
                    canvas?.drawRoundRect(this, 12F, 12F, rectPaint)

                    // Renders the landmark at the bottom of the box (with some displacement).
                    canvas?.drawText(lm, right + 6, bottom - 6, landmarkPaint)
                }
            }
        }
    }

    companion object {
        private const val TEXT_COLOR = Color.WHITE
        private const val SHADOW_COLOR = Color.BLACK
        private const val TEXT_SIZE = 54.0f
        private const val STROKE_WIDTH = 4.0f
    }
}