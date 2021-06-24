package com.caisheng.cheetah.api.service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 此类是Listener的包装类
 * 扩展功能：
 *  使用该包装类可以做promise规范
 *  使用该包装类可以做服务操作过程中超时引发异步回调
 */
public class FutureListener extends CompletableFuture<Boolean> implements Listener {
    private AtomicBoolean started;
    private Listener listener;

    public FutureListener(Listener listener) {
        this.listener = listener;
    }

    public FutureListener(Listener listener,AtomicBoolean started) {
        this.started = started;
        this.listener = listener;
    }

    @Override
    public void onSuccess(Object ...object) {
        if (isDone()) {
            return;
        }//如果当前CompletableFuture实例已经执行完成则return
        complete(started.get());
        if (this.listener != null) {
            this.listener.onSuccess(object);
        }
    }


    @Override
    public void onFailure(Throwable cause) {
        if (isDone()) return;
        completeExceptionally(cause);
        if (this.listener != null) {
            this.listener.onFailure(cause);
        }
    }

    /**
     * 监控服务启动和停止动作中的超时异常，并执行回调
     * @param baseService
     */
    public void monitor(BaseService baseService) {
        this.runAsync(() -> {
            try {
                this.get(baseService.timeoutMillis(), TimeUnit.MILLISECONDS);
            } catch (Exception e) {
                this.onFailure(new ServiceException(String.format("service %s monitor timeout",baseService.getClass().getSimpleName())));
            }
        });
    }

}
