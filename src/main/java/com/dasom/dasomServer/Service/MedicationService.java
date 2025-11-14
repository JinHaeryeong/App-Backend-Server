package com.dasom.dasomServer.Service;

import com.dasom.dasomServer.DAO.UserDAO;
import com.dasom.dasomServer.DTO.SilverMedication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class MedicationService {

    @Autowired
    private UserDAO userDAO;

    public List<SilverMedication> getMedicationsBySilverId(String silverId) {
        // DAO를 호출하여 약물 리스트를 가져옵니다.
        List<SilverMedication> medications = userDAO.findSilverMedicationsBySilverId(silverId);

        // 약물이 등록되어 있는지 확인
        if (medications != null && !medications.isEmpty()) {
            return medications; // 약물 리스트 반환
        } else {
            return Collections.emptyList(); // 등록된 약물이 없으면 빈 리스트 반환
        }
    }
}