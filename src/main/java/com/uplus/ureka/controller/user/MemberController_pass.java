package com.uplus.ureka.controller.user;

import com.uplus.ureka.service.user.mail.Impl.MailSendServiceImpl;
import com.uplus.ureka.service.user.password.MemberService_pass;
import com.uplus.ureka.dto.user.mail.MailTxtSendDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Random;

@Controller
@RequestMapping("/gooham/users")
public class MemberController_pass {

    private final MemberService_pass memberServicePass;
    private final MailSendServiceImpl mailSendService; // 이메일 서비스 주입

    @Autowired
    public MemberController_pass(MemberService_pass memberServicePass, MailSendServiceImpl mailSendService) {
        this.memberServicePass = memberServicePass;
        this.mailSendService = mailSendService;
    }

    @PostMapping("/generateCode")
    @ResponseBody
    public String generateCode(@RequestBody Map<String, Object> payload) {
        String email = (String) payload.get("email");

        // 이메일 존재 여부 및 삭제 여부 확인
        if (email == null || !memberServicePass.checkEmailExists(email)) {
            return "없는 계정의 이메일입니다.";
        } else if (memberServicePass.checkUserStatus(email) == 1) {
            return "이미 삭제된 계정입니다.";
        }

        // 6자리 랜덤 인증번호 생성
        int codeNumber = new Random().nextInt(900000) + 100000;

        // DB에 인증번호 저장
        memberServicePass.updateCode(email, codeNumber);

        // 이메일 전송
        MailTxtSendDto mailDto = new MailTxtSendDto();
        mailDto.setEmailAddr(email);
        mailDto.setSubject("GooHam 비밀번호 찾기 인증코드");
        mailDto.setContent("회원님의 인증번호는: " + codeNumber + " 입니다.");

        mailSendService.sendTxtEmail(mailDto);

        return "인증번호가 회원님의 이메일로 발송되었습니다.";
    }

    @PostMapping("/verifyCode")
    @ResponseBody
    public String verifyCode(@RequestBody Map<String, Object> payload) {
        String email = (String) payload.get("email");

        Object codeObj = payload.get("code");
        if (email == null || codeObj == null) {
            return "이메일 또는 인증번호가 누락되었습니다.";
        }

        int code;
        try {
            code = Integer.parseInt(codeObj.toString());
        } catch (NumberFormatException e) {
            return "인증번호 형식이 잘못되었습니다.";
        }

        return memberServicePass.verifyCodeAndGetPassword(email, code);
    }

}
