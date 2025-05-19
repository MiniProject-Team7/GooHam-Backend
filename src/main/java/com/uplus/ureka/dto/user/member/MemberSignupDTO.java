package com.uplus.ureka.dto.user.member;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberSignupDTO {
    private Long id;
    private String member_email;
    private String member_password;
    private String member_name;
    private String member_nickname;
    private String member_phone;
    private String member_introduce;
    private String profile_image;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate birth_date;

    // 관심사 카테고리 ID만 따로 포함하는 필드 추가
    private List<Integer> categoryIds;
}