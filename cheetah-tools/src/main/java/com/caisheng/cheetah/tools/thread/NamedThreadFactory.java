package com.caisheng.cheetah.tools.thread;


import com.caisheng.cheetah.tools.config.CC;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 在netty之外的线程工厂，生产的每个线程都携带者相关业务名称前缀、线程数计数、所属线程组
 */
public class NamedThreadFactory implements ThreadFactory{
    private AtomicInteger threadNumber = new AtomicInteger(0);
    private String namePrefix;
    private ThreadGroup threadGroup;

    public NamedThreadFactory() {
        this(ThreadNames.THREAD_NAME_PREFIX);
    }

    public NamedThreadFactory(String namePrefix) {
        this.namePrefix = namePrefix;
        this.threadGroup=Thread.currentThread().getThreadGroup();
    }

    public Thread newThread(String name,Runnable r){
        Thread thread = new Thread(this.threadGroup, r, namePrefix+"-"+this.threadNumber.getAndIncrement()+name);
        thread.setDaemon(true);
        return thread;
    }

    @Override
    public Thread newThread(Runnable r) {
        return newThread("none", r);
    }

    public static ThreadFactory build(){
        return new NamedThreadFactory();
    }
    public static ThreadFactory build(String namePrefix){
        return new NamedThreadFactory(namePrefix);
    }
}
