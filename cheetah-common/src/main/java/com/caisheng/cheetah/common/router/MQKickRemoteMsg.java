package com.caisheng.cheetah.common.router;

public class MQKickRemoteMsg implements KickRemoteMsg{
    private String userId;
    private String deviceId;
    private String connId;
    private int clientType;
    private String targetServer;
    private int targetPort;



    @Override
    public String toString() {
        return "KickRemoteMsg{"
                + "userId='" + userId + '\''
                + ", deviceId='" + deviceId + '\''
                + ", connId='" + connId + '\''
                + ", clientType='" + clientType + '\''
                + ", targetServer='" + targetServer + '\''
                + ", targetPort=" + targetPort
                + '}';
    }

    @Override
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    @Override
    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    @Override
    public String getConnId() {
        return connId;
    }

    public void setConnId(String connId) {
        this.connId = connId;
    }

    @Override
    public int getClientType() {
        return clientType;
    }

    public void setClientType(int clientType) {
        this.clientType = clientType;
    }

    @Override
    public String getTargetServer() {
        return targetServer;
    }

    public void setTargetServer(String targetServer) {
        this.targetServer = targetServer;
    }

    @Override
    public int getTargetPort() {
        return targetPort;
    }

    public void setTargetPort(int targetPort) {
        this.targetPort = targetPort;
    }
}
