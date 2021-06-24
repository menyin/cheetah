package com.caisheng.cheetah.core.router;

import com.caisheng.cheetah.api.connection.Connection;
import com.caisheng.cheetah.api.router.Router;

public class LocalRouter implements Router<Connection>{
    private final Connection connection;

    public LocalRouter (Connection connection) {
        this.connection = connection;
    }

    public int getClientType() {
        return this.connection.getSessionConext().getClientType();
    }

    @Override
    public Connection getRouterValue() {
        return this.connection;
    }

    @Override
    public RouterType getRouterType() {
        return RouterType.LOCAL;
    }

    @Override
    public boolean equals(Object obj) {
        if (this==obj) {
            return true;
        }
        if (obj.getClass() != this.getClass()) {
            return false;
        }
        LocalRouter localRouter = (LocalRouter) obj;
        return this.getClientType()==localRouter.getClientType();
    }
    @Override
    public int hashCode() {
        return Integer.hashCode(getClientType());
    }

    @Override
    public String toString() {
        return "LocalRouter{" + connection + '}';
    }
}
