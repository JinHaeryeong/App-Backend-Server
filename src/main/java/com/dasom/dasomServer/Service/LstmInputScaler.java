package com.dasom.dasomServer.Service;

import org.springframework.stereotype.Component;

/**
 * PyTorch 모델 학습 시 사용한 MinMaxScaler 값을 저장하고 정규화를 수행
 */

/*
*
*
* lstm 학습시킬때 나온 거
* ==================================================
// 시계열 데이터 스케일링 파라미터
const seqScalerParams = {
    Heartrate: { min: 34.0, max: 154.0 },
    SPO2: { min: 84.0, max: 100.0 },
    Walking_steps: { min: 0.0, max: 2785.0 },
    Caloricexpenditure: { min: 0.0, max: 1170.0 },
    Sleep_phase_DEEP: { min: 0.0, max: 1.0 },
    Sleep_phase_LIGHT: { min: 0.0, max: 1.0 },
    Sleep_phase_REM: { min: 0.0, max: 1.0 },
    Sleep_phase_AWAKE: { min: 0.0, max: 1.0 },
};

// 정적 데이터 스케일링 파라미터
const staticScalerParams = {
    Age: { min: 60.0, max: 90.0 },
    Gender: { min: 0.0, max: 1.0 },
    RHR: { min: 42.0, max: 76.0 },
};
==================================================
* */
@Component
public class LstmInputScaler {

    // LSTM N_STEPS
    public static final int N_STEPS = 6;

    // SEQ_CONT_FEATURES 정규화 상수 (Heartrate, SPO2, Steps, Calories)
    // Min: 34.0, 84.0, 0.0, 0.0
    // Range: 120.0, 16.0, 2785.0, 1170.0
    private static final double[] SEQ_MINS = {34.0, 84.0, 0.0, 0.0};
    private static final double[] SEQ_RANGES = {120.0, 16.0, 2785.0, 1170.0};

    // STATIC_FEATURE_COLUMNS 정규화 상수 (Age, Gender(0/1), RHR)
    // Min: 60.0, 0.0, 42.0
    // Range: 30.0, 1.0, 34.0
    private static final double[] STATIC_MINS = {60.0, 0.0, 42.0};
    private static final double[] STATIC_RANGES = {30.0, 1.0, 34.0};

    /**
     * 연속적인 시퀀스 특성 (Heartrate, SPO2, Steps, Caloric)을 0-1 범위로 정규화
     */
    public double[] scaleSeqContFeatures(double heartrate, double spo2, int steps, double calories) {
        // 입력 순서: Heartrate, SPO2, Walking_steps, Caloricexpenditure
        double[] features = {heartrate, spo2, (double)steps, calories};
        double[] scaled = new double[features.length];

        for (int i = 0; i < features.length; i++) {
            // 정규화 공식: (X - Min) / Range
            scaled[i] = (features[i] - SEQ_MINS[i]) / SEQ_RANGES[i];
            // 0-1 클리핑 (학습 범위를 벗어나는 값 처리)
            scaled[i] = Math.max(0.0, Math.min(1.0, scaled[i]));
        }
        return scaled;
    }

    /**
     * 정적 특성 (Age, Gender, RHR)을 0-1 범위로 정규화
     */
    public double[] scaleStaticFeatures(int age, int gender, double rhr) {
        // 입력 순서: Age, Gender, RHR
        double[] features = {(double)age, (double)gender, rhr};
        double[] scaled = new double[features.length];

        for (int i = 0; i < features.length; i++) {
            scaled[i] = (features[i] - STATIC_MINS[i]) / STATIC_RANGES[i];
            scaled[i] = Math.max(0.0, Math.min(1.0, scaled[i]));
        }
        return scaled;
    }
}