package com.caisheng.cheetah.api.connection;

public interface Cipher {
    byte[] encrypt(byte[] data);
    byte[] decrypt(byte[] data);
}
