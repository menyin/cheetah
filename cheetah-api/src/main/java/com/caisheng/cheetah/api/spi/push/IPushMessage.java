package com.caisheng.cheetah.api.spi.push;


import com.caisheng.cheetah.api.common.Condition;
import com.caisheng.cheetah.api.message.Message;

public interface IPushMessage extends Message{
    boolean isBroadcast();

    String getUserId();

    int getClientType();

    byte[] getContent();

    boolean isNeedAck();

    byte getFlags();

    int getTimeoutMills();

    default String getTaskId(){return null;}
    default Condition getCondition(){return null;}
    default void finalized(){}

}
