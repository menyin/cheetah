package com.caisheng.cheetah.api.service;

import java.util.concurrent.CompletableFuture;

/**
 * 规约一个服务应该具备的能力
 * 服务可以是前端的也可以是后端的
 */
public interface Service {

    void start(Listener listener);
    void stop(Listener listener);
    /**
     *这两个用于异步启动和停止
     * 用CompletableFuture框架
     */
    CompletableFuture<Boolean> start();
    CompletableFuture<Boolean> stop();

    /**
     *这两个用于同步启动和停止
     */
    boolean syncStart();
    boolean syncStop();

    /**
     * 初始化操作
     */
     void init();

    /**
     * 是否运行中
     * @return
     */
    boolean isRunning();

}
