package com.caisheng.cheetah.api.event;

import com.caisheng.cheetah.api.router.Router;

public class RouterChangeEvent implements Event {
    private String userId;
    private Router<?> router;

    public RouterChangeEvent(String userId, Router<?> router) {
        this.userId = userId;
        this.router = router;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Router<?> getRouter() {
        return router;
    }

    public void setRouter(Router<?> router) {
        this.router = router;
    }
}
