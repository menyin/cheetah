package com.caisheng.cheetah.common.router;

import java.util.Arrays;

public enum ClientType {
    MOBILE((byte) 1, "android", "ios"),
    PC((byte)2, "windows", "mac","linux"),
    WEB((byte)3, "web", "h5"),
    UNKNOWN((byte)-1);

    private byte type;
    private String[] os;

    ClientType(byte type, String ... os) {
        this.type = type;
        this.os = os;
    }

    public boolean contains(String osName){
        return Arrays.asList(this.os).stream().anyMatch(osName::contains);
    }

    public static ClientType find(String osName){
        for (ClientType clientType : values()) {
            if (clientType.contains(osName)) {
                return clientType;
            }
        }
        return UNKNOWN;
    }


    public byte getType() {
        return type;
    }

    public void setType(byte type) {
        this.type = type;
    }

    public String[] getOs() {
        return os;
    }

    public void setOs(String[] os) {
        this.os = os;
    }
}
