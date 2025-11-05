package com.dasom.dasomServer.Service;

import com.dasom.dasomServer.DAO.UserDAO;
import com.dasom.dasomServer.DTO.Guardian;
import com.dasom.dasomServer.DTO.GuardianResponseDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class GuardianService {

    private final UserDAO userDAO;

    @Autowired
    public GuardianService(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    public List<GuardianResponseDTO> getGuardiansForApp(String silverId) {

        // 1. DAO를 통해 DB에서 전체 Guardian 정보 리스트를 가져옵니다.
        List<Guardian> guardians = userDAO.findGuardiansBySilverId(silverId);

        // 2. 이 리스트를 앱에서 필요한 4개 필드만 가진 DTO 리스트로 변환합니다.
        return guardians.stream()
                .map(guardian -> new GuardianResponseDTO(
                        guardian.getName(),
                        guardian.getTel(),
                        guardian.getRelationship(),
                        guardian.getAddress()
                ))
                .collect(Collectors.toList());
    }
}