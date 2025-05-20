package com.uplus.ureka.controller.user;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.uplus.ureka.dto.user.Mypage.CommonResponseDTO;
import com.uplus.ureka.dto.user.Mypage.MyPageDTO;
import com.uplus.ureka.service.user.mypage.MyPageServicelmpl;
import io.swagger.v3.oas.annotations.media.Content;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.uplus.ureka.service.s3.S3Service;
import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@Controller
@RestController
@RequestMapping("/gooham/users/mypage")
public class MyPageController {

    @Autowired
    private MyPageServicelmpl myPageServicelmpl;
    private final S3Service s3Service;

    // 이미지 사이즈 제한을 위함
    private static final long MAX_IMAGE_SIZE = 5 * 1024 * 1024; // 5MB

    //회원 상세 페이지
    // memberId 파라미터를 받아 해당 회원의 상세 정보 페이지를 반환
    @GetMapping("/detail")
    public ResponseEntity<MyPageDTO> selectMemberDetailsById(@RequestParam String member_email){
        System.out.println("=================================");
        System.out.println("member_email:" + member_email);
        // 서비스를 통해 회원의 상세 정보를 가져옴
        MyPageDTO memberDetails = myPageServicelmpl.getMemberDetails(member_email);
        return ResponseEntity.ok(memberDetails);
    }


    // 회원의 프로필 이미지를 업로드하는 로직
    @PostMapping(value = "/uploadProfileImage", consumes = "multipart/form-data")
    public ResponseEntity<CommonResponseDTO> uploadProfileImage(
            @RequestParam String member_id,
            @Parameter
            @RequestParam("profileImage")  MultipartFile profileImage) {

        try {
            if (profileImage.isEmpty() || profileImage.getSize() > MAX_IMAGE_SIZE) {
                throw new IllegalArgumentException("사이즈가 너무 큽니다.");
            }

            // S3에 "user/" 경로로 업로드하고 파일명(또는 URL)을 받아옴
            String uploadedFileNameJson = s3Service.uploadFile(List.of(profileImage), "user");
            ObjectMapper objectMapper = new ObjectMapper();
            List<String> uploadedFileNameList = objectMapper.readValue(uploadedFileNameJson, new TypeReference<List<String>>() {});
            String uploadedFileName = uploadedFileNameList.get(0);

            // 업로드된 파일명을 DB에 저장
            myPageServicelmpl.updateProfileImage(member_id, uploadedFileName);

            return ResponseEntity.ok(new CommonResponseDTO("success", uploadedFileName));

        } catch (Exception e) {
            return ResponseEntity.ok(new CommonResponseDTO("fail", "이미지 업로드 문제: " + e.getMessage()));
        }
    }

    // 회원의 상세 정보를 업데이트하는 로직을 처리
    @PostMapping("/updateInfo")
    public ResponseEntity<CommonResponseDTO> updateMemberInfo(@RequestBody MyPageDTO memberInfo) {
        System.out.println("---------------------------------");
        System.out.println("memberInfo:" + memberInfo);
        myPageServicelmpl.updateMemberInfo(memberInfo);
        return ResponseEntity.ok(new CommonResponseDTO("success", "회원 정보가 성공적으로 업데이트되었습니다."));
    }

    // 프로필 이미지 조회 (member_email로 검색)
    @GetMapping("/image")
    public ResponseEntity<String> getProfileImage(@RequestParam String id) {
        String imageUrl = myPageServicelmpl.getProfileImageByMemberId(id);
        return ResponseEntity.ok(imageUrl);
    }


}