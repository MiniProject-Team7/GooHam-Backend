package com.uplus.ureka.dto.user.member;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberSignupDTO {
    private String member_email;
    private String member_password;
    private String member_name;
    private String member_nickname;
    private String member_phone;
    private String member_introduce;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate birth_date;
}