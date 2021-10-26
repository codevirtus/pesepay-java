package com.codevirtus.payments;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Transaction {

    private final Amount amountDetails;

    private final String reasonForPayment;

    private String resultUrl;

    private String returnUrl;

    private String merchantReference;

    public Transaction(double amount, String currencyCode, String reasonForPayment) {
        this.amountDetails = new Amount(amount, currencyCode);
        this.reasonForPayment = reasonForPayment;
    }
}
