package com.example.backend.model.entity;

import lombok.Data;
import org.apache.commons.lang3.Range;

import java.util.ArrayList;
import java.util.List;

@Data
public class BloodPressureForAge {
    private List <BloodPressureRange> lowRanges = new ArrayList<>();
    private List <BloodPressureRange> normalRanges = new ArrayList<>();
    private List <BloodPressureRange> highRanges = new ArrayList<>();

    public BloodPressureForAge() {
        setLowRanges();
        setNormalRanges();
        setHighRanges();
    }

    /**
     * setting the range for HYPOTENSION based on age groups
     */
    public void setLowRanges() {
        List<BloodPressureRange> lowRangeList = new ArrayList<>();
        // BloodPressureRange are 3 Range: age, systolic, diastolic
        lowRangeList.add(new BloodPressureRange(Range.between(0, 1), Range.between(50, 75), Range.between(40, 50)));
        lowRangeList.add(new BloodPressureRange(Range.between(1, 5), Range.between(60, 80), Range.between(45, 55)));
        lowRangeList.add(new BloodPressureRange(Range.between(6, 13), Range.between(70, 90), Range.between(50, 60)));
        lowRangeList.add(new BloodPressureRange(Range.between(14, 19), Range.between(80, 105), Range.between(60, 70)));
        lowRangeList.add(new BloodPressureRange(Range.between(20, 24), Range.between(85, 107), Range.between(65, 75)));
        lowRangeList.add(new BloodPressureRange(Range.between(25, 29), Range.between(90, 109), Range.between(65, 76)));
        lowRangeList.add(new BloodPressureRange(Range.between(30, 34), Range.between(95, 110), Range.between(65, 77)));
        lowRangeList.add(new BloodPressureRange(Range.between(35, 39), Range.between(95, 111), Range.between(65, 78)));
        lowRangeList.add(new BloodPressureRange(Range.between(40, 44), Range.between(95, 112), Range.between(65, 79)));
        lowRangeList.add(new BloodPressureRange(Range.between(45, 49), Range.between(95, 115), Range.between(65, 80)));
        lowRangeList.add(new BloodPressureRange(Range.between(50, 54), Range.between(95, 116), Range.between(70, 82)));
        lowRangeList.add(new BloodPressureRange(Range.between(55, 64), Range.between(95, 118), Range.between(70, 83)));
        lowRangeList.add(new BloodPressureRange(Range.between(65, 120), Range.between(100, 121), Range.between(70, 85)));
        lowRanges = lowRangeList;
    }

    /**
     * get the range for HYPOTENSION based on the patient's age
     * @param age
     * @return
     */
    public BloodPressureRange getLowRange(int age) {
        return lowRanges.stream()
                .filter(range -> range.getAges().contains(age))
                .findFirst()
                .orElse(null);
    }

    public void setNormalRanges() {
        for (BloodPressureRange lowRange : lowRanges) {
            int startAge = lowRange.getAges().getMinimum();
            int endAge = lowRange.getAges().getMaximum();
            int systolicLow = lowRange.getSystolic().getMaximum() + 1;
            int diastolicLow = lowRange.getDiastolic().getMaximum() + 1;
            int systolicHigh = systolicLow + 15;
            int diastolicHigh = diastolicLow + 10;

            normalRanges.add(new BloodPressureRange(
                    Range.between(startAge, endAge),
                    Range.between(systolicLow, systolicHigh),
                    Range.between(diastolicLow, diastolicHigh)
            ));
        }
    }

    public BloodPressureRange getNormalRange(int age) {
        return normalRanges.stream()
                .filter(range -> range.getAges().contains(age))
                .findFirst()
                .orElse(null);
    }

    public void setHighRanges() {
        for (BloodPressureRange normalRange : normalRanges) {
            int startAge = normalRange.getAges().getMinimum();
            int endAge = normalRange.getAges().getMaximum();
            int systolicLow = normalRange.getSystolic().getMaximum() + 1;
            int diastolicLow = normalRange.getDiastolic().getMaximum() + 1;
            int systolicHigh = systolicLow + 100;
            int diastolicHigh = diastolicLow + 100;

            highRanges.add(new BloodPressureRange(
                    Range.between(startAge, endAge),
                    Range.between(systolicLow, systolicHigh),
                    Range.between(diastolicLow, diastolicHigh)
            ));
        }
    }

    public BloodPressureRange getHighRange(int age) {
        return highRanges.stream()
                .filter(range -> range.getAges().contains(age))
                .findFirst()
                .orElse(null);
    }
}
