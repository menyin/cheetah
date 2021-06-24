package com.caisheng.cheetah.core.server;

import com.caisheng.cheetah.api.connection.Connection;
import com.caisheng.cheetah.api.connection.ConnectionManager;
import com.caisheng.cheetah.network.netty.connection.NettyConnection;
import com.caisheng.cheetah.tools.config.CC;
import com.caisheng.cheetah.tools.thread.NamedThreadFactory;
import com.caisheng.cheetah.tools.thread.ThreadNames;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelId;
import io.netty.util.HashedWheelTimer;
import io.netty.util.Timeout;
import io.netty.util.TimerTask;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

public class ServerConnectionManager implements ConnectionManager {
    private final ConcurrentMap<ChannelId, ConnectionHolder> connections = new ConcurrentHashMap<>();
    private final ConnectionHolder DEFAULT = new SimpleConnectionHolder(null);
    private final boolean heartbeatCheck;
    private final ConnectionHolderFactory connectionHolderFactory;
    private HashedWheelTimer timer;

    public ServerConnectionManager(boolean heartbeatCheck) {
        this.heartbeatCheck = heartbeatCheck;
        this.connectionHolderFactory = heartbeatCheck ? HeartbeatCheckHolder::new : SimpleConnectionHolder::new;
        this.init();
    }

    @Override
    public Connection get(Channel channel) {
        return connections.getOrDefault(channel.id(),DEFAULT).get();
    }

    @Override
    public Connection removeAndClose(Channel channel) {
        ConnectionHolder connectionHolder = connections.remove(channel.id());
        if (connectionHolder != null) {
            Connection connection = connectionHolder.get();
            connection.close();
            return connection;
        }
        NettyConnection nettyConnection = new NettyConnection();
        nettyConnection.init(channel, false);
        nettyConnection.close();
        return nettyConnection;
    }

    @Override
    public void add(Connection connection) {
        ConnectionHolder connectionHolder = this.connectionHolderFactory.create(connection);
        connections.putIfAbsent(connection.getChannel().id(), connectionHolder);

    }

    @Override
    public int getConnNum() {
        return connections.size();
    }

    @Override
    public void init() {
        if (this.heartbeatCheck) {
            //TODO
            int tickDuration = 1000;
            int ticksPerWheel = CC.lion.core.max_heartbeat / tickDuration;

            timer = new HashedWheelTimer(
//                    Executors.defaultThreadFactory(), //使用Executors的默认线程工厂，也可以自定义工厂
                    new NamedThreadFactory(ThreadNames.T_CONN_TIMER),
                    tickDuration, //注意这个时间单位其实是算“最小精度”
                    TimeUnit.MILLISECONDS, //“最小精度”的单位
                    ticksPerWheel); //时间钟的周期 ？？这个的作用
        }
    }

    @Override
    public void destroy() {
        if (this.timer != null) {
            this.timer.stop();
        }
        connections.values().forEach(ConnectionHolder::close);
        connections.clear();

    }


    private interface ConnectionHolder {
        Connection get();
        void close();
    }

    private class SimpleConnectionHolder implements ConnectionHolder {

        private Connection connection;

        public SimpleConnectionHolder(Connection connection) {
            this.connection = connection;
        }

        @Override
        public Connection get() {
            return connection;
        }

        @Override
        public void close() {
            if (connection != null) {
                //TODO
//                connection.close();
            }
        }
    }

    private interface ConnectionHolderFactory {
        ConnectionHolder create(Connection connection);
    }

    private class HeartbeatCheckHolder implements ConnectionHolder, TimerTask {
        private final HashedWheelTimer timer;
        private Connection connection;
        private int readTimeoutCount;
        /*public HeartbeatCheckFactory(HashedWheelTimer timer, Connection connection) {

            timer.newTimeout(this, 2000, TimeUnit.MILLISECONDS);
        }*/

        public HeartbeatCheckHolder(Connection connection) {
            this(ServerConnectionManager.this.timer, connection);
        }

        public HeartbeatCheckHolder(HashedWheelTimer timer, Connection connection) {
            this.timer = timer;
            this.connection = connection;
            startTimeout();

        }

        private void startTimeout() {
            if (this.connection != null && this.connection.isConnected()) {
                int heartbeat = this.connection.getSessionConext().getHeartbeat();
                timer.newTimeout(this, heartbeat, TimeUnit.MILLISECONDS);
            }
        }


        @Override
        public void run(Timeout timeout) throws Exception {
            if (this.connection == null && !this.connection.isConnected()) {

            }
            if (this.connection.isReadTimeout()) {
                readTimeoutCount++;
                if (readTimeoutCount >= 3) {
                    this.connection.close();
                    readTimeoutCount = 0;
                }
            }
            timer.newTimeout(this, 2000, TimeUnit.MILLISECONDS);
        }

        @Override
        public Connection get() {
            return this.connection;
        }

        @Override
        public void close() {
            if (this.connection != null) {
                connection.close();
            }
        }
    }


}
