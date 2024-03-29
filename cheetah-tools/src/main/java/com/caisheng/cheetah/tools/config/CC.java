
package com.caisheng.cheetah.tools.config;

import com.caisheng.cheetah.api.spi.net.DnsMapping;
import com.caisheng.cheetah.tools.config.data.RedisNode;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigList;

import java.io.File;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static java.util.stream.Collectors.toCollection;

/**
 * 配置文件转对象工具类
 */
public interface CC {
    Config cfg = load();

    static Config load() {
        Config config = ConfigFactory.load();//扫描加载所有可用的配置文件
        String custom_conf = "lion.conf";//加载自定义配置, 值来自jvm启动参数指定-Dlion.conf
        if (config.hasPath(custom_conf)) {
            File file = new File(config.getString(custom_conf));
            if (file.exists()) {
                Config custom = ConfigFactory.parseFile(file);
                config = custom.withFallback(config);//custom和config合并，以custom为准
            }
        }
        return config;
    }

    interface lion {
        Config cfg = CC.cfg.getObject("lion").toConfig();
        String log_dir = cfg.getString("log-dir");
        String log_level = cfg.getString("log-level");
        String log_conf_path = cfg.getString("log-conf-path");

        interface core {
            Config cfg = lion.cfg.getObject("core").toConfig();

            int session_expired_time = (int) cfg.getDuration("session-expired-time").getSeconds();

            int max_heartbeat = (int) cfg.getDuration("max-heartbeat", TimeUnit.MILLISECONDS);

            int max_packet_size = (int) cfg.getMemorySize("max-packet-size").toBytes();

            int min_heartbeat = (int) cfg.getDuration("min-heartbeat", TimeUnit.MILLISECONDS);

            long compress_threshold = cfg.getBytes("compress-threshold");

            int max_hb_timeout_times = cfg.getInt("max-hb-timeout-times");

            String epoll_provider = cfg.getString("epoll-provider");

            static boolean useNettyEpoll() {
                if (!"netty".equals(lion.core.epoll_provider)) return false;
                String name = CC.cfg.getString("os.name").toLowerCase(Locale.UK).trim();
                return name.startsWith("linux");//只在linux下使用netty提供的epoll库
            }
        }

        interface net {
            Config cfg = lion.cfg.getObject("net").toConfig();

            String local_ip = cfg.getString("local-ip");
            String public_ip = cfg.getString("public-ip");

            int connect_server_port = cfg.getInt("connect-server-port");
            String connect_server_bind_ip = cfg.getString("connect-server-bind-ip");
            String connect_server_register_ip = cfg.getString("connect-server-register-ip");
            Map<String, Object> connect_server_register_attr = cfg.getObject("connect-server-register-attr").unwrapped();


            int admin_server_port = cfg.getInt("admin-server-port");

            int gateway_server_port = cfg.getInt("gateway-server-port");
            String gateway_server_bind_ip = cfg.getString("gateway-server-bind-ip");
            String gateway_server_register_ip = cfg.getString("gateway-server-register-ip");
            String gateway_server_net = cfg.getString("gateway-server-net");
            String gateway_server_multicast = cfg.getString("gateway-server-multicast");
            String gateway_client_multicast = cfg.getString("gateway-connect-multicast");
            int gateway_client_port = cfg.getInt("gateway-connect-port");

            int ws_server_port = cfg.getInt("ws-server-port");
            String ws_path = cfg.getString("ws-path");
            int gateway_client_num = cfg.getInt("gateway-connect-num");

            static boolean tcpGateway() {
                return "tcp".equals(gateway_server_net);
            }

            static boolean udpGateway() {
                return "udp".equals(gateway_server_net);
            }

            static boolean wsEnabled() {
                return ws_server_port > 0;
            }

            static boolean udtGateway() {
                return "udt".equals(gateway_server_net);
            }

            static boolean sctpGateway() {
                return "sctp".equals(gateway_server_net);
            }


            interface public_ip_mapping {

                Map<String, Object> mappings = net.cfg.getObject("public-host-mapping").unwrapped();

                static String getString(String localIp) {
                    return (String) mappings.get(localIp);
                }
            }

            interface snd_buf {
                Config cfg = net.cfg.getObject("snd_buf").toConfig();
                int connect_server = (int) cfg.getMemorySize("connect-server").toBytes();
                int gateway_server = (int) cfg.getMemorySize("gateway-server").toBytes();
                int gateway_client = (int) cfg.getMemorySize("gateway-connect").toBytes();
            }

            interface rcv_buf {
                Config cfg = net.cfg.getObject("rcv_buf").toConfig();
                int connect_server = (int) cfg.getMemorySize("connect-server").toBytes();
                int gateway_server = (int) cfg.getMemorySize("gateway-server").toBytes();
                int gateway_client = (int) cfg.getMemorySize("gateway-connect").toBytes();
            }

            interface write_buffer_water_mark {
                Config cfg = net.cfg.getObject("write-buffer-water-mark").toConfig();
                int connect_server_low = (int) cfg.getMemorySize("connect-server-low").toBytes();
                int connect_server_high = (int) cfg.getMemorySize("connect-server-high").toBytes();
                int gateway_server_low = (int) cfg.getMemorySize("gateway-server-low").toBytes();
                int gateway_server_high = (int) cfg.getMemorySize("gateway-server-high").toBytes();
            }

            interface traffic_shaping {
                Config cfg = net.cfg.getObject("traffic-shaping").toConfig();

                interface gateway_client {
                    Config cfg = traffic_shaping.cfg.getObject("gateway-connect").toConfig();
                    boolean enabled = cfg.getBoolean("enabled");
                    long check_interval = cfg.getDuration("check-interval", TimeUnit.MILLISECONDS);
                    long write_global_limit = cfg.getBytes("write-global-limit");
                    long read_global_limit = cfg.getBytes("read-global-limit");
                    long write_channel_limit = cfg.getBytes("write-channel-limit");
                    long read_channel_limit = cfg.getBytes("read-channel-limit");
                }

                interface gateway_server {
                    Config cfg = traffic_shaping.cfg.getObject("gateway-server").toConfig();
                    boolean enabled = cfg.getBoolean("enabled");
                    long check_interval = cfg.getDuration("check-interval", TimeUnit.MILLISECONDS);
                    long write_global_limit = cfg.getBytes("write-global-limit");
                    long read_global_limit = cfg.getBytes("read-global-limit");
                    long write_channel_limit = cfg.getBytes("write-channel-limit");
                    long read_channel_limit = cfg.getBytes("read-channel-limit");
                }

                interface connect_server {
                    Config cfg = traffic_shaping.cfg.getObject("connect-server").toConfig();
                    boolean enabled = cfg.getBoolean("enabled");
                    long check_interval = cfg.getDuration("check-interval", TimeUnit.MILLISECONDS);
                    long write_global_limit = cfg.getBytes("write-global-limit");
                    long read_global_limit = cfg.getBytes("read-global-limit");
                    long write_channel_limit = cfg.getBytes("write-channel-limit");
                    long read_channel_limit = cfg.getBytes("read-channel-limit");
                }
            }
        }

        interface security {

            Config cfg = lion.cfg.getObject("security").toConfig();

            int aes_key_length = cfg.getInt("aes-key-length");

            String public_key = cfg.getString("public-key");

            String private_key = cfg.getString("private-key");

        }

        interface thread {

            Config cfg = lion.cfg.getObject("thread").toConfig();

            interface pool {

                Config cfg = thread.cfg.getObject("pool").toConfig();

                int conn_work = cfg.getInt("conn-work");
                int http_work = cfg.getInt("http-work");
                int push_task = cfg.getInt("push-task");
                int push_client = cfg.getInt("push-connect");
                int ack_timer = cfg.getInt("ack-timer");
                int gateway_server_work = cfg.getInt("gateway-server-work");
                int gateway_client_work = cfg.getInt("gateway-connect-work");

                interface event_bus {
                    Config cfg = pool.cfg.getObject("event-bus").toConfig();
                    int min = cfg.getInt("min");
                    int max = cfg.getInt("max");
                    int queue_size = cfg.getInt("queue-size");

                }

                interface mq {
                    Config cfg = pool.cfg.getObject("mq").toConfig();
                    int min = cfg.getInt("min");
                    int max = cfg.getInt("max");
                    int queue_size = cfg.getInt("queue-size");
                }
            }
        }

        interface zk {

            Config cfg = lion.cfg.getObject("zk").toConfig();

            int sessionTimeoutMs = (int) cfg.getDuration("sessionTimeoutMs", TimeUnit.MILLISECONDS);

            String watch_path = cfg.getString("watch-path");

            int connectionTimeoutMs = (int) cfg.getDuration("connectionTimeoutMs", TimeUnit.MILLISECONDS);

            String namespace = cfg.getString("namespace");

            String digest = cfg.getString("digest");

            String server_address = cfg.getString("server-address");

            interface retry {

                Config cfg = zk.cfg.getObject("retry").toConfig();

                int maxRetries = cfg.getInt("maxRetries");

                int baseSleepTimeMs = (int) cfg.getDuration("baseSleepTimeMs", TimeUnit.MILLISECONDS);

                int maxSleepMs = (int) cfg.getDuration("maxSleepMs", TimeUnit.MILLISECONDS);
            }
        }

        interface redis {
            Config cfg = lion.cfg.getObject("redis").toConfig();

            String password = cfg.getString("password");
            String clusterModel = cfg.getString("cluster-model");
            String sentinelMaster = cfg.getString("sentinel-master");

            List<RedisNode> nodes = cfg.getList("nodes")
                    .stream()//第一纬度数组
                    .map(v -> RedisNode.from(v.unwrapped().toString()))
                    .collect(toCollection(ArrayList::new));

            static boolean isCluster() {
                return "cluster".equals(clusterModel);
            }

            static boolean isSentinel() {
                return "sentinel".equals(clusterModel);
            }

            static <T> T getPoolConfig(Class<T> clazz) {
                return ConfigBeanImpl.createInternal(cfg.getObject("config").toConfig(), clazz);
            }
        }

        interface http {

            Config cfg = lion.cfg.getObject("http").toConfig();
            boolean proxy_enabled = cfg.getBoolean("proxy-enabled");
            int default_read_timeout = (int) cfg.getDuration("default-read-timeout", TimeUnit.MILLISECONDS);
            int max_conn_per_host = cfg.getInt("max-conn-per-host");


            long max_content_length = cfg.getBytes("max-content-length");

            Map<String, List<DnsMapping>> dns_mapping = loadMapping();

            static Map<String, List<DnsMapping>> loadMapping() {
                Map<String, List<DnsMapping>> map = new HashMap<>();
                cfg.getObject("dns-mapping").forEach((s, v) ->
                        map.put(s, ConfigList.class.cast(v)
                                .stream()
                                .map(cv -> DnsMapping.parse((String) cv.unwrapped()))
                                .collect(toCollection(ArrayList::new))
                        )
                );
                return map;
            }
        }

        interface push {

            Config cfg = lion.cfg.getObject("push").toConfig();

            interface flow_control {

                Config cfg = push.cfg.getObject("flow-control").toConfig();

                interface global {
                    Config cfg = flow_control.cfg.getObject("global").toConfig();
                    int limit = cfg.getNumber("limit").intValue();
                    int max = cfg.getInt("max");
                    int duration = (int) cfg.getDuration("duration").toMillis();
                }

                interface broadcast {
                    Config cfg = flow_control.cfg.getObject("broadcast").toConfig();
                    int limit = cfg.getInt("limit");
                    int max = cfg.getInt("max");
                    int duration = (int) cfg.getDuration("duration").toMillis();
                }
            }
        }

        interface monitor {
            Config cfg = lion.cfg.getObject("monitor").toConfig();
            String dump_dir = cfg.getString("dump-dir");
            boolean dump_stack = cfg.getBoolean("dump-stack");
            boolean print_log = cfg.getBoolean("print-log");
            Duration dump_period = cfg.getDuration("dump-period");
            boolean profile_enabled = cfg.getBoolean("profile-enabled");
            Duration profile_slowly_duration = cfg.getDuration("profile-slowly-duration");
        }
    }
}