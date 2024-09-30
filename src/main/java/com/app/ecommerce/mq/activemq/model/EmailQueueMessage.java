package com.app.ecommerce.mq.activemq.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EmailQueueMessage {

    private String firstname;

    private String lastname;

    private String email;

    private String verificationToken;
}
