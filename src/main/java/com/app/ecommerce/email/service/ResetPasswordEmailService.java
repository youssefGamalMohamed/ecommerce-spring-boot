package com.app.ecommerce.email.service;

import java.io.UnsupportedEncodingException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import com.app.ecommerce.email.model.ResetPasswordMessageDetails;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;


@Service
public class ResetPasswordEmailService {

    @Value("${ecommerce.email}")
    private String ecommerceApplicationEmail;


    @Autowired
    private JavaMailSender mailSender;

    public void sendResetPasswordEmailToUser(ResetPasswordMessageDetails resetPasswordMessageDetails) throws MessagingException, UnsupportedEncodingException {
        String toAddress = resetPasswordMessageDetails.getEmail();
        String fromAddress = ecommerceApplicationEmail;
        String senderName = "E-Commerce Company For Selling Products";
        String subject = "Reset Password Email";
        String content = "Dear [[name]],<br>"
                + "Please click the link below to change your password:<br>"
                + "<h2><a href=\"[[URL]]\" target=\"_self\">LINK</a></h2>"
                + "Thank you,<br>"
                + "E-Commerce Company.";

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message);

        helper.setFrom(fromAddress, senderName);
        helper.setTo(toAddress);
        helper.setSubject(subject);

        content = content.replace("[[name]]", resetPasswordMessageDetails.getFirstName() +
                " " + resetPasswordMessageDetails.getLastName());
        String resetPasswordUrl = "http://localhost:8081/ecommerce/api/v1/auth/reset-password/" + resetPasswordMessageDetails.getForgetPasswordToken();

        content = content.replace("[[URL]]", resetPasswordUrl);

        helper.setText(content, true);

        mailSender.send(message);



    }
}
