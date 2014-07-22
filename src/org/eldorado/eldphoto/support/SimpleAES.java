package org.eldorado.eldphoto.support;

import java.security.spec.KeySpec;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import android.util.Base64;


public class SimpleAES {
    private static final byte[] SALT = {(byte) 0xA9, (byte) 0x9B, (byte) 0xC8, (byte) 0x32, (byte) 0x56, (byte) 0x35, (byte) 0xE3, (byte) 0x03};
    private static final int ITERATION_COUNT = 65536;
    private static final int KEY_LENGTH = 128;
    private static final int IV_LENGTH = 16;
    private Cipher eCipher;
    private Cipher dCipher;
    private byte[] iv;
    private byte[] encrypt;    
    private SecretKey secretKey;

    public SimpleAES(String passPhrase) throws Exception {
        SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        KeySpec keySpec = new PBEKeySpec(passPhrase.toCharArray(), SALT, ITERATION_COUNT, KEY_LENGTH);
        SecretKey secretKeyTemp = secretKeyFactory.generateSecret(keySpec);
        secretKey = new SecretKeySpec(secretKeyTemp.getEncoded(), "AES");

        eCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        eCipher.init(Cipher.ENCRYPT_MODE, secretKey);

    }

    public String encrypt(String encrypt) throws Exception {
        dCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        iv = eCipher.getParameters().getParameterSpec(IvParameterSpec.class).getIV();
        dCipher.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(iv));    	
        byte[] bytes = encrypt.getBytes("UTF8");
        byte[] encrypted = encrypt(bytes);
        byte[] cipherText = new byte[encrypted.length + iv.length];
        System.arraycopy(iv, 0, cipherText, 0, iv.length);
        System.arraycopy(encrypted, 0, cipherText, iv.length, encrypted.length);
        return new String(Base64.encodeToString(cipherText,0));
    }

    public byte[] encrypt(byte[] plain) throws Exception {
        return eCipher.doFinal(plain);
    }


    private byte[] extractIV() {
        byte[] iv = new byte[IV_LENGTH];
        System.arraycopy(encrypt, 0, iv, 0, iv.length);
        return iv;
    }

    public String decrypt(String encryptedString) throws Exception {
    	encrypt = Base64.decode(encryptedString,0);
        byte[] iv2 = extractIV();
        dCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        dCipher.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(iv2));    	
        byte[] bytes = extractCipherText();

        byte[] decrypted = decrypt(bytes);
        return new String(decrypted, "UTF8");
    }

    private byte[] extractCipherText() {
        byte[] ciphertext = new byte[encrypt.length - IV_LENGTH];
        System.arraycopy(encrypt, 16, ciphertext, 0, ciphertext.length);
        return ciphertext;
    }

    public byte[] decrypt(byte[] encrypt) throws Exception {
        return dCipher.doFinal(encrypt);
    }
    
}