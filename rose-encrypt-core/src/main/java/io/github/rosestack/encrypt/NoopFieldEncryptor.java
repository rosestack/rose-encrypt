package io.github.rosestack.encrypt;

import io.github.rosestack.encrypt.enums.EncryptType;

public class NoopFieldEncryptor implements FieldEncryptor {
    @Override
    public String encrypt(String plainText, EncryptType encryptType) {
        return plainText;
    }

    @Override
    public String decrypt(String cipherText, EncryptType encryptType) {
        return cipherText;
    }
}
