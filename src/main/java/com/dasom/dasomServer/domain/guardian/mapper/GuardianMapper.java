package com.dasom.dasomServer.domain.guardian.mapper;

import com.dasom.dasomServer.domain.guardian.dto.Guardian;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface GuardianMapper {
    List<Guardian> findGuardiansBySilverId(String silverId);
    String findGuardianStoredFilenameByGuardianId(Long guardianId);
}
