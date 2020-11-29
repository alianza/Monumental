package com.example.monumental.view.common.helpers

import android.util.Pair
import android.view.View
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout

class ImageHelper(private var previewPane: ImageView, private var controlPanel: ConstraintLayout) {

    private var selectedSize: String = SIZE_PREVIEW
    private var isLandScape: Boolean = false
    // Max width (portrait mode)
    private var imageMaxWidth = 0
    // Max height (portrait mode)
    private var imageMaxHeight = 0

    /** Returns max image width, always for portrait mode. Caller needs to swap width / height for
    landscape mode. */
    private fun getImageMaxWidth(previewPane: ImageView, controlPanel: ConstraintLayout): Int {
        if (imageMaxWidth == 0) {
            // Calculate the max width in portrait mode. This is done lazily since we need to wait for
            // a UI layout pass to get the right values. So delay it to first time image rendering time.
            imageMaxWidth = if (isLandScape) {
                (previewPane.parent as View).height - controlPanel.height
            } else {
                (previewPane.parent as View).width
            }
        }

        return imageMaxWidth
    }

    /** Returns max image height, always for portrait mode. Caller needs to swap width / height for
    landscape mode. */
    private fun getImageMaxHeight(previewPane: ImageView, controlPanel: ConstraintLayout): Int {
        if (imageMaxHeight == 0) {
            // Calculate the max width in portrait mode. This is done lazily since we need to wait for
            // a UI layout pass to get the right values. So delay it to first time image rendering time.
            imageMaxHeight = if (isLandScape) {
                (previewPane.parent as View).width
            } else {
                (previewPane.parent as View).height - controlPanel.height
            }
        }

        return imageMaxHeight
    }

    /** Gets the targeted width / height. */
    fun getTargetedWidthHeight(): Pair<Int, Int> {
        val targetWidth: Int
        val targetHeight: Int

        when (selectedSize) {
            SIZE_PREVIEW -> {
                val maxWidthForPortraitMode = getImageMaxHeight(previewPane, controlPanel)
                val maxHeightForPortraitMode = getImageMaxWidth(previewPane, controlPanel)
                targetWidth = if (isLandScape) maxHeightForPortraitMode else maxWidthForPortraitMode
                targetHeight =
                    if (isLandScape) maxWidthForPortraitMode else maxHeightForPortraitMode
            }
            SIZE_640_480 -> {
                targetWidth = if (isLandScape) 640 else 480
                targetHeight = if (isLandScape) 480 else 640
            }
            SIZE_1024_768 -> {
                targetWidth = if (isLandScape) 1024 else 768
                targetHeight = if (isLandScape) 768 else 1024
            }
            else -> throw IllegalStateException("Unknown size")
        }

        return Pair(targetWidth, targetHeight)
    }

    companion object {
        private const val SIZE_PREVIEW = "w:max" // Available on-screen width.
        private const val SIZE_1024_768 = "w:1024" // ~1024*768 in a normal ratio
        private const val SIZE_640_480 = "w:640" // ~640*480 in a normal ratio
    }

}