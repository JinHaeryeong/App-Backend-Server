package com.dasom.dasomServer.DTO;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDate;

@Data
public class User {
    private Long id;
    private Integer caregiverId;
    private String loginId;
    private String password;
    private String username;
    private String gender;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate birthday;
    private String created_at;
}