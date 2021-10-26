package com.codevirtus.encyrption;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import java.security.NoSuchAlgorithmException;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
class CipherFactory {

    private static final String TRANSFORMATION = "AES/CBC/PKCS5PADDING";

    private static final String ALGORITHM = "AES";

    private static Cipher encryptionCipher;

    private static Cipher decryptionCipher;

    static Cipher getEncryptionCipherInstance() {

        return buildCipher(encryptionCipher);

    }

    static Cipher getDecryptionCipherInstance() {

        return buildCipher(decryptionCipher);

    }

    private static Cipher buildCipher(Cipher cipher) {

        if (cipher == null) {

            try {

                cipher = Cipher.getInstance(TRANSFORMATION);

            } catch (NoSuchAlgorithmException | NoSuchPaddingException ex) {

                throw new IllegalStateException("Failed to encrypt your data");

            }

        }

        return cipher;
    }

    static String getALGORITHM() {
        return ALGORITHM;
    }
}
