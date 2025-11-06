package com.dasom.dasomServer.DTO;

import lombok.Data;
import java.sql.Timestamp;
import java.util.List;

@Data
public class Guardian {
    private Long id; // g_id
    private String silverId; // g_silver_id (N:M 관계에서 사용되지 않을 수 있지만, 1:N 유산을 위해 남겨둠)
    private String name; // g_name
    private String tel; // g_tel
    private String relationship; // g_relationship
    private String address; // g_address
    private Timestamp createdAt; // g_created_at

    // GuardianImage와의 1:N 관계 매핑을 위한 리스트
    private List<GuardianImage> images;
}