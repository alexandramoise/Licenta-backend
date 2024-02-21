package com.example.backend.model.entity;

public enum BloodPressureType {
    Hypotension, // < 90/60
    Optimal, // < 120/80
    Normal, // 120-129/80-84
    Prehypertension, // 130-139/85-89
    Hypertension_stage1, // 140-159/90-99
    Hypertension_stage2, // 160-179/100-109
    Hypertension_stage3 // > 180/110
}
