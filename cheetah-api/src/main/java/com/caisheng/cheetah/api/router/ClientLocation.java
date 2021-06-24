package com.caisheng.cheetah.api.router;

import com.caisheng.cheetah.api.connection.Connection;
import com.caisheng.cheetah.api.connection.SessionContext;

/**
 * cny_note 推送目标骑手客户端的ip+port等信息聚合
 */
public final class ClientLocation {
    /**
     * 长链接所在的机器IP
     */
    private String host;

    /**
     * 长链接所在的机器端口
     */
    private int port;

    /**
     * 客户端系统类型
     */
    private String osName;

    /**
     * 客户端版本
     */
    private String clientVersion;

    /**
     * 客户端设备ID
     */
    private String deviceId;

    /**
     * 链接ID
     */
    private String connId;

    /**
     * 客户端类型
     */
    private transient int clientType;


    public boolean isOnline() {
        return connId != null;
    }

    public boolean isOffline() {
        return connId == null;
    }

    public ClientLocation offline() {
        this.connId = null;
        return this;
    }

    public boolean isThisMachine(String host, int port) {
        return this.port == port && this.host.equals(host);
    }

    public String getHostAndPort() {
        return host + ":" + port;
    }
    public static ClientLocation from(Connection connection) {
        SessionContext context = connection.getSessionConext();
        ClientLocation location = new ClientLocation();
        location.osName = context.getOsName();
        location.clientVersion = context.getClientVersion();
        location.deviceId = context.getDeviceId();
        location.connId = connection.getId();
        return location;
    }

    public String toJson() {
        return "{"
                + "\"port\":" + port
                + (host == null ? "" : ",\"host\":\"" + host + "\"")
                + (deviceId == null ? "" : ",\"deviceId\":\"" + deviceId + "\"")
                + (osName == null ? "" : ",\"osName\":\"" + osName + "\"")
                + (clientVersion == null ? "" : ",\"clientVersion\":\"" + clientVersion + "\"")
                + (connId == null ? "" : ",\"connId\":\"" + connId + "\"")
                + "}";

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ClientLocation location = (ClientLocation) o;

        return clientType == location.clientType;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(clientType);
    }

    @Override
    public String toString() {
        return "ClientLocation{" +
                "host='" + host + ":" + port + "\'" +
                ", osName='" + osName + '\'' +
                ", clientVersion='" + clientVersion + '\'' +
                ", deviceId='" + deviceId + '\'' +
                ", connId='" + connId + '\'' +
                '}';
    }


    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getOsName() {
        return osName;
    }

    public void setOsName(String osName) {
        this.osName = osName;
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

    public String getConnId() {
        return connId;
    }

    public void setConnId(String connId) {
        this.connId = connId;
    }

    public int getClientType() {
        if (clientType == 0) {
            clientType = ClientClassifier.I.getClientType(osName);
        }
        return clientType;
    }


}
