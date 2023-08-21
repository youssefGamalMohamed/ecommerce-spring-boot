package com.app.ecommerce.email.service;

import com.app.ecommerce.email.model.VerificationRegistrationMessageDetails;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;


@Service
public class VerificationRegistrationEmailService {

    @Value("${ecommerce.email}")
    private String ecommerceApplicationEmail;


    @Autowired
    private JavaMailSender mailSender;

    public void sendVerificationEmailToRegisteredUser(VerificationRegistrationMessageDetails verificationRegistrationMessageDetails) throws MessagingException, UnsupportedEncodingException {
        String toAddress = verificationRegistrationMessageDetails.getEmail();
        String fromAddress = ecommerceApplicationEmail;
        String senderName = "E-Commerce Company For Selling Products";
        String subject = "Please verify your registration";
        String content = "Dear [[name]],<br>"
                + "Please click the link below to verify your registration:<br>"
                + "<h2><a href=\"[[URL]]\" target=\"_self\">VERIFY</a></h2>"
                + "Thank you,<br>"
                + "Your company name.";

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message);

        helper.setFrom(fromAddress, senderName);
        helper.setTo(toAddress);
        helper.setSubject(subject);

        content = content.replace("[[name]]", verificationRegistrationMessageDetails.getFirstName() +
                " " + verificationRegistrationMessageDetails.getLastName());
        String verifyURL = "http://localhost:8081/ecommerce/api/v1/auth/verify-registration/" + verificationRegistrationMessageDetails.getVerificationToken();

        content = content.replace("[[URL]]", verifyURL);

        helper.setText(content, true);

        mailSender.send(message);
    }
}
