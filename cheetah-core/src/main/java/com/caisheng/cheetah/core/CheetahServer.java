package com.caisheng.cheetah.core;

import com.caisheng.cheetah.api.CheetahContext;
import com.caisheng.cheetah.core.push.PushCenter;
import com.caisheng.cheetah.core.router.RouterCenter;
import com.caisheng.cheetah.core.session.ReusableSessionManager;
//TODO
public class CheetahServer implements CheetahContext {





















    public ReusableSessionManager getReusableSessionManager(){
        return null;
    }



    public GatewayServerNode getGatewayServerNode(){
        return null;
    }


    public boolean isTargetMachine(String host, int port) {
        return true;
//        return port == gatewayServerNode.getPort() && gatewayServerNode.getHost().equals(host);
    }
    //    TODO
    public RouterCenter getRouterCenter(){
        return null;
    }

    public PushCenter getPushCenter() {
        return null;
    }
}
