package com.codevirtus.payments;

import lombok.*;

import javax.validation.constraints.Email;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Customer {

    @Email(message = "A valid email should be provided")
    private String email;

    private String phoneNumber;

    private String name;

}
