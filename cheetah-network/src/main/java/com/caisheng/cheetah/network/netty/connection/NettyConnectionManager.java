package com.caisheng.cheetah.network.netty.connection;

import com.caisheng.cheetah.api.connection.Connection;
import com.caisheng.cheetah.api.connection.ConnectionManager;
import io.netty.channel.Channel;
import io.netty.channel.ChannelId;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class NettyConnectionManager implements ConnectionManager {
    private Map<ChannelId, Connection> connections = new ConcurrentHashMap<>();

    @Override
    public Connection get(Channel channel) {
        return this.connections.get(channel.id());
    }

    @Override
    public Connection removeAndClose(Channel channel) {
        return this.connections.remove(channel.id());
    }

    @Override
    public void add(Connection connection) {
        this.connections.putIfAbsent(connection.getChannel().id(), connection);
    }

    @Override
    public int getConnNum() {
        return this.connections.size();
    }

    @Override
    public void init() {

    }

    @Override
    public void destroy() {
        connections.values().forEach(Connection::close);
        connections.clear();
    }
}
