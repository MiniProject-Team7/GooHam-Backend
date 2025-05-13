package com.uplus.ureka.controller.user;

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
//        System.out.println("==============================");
//        System.out.println("memberId:" + memberId);

        try {
            if (profileImage.isEmpty() || profileImage.getSize() > MAX_IMAGE_SIZE) {
                throw new IllegalArgumentException("사이즈가 너무 큽니다.");
            }

            // S3에 업로드하고 파일명(또는 URL)을 받아옴
            String uploadedFileName = s3Service.uploadFile(List.of(profileImage)).get(0);

            // 업로드된 파일명을 DB에 저장
            myPageServicelmpl.updateProfileImage(member_id, uploadedFileName);

            return ResponseEntity.ok(new CommonResponseDTO("success", uploadedFileName));

//            String filename = myPageServicelmpl.updateProfileImage(member_id, profileImage);
//            if(filename != null)
//                return ResponseEntity.ok(new CommonResponseDTO("success", filename));
//            else
//                return ResponseEntity.ok(new CommonResponseDTO("fail", ""));
        } catch (Exception e) {
//            return ResponseEntity.badRequest().body("이미지 업로드 문제: " + e.getMessage());
            return ResponseEntity.ok(new CommonResponseDTO("fail", "이미지 업로드 문제: " + e.getMessage()));
        }
    }

    // 회원의 상세 정보를 업데이트하는 로직을 처리
    @PostMapping("/updateInfo")
    public ResponseEntity<CommonResponseDTO> updateMemberInfo(@RequestBody MyPageDTO memberInfo) {
        System.out.println("---------------------------------");
        System.out.println("memberInfo:" + memberInfo);
        myPageServicelmpl.updateMemberInfo(memberInfo);
       //MyPageDTO updatedMember = myPageServicelmpl.updateMemberInfo(memberInfo);
        //return "redirect:/mypage/detail?memberId=" + memberInfo.getMember_email();
        return ResponseEntity.ok(new CommonResponseDTO("success", "회원 정보가 성공적으로 업데이트되었습니다."));
    }

    // 프로필 이미지 조회 (member_email로 검색)
    @GetMapping("/image")
    public ResponseEntity<String> getProfileImage(@RequestParam String id) {
        String imageUrl = myPageServicelmpl.getProfileImageByMemberId(id);
        return ResponseEntity.ok(imageUrl);
    }

    //네비게이션 바 이미지 띄우기
//    @GetMapping("/{memberId}/image")
//    public ResponseEntity<String> getProfileImage(@PathVariable  String memberId) {
//        String imageUrl = myPageServicelmpl.getProfileImageByMemberId(memberId);
//        return ResponseEntity.ok(imageUrl);
//    }

}