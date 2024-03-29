
package com.caisheng.cheetah.api;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public interface Constants {
    Charset UTF_8 = StandardCharsets.UTF_8;
    byte[] EMPTY_BYTES = new byte[0];
    String HTTP_HEAD_READ_TIMEOUT = "readTimeout";
    String EMPTY_STRING = "";
    String ANY_HOST = "0.0.0.0";
    String KICK_CHANNEL_PREFIX = "/cheetah/kick/";

    static String getKickChannel(String hostAndPort) {
        return KICK_CHANNEL_PREFIX + hostAndPort;
    }

}
