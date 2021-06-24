package com.caisheng.cheetah.core.handler;

import com.caisheng.cheetah.api.connection.Connection;
import com.caisheng.cheetah.api.connection.SessionContext;
import com.caisheng.cheetah.api.protocol.Command;
import com.caisheng.cheetah.api.protocol.Packet;
import com.caisheng.cheetah.common.ErrorCode;
import com.caisheng.cheetah.common.handler.BaseMessageHandler;
import com.caisheng.cheetah.common.message.ErrorMessage;
import com.caisheng.cheetah.common.message.HandshakeMessage;
import com.caisheng.cheetah.common.message.HandshakeOkMessage;
import com.caisheng.cheetah.common.security.AesCipher;
import com.caisheng.cheetah.common.security.CipherBox;
import com.caisheng.cheetah.core.CheetahServer;
import com.caisheng.cheetah.core.session.ReusableSessionManager;
import com.caisheng.cheetah.core.session.ReusableSession;
import com.caisheng.cheetah.tools.config.ConfigTools;
import com.caisheng.cheetah.tools.log.Logs;
import org.apache.commons.lang3.StringUtils;

public class HandshakeHandler extends BaseMessageHandler<HandshakeMessage> {

    private final ReusableSessionManager resusableSessionManager;

    public HandshakeHandler(CheetahServer cheetahServer) {
        //this.resusableSessionManager = cheetahServer.getResusableSessionManager();
        this.resusableSessionManager = null;
    }

    @Override
    protected void handle(HandshakeMessage handshakeMessage) {
        if (handshakeMessage.getConnection().getSessionConext().isSecurity()) {
            doSecurity(handshakeMessage);
        } else {
            doInSecurity(handshakeMessage);
        }

    }

    private void doSecurity(HandshakeMessage handshakeMessage) {
        byte[] iv = handshakeMessage.getIv();
        byte[] clientKey = handshakeMessage.getClientKey();
        byte[] serverKey = CipherBox.I.randomAESKey();
        byte[] sessionKey = CipherBox.I.mixKey(clientKey, serverKey);

        //验证客户端提交消息
        if (StringUtils.isBlank(handshakeMessage.getDeviceId()) || iv.length != CipherBox.I.getAesKeyLength() || clientKey.length != CipherBox.I.getAesKeyLength()) {
            ErrorMessage errorMessage = ErrorMessage.from(handshakeMessage);
            errorMessage.setReason("Param invalid.");
            errorMessage.close();
            Logs.CONN.error("handshake failure,message={},conn={}", handshakeMessage, handshakeMessage.getConnection());
            return;
        }
        SessionContext sessionConext = handshakeMessage.getConnection().getSessionConext();
        //验证是否已经握手成功后重复握手
        if (handshakeMessage.getDeviceId().equals(sessionConext.getDeviceId())) {
            ErrorMessage errorMessage = ErrorMessage.from(handshakeMessage);
            errorMessage.setReason(ErrorCode.REPEAT_HANDSHAKE.getErrorMsg());
            errorMessage.setCode(ErrorCode.REPEAT_HANDSHAKE.getErrorCode());
            errorMessage.send();
            Logs.CONN.warn("handshake failure,repeat handshake,message={},conn={}", handshakeMessage, handshakeMessage.getConnection());
            return;
        }

        sessionConext.setCipher(new AesCipher(clientKey, iv));

        ReusableSession reusableSession = resusableSessionManager.genSession(sessionConext);

        int heartbeat = ConfigTools.getHeartbeat(handshakeMessage.getMinHeartbeat(), handshakeMessage.getMaxHeartbeat());
        Packet responsePacket = new Packet(Command.HANDSHAKE.getCmd());
        HandshakeOkMessage handshakeOkMessage = new HandshakeOkMessage(responsePacket, handshakeMessage.getConnection());
        handshakeOkMessage.setPacket(responsePacket);
        handshakeOkMessage.setServerKey(serverKey);
        handshakeOkMessage.setHeartbeat(heartbeat);
        handshakeOkMessage.setExpireTime(reusableSession.getExpireTime());//过期时间由resusableSessionManager里面设置，其实是配置文件配置
        handshakeOkMessage.send(f -> {
            if (f.isSuccess()) {
                sessionConext.setCipher(new AesCipher(sessionKey, iv));
                sessionConext.setOsName(handshakeMessage.getOsName());
                sessionConext.setOsVersion(handshakeMessage.getOsVersion());
                sessionConext.setClientVersion(handshakeMessage.getClientVersion());
                sessionConext.setDeviceId(handshakeMessage.getDeviceId());
                sessionConext.setHeartbeat(heartbeat);
                resusableSessionManager.cacheSession(reusableSession);
                Logs.CONN.info("handshake success,conn={}", handshakeMessage.getConnection());
            } else {
                Logs.CONN.info("handshake failure, conn={}", handshakeMessage.getConnection(), f.cause());
            }
        });


    }

    private void doInSecurity(HandshakeMessage handshakeMessage) {
        //验证客户端提交消息
        if (StringUtils.isBlank(handshakeMessage.getDeviceId()) ) {
            ErrorMessage errorMessage = ErrorMessage.from(handshakeMessage);
            errorMessage.setReason("Param invalid.");
            errorMessage.close();
            Logs.CONN.error("handshake failure,message={},conn={}", handshakeMessage, handshakeMessage.getConnection());
            return;
        }
        SessionContext sessionConext = handshakeMessage.getConnection().getSessionConext();
        //验证是否已经握手成功后重复握手
        if (handshakeMessage.getDeviceId().equals(sessionConext.getDeviceId())) {
            ErrorMessage errorMessage = ErrorMessage.from(handshakeMessage);
            errorMessage.setReason(ErrorCode.REPEAT_HANDSHAKE.getErrorMsg());
            errorMessage.setCode(ErrorCode.REPEAT_HANDSHAKE.getErrorCode());
            errorMessage.send();
            Logs.CONN.warn("handshake failure,repeat handshake,message={},conn={}", handshakeMessage, handshakeMessage.getConnection());
            return;
        }

        Packet responsePacket = new Packet(Command.HANDSHAKE.getCmd());
        HandshakeOkMessage handshakeOkMessage = new HandshakeOkMessage(responsePacket, handshakeMessage.getConnection());
        handshakeOkMessage.send();

        sessionConext.setOsName(handshakeMessage.getOsName());
        sessionConext.setOsVersion(handshakeMessage.getOsVersion());
        sessionConext.setClientVersion(handshakeMessage.getClientVersion());
        sessionConext.setDeviceId(handshakeMessage.getDeviceId());
        sessionConext.setHeartbeat(Integer.MAX_VALUE);

        Logs.CONN.info("handshake success conn={}",handshakeMessage.getConnection());

    }

    @Override
    protected HandshakeMessage decode(Packet packet, Connection connection) {
        return new HandshakeMessage(packet, connection);
    }
}
