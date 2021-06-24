package com.caisheng.cheetah.core.handler;

import com.caisheng.cheetah.api.connection.Connection;
import com.caisheng.cheetah.api.connection.SessionContext;
import com.caisheng.cheetah.api.event.UserOfflineEvent;
import com.caisheng.cheetah.api.event.UserOnlineEvent;
import com.caisheng.cheetah.api.protocol.Command;
import com.caisheng.cheetah.api.protocol.Packet;
import com.caisheng.cheetah.api.spi.handler.BindValidator;
import com.caisheng.cheetah.api.spi.handler.BindValidatorFactory;
import com.caisheng.cheetah.common.handler.BaseMessageHandler;
import com.caisheng.cheetah.common.message.BindUserMessage;
import com.caisheng.cheetah.common.message.ErrorMessage;
import com.caisheng.cheetah.common.message.OkMessage;
import com.caisheng.cheetah.common.router.RemoteRouter;
import com.caisheng.cheetah.common.router.RemoteRouterManager;
import com.caisheng.cheetah.core.CheetahServer;
import com.caisheng.cheetah.core.router.LocalRouter;
import com.caisheng.cheetah.core.router.LocalRouterManager;
import com.caisheng.cheetah.core.router.RouterCenter;
import com.caisheng.cheetah.tools.event.EventBus;
import com.caisheng.cheetah.tools.log.Logs;
import org.apache.commons.lang3.StringUtils;

public class BindUserHandler extends BaseMessageHandler<BindUserMessage> {
    private BindValidator bindValidator = BindValidatorFactory.create();//与具体业务相关的验证器
    private RouterCenter routerCenter;

    public BindUserHandler(CheetahServer cheetahServer) {

    }

    @Override
    protected void handle(BindUserMessage bindUserMessage) {
        if (bindUserMessage.getPacket().getCmd() == Command.BIND.getCmd()) {
            bind(bindUserMessage);
        } else {
            unbind(bindUserMessage);
        }
    }

    private void bind(BindUserMessage bindUserMessage) {
        if (StringUtils.isBlank(bindUserMessage.getUserId())) {
            ErrorMessage errorMessage = ErrorMessage.from(bindUserMessage);
            errorMessage.setReason("Param invalid.");
            errorMessage.close();
            Logs.CONN.error("bind user failure for invalid param,message={},conn={}", bindUserMessage, bindUserMessage.getConnection());
            return;
        }

        SessionContext sessionConext = bindUserMessage.getConnection().getSessionConext();
        if (sessionConext.handshakeOk()) {
            if (sessionConext.getUserId() != null) {
                if (bindUserMessage.getUserId() == sessionConext.getUserId()) {
                    sessionConext.setTags(bindUserMessage.getTags());
                    OkMessage okMessage = OkMessage.from(bindUserMessage);
                    okMessage.setData("bind user success.");
                    okMessage.sendRaw();
                    Logs.CONN.info("rebind user success,userId={},session={}", bindUserMessage.getUserId(), sessionConext);
                    return;
                } else {
                    unbind(bindUserMessage);
                }
            }

            boolean successs = bindValidator.validate(bindUserMessage.getUserId(), bindUserMessage.getData());

            if (successs) {
                successs = routerCenter.register(bindUserMessage.getUserId(), bindUserMessage.getConnection());//绑定成功后 注册本地和远程路由
            }
            if (successs) {
                sessionConext.setUserId(bindUserMessage.getUserId());
                sessionConext.setTags(bindUserMessage.getTags());
                EventBus.post(new UserOnlineEvent(bindUserMessage.getConnection(), bindUserMessage.getUserId()));//在事件总线触发事件
                OkMessage okMessage = OkMessage.from(bindUserMessage);
                okMessage.setData("bind user success.");
                okMessage.sendRaw();
                Logs.CONN.info("bind user success.userId={},sessionContex={}", bindUserMessage.getUserId(), sessionConext);
            } else {
                ErrorMessage errorMessage = ErrorMessage.from(bindUserMessage);
                errorMessage.setReason("bind user faild.");
                errorMessage.close();
                Logs.CONN.info("bind user failure, userId={}, sessionContext={}", bindUserMessage.getUserId(), sessionConext);
            }
        } else {
            ErrorMessage errorMessage = ErrorMessage.from(bindUserMessage);
            errorMessage.setReason("not handshake.");
            errorMessage.close();
            Logs.CONN.info("bind user failure, userId={}, sessionContext={}", bindUserMessage.getUserId(), sessionConext);
        }


    }

    private void unbind(BindUserMessage bindUserMessage) {
        if (StringUtils.isBlank(bindUserMessage.getUserId())) {
            ErrorMessage errorMessage = ErrorMessage.from(bindUserMessage);
            errorMessage.setReason("invalid param.");
            errorMessage.close();
            Logs.CONN.error("unbind user failure invalid param,session={}",bindUserMessage.getConnection().getSessionConext());
            return;
        }

        SessionContext sessionConext = bindUserMessage.getConnection().getSessionConext();
        if (sessionConext.handshakeOk()) {
            byte clientType = sessionConext.getClientType();
            String userId = sessionConext.getUserId();
            boolean isUnRegisterSuccess = true;
            RemoteRouterManager remoteRouterManager = this.routerCenter.getRemoteRouterManager();
            RemoteRouter remoteRouter = remoteRouterManager.lookup(userId, clientType);
            if (remoteRouter != null) {
                String deviceId = remoteRouter.getRouterValue().getDeviceId();
                if (sessionConext.getDeviceId().equals(deviceId)) {
                    isUnRegisterSuccess = remoteRouterManager.unRegister(userId, clientType);
                }
            }
            LocalRouterManager localRouterManager = this.routerCenter.getLocalRouterManager();
            LocalRouter localRouter = localRouterManager.lookup(userId, clientType);
            if (localRouter != null) {
                String deviceId = localRouter.getRouterValue().getSessionConext().getDeviceId();
                if (sessionConext.getDeviceId().equals(deviceId)) {
                    isUnRegisterSuccess = localRouterManager.unRegister(userId, clientType);
                }
            }

            if (isUnRegisterSuccess) {
                sessionConext.setUserId(null);
                sessionConext.setTags(null);
                EventBus.post(new UserOfflineEvent(bindUserMessage.getConnection(), userId));
                OkMessage okMessage = OkMessage.from(bindUserMessage);
                okMessage.setData("unbind user success.");
                okMessage.sendRaw();
                Logs.CONN.info("unbind user success,userId={},session={}", userId, sessionConext);
            } else {
                ErrorMessage errorMessage = ErrorMessage.from(bindUserMessage);
                errorMessage.setReason("unbind user failure.");
                errorMessage.sendRaw();
                Logs.CONN.error("unbind user failure,unRegister router failure,userId={},session={}", userId, sessionConext);
            }
        } else {
            ErrorMessage errorMessage = ErrorMessage.from(bindUserMessage);
            errorMessage.setReason("not handshake.");
            errorMessage.close();
            Logs.CONN.error("unbind user failure,not handshake,userId={},session={}", bindUserMessage.getUserId(), sessionConext);
        }


    }


    @Override
    protected BindUserMessage decode(Packet packet, Connection connection) {
        return new BindUserMessage(packet, connection);
    }
}
