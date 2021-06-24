package com.caisheng.cheetah.api.service;

/**
 * 规约了成功与失败的回调
 * 可用于任何有成功或失败的异步操作
 */
public interface Listener {
    void onSuccess(Object ...object);
    void onFailure(Throwable cause);
}
