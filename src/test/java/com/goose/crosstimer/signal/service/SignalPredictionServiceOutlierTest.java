package com.goose.crosstimer.signal.service;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SignalPredictionServiceOutlierTest {

    @Test
    void testRemoveOutliers_withOutliers() throws Exception {
        //Given & When
        //100.0은 제거 되야함
        List<Double> input = Arrays.asList(10.0, 12.0, 11.0, 100.0, 9.0);

        //private 메서드라 리플랙션 사용
        Method method = SignalPredictionService.class.getDeclaredMethod("removeOutliers", List.class);
        method.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<Double> result = (List<Double>) method.invoke(new SignalPredictionService(null, null, null, null), input);

        //Then
        assertThat(result).containsExactlyInAnyOrder(10.0, 12.0, 11.0, 9.0);
    }

    @Test
    void testRemoveOutliers_emptyList() throws Exception {
        //Given & When
        List<Double> input = List.of();
        Method method = SignalPredictionService.class.getDeclaredMethod("removeOutliers", List.class);
        method.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<Double> result = (List<Double>) method.invoke(new SignalPredictionService(null, null, null, null), input);

        //Then
        assertThat(result).isEmpty();
    }

    @Test
    void testRemoveOutliers_allWithinRange() throws Exception {
        //Given & When
        List<Double> input = Arrays.asList(5.0, 7.0, 8.0);
        Method method = SignalPredictionService.class.getDeclaredMethod("removeOutliers", List.class);
        method.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<Double> result = (List<Double>) method.invoke(new SignalPredictionService(null, null, null, null), input);

        //Then
        assertThat(result).containsExactlyInAnyOrder(5.0, 7.0, 8.0);
    }

}