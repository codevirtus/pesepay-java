package com.pesepay;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pesepay.encyrption.PayloadDecryptionContext;
import com.pesepay.encyrption.PaymentPayloadDecryptionHelper;
import com.pesepay.payments.*;
import com.pesepay.response.Response;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.val;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
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

    public Pesepay(String integrationKey, String encryptionKey, Mode mode) {
        this.integrationKey = integrationKey;
        this.encryptionKey = encryptionKey;
        this.baseUrl = mode == Mode.TEST ? TEST_BASE_URL : LIVE_BASE_URL;
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
        response.setPaid(response.getTransactionStatus().equals("SUCCESS"));
        return response;
    }

    @SneakyThrows
    private <T> Response sendRequest(RequestType requestType, String url, T t) {
        HttpClient client = HttpClient.newHttpClient();

        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .headers("Content-Type", "application/json", "key", this.integrationKey)
                .uri(URI.create(url));

        if (requestType == RequestType.POST) {
            val requestBody = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(t);
            requestBuilder.POST(HttpRequest.BodyPublishers.ofString(requestBody));
        }

        HttpResponse<String> httpResponse = client.send(requestBuilder.build(), BodyHandlers.ofString());

        if (httpResponse.statusCode() == 200) {
            val result = MAPPER.readValue(httpResponse.body(), TransactionDetailsHolder.class);

            val decryptionContext = PayloadDecryptionContext.builder()
                    .encryptedData(result.getPayload())
                    .encryptionKey(this.encryptionKey)
                    .build();

            val response = PaymentPayloadDecryptionHelper.decrypt(decryptionContext, Response.class);
            response.setSuccess(true);

            return response;
        }

        return MAPPER.readValue(httpResponse.body(), Response.class);
    }

}
