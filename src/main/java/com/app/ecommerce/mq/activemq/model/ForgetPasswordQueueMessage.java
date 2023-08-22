package com.app.ecommerce.mq.activemq.model;

import lombok.*;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ForgetPasswordQueueMessage {
    private String firstname;

    private String lastname;

    private String email;

    private String forgetPasswordToken;
}
