package com.caisheng.cheetah.api.event;

import com.caisheng.cheetah.api.connection.Connection;

public class UserOnlineEvent implements Event {
    private final Connection connection;
    private final String userId;

    public UserOnlineEvent(Connection connection, String userId) {
        this.connection = connection;
        this.userId = userId;
    }

    public Connection getConnection() {
        return connection;
    }

    public String getUserId() {
        return userId;
    }
}
