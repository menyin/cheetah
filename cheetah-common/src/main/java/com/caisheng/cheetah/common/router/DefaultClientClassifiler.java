package com.caisheng.cheetah.common.router;

import com.caisheng.cheetah.api.router.ClientClassifier;
import com.caisheng.cheetah.api.spi.router.ClientClassifierFactory;

public class DefaultClientClassifiler implements ClientClassifier , ClientClassifierFactory {

    @Override
    public byte getClientType(String osName) {
        return ClientType.find(osName).getType();
    }

    @Override
    public ClientClassifier get() {
        return this;
    }
}
