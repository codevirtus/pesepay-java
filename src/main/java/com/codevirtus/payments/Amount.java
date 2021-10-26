package com.codevirtus.payments;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class Amount {

    private double amount;

    private String currencyCode;

}
