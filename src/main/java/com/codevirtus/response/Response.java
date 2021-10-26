package com.codevirtus.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Response {
    private boolean success;
    private String message;
    private String referenceNumber;
    private String pollUrl;
    private String redirectUrl;
    private boolean paid;
    private String transactionStatus;

    public boolean paid() {
        return this.isPaid();
    }
}
