package com.app.ecommerce.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Entity
@SuperBuilder
@AllArgsConstructor
@Getter
@Setter
@DiscriminatorValue("ADMIN")
public class Admin extends User {

}
