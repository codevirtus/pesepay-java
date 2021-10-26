package com.codevirtus.payments;

import lombok.*;

import java.util.Map;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Payment {
    private Customer customer;
    private String currencyCode;
    private String paymentMethodCode;
    private Amount amountDetails;
    private String reasonForPayment;
    private Map<String, String> paymentRequestFields;
    private Map<String, String> paymentMethodRequiredFields;
    private String merchantReference;
    private String returnUrl;
    private String resultUrl;
    private String referenceNumber;
}
