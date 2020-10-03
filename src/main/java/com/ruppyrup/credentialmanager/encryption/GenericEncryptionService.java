package com.ruppyrup.credentialmanager.encryption;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import java.lang.reflect.Field;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;
import java.util.function.Consumer;

/**
 * @param <T> is an object with fields that can be encrypted and decrypted
 * fields are annotated with {@Encrypt} mark out the fields to be encrypted and decrypted
 */
@Service
public class GenericEncryptionService<T> implements IEncryptionService<T> {

    @Value("${encryption.key}")
    private String key;

    @Value("${encryption.algorithm}")
    private String algorithm;

    public GenericEncryptionService(String key, String algorithm) {
        this.key = key;
        this.algorithm = algorithm;
    }

    public GenericEncryptionService() {
    }

    @Override
    public T encrypt(T data) {
        Field[] fields = data.getClass().getDeclaredFields();

        Arrays.stream(fields)
                .filter(field -> field.isAnnotationPresent(Encrypt.class))
                .forEach(safeEncryptor(data));

        return data;
    }

    @Override
    public T decrypt(T data) {
        Field[] fields = data.getClass().getDeclaredFields();

        Arrays.stream(fields)
                .filter(field -> field.isAnnotationPresent(Encrypt.class))
                .forEach(safeDecryptor(data));

        return data;
    }

    private Consumer<Field> safeEncryptor(T data) {
        return field -> {
            try {
                field.setAccessible(true);
                field.set(data, encryptString((String) field.get(data)));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        };
    }

    private Consumer<Field> safeDecryptor(T data) {
        return field -> {
            try {
                field.setAccessible(true);
                field.set(data, decryptString((String) field.get(data)));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        };
    }

    private String encryptString(String data) {
        try {
            Cipher cipher = getCipher(Cipher.ENCRYPT_MODE);
            byte[] hasil = cipher.doFinal(data.getBytes("UTF-8"));
            return new String(Base64.getEncoder().encode(hasil));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private String decryptString(String data) {
        try {
            Cipher cipher = getCipher(Cipher.DECRYPT_MODE);
            byte[] hasil = cipher.doFinal(Base64.getDecoder().decode(data));
            return new String(hasil);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private Cipher getCipher(int encryptMode) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException {
        byte[] keyData = key.getBytes();
        SecretKeySpec secretKeySpec = new SecretKeySpec(keyData, algorithm);
        Cipher cipher = Cipher.getInstance(algorithm);
        cipher.init(encryptMode, secretKeySpec);
        return cipher;
    }


}
