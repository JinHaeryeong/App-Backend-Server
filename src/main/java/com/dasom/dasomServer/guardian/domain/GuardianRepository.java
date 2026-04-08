package com.dasom.dasomServer.guardian.domain;

import java.util.List;

public interface GuardianRepository {
    List<Guardian> findBySilverLoginId(String silverLoginId);
}