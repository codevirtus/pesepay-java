package com.codevirtus.encyrption;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.val;
import org.apache.commons.codec.binary.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;


@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PaymentPayloadDecryptionHelper {

    public static <T> T decrypt(PayloadDecryptionContext payloadDecryptionContext, Class<T> tClass)
            throws InvalidAlgorithmParameterException, InvalidKeyException, BadPaddingException,
            IllegalBlockSizeException, IOException {

        val key = payloadDecryptionContext.getEncryptionKey();

        val initializingVector = key.substring(0, 16);

        val target = payloadDecryptionContext.getEncryptedData();

        IvParameterSpec iv = new IvParameterSpec(initializingVector.getBytes(StandardCharsets.UTF_8));

        SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), CipherFactory.getALGORITHM());

        Cipher cipher = CipherFactory.getDecryptionCipherInstance();

        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, iv);

        byte[] decryptedPayload = cipher.doFinal(Base64.decodeBase64(target));

        return new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .readValue(decryptedPayload, tClass);
    }
}
