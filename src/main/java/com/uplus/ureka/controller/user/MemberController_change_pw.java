package com.uplus.ureka.controller.user;

import com.uplus.ureka.service.user.change.MemberService_change_pw;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/gooham/users")
public class MemberController_change_pw {

    private final MemberService_change_pw memberServiceChangePw;

    @Autowired
    public MemberController_change_pw(MemberService_change_pw memberServiceChangePw) {
        this.memberServiceChangePw = memberServiceChangePw;
    }

    @PostMapping("/change_password")
    @ResponseBody
    public Map<String, String> changePassword(@RequestBody Map<String, Object> payload) {
        String email = (String) payload.get("member_email");
        String newPassword = (String) payload.get("newPassword");

        Map<String, String> response = new HashMap<>();
        response.put("message", memberServiceChangePw.changePassword(email, newPassword));
        System.out.println(response);
        return response;
    }
}