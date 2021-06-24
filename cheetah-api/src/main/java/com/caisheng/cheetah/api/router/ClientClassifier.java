package com.caisheng.cheetah.api.router;

import com.caisheng.cheetah.api.spi.router.ClientClassifierFactory;

public interface ClientClassifier {
    ClientClassifier I= ClientClassifierFactory.create();
    byte getClientType(String osName);
}
