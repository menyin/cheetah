package com.caisheng.cheetah.api.push;

import com.caisheng.cheetah.api.router.ClientLocation;

import java.util.Arrays;

public class PushResult {
    public final static int CODE_SUCCESS=1;
    public final static int CODE_FAILURE=2;
    public final static int CODE_OFFLINE=3;
    public final static int CODE_TIMEOUT=4;

    private int resultCode;
    private String userId;
    private Object[] timeLine;
    private ClientLocation clientLocation;

    public PushResult(int resultCode) {
        this.resultCode = resultCode;
    }


    public String getResultDesc() {
        switch (resultCode) {
            case CODE_SUCCESS:
                return "success";
            case CODE_FAILURE:
                return "failure";
            case CODE_OFFLINE:
                return "offline";
            case CODE_TIMEOUT:
                return "timeout";
        }
        return Integer.toString(CODE_TIMEOUT);
    }

    @Override
    public String toString() {
        return "PushResult{" +
                "resultCode=" + getResultDesc() +
                ", userId='" + userId + '\'' +
                ", timeLine=" + Arrays.toString(timeLine) +
                ", " + clientLocation +
                '}';
    }

    public int getResultCode() {
        return resultCode;
    }

    public void setResultCode(int resultCode) {
        this.resultCode = resultCode;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Object[] getTimeLine() {
        return timeLine;
    }

    public void setTimeLine(Object[] timeLine) {
        this.timeLine = timeLine;
    }

    public ClientLocation getClientLocation() {
        return clientLocation;
    }

    public void setClientLocation(ClientLocation clientLocation) {
        this.clientLocation = clientLocation;
    }
}
