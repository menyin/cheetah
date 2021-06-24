package com.caisheng.cheetah.core.session;

import com.caisheng.cheetah.api.connection.SessionContext;
import com.caisheng.cheetah.api.spi.common.CacheManager;
import com.caisheng.cheetah.api.spi.common.CacheManagerFactory;
import com.caisheng.cheetah.common.CacheKeys;
import com.caisheng.cheetah.tools.config.CC;
import com.caisheng.cheetah.tools.crypto.MD5Utils;
import org.apache.commons.lang3.StringUtils;

public class ReusableSessionManager {
    private final int expiredTime = CC.lion.core.session_expired_time;
    private final CacheManager checheManager = CacheManagerFactory.create();

    public boolean cacheSession(ReusableSession session) {
        String sessionKey = CacheKeys.getSessionKey(session.getSessionId());
        this.checheManager.set(sessionKey, ReusableSession.encode(session.getSessionContext()), this.expiredTime);
        return true;
    }

    public ReusableSession querySession(String sessionId){
        String sessionKey = CacheKeys.getSessionKey(sessionId);
        String sessionContextStr = this.checheManager.get(sessionKey, String.class);
        if(StringUtils.isBlank(sessionContextStr))return null;
        return ReusableSession.decode(sessionContextStr);
    }

    public ReusableSession genSession(SessionContext sessionContext) {
        long now = System.currentTimeMillis();
        ReusableSession reusableSession = new ReusableSession();
        reusableSession.setSessionContext(sessionContext);
        reusableSession.setExpireTime(now + expiredTime * 1000);
        reusableSession.setSessionId(MD5Utils.encrypt(sessionContext.getDeviceId()+ now));
        return reusableSession;
    }



}
