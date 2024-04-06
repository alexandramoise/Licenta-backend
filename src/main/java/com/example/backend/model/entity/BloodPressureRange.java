package com.example.backend.model.entity;

import lombok.Data;
import org.apache.commons.lang3.Range;

@Data
public class BloodPressureRange {
    private Range<Integer> ages;
    private Range<Integer> systolic;
    private Range<Integer> diastolic;

    public BloodPressureRange(Range<Integer> ages,
                              Range<Integer> systolic,
                              Range<Integer> diastolic) {
        this.ages = ages;
        this.systolic = systolic;
        this.diastolic = diastolic;
    }
}
