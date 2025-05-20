package com.uplus.ureka.controller.user;

import com.uplus.ureka.service.user.delete.MemberService_delete;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Controller
@RequestMapping("/gooham/users")
public class MemberController_delete {

    private final MemberService_delete memberServiceDelete;

    @Autowired
    public MemberController_delete(MemberService_delete memberServiceDelete) {
        this.memberServiceDelete = memberServiceDelete;
    }

    @GetMapping("/delete")
    public String deleteAccountPage() {
        return "delete";  // HTML 파일의 이름을 반환
    }

    @PostMapping("/delete_account")
    @ResponseBody
    public ResponseEntity<?> deleteAccount(@RequestBody Map<String, Object> payload) {
        String email = (String) payload.get("email");
        String password = (String) payload.get("password");

        try {
            memberServiceDelete.deleteAccount(email, password);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "계정이 성공적으로 삭제되었습니다."
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.ok(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }
}