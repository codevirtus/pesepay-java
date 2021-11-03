package com.codevirtus;

import com.codevirtus.encyrption.PayloadDecryptionContext;
import com.codevirtus.encyrption.PaymentPayloadDecryptionHelper;
import com.codevirtus.payments.*;
import com.codevirtus.response.Response;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.val;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Getter
@Setter
public class Pesepay {
    private final String LIVE_BASE_URL = "https://api.pesepay.com/api/payments-engine";
    private final String TEST_BASE_URL = "https://api.test.pesepay.com/api/payments-engine";
    private final String MAKE_SEAMLESS_PAYMENT_URL = "/v2/payments/make-payment";
    private final String INITIATE_PAYMENT_URL = "/v1/payments/initiate";
    private final String CHECK_PAYMENT_URL = "/v1/payments/check-payment";
    private final ObjectMapper MAPPER = new ObjectMapper().configure( DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    private final String integrationKey;
    private final String encryptionKey;
    private final String baseUrl;
    private String resultUrl;
    private String returnUrl;


    public enum Mode {
        TEST,
        LIVE
    }

    private enum RequestType {
        POST,
        GET
    }

    public Pesepay(String integrationKey, String encryptionKey) {
        this.integrationKey = integrationKey;
        this.encryptionKey = encryptionKey;
        this.baseUrl = LIVE_BASE_URL; // TODO mode == Mode.TEST ? TEST_BASE_URL : LIVE_BASE_URL;
    }

    public Payment createPayment(String currencyCode, String paymentMethodCode, String email) {
        val customer = Customer.builder()
                .email(email)
                .build();

        return Payment.builder()
                .customer(customer)
                .currencyCode(currencyCode)
                .paymentMethodCode(paymentMethodCode)
                .build();
    }

    public Transaction createTransaction(double amount, String currencyCode, String paymentReason) {
        return new Transaction(amount, currencyCode, paymentReason);
    }

    public Transaction createTransaction(double amount, String currencyCode, String paymentReason, String merchantReference) {
        val transaction = createTransaction(amount, currencyCode, paymentReason);
        transaction.setMerchantReference(merchantReference);
        return transaction;
    }

    @SneakyThrows
    public Response initiateTransaction(Transaction transaction) {
        if (this.resultUrl == null)
            throw new IllegalStateException("Result url has not beeen specified");

        if (this.returnUrl == null)
            throw new IllegalStateException("Return url has not been specified");

        transaction.setResultUrl(this.resultUrl);
        transaction.setReturnUrl(this.returnUrl);

        val transactionDetails = TransactionDetailsHolder.buildTransactionDetailsHolder(this.encryptionKey, transaction);

        val url = this.baseUrl + INITIATE_PAYMENT_URL;

        return sendRequest(RequestType.POST, url, transactionDetails);
    }

    @SneakyThrows
    public Response makeSeamlessPayment(Payment payment, String reasonForPayment, double amount, Map<String, String> requiredFields) {
        if (resultUrl == null)
            throw new IllegalStateException("Result url has not beeen specified");

        payment.setResultUrl(this.resultUrl);
        payment.setReturnUrl(this.returnUrl);
        payment.setReasonForPayment(reasonForPayment);
        payment.setPaymentMethodRequiredFields(requiredFields);
        payment.setAmountDetails(new Amount(amount, payment.getCurrencyCode()));

        val transactionDetails = TransactionDetailsHolder.buildTransactionDetailsHolder(this.encryptionKey, payment);

        val url = this.baseUrl + MAKE_SEAMLESS_PAYMENT_URL;
        return sendRequest(RequestType.POST, url, transactionDetails);
    }

    public Response checkPayment(String referenceNumber) {
        val url = String.format("%s%s?referenceNumber=%s", this.baseUrl, CHECK_PAYMENT_URL, referenceNumber);
        return pollTransaction(url);
    }

    public Response pollTransaction(String  pollUrl) {
        val response = sendRequest(RequestType.GET, pollUrl, null);
        if (response.getTransactionStatus() != null) {
            response.setPaid(response.getTransactionStatus().equals("SUCCESS"));
        }
        return response;
    }

    @SneakyThrows
    private <T> Response sendRequest(RequestType requestType, String urlString, T t) {

        val url = new URL(urlString);

        val connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod(requestType.name());
        connection.setRequestProperty("Content-Type", "application/json; utf-8");
        connection.setRequestProperty("Accept", "application/json");
        connection.setRequestProperty("key", this.integrationKey);
        connection.setDoOutput(true);

        if (requestType.equals(RequestType.POST)) {
            val requestBody = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(t);
            try(val os = connection.getOutputStream()) {
                byte[] input = requestBody.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }
        }

        if (connection.getResponseCode() == 200) {
            try(val br = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                val response = parseResponse(br);

                val result = MAPPER.readValue(response, TransactionDetailsHolder.class);

                val decryptionContext = PayloadDecryptionContext.builder()
                    .encryptedData(result.getPayload())
                    .encryptionKey(this.encryptionKey)
                    .build();

                val decryptedResponse = PaymentPayloadDecryptionHelper.decrypt(decryptionContext, Response.class);
                decryptedResponse.setSuccess(true);

                return decryptedResponse;
            }
        } else {
            try(val br = new BufferedReader(new InputStreamReader(connection.getErrorStream(), StandardCharsets.UTF_8))) {
                val response = parseResponse(br);
                return MAPPER.readValue(response, Response.class);
            }
        }
    }

    @SneakyThrows
    private String parseResponse(BufferedReader reader) {
        val response = new StringBuilder();
        String responseLine;
        while ((responseLine = reader.readLine()) != null) {
            response.append(responseLine.trim());
        }

        return response.toString();
    }

}
