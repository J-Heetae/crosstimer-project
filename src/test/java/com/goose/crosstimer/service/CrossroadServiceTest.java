package com.goose.crosstimer.service;

import com.goose.crosstimer.crossroad.service.CrossroadService;
import com.goose.crosstimer.crossroad.domain.Crossroad;
import com.goose.crosstimer.crossroad.dto.CrossroadRangeRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
class CrossroadServiceTest {

    @Autowired
    private CrossroadService crossroadService;

    @Test
    void 범위내의_교차로_가져오기() {
        double swLat = 37.5143933;
        double swLon = 126.829315;
        double neLat = 37.5212011;
        double neLon = 126.8378981;

        List<Crossroad> result = crossroadService.getCrossroadsInRange(new CrossroadRangeRequest(
                swLat, swLon, neLat, neLon
        ));

        for (Crossroad c : result) {
            System.out.println(c);
        }
    }
}