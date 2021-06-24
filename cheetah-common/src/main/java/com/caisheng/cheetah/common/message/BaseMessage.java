package com.caisheng.cheetah.common.message;

import com.caisheng.cheetah.api.connection.Cipher;
import com.caisheng.cheetah.api.connection.Connection;
import com.caisheng.cheetah.api.message.Message;
import com.caisheng.cheetah.api.protocol.Packet;
import com.caisheng.cheetah.tools.common.IOUtils;
import com.caisheng.cheetah.tools.config.CC;
import io.netty.channel.ChannelFutureListener;

import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class BaseMessage implements Message {
    public static final byte STATUS_DECODED = 1;
    public static final byte STATUS_ENCODED = 2;
    private static final AtomicInteger ID_SEQ = new AtomicInteger(0);
    protected Packet packet;
    protected Connection connection;
    transient private byte status = 0;//状态标志位

    public BaseMessage(Packet packet, Connection connection) {
        this.packet = packet;
        this.connection = connection;
    }

    @Override
    public void decodeBody() {
        if ((status & STATUS_DECODED) == 0) {
            status |= STATUS_DECODED;
            if (this.packet.getBodyLength() > 0) {
                if (packet.hasFlag(Packet.FLAG_JSON_BODY)) {
                    decodeJsonBody0();
                } else {
                    decodeBinaryBody0();
                }
            }
        }

    }

    @Override
    public void encodeBody() {
        if ((status & STATUS_ENCODED) == 0) {
            status |= STATUS_ENCODED;
            if (packet.hasFlag(Packet.FLAG_JSON_BODY)) {
                encodeJsonBody0();
            } else {
                encodeBinaryBody0();
            }

        }

    }

    @Override
    public void send(ChannelFutureListener channelFutureListener) {
        encodeBody();//这里主要处理了packet
        this.connection.send(this.packet, channelFutureListener);
    }

    @Override
    public void sendRaw(ChannelFutureListener channelFutureListener) {
        encodeBodyRaw();
        this.connection.send(this.packet, channelFutureListener);
    }

    @Override
    public Packet getPacket() {
        return this.packet;
    }

    @Override
    public Connection getConnection() {
        return this.connection;
    }

    public void send(){
        send(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
    }
    public void sendRaw(){
        sendRaw(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
    }

    public void close(){
        send(ChannelFutureListener.CLOSE);
    }

    private void decodeBinaryBody0() {
        byte[] bodyBytes = packet.getBody();

        //解密
        if (packet.hasFlag(Packet.FLAG_CRYPTO)) {
            Cipher cipher = getCipher();
            if (cipher != null) {
                bodyBytes = cipher.decrypt(bodyBytes);
            }
        }
        //解压
        if (packet.hasFlag(Packet.FLAG_COMPRESS)) {
            bodyBytes = IOUtils.decompress(bodyBytes);
        }
        if (bodyBytes.length == 0) {
            throw new RuntimeException("message decode ex.");
        }
        packet.setBody(bodyBytes);
        decode(packet.getBody());
        packet.setBody(null);
    }

    protected abstract void decode(byte[] body);


    private void decodeJsonBody0() {
        Map<String, Object> bodyMap = packet.getBody();
        decodeJsonBody(bodyMap);
    }

    /**
     * 交由子类处理
     *
     * @param bodyMap
     */
    protected void decodeJsonBody(Map<String, Object> bodyMap) {
    }


    private void encodeBinaryBody0() {
        byte[] body = encode();//cny_note 具体xxxMessage子类去加密自己想要加密的东西
        if (body != null && body.length > 0) {
            if (body.length > CC.lion.core.compress_threshold) {
                byte[] compress = IOUtils.compress(body);
                if (compress.length > 0) {
                    body = compress;
                    this.packet.addFlag(Packet.FLAG_COMPRESS);
                }
            }
        }
        Cipher cipher = this.getCipher();
        if (cipher != null) {
            byte[] encrypt = cipher.encrypt(body);
            if (encrypt.length > 0) {
                body = encrypt;
                packet.addFlag(Packet.FLAG_CRYPTO);
            }
        }
        packet.setBody(body);
    }

    protected abstract byte[] encode();

    private byte[] encodeBinaryBody() {
        packet.getBody();
        return null;
    }

    private void encodeJsonBody0() {
        packet.setBody(encodeJsonBody());
    }

    //让子类自己将数据填充到Map类型的body
    protected Map<String, Object> encodeJsonBody() {
        return null;
    }

    protected Cipher getCipher() {
        return this.connection.getSessionConext().getCipher();
    }


    private void encodeBodyRaw() {
        if ((status & STATUS_ENCODED) == 0) {
            status |= STATUS_ENCODED;
            if (packet.hasFlag(Packet.FLAG_JSON_BODY)) {
                encodeJsonBody0();
            } else {
                byte[] encode = encode();// 没有再经过压缩加密
                packet.setBody(encode);
            }
        }
    }

    public void setPacket(Packet packet) {
        this.packet = packet;
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    @Override
    public abstract String toString() ;

    /**
     * ？？此方法生产给Packet.sessionId
     * @return
     */
    public static int genSessionId() {
        return ID_SEQ.incrementAndGet();
    }


    public ScheduledExecutorService getExecutor() {
        return connection.getChannel().eventLoop();
    }

}



