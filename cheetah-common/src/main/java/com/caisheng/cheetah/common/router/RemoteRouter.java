package com.caisheng.cheetah.common.router;

import com.caisheng.cheetah.api.router.ClientLocation;
import com.caisheng.cheetah.api.router.Router;

public class RemoteRouter implements Router<ClientLocation> {
    private final ClientLocation clientLocation;

    public RemoteRouter(ClientLocation clientLocation) {
        this.clientLocation = clientLocation;
    }


    public boolean isOnline(){
        return clientLocation.isOnline();
    }

    public boolean isOffline(){
        return clientLocation.isOffline();
    }

    @Override
    public String toString() {
        return "RemoteRouter{" + clientLocation + '}';
    }

    @Override
    public ClientLocation getRouterValue() {
        return this.clientLocation;
    }

    @Override
    public RouterType getRouterType() {
        return RouterType.REMOTE;
    }
}
