package com.codevirtus.payments;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.codevirtus.encyrption.PayloadEncryptionContext;
import com.codevirtus.encyrption.PaymentPayloadEncryptionHelper;
import lombok.Data;
import lombok.val;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.validation.constraints.NotBlank;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;

@Data
public final class TransactionDetailsHolder {

    @NotBlank(message = "Transaction payload details should be provided")
    private String payload;

    public static <T> TransactionDetailsHolder buildTransactionDetailsHolder(String encryptionKey, T target)
            throws JsonProcessingException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException, InvalidKeyException {
        TransactionDetailsHolder transactionDetailsHolderResponse;

        val responseJsonString = new ObjectMapper().writeValueAsString(target);

        val payloadEncryptionContext = PayloadEncryptionContext.builder()
                .encryptionKey(encryptionKey)
                .rawData(responseJsonString)
                .build();

        val encryptedString = PaymentPayloadEncryptionHelper.encrypt(payloadEncryptionContext);

        transactionDetailsHolderResponse = new TransactionDetailsHolder();

        transactionDetailsHolderResponse.setPayload(encryptedString);

        return transactionDetailsHolderResponse;

    }

}
