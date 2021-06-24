package com.caisheng.cheetah.common.security;

import com.caisheng.cheetah.api.connection.Cipher;
import com.caisheng.cheetah.tools.crypto.AESUtils;

import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class AesCipher implements Cipher {
    private final byte[] key;
    private final byte[] iv;
    private final IvParameterSpec zeroIv;
    private final SecretKeySpec keySpec;

    public AesCipher(byte[] key, byte[] iv) {
        this.key = key;
        this.iv = iv;
        this.zeroIv = new IvParameterSpec(iv);
        this.keySpec = new SecretKeySpec(key, AESUtils.KEY_ALGORITHM);
    }


    @Override
    public byte[] encrypt(byte[] data) {
        return AESUtils.encrypt(data, zeroIv, keySpec);
    }

    @Override
    public byte[] decrypt(byte[] data) {
        return AESUtils.decrypt(data, zeroIv, keySpec);
    }

    public byte[] getKey() {
        return key;
    }

    public byte[] getIv() {
        return iv;
    }
    @Override
    public String toString() {
        return toString(key) + ',' + toString(iv);
    }

    public String toString(byte[] a) {
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < a.length; i++) {
            if (i != 0) b.append('|');
            b.append(a[i]);
        }
        return b.toString();
    }

    public static byte[] toArray(String str) {
        String[] a = str.split("\\|");
        if (a.length != CipherBox.I.getAesKeyLength()) {
            return null;
        }
        byte[] bytes = new byte[a.length];
        for (int i = 0; i < a.length; i++) {
            bytes[i] = Byte.parseByte(a[i]);
        }
        return bytes;
    }
}
