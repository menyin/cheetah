package com.caisheng.cheetah.api.event;

import com.caisheng.cheetah.api.connection.Connection;

public class UserOfflineEvent implements Event{
    private final Connection connection;
    private final String userId;

    public UserOfflineEvent(Connection connection, String userId) {
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
