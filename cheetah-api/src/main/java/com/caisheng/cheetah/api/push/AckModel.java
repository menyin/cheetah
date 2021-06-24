package com.caisheng.cheetah.api.push;

import com.caisheng.cheetah.api.protocol.Packet;

public enum AckModel {
    NO_ACK((byte)0),
    AUTO_ACK(Packet.FLAG_AUTO_ACK),
    BIZ_ACK(Packet.FLAG_AUTO_ACK);
    private byte flag;

    AckModel(byte flag) {
        this.flag = flag;
    }

    public byte getFlag() {
        return flag;
    }
}
