package com.caisheng.cheetah.common;

import com.caisheng.cheetah.api.connection.Connection;
import com.caisheng.cheetah.api.message.MessageHandler;
import com.caisheng.cheetah.api.message.PacketReceiver;
import com.caisheng.cheetah.api.protocol.Command;
import com.caisheng.cheetah.api.protocol.Packet;
import com.caisheng.cheetah.common.message.ErrorMessage;
import com.caisheng.cheetah.tools.log.Logs;
import jdk.nashorn.internal.runtime.regexp.joni.exception.ErrorMessages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class MessageDispatcher implements PacketReceiver {
    public static final int POLICY_REJECT=2;
    public static final int POLICY_LOG=1;
    public static final int POLICY_IGNORE=0;
    private int unsupportPolicy;
    private Logger logger = LoggerFactory.getLogger(MessageDispatcher.class);
    private final Map<Byte, MessageHandler> handlers = new HashMap<>();

    public MessageDispatcher() {
        this(POLICY_REJECT);
    }

    public MessageDispatcher(int unsupportPolicy) {
        this.unsupportPolicy = unsupportPolicy;
    }

    public void register(Command command, MessageHandler messageHandler) {
        this.handlers.put(command.getCmd(), messageHandler);
    }
    public void register(Command command, Supplier<MessageHandler> messageHandlerSupplier) {
        this.handlers.put(command.getCmd(), messageHandlerSupplier.get());
    }


    public void register(Command command, Supplier<MessageHandler> messageHandlerSupplier, boolean ennable) {
        if (ennable && !this.handlers.containsKey(command.getCmd())) {
            register(command, messageHandlerSupplier.get());
        }
    }

    public void unRegister(Command command) {
        this.handlers.remove(command.getCmd());
    }


    @Override
    public void onReceive(Packet packet, Connection connection) {
        MessageHandler messageHandler = this.handlers.get(packet.getCmd());
        if (messageHandler != null) {
            //Profiler.enter("time cost on [dispatch]");//TODO
            try {
                messageHandler.handle(packet, connection);
            } catch (Throwable throwable) {
                byte[] body = packet.getBody();
                logger.error("dispatch message ex,packet={},connect={},body={}", packet, connection, Arrays.toString(body), throwable);
                logger.error("dispatch message ex,packet={},connect={},body={}", packet, connection, Arrays.toString(body), throwable.getMessage());
                ErrorMessage em = ErrorMessage.from(packet, connection);
                em.setReason(ErrorCode.DISPATCH_ERROR.getErrorMsg());
                em.setCode(ErrorCode.DISPATCH_ERROR.getErrorCode());
                em.close();

            } finally {
                //Profiler.release();//TODO
            }
        } else {
            if (this.unsupportPolicy > POLICY_IGNORE) {
                Logs.CONN.error("dispatch message failure, cmd={} unsupported, packet={}, connect={}, body={}"
                        , Command.toCMD(packet.getCmd()), packet, connection);
                if(this.unsupportPolicy > POLICY_LOG){
                    ErrorMessage em = ErrorMessage.from(packet, connection);
                    em.setCode(ErrorCode.UNSUPPORTED_CMD.getErrorCode());
                    em.setReason(ErrorCode.UNSUPPORTED_CMD.getErrorMsg());
                    em.close();
                }
            }
        }

    }
}
