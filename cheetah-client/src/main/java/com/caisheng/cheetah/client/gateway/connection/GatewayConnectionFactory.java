package com.caisheng.cheetah.client.gateway.connection;

import ch.qos.logback.core.net.server.ServerListener;
import com.caisheng.cheetah.api.connection.Connection;
import com.caisheng.cheetah.api.service.BaseService;
import com.caisheng.cheetah.api.srd.ServiceListener;
import com.caisheng.cheetah.common.message.BaseMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;
import java.util.function.Function;

public abstract class GatewayConnectionFactory extends BaseService implements ServiceListener {
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    public static GatewayConnectionFactory create() {
//        return CC.lion.net.udpGateway() ? new GatewayUDPConnectionFactory(lionClient) : new GatewayTCPConnectionFactory(lionClient); TODO
        return null;

    }

    public abstract Connection getConnection(String hostAndPort);

    public abstract <M extends BaseMessage> boolean send(String hostAndPort, Function<Connection, M> function, Consumer<M> consumer);

    public abstract <M extends BaseMessage> boolean broadcast(Function<Connection, M> function, Consumer<M> consumer);
}
