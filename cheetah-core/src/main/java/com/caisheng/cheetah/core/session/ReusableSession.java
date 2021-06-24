package com.caisheng.cheetah.core.session;

import com.caisheng.cheetah.api.connection.SessionContext;
import com.caisheng.cheetah.common.security.AesCipher;

public class ReusableSession {
    private String sessionId;
    private long expireTime;
    private SessionContext sessionContext;

    public static String encode(SessionContext context) {
        StringBuilder sb = new StringBuilder();
        sb.append(context.getOsName()).append(",");
        sb.append(context.getOsVersion()).append(",");
        sb.append(context.getClientVersion()).append(",");
        sb.append(context.getDeviceId()).append(",");
        sb.append(context.getCipher());
        return sb.toString();
    }

    public static ReusableSession decode(String value) {
        String[] array = value.split(",");
        SessionContext sessionContext = new SessionContext();
        sessionContext.setOsName(array[0]);
        sessionContext.setOsVersion(array[1]);
        sessionContext.setClientVersion(array[2]);
        sessionContext.setDeviceId(array[3]);
        byte[] key = array[4].getBytes();
        byte[] iv = array[5].getBytes();
        AesCipher aesCipher = new AesCipher(key, iv);
        sessionContext.setCipher(aesCipher);
        ReusableSession reusableSession = new ReusableSession();
        reusableSession.setSessionContext(sessionContext);
        return reusableSession;
    }



    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public long getExpireTime() {
        return expireTime;
    }

    public void setExpireTime(long expireTime) {
        this.expireTime = expireTime;
    }

    public SessionContext getSessionContext() {
        return sessionContext;
    }

    public void setSessionContext(SessionContext sessionContext) {
        this.sessionContext = sessionContext;
    }
}
