package com.caisheng.cheetah.client.gateway.connection;

import ch.qos.logback.core.net.server.Client;
import com.caisheng.cheetah.api.connection.Connection;
import com.caisheng.cheetah.api.service.Listener;
import com.caisheng.cheetah.api.spi.common.ServiceDiscoverFactory;
import com.caisheng.cheetah.api.srd.ServiceDiscover;
import com.caisheng.cheetah.api.srd.ServiceListener;
import com.caisheng.cheetah.api.srd.ServiceNames;
import com.caisheng.cheetah.api.srd.ServiceNode;
import com.caisheng.cheetah.client.CheetahClient;
import com.caisheng.cheetah.client.gateway.GatewayClient;
import com.caisheng.cheetah.common.message.BaseMessage;
import com.caisheng.cheetah.tools.event.EventBus;
import com.google.common.collect.Maps;
import com.google.common.net.HostAndPort;
import io.netty.channel.ChannelFuture;
import io.netty.util.AttributeKey;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

import static com.caisheng.cheetah.tools.config.CC.lion.net.gateway_client_num;

public class TcpGatewayConnectionFactory extends GatewayConnectionFactory {

    private AttributeKey<String> attributeKey = AttributeKey.valueOf("host_port");
    private Map<String, List<Connection>> connections = Maps.newConcurrentMap();
    private ServiceDiscover serviceDiscover;
    private GatewayClient gatewayClient;
    private CheetahClient cheetahClient;

    public TcpGatewayConnectionFactory(CheetahClient cheetahClient) {
        this.cheetahClient = cheetahClient;
    }

    @Override
    protected void doStart(Listener listener) throws Throwable {
        EventBus.register(this);
        this.gatewayClient = new GatewayClient(cheetahClient);
        this.gatewayClient.start().join();

        this.serviceDiscover = ServiceDiscoverFactory.create();
        this.serviceDiscover.subscribe(ServiceNames.GATEWAY_SERVER, this);
        this.serviceDiscover.lookup(ServiceNames.GATEWAY_SERVER).forEach(this::syncAddConnection);

        listener.onSuccess();
    }


    @Override
    protected void doStop(Listener listener) throws Throwable {
        this.connections.values().forEach(list -> list.forEach(Connection::close));
        if (this.gatewayClient != null) {
            this.gatewayClient.stop().join();
        }
        serviceDiscover.unsubscribe(ServiceNames.GATEWAY_SERVER, this);
    }


    @Override
    public Connection getConnection(String hostAndPort) {
        List<Connection> connections = this.connections.get(hostAndPort);
        if (connections == null || connections.isEmpty()) {
            synchronized (hostAndPort.intern()) {
                connections = this.connections.get(hostAndPort);
                if (connections == null || connections.isEmpty()) {
                    this.serviceDiscover.lookup(ServiceNames.GATEWAY_SERVER).stream()
                            .filter(serviceNode -> serviceNode.hostAndPort().equals(hostAndPort))
                            .forEach(this::syncAddConnection);

                    if (connections == null || connections.isEmpty()) {
                        return null;
                    }

                }
            }
        }

        int l = connections.size();
        Connection connection;
        if (l == 1) {
            connection = connections.get(0);
        } else {
            connection = connections.get((int) (Math.random() * l % l));
        }
        if (connection.isConnected()) {
            return connection;
        } else {
            reconnect(connection, hostAndPort);
            return getConnection(hostAndPort);
        }
    }

    private void reconnect(Connection connection, String hostAndPort) {
        HostAndPort hostAndPortObj = HostAndPort.fromString(hostAndPort);
        this.connections.get(hostAndPort).remove(connection);
        connection.close();
        addConnection(hostAndPortObj.getHost(), hostAndPortObj.getPort(), false);
    }

    @Override
    public <M extends BaseMessage> boolean send(String hostAndPort, Function<Connection, M> function, Consumer<M> consumer) {
        Connection connection = getConnection(hostAndPort);
        if (connection == null) {
            return false;
        }
        M msg = function.apply(connection);
        consumer.accept(msg);
        return true;
    }

    @Override
    public <M extends BaseMessage> boolean broadcast(Function<Connection, M> function, Consumer<M> consumer) {
        if (this.connections.isEmpty()) {
            return false;
        }
        this.connections.values().stream()
                .filter(conns -> conns.size() > 0)
                .forEach(conns->consumer.accept(function.apply(conns.get(0))));
        return true;
    }






    /******** ServiceListener *********/
    @Override
    public void onServiceAdd(String path, ServiceNode serviceNode) {
        asyncAddConnection(serviceNode);
    }

    @Override
    public void onServiceUpdate(String path, ServiceNode serviceNode) {
        removeClient(serviceNode);
        asyncAddConnection(serviceNode);
    }


    @Override
    public void onServiceRemove(String path, ServiceNode serviceNode) {
        removeClient(serviceNode);
    }


    private void asyncAddConnection(ServiceNode serviceNode) {
        for (int i = 0; i < gateway_client_num; i++) {// cny_note gateway_client_num网关客户端连接数,多建立一些连接应该可以作为“池”使用
            addConnection(serviceNode.getHost(), serviceNode.getPort(), false);
        }
    }

    private void syncAddConnection(ServiceNode serviceNode) {
        for (int i = 0; i < gateway_client_num; i++) {// cny_note gateway_client_num网关客户端连接数,多建立一些连接应该可以作为“池”使用
            addConnection(serviceNode.getHost(), serviceNode.getPort(), true);
        }
    }

    private void addConnection(String host, int port, boolean sync) {
        ChannelFuture channelFuture = this.gatewayClient.connect(host, port);
        channelFuture.channel().attr(this.attributeKey).set(getHostAndPort(host, port));
        channelFuture.addListener(future -> {
            if (!future.isSuccess()) {
                logger.error("create gateway connection failure,host={},port={}", host, port, future.cause());
            }
        });

        if (sync) {
            channelFuture.awaitUninterruptibly();
        }
    }

    private void removeClient(ServiceNode serviceNode) {
        if (serviceNode != null) {
            List<Connection> clients = this.connections.remove(getHostAndPort(serviceNode.getHost(), serviceNode.getPort()));
            if (clients != null) {
                clients.forEach(Connection::close);
            }

        }
    }


    private static String getHostAndPort(String host, int port) {
        return host + ":" + port;
    }


}
