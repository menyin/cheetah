package com.caisheng.cheetah.api.push;

import com.caisheng.cheetah.api.router.ClientLocation;

public interface PushCallback {
    default void onResult(PushResult pushResult){
        switch (pushResult.getResultCode()) {
            case PushResult.CODE_SUCCESS:
                onSuccess(pushResult.getUserId(), pushResult.getClientLocation());
                break;
            case PushResult.CODE_FAILURE:
                onFailure(pushResult.getUserId(), pushResult.getClientLocation());
                break;
            case PushResult.CODE_OFFLINE:
                onOffline(pushResult.getUserId(), pushResult.getClientLocation());
                break;
            case PushResult.CODE_TIMEOUT:
                onTimeout(pushResult.getUserId(), pushResult.getClientLocation());
                break;
        }
    }

    default void onTimeout(String userId, ClientLocation clientLocation){

    }

    default void onOffline(String userId, ClientLocation clientLocation){

    }

    default void onFailure(String userId, ClientLocation clientLocation){

    }

    default void onSuccess(String userId, ClientLocation clientLocation) {
    }
}
