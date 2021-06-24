package com.caisheng.cheetah.client.connect;

import com.caisheng.cheetah.api.Constants;
import com.caisheng.cheetah.api.connection.Connection;
import com.caisheng.cheetah.api.event.ConnectionCloseEvent;
import com.caisheng.cheetah.api.protocol.Command;
import com.caisheng.cheetah.api.protocol.Packet;
import com.caisheng.cheetah.api.spi.common.CacheManager;
import com.caisheng.cheetah.api.spi.common.CacheManagerFactory;
import com.caisheng.cheetah.common.CacheKeys;
import com.caisheng.cheetah.common.message.*;
import com.caisheng.cheetah.common.security.AesCipher;
import com.caisheng.cheetah.common.security.CipherBox;
import com.caisheng.cheetah.network.netty.connection.NettyConnection;
import com.caisheng.cheetah.tools.event.EventBus;
import com.caisheng.cheetah.tools.thread.NamedPoolThreadFactory;
import com.caisheng.cheetah.tools.thread.ThreadNames;
import com.google.common.collect.Maps;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Time;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class ConnectClientChannelHandler extends ChannelInboundHandlerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(ConnectClientChannelHandler.class);
    public static final Timer HASHED_WHEEL_TIMER = new HashedWheelTimer(new NamedPoolThreadFactory(ThreadNames.T_CONN_TIMER));
    public static final AttributeKey<ClientConfig> CONFIG_KEY = AttributeKey.newInstance("clientConfig");
    public static final TestStatistics TESTSTATISTICS = new TestStatistics();
    public static final CacheManager cacheManager = CacheManagerFactory.create();
    private final Connection connection = new NettyConnection();

    private ClientConfig clientConfig;
    private boolean perfTest;
    private int hbTimeoutTimes;

    public ConnectClientChannelHandler() {
        this.perfTest = true;
    }

    public ConnectClientChannelHandler(ClientConfig clientConfig) {
        this.clientConfig = clientConfig;
    }


    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        int clientNum = TESTSTATISTICS.clientNum.incrementAndGet();
        logger.info("connect connect channel={},clientNum={}", ctx.channel(), clientNum);
        for (int i = 0; i < 3; i++) {
            if (this.clientConfig != null) {
                break;
            }
            this.clientConfig = ctx.channel().attr(CONFIG_KEY).get();
        }

        if (this.clientConfig == null) {
            throw new NullPointerException("client config is null,channel=" + ctx.channel());
        }

        this.connection.init(ctx.channel(), true);

        if (perfTest) {
            handshake();
        } else {
            tryFastConnect();
        }

    }


    private void handshake() {
        Packet handshakePacket = new Packet(Command.HANDSHAKE, HandshakeMessage.genSessionId());
        HandshakeMessage handshakeMessage = new HandshakeMessage(handshakePacket, connection);
        handshakeMessage.setClientKey(this.clientConfig.getClientKey());
        handshakeMessage.setIv(this.clientConfig.getIv());
        handshakeMessage.setClientVersion(this.clientConfig.getClientVersion());
        handshakeMessage.setDeviceId(this.clientConfig.getDeviceId());
        handshakeMessage.setOsName(this.clientConfig.getOsName());
        handshakeMessage.setOsVersion(this.clientConfig.getOsVersion());
        handshakeMessage.setTimestamp(System.currentTimeMillis());
        handshakeMessage.send();
        logger.debug("send handshake message={}", handshakeMessage);


    }

    private void tryFastConnect() {
        Map<String, String> sessionTickets = getFastConnectionInfo(clientConfig.getDeviceId());
        if (sessionTickets == null) {
            handshake();
            return;
        }
        String sessionId = sessionTickets.get("sessionId");
        if (sessionId == null) {
            handshake();
            return;
        }
        String expireTime = sessionTickets.get("expireTime");
        if (expireTime != null) {
            Long expireTimeLong = Long.valueOf(expireTime.trim());
            if (expireTimeLong < System.currentTimeMillis()) {
                handshake();
                return;
            }
        }
        String cipher = sessionTickets.get("cipher");
        FastConnectMessage fastConnectMessage = new FastConnectMessage(connection);
        fastConnectMessage.setDeviceId(clientConfig.getDeviceId());
        fastConnectMessage.setSessionId(sessionId);
        fastConnectMessage.sendRaw(future -> {
            if (future.isSuccess()) {
                clientConfig.setCipher(cipher);
            } else {
                handshake();
            }
        });
        logger.debug("send fast connect message={}", fastConnectMessage);

    }

    private Map<String, String> getFastConnectionInfo(String deviceId) {
        String deviceIdKey = CacheKeys.getDeviceIdKey(deviceId);
        return this.cacheManager.get(deviceIdKey, Map.class);
    }


    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        this.connection.updateLastReadTime();
        if (msg instanceof Packet) {
            Packet packet = (Packet) msg;
            Command command = Command.toCMD(((Packet) msg).getCmd());
            if (command == Command.HANDSHAKE) {
                int connectedNum = TESTSTATISTICS.connectedNum.incrementAndGet();
                String cipher = clientConfig.getCipher();
                String[] split = cipher.split(",");
                byte[] key = AesCipher.toArray(split[0]);
                byte[] iv = AesCipher.toArray(split[1]);
                this.connection.getSessionConext().setCipher(new AesCipher(key, iv));

                HandshakeOkMessage handshakeOkMessage = new HandshakeOkMessage(packet, this.connection);
                handshakeOkMessage.decodeBody();
                byte[] sessionKey = CipherBox.I.mixKey(clientConfig.getClientKey(), handshakeOkMessage.getServerKey());
                this.connection.getSessionConext().setCipher(new AesCipher(sessionKey,clientConfig.getIv()));
                this.connection.getSessionConext().setHeartbeat(handshakeOkMessage.getHeartbeat());
                startHeartbeat(handshakeOkMessage.getHeartbeat() - 1000);
                logger.info("handshake success,clientConfig={},connectedNum={}", clientConfig, connectedNum);

                bindUser(clientConfig);

                if (this.perfTest) {
                    saveToRedisForFastConnection(clientConfig,handshakeOkMessage.getPacket().getSessionId(),handshakeOkMessage.getExpireTime(),sessionKey);
                }

            } else if (command == Command.FAST_CONNECT) {
                int connectedNum = TESTSTATISTICS.connectedNum.incrementAndGet();
                String cipher = clientConfig.getCipher();
                String[] cs = cipher.split(",");
                byte[] key = AesCipher.toArray(cs[0]);
                byte[] iv = AesCipher.toArray(cs[1]);
                connection.getSessionConext().setCipher(new AesCipher(key, iv));
                FastConnectOkMessage fastConnectOkMessage = new FastConnectOkMessage(packet, connection);
                fastConnectOkMessage.decodeBody();
                this.connection.getSessionConext().setHeartbeat(fastConnectOkMessage.getHeartbeat());
                startHeartbeat(fastConnectOkMessage.getHeartbeat() - 1000);
                bindUser(clientConfig);
                logger.info("fast connect success,clientConfig={},connectedNum={}", clientConfig, connectedNum);
            } else if (command == Command.KICK) {
                KickUserMessage kickUserMessage = new KickUserMessage(packet, connection);
                logger.error("receive kick user msg userId={}, deviceId={}, message={},", clientConfig.getUserId(), clientConfig.getDeviceId(), kickUserMessage);
                ctx.close();
            } else if (command == Command.ERROR) {
                ErrorMessage errorMessage = new ErrorMessage(packet, connection);
                errorMessage.decodeBody();
                logger.error("receive an error packet=" + errorMessage);
            } else if (command == Command.PUSH) {
                int receivePushNum = TESTSTATISTICS.receivePushNum.incrementAndGet();
                PushMessage pushMessage = new PushMessage(packet, connection);
                pushMessage.decodeBody();
                logger.info("receive push message, content={}, receivePushNum={}", new String(pushMessage.getContent(), Constants.UTF_8), receivePushNum);
                if (pushMessage.needAck()) {
                    AckMessage.from(pushMessage).sendRaw();
                    logger.info("send ack success for sessionId={}",pushMessage.getPacket().getSessionId());
                }
            } else if (command == Command.HEARTBEAT) {
                logger.info("receive heartbeat pong...");
            } else if (command == Command.OK) {
                OkMessage okMessage = new OkMessage(packet, connection);
                okMessage.decodeBody();
                int bindUserNum = TESTSTATISTICS.bindUserNum.get();
                if (okMessage.getCmd()== Command.BIND.getCmd()) {
                    bindUserNum = TESTSTATISTICS.bindUserNum.incrementAndGet();
                }

                logger.info("receive {}, bindUserNum={}", okMessage, bindUserNum);
            } else if (command == Command.HTTP_PROXY) {


            }
        }

    }

    private void saveToRedisForFastConnection(ClientConfig clientConfig, int sessionId, long expireTime, byte[] sessionKey) {
        HashMap<String, String> map = Maps.newHashMap();
        map.put("sessionId", Integer.toString(sessionId));
        map.put("expireTime", Long.toString(expireTime));
        map.put("cipherStr", this.connection.getSessionConext().getCipher().toString());

        String deviceIdKey = CacheKeys.getDeviceIdKey(clientConfig.getDeviceId());
        cacheManager.set(deviceIdKey, map, 60 * 5);
    }

    private void bindUser(ClientConfig clientConfig) {
        BindUserMessage bindUserMessage = new BindUserMessage(this.connection);
        bindUserMessage.setUserId(clientConfig.getUserId());
        bindUserMessage.setTags("test");
        bindUserMessage.send();
        this.connection.getSessionConext().setUserId(clientConfig.getUserId());
        logger.debug("send bind user message={}", bindUserMessage);
    }

    private void startHeartbeat(int heartbeat) {
        HASHED_WHEEL_TIMER.newTimeout(new TimerTask() {
            @Override
            public void run(Timeout timeout) throws Exception {
                if (connection.isConnected() && healthCheck()) {
                    HASHED_WHEEL_TIMER.newTimeout(this, heartbeat, TimeUnit.MILLISECONDS);
                }

            }
        }, heartbeat, TimeUnit.MILLISECONDS);
    }

    private boolean healthCheck() {
        if (this.connection.isReadTimeout()) {
            hbTimeoutTimes++;
            logger.warn("heartbeat timeout times={},client connection={}", hbTimeoutTimes, connection);
        } else {
            hbTimeoutTimes=0;
        }
        if (hbTimeoutTimes>=2) {
            logger.warn("heartbeat timeout times={},over limit={},client connection={}",hbTimeoutTimes,2,connection);
            hbTimeoutTimes=0;
            this.connection.close();
            return false;
        }
        if (this.connection.isWriteTimeout()) {
            logger.warn("send heartbeat ping...");
            this.connection.send(Packet.HB_PACKET);
        }

        return true;

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        connection.close();
        logger.error("caught an ex, channel={}", ctx.channel(), cause);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        int clientNum = TESTSTATISTICS.clientNum.decrementAndGet();
        this.connection.close();
        EventBus.post(new ConnectionCloseEvent(this.connection));
        logger.info("client diconnected connection={},clientNum={}", this.connection, clientNum);
    }

    public Connection getConnection() {
        return connection;
    }
}
