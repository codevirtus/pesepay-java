package com.pesepay.encyrption;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.val;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.util.Base64;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PaymentPayloadEncryptionHelper {

    public static String encrypt(PayloadEncryptionContext payloadEncryptionContext) throws InvalidAlgorithmParameterException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {

        val key = payloadEncryptionContext.getEncryptionKey();

        val initializingVector = key.substring(0, 16);

        val target = payloadEncryptionContext.getRawData();

        IvParameterSpec iv = new IvParameterSpec(initializingVector.getBytes(StandardCharsets.UTF_8));

        SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), CipherFactory.getALGORITHM());

        Cipher cipher = CipherFactory.getEncryptionCipherInstance();

        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, iv);

        byte[] encrypted = cipher.doFinal(target.getBytes());

        return Base64.getEncoder().encodeToString(encrypted);

    }
}
