package com.goose.crosstimer.signal.dto;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

/**
 * Holt–Winters 삼중 지수 평활 모델 (Additive 계절성)
 */
@Getter
public class HoltWinters {
    private final double alpha;
    private final double beta;
    private final double gamma;
    private final int seasonLength;

    private List<Double> data;
    private double[] level;
    private double[] trend;
    private double[] seasonal;

    public HoltWinters(double[] initData, int seasonLength,
                       double alpha, double beta, double gamma) {
        this.alpha = alpha;
        this.beta = beta;
        this.gamma = gamma;
        this.seasonLength = seasonLength;
        this.data = new ArrayList<>();
        for (double d : initData) this.data.add(d);
        initialize();
        fitModel();
    }

    private void initialize() {
        int n = data.size();
        level = new double[n];
        trend = new double[n];
        seasonal = new double[n];

        // 초기 level
        double sum = 0;
        for (int i = 0; i < Math.min(seasonLength, n); i++) sum += data.get(i);
        level[0] = sum / Math.min(seasonLength, n);
        // 초기 trend
        if (n > seasonLength) {
            double sumTrend = 0;
            for (int i = 0; i < seasonLength; i++) {
                sumTrend += (data.get(i + seasonLength) - data.get(i));
            }
            trend[0] = sumTrend / (seasonLength * seasonLength);
        } else {
            trend[0] = 0;
        }
        // 초기 seasonal
        for (int i = 0; i < n; i++) {
            seasonal[i] = (i < seasonLength) ? data.get(i) - level[0] : 0;
        }
    }

    private void fitModel() {
        int n = data.size();
        for (int t = 1; t < n; t++) {
            double lastLevel = level[t - 1];
            double lastTrend = trend[t - 1];
            double lastSeason = (t - seasonLength >= 0) ? seasonal[t - seasonLength] : 0;
            level[t] = alpha * (data.get(t) - lastSeason) + (1 - alpha) * (lastLevel + lastTrend);
            trend[t] = beta * (level[t] - lastLevel) + (1 - beta) * lastTrend;
            seasonal[t] = gamma * (data.get(t) - level[t]) + (1 - gamma) * lastSeason;
        }
    }

    public void update(double[] newData) {
        for (double d : newData) data.add(d);
        initialize();
        fitModel();
    }

    public double[] forecast(int k) {
        int n = data.size();
        double[] result = new double[k];
        // not enough history -> return zeros or safe default
        if (n < 2) {
            for (int i = 0; i < k; i++) result[i] = data.isEmpty() ? 0 : data.get(data.size() - 1);
            return result;
        }
        for (int i = 1; i <= k; i++) {
            double m = i;
            double lastLevel = level[n - 1];
            double lastTrend = trend[n - 1];
            double seasonalIdx = seasonal[(n - seasonLength + (i % seasonLength) + seasonLength) % n];
            result[i - 1] = lastLevel + lastTrend * m + seasonalIdx;
        }
        return result;
    }

    public int getDataLength() {
        return data.size();
    }
}
