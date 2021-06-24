package com.caisheng.cheetah.api.router;

import java.util.Set;

public interface RouterManager<R extends Router> {

    /**
     *
     * @param userId
     * @param router
     * @return 返回旧的路由，
     */
    R register(String userId,R router);


    /**
     * 只有userId和clientType两个维度确定一个设备
     * @param userId
     * @param clientType
     * @return
     */
    boolean unRegister(String userId, int clientType);

    /**
     *
     * @param userId
     * @return 返回set保证不重复
     */
    Set<R> lookupAll(String userId);

    R lookup(String userId, int clientType);
}
