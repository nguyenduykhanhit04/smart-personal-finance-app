package com.example.personalfinance.models

data class ProductClassificationRequest(
    var userId: Int = 0,
    var detections: List<YoloDetectionDTO> = emptyList()
) {
    data class YoloDetectionDTO(
        var className: String? = null,
        var confidence: Double = 0.0
    )
}
