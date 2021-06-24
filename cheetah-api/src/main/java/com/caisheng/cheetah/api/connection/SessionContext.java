package com.caisheng.cheetah.api.connection;


import com.caisheng.cheetah.api.router.ClientClassifier;

public class SessionContext {
    private String osName;
    private String osVersion;
    private String clientVersion;
    private String deviceId;
    private String userId;
    private String tags;
    private int heartbeat = 10000;//ms
    private Cipher cipher;
    transient private byte clientType;//通过分类器对osName进行分类可以得到

    /**
     * 是否握手成功
     * @return
     */
    public boolean handshakeOk() {
        return this.deviceId != null && this.deviceId.length() > 0;
    }
    public boolean isSecurity(){return cipher!=null;}

    @Override
    public String toString() {
        if (userId == null && deviceId == null) {
            return "";
        }
        return "{osName="+this.osName+
        "osVersion="+this.osVersion+
        "clientVersion="+this.clientVersion+
        "deviceId="+this.deviceId+
        "userId="+this.userId+
        "tags="+this.tags+
        "heartbeat ="+this.heartbeat +"}";

    }

    public String getOsName() {
        return osName;
    }

    public void setOsName(String osName) {
        this.osName = osName;
    }

    public String getOsVersion() {
        return osVersion;
    }

    public void setOsVersion(String osVersion) {
        this.osVersion = osVersion;
    }

    public String getClientVersion() {
        return clientVersion;
    }

    public void setClientVersion(String clientVersion) {
        this.clientVersion = clientVersion;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public int getHeartbeat() {
        return heartbeat;
    }

    public void setHeartbeat(int heartbeat) {
        this.heartbeat = heartbeat;
    }

    public Cipher getCipher() {
        return cipher;
    }

    public void setCipher(Cipher cipher) {
        this.cipher = cipher;
    }

    public byte getClientType() {
        if (this.clientType == 0) {//cny_note byte成员变量初始化值为0
            this.clientType = ClientClassifier.I.getClientType(this.osName);
        }
        return this.clientType;
    }
}
