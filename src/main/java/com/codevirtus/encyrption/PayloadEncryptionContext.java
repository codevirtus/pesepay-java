package com.codevirtus.encyrption;

import lombok.*;

@Data
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PayloadEncryptionContext {

    private String rawData;

    private String encryptionKey;

}
