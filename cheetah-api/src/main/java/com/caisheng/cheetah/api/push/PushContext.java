package com.caisheng.cheetah.api.push;

import com.caisheng.cheetah.api.Constants;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public class PushContext {
    private byte[] content;
    private PushMsg pushMsg;
    private String userId;
    private List<String> userIds;
    private AckModel ackModel = AckModel.NO_ACK;
    private PushCallback pushCallback;
    private int timeout;
    private boolean broadcast;
    private Set<String> tags;
    private String condition;
    private String taskId;

    public PushContext(byte[] content) {
        this.content = content;
    }

    public PushContext(String content) {
        this(content.getBytes(Constants.UTF_8));
    }

    public PushContext(PushMsg pushMsg) {
        this.pushMsg = pushMsg;
    }

    public static String genTaskId() {
        return UUID.randomUUID().toString();
    }


    /******** get set **********/

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }

    public PushMsg getPushMsg() {
        return pushMsg;
    }

    public void setPushMsg(PushMsg pushMsg) {
        this.pushMsg = pushMsg;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public List<String> getUserIds() {
        return userIds;
    }

    public void setUserIds(List<String> userIds) {
        this.userIds = userIds;
    }

    public AckModel getAckModel() {
        return ackModel;
    }

    public void setAckModel(AckModel ackModel) {
        this.ackModel = ackModel;
    }

    public PushCallback getPushCallback() {
        return pushCallback;
    }

    public void setPushCallback(PushCallback pushCallback) {
        this.pushCallback = pushCallback;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public boolean isBroadcast() {
        return broadcast;
    }

    public void setBroadcast(boolean broadcast) {
        this.broadcast = broadcast;
    }

    public Set<String> getTags() {
        return tags;
    }

    public void setTags(Set<String> tags) {
        this.tags = tags;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }
}
