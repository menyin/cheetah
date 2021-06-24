package com.caisheng.cheetah.tools.log;

import com.caisheng.cheetah.tools.config.CC;
import com.typesafe.config.ConfigRenderOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface Logs {
    boolean logInit = init();

    static boolean init() {
        if (logInit) return true;
        System.setProperty("log.home", CC.lion.log_dir);
        System.setProperty("log.root.level", CC.lion.log_level);//设置系统环境变量  日志级别
        System.setProperty("logback.configurationFile", CC.lion.log_conf_path);
        LoggerFactory
                .getLogger("console")
                .info(CC.lion.cfg.root().render(ConfigRenderOptions.concise().setFormatted(true)));//cny_note 打印lion.conf里的所有配置文本
        return true;
    }

    Logger Console = LoggerFactory.getLogger("console");
    Logger CONN= LoggerFactory.getLogger("lion.conn.log");
    Logger MONITOR= LoggerFactory.getLogger("lion.monitor.log");
    Logger PUSH= LoggerFactory.getLogger("lion.push.log");
    Logger HEARTBEAT= LoggerFactory.getLogger("lion.heartbeat.log");
    Logger CACHE= LoggerFactory.getLogger("lion.cache.log");
    Logger SRD= LoggerFactory.getLogger("lion.srd.log");
    Logger HTTP= LoggerFactory.getLogger("lion.http.log");
    Logger PROFILER= LoggerFactory.getLogger("lion.profiler.log");


}
