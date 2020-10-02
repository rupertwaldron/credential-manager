package com.ruppyrup.credentialmanager.encryption;

public interface IEncryptionService<T> {
    T encrypt(T data);

    T decrypt(T data);
}
