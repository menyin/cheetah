package com.caisheng.cheetah.api.router;

public interface Router<T> {
    T getRouterValue();
    RouterType getRouterType();
    enum RouterType {
        LOCAL, REMOTE
    }
}
