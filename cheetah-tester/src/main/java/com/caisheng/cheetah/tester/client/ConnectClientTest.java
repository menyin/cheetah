package com.caisheng.cheetah.tester.client;

import com.caisheng.cheetah.api.srd.CommonServiceNode;
import com.caisheng.cheetah.api.srd.ServiceNode;
import com.caisheng.cheetah.client.connect.ClientConfig;
import com.caisheng.cheetah.common.security.CipherBox;
import com.caisheng.cheetah.tools.config.CC;
import com.caisheng.cheetah.tools.log.Logs;
import io.netty.channel.ChannelFuture;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class ConnectClientTest {
    public static void main(String[] args) {
        int count = 1;
        String userPrefix = "";
        int printDelay = 1;
        boolean sync = true;
        if (args.length > 0) {
            count = Integer.valueOf(args[0]);
        }
        if (args.length > 1) {
            userPrefix = args[1].trim();
        }
        if (args.length > 2) {
            printDelay = Integer.valueOf(args[2]);
        }
        if (args.length > 3) {
            sync = Boolean.valueOf(args[3]);
        }

        testConnectClient(count,userPrefix,printDelay,sync);
    }

    private static void testConnectClient(int count, String userPrefix, int printDelay, boolean sync) throws ExecutionException, InterruptedException {
        Logs.init();
        ConnectClientBoot connectClientBoot = new ConnectClientBoot();
        connectClientBoot.start().get();
        //模拟请求负载均衡服务
        Map<String, Object> attrs = new HashMap<>();
        attrs.put("weight", 1);
        CommonServiceNode node1 = new CommonServiceNode(
                "192.168.1.11",8010,attrs,
                "server1","node1",false
        );
        List<ServiceNode> serviceNodes = Arrays.asList(node1);

        for (int i = 0; i < count; i++) {
            String clientVersion = "1.0." + i;
            String osName = "android";
            String osVersion = "1.0.1";
            String userId = userPrefix + "user-" + i;
            String deviceId = userPrefix + "test-device-id-" + i;
            byte[] clientKey = CipherBox.I.randomAESKey();
            byte[] iv = CipherBox.I.randomAESIV();

            ClientConfig config = new ClientConfig();
            config.setClientKey(clientKey);
            config.setIv(iv);
            config.setClientVersion(clientVersion);
            config.setDeviceId(deviceId);
            config.setOsName(osName);
            config.setOsVersion(osVersion);
            config.setUserId(userId);

            int L = serviceNodes.size();
            int index = (int) ((Math.random() % L) * L);
            ServiceNode node = serviceNodes.get(index);//cny_note 这里相当于负载均衡
            ChannelFuture future = connectClientBoot.connect(node.getHost(), node.getPort(), config);
            if (sync) future.awaitUninterruptibly();
        }


    }
}
