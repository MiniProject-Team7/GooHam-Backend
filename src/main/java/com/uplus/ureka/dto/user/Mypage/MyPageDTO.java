package com.uplus.ureka.dto.user.Mypage;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class MyPageDTO {

    private String id; // 식별 번호

    private String member_email; //이메일

    private String member_name; //이름

    private String member_nickname;     //닉네임

    private String profile_image;    //프로필사진

    private String birth_date;  // 생일

    //private String category_name; //카테고리 이름

    private String member_introduce; //멤버 소개

    private String member_phone;    // 전화번호
    private String created_at;      // 생성시간
    private String updated_at;      // 업데이트 시간

    private List<InterestDTO> interests;  // 관심사 리스트 추가

    // 관심사 카테고리 ID만 따로 포함하는 필드 추가
    private List<Integer> categoryIds;

}