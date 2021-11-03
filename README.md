### Installation
To use the Java Pesepay SDK, you need to add it as a dependency to your project. The release version will be in the Maven Central Repository.
#### Maven
```xml
<dependency>
    <groupId>com.pesepay</groupId>
    <artifactId>pesepay</artifactId>
    <version>1.0.0</version>
</dependency>
```
#### Gradle
```properties
repositories {
    mavenCentral()
}

dependencies {
    implementation 'com.pesepay:pesepay:1.0.0'
}
```
### Getting Started
Create an instance of the `Pesepay` class using your integration key and encryption key as supplied by Pesepay.

```java 
Pesepay pesepay = new Pesepay(integrationKey, encryptionKey);
```

Set return and result urls

```java 
pesepay.setResultUrl("http://example.com/gateway/return");
pesepay.setReturnUrl("http://example.com/gateway/return");
```

### Make seamless payment

Create the payment 
##### NB: Customer email or number should be provided

```java
Payment payment = pesepay.createPayment("CURRENCY_CODE", "PAYMENT_METHOD_CODE", "CUSTOMER_EMAIL");
```

Create a `Map` of the required fields (if any)
```java
Map<String, String> requiredFields = new HashMap<>();
requiredFields.put("requiredFieldKey", "requiredFieldValue");
```

Send of the payment
```java
Response response = pesepay.makeSeamlessPayment(payment, "Online Payment", 1.0, requiredFields);

if (response.isSuccess()) {
    // Save the reference number and/or poll url (used to check the status of a transaction)
    String pollUrl = response.getPollUrl();
    String referenceNumber = response.getReferenceNumber();

} else  {
    // Get Error Message
    String errorMessage = response.getMessage();
}
```

### Make redirect payment

Create a transaction
```java
Transaction transaction = pesepay.createTransaction(AMOUNT, "CURRENCY_CODE", "PAYMENT_REASON");
```

Initiate the transaction
```java
Response response = pesepay.initiateTransaction(transaction);

if (response.isSuccess()) {
    // Save the reference number and/or poll url (used to check the status of a transaction)
    String referenceNumber = response.getReferenceNumber();
    String pollUrl = response.getPollUrl();

    // Get the redirect url and redirect user to complete transaction 
    String redirectUrl = response.getRedirectUrl();

} else  {
    // Get Error Message
    String errorMessage = response.getMessage();
}
```

### Check Payment Status
#### Method 1: Using referenceNumber
```java
Response response = pesepay.checkPayment("REFERENCE_NUMBER");

if (response.isSuccess()) {
    if (response.paid()) {
        // Payment was successful
    }
} else {
    // Get Error Message
    String errorMessage = response.getMessage();
}
```
#### Method 2: Using poll url
```java
Response response = pesepay.pollTransaction("POLL_URL");

if (response.isSuccess()) {
    if (response.paid()) {
        // Payment was successful
    }
} else {
    // Get Error Message
    String errorMessage = response.getMessage();
}
```