package com.caisheng.cheetah.core.handler;

import com.caisheng.cheetah.api.connection.Connection;
import com.caisheng.cheetah.api.protocol.Packet;
import com.caisheng.cheetah.common.ErrorCode;
import com.caisheng.cheetah.common.handler.BaseMessageHandler;
import com.caisheng.cheetah.common.message.ErrorMessage;
import com.caisheng.cheetah.common.message.FastConnectMessage;
import com.caisheng.cheetah.common.message.FastConnectOkMessage;
import com.caisheng.cheetah.core.CheetahServer;
import com.caisheng.cheetah.core.session.ReusableSession;
import com.caisheng.cheetah.core.session.ReusableSessionManager;
import com.caisheng.cheetah.tools.config.ConfigTools;
import com.caisheng.cheetah.tools.log.Logs;

public class FastConnectHandler extends BaseMessageHandler<FastConnectMessage> {
    private final ReusableSessionManager reusableSessionManager;

    public FastConnectHandler(CheetahServer cheetahServer) {
        this.reusableSessionManager = cheetahServer.getReusableSessionManager();
    }

    @Override
    protected void handle(FastConnectMessage fastConnectMessage) {
        ReusableSession reusableSession = reusableSessionManager.querySession(fastConnectMessage.getSessionId());
        if (reusableSession == null) {
            ErrorMessage errorMessage = ErrorMessage.from(fastConnectMessage);
            errorMessage.setCode(ErrorCode.SESSION_EXPIRED.getErrorCode());
            errorMessage.setReason(ErrorCode.SESSION_EXPIRED.getErrorMsg());
            errorMessage.send();
            Logs.CONN.warn("fast connect failure,session is expired,sessionId={},deviceId={},conn={}"
                    , fastConnectMessage.getSessionId(), fastConnectMessage.getDeviceId(), fastConnectMessage.getConnection());
        } else if (!reusableSession.getSessionContext().getDeviceId().equals(fastConnectMessage.getDeviceId())) {
            ErrorMessage errorMessage = ErrorMessage.from(fastConnectMessage);
            errorMessage.setCode(ErrorCode.INVALID_DEVICE.getErrorCode());
            errorMessage.setReason(ErrorCode.INVALID_DEVICE.getErrorMsg());
            errorMessage.send();
            Logs.CONN.warn("fast connect failure,not the same device,deviceId={},session={},conn={}"
                    , fastConnectMessage.getDeviceId(), reusableSession.getSessionContext(), fastConnectMessage.getConnection());
        } else {
            int heartbeat = ConfigTools.getHeartbeat(fastConnectMessage.getMaxHeartbeat(), fastConnectMessage.getMaxHeartbeat());
            reusableSession.getSessionContext().setHeartbeat(heartbeat);
            //响应消息
            FastConnectOkMessage fastConnectOkMessage = FastConnectOkMessage.from(fastConnectMessage);
            fastConnectOkMessage.setHeartbeat(heartbeat);
            fastConnectOkMessage.send(f -> {
                if (f.isSuccess()) {
                    fastConnectMessage.getConnection().setSessionContext(reusableSession.getSessionContext());
                    Logs.CONN.info("fast connect success, session={}, conn={}", reusableSession.getSessionContext(), fastConnectMessage.getConnection().getChannel());
                } else {
                    Logs.CONN.info("fast connect failure, session={}, conn={}", reusableSession.getSessionContext(), fastConnectMessage.getConnection().getChannel());
                }
            });

        }
    }

    @Override
    protected FastConnectMessage decode(Packet packet, Connection connection) {
        return new FastConnectMessage(packet, connection);
    }
}
