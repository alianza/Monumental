package com.example.monumental.model.entity

/**
 * List of detected Landmark results
 *
 * @property results List of LandmarkResults
 */
data class LandmarkResultList(
    var results: MutableList<LandmarkResult>
)