package com.caisheng.cheetah.api.spi.push;

import com.caisheng.cheetah.api.spi.Plugin;

import java.util.Objects;

public interface PushListener<T extends IPushMessage> extends Plugin {
    void onSuccess(T message, Object[] timePoints);

    void onAckSuccess(T message, Object[] timePoints);

    void onBroadcastSuccess(T message, Object[] timePoints);

    void onFailure(T message, Object[] timePoints);

    void onOffline(T message, Object[] timePoints);

    void onRedirect(T message, Object[] timePoints);

    void onTimeout(T message, Object[] timePoints);
}
