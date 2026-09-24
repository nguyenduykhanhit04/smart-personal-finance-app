package com.example.personalfinance.models;

import java.util.List;

public class ProductClassificationRequest {
    private int userId;
    private List<YoloDetectionDTO> detections;

    public ProductClassificationRequest(int userId, List<YoloDetectionDTO> detections) {
        this.userId = userId;
        this.detections = detections;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public List<YoloDetectionDTO> getDetections() {
        return detections;
    }

    public void setDetections(List<YoloDetectionDTO> detections) {
        this.detections = detections;
    }

    public static class YoloDetectionDTO {
        private String className;
        private double confidence;

        public YoloDetectionDTO(String className, double confidence) {
            this.className = className;
            this.confidence = confidence;
        }

        public String getClassName() {
            return className;
        }

        public void setClassName(String className) {
            this.className = className;
        }

        public double getConfidence() {
            return confidence;
        }

        public void setConfidence(double confidence) {
            this.confidence = confidence;
        }
    }
}
