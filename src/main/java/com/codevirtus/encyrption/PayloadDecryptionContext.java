package com.codevirtus.encyrption;

import lombok.*;

@Data
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PayloadDecryptionContext {

    private String encryptedData;

    private String encryptionKey;

}
