package com.caisheng.cheetah.api.event;

import com.caisheng.cheetah.api.connection.Connection;

public class ConnectionCloseEvent implements Event{
    private final Connection connection;

    public ConnectionCloseEvent(Connection connection) {
        this.connection = connection;
    }

    public Connection getConnection() {
        return connection;
    }
}
