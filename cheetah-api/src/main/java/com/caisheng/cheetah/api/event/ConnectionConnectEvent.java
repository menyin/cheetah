package com.caisheng.cheetah.api.event;

import com.caisheng.cheetah.api.connection.Connection;

public class ConnectionConnectEvent implements Event {
    private Connection connection;

    public ConnectionConnectEvent(Connection connection) {
        this.connection = connection;
    }
}
