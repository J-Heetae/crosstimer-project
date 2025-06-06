package com.goose.crosstimer.api.service;

import com.goose.crosstimer.crossroad.domain.Crossroad;
import com.goose.crosstimer.signal.domain.SignalInfo;
import com.goose.crosstimer.crossroad.repository.CrossroadRepository;
import com.goose.crosstimer.signal.repository.SignalInfoRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.*;

@SpringBootTest
class TDataBatchServiceTest {

    @Autowired
    private CrossroadRepository crossroadRepository;
    @Autowired
    private SignalInfoRepository signalInfoRepository;
    
    @Test
    void 교차로_신호_일치_테스트() {
        List<Crossroad> crossroadList = crossroadRepository.findAll();
        List<SignalInfo> signalInfoList = signalInfoRepository.findAll();

        Map<Integer, SignalInfo> signalMap = new HashMap<>();
        for (SignalInfo s : signalInfoList) {
            signalMap.put(s.getItstId(), s);
        }
        
        int count = 0;
        for (Crossroad c : crossroadList) {
            int currItstId = c.getItstId();
            if(signalMap.containsKey(currItstId)) {
                count++;
                System.out.println(signalMap.get(currItstId));
            }
        }
        System.out.println(count);
    }

    @Test
    void 신호상태_타입_확인_테스트() {
        List<SignalInfo> signalInfoList = signalInfoRepository.findAll();
        Set<String> statement = new HashSet<>();

        for (SignalInfo s : signalInfoList) {
            statement.add(s.getNtPdsgStatNm());
            statement.add(s.getEtPdsgStatNm());
            statement.add(s.getStPdsgStatNm());
            statement.add(s.getWtPdsgStatNm());
            statement.add(s.getNePdsgStatNm());
            statement.add(s.getNwPdsgStatNm());
            statement.add(s.getSePdsgStatNm());
            statement.add(s.getSwPdsgStatNm());
        }

        System.out.println("size = " + statement.size());
        for (String s : statement) {
            System.out.println(s);
        }
    }
}