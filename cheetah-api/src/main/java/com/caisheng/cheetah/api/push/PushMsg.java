package com.caisheng.cheetah.api.push;

public class PushMsg {
    private final MsgType msgType;
    private String msgId;
    private String content;

    public PushMsg(MsgType msgType) {
        this.msgType = msgType;
    }


    public static PushMsg build(MsgType msgType,String content){
        PushMsg pushMsg = new PushMsg(msgType);
        pushMsg.setContent(content);
        return pushMsg;
    }

    public MsgType getMsgType() {
        return msgType;
    }

    public String getMsgId() {
        return msgId;
    }

    public void setMsgId(String msgId) {
        this.msgId = msgId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
