package com.app.ecommerce.email.service;

import com.app.ecommerce.email.model.GreetingNewRegisteredUserMessageDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;


@Service
public class GreetingNewRegisteredUserEmailService {

    @Value("${ecommerce.email}")
    private String ecommerceApplicationEmail;

    @Autowired
    private EmailService emailService;

    public void sendGreetingMessageToNewRegisteredUser(GreetingNewRegisteredUserMessageDetails greetingMessageDetails) {

        String subject = greetingMessageDetails.getFirstName()
                + " " + greetingMessageDetails.getLastName()
                + ", welcome to EcommerceApplication!";

        String body =  greetingMessageDetails.getFirstName()
                + " " + greetingMessageDetails.getLastName()
                + ", welcome to EcommerceApplication!\r\n" + "\r\n"
                + "Browse your favorite books, our editorial picks, bestsellers, or customer favorites.\r\n"
                + "\r\n";

        emailService.sendEmail(ecommerceApplicationEmail
                , greetingMessageDetails.getEmail() ,
                subject
                , body
        );
    }
}
