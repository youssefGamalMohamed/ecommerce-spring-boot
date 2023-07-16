package com.app.ecommerce.mq.activemq.model;

import lombok.*;

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

}
