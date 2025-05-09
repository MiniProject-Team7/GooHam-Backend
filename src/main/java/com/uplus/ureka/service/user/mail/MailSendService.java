package com.uplus.ureka.service.user.mail;

import com.uplus.ureka.dto.user.mail.MailHtmlSendDto;
import com.uplus.ureka.dto.user.mail.MailTxtSendDto;
import org.springframework.stereotype.Service;


@Service
public interface MailSendService {
    void sendTxtEmail(MailTxtSendDto mailTxtSendDto);       // SimpleMailMessage를 활용하여 텍스트 기반 메일을 전송합니다.

    void sendHtmlEmail(MailHtmlSendDto mailHtmlSendDto);    // MimeMessageHelper를 활용하여 HTML 기반 메일을 전송합니다.
}