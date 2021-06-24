package com.caisheng.cheetah.api.service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * 此类规约了一个基础服务的启动过程和状态管理的实现
 */
public abstract class BaseService implements Service {
    protected AtomicBoolean started = new AtomicBoolean();//是否启动标志，防止多线程多次启动

    protected void tryStart(Listener listener, ConsumerEx<Listener> consumer) {//cny_note consumer未按原计划
        FutureListener futureListener = wrap(listener);
        if (started.compareAndSet(false, true)) {//？一个大的服务启动一个小服务如ZKClient只能启动一次，如出错则整个应用服务都要重新启动
            try {
                this.init();//进行初始化工作
                consumer.accept(futureListener);//listener被具体的启动过程consumer所消费
                futureListener.monitor(this);//监控启动过程是否超时
            } catch (Throwable cause) {
                futureListener.onFailure(cause);
                //throw new ServiceException(e);//都已经执行了失败回调，就不抛出异常了
            }
        } else {
            if (throwIfStart()) {
                futureListener.onFailure(new ServiceException(String.format("service %s already started.", this.getClass().getSimpleName())));
            } else {
                futureListener.onSuccess();
            }
        }
    }

    /**
     * 子类可重写，自己决定在服务已启动后再次启动是执行失败回调或成功回调
     * @return 默认为true，执行失败回调
     */
    protected boolean throwIfStart() {
        return true;
    }
    /**
     * 子类可重写，自己决定在服务已停止后再次停止是执行失败回调或成功回调
     * @return 默认为true，执行失败回调
     */
    protected boolean throwIfStop() {
        return true;
    }

    private FutureListener wrap(Listener listener) {
        if (listener instanceof FutureListener) {
            return (FutureListener) listener;
        }
        return new FutureListener(listener, this.started);//包括listener=null的情况
    }

    @Override
    public void start(Listener listener) {
        tryStart(listener, this::doStart);
    }

    @Override
    public void stop(Listener listener) {
        tryStop(listener, this::doStop);
    }

    protected void tryStop(Listener listener,ConsumerEx<Listener> consumer){
        FutureListener futureListener = this.wrap(listener);
        if (started.compareAndSet(true, false)) {
            try {
                init();
                consumer.accept(futureListener);//在消费futureListener过程一般是根据现实业务执行监听的响应回调
                futureListener.monitor(this);
            } catch (Throwable throwable) {
                futureListener.onFailure(throwable);
//                throw new ServiceException(throwable);//既然有传递listener了，就不做异常抛出了
            }
        } else {
            if (throwIfStop()) {
                futureListener.onFailure(new ServiceException(String.format("service %s already stop.", this.getClass().getSimpleName())));
            } else {
                futureListener.onSuccess();
            }


        }
    }

    /**
     * 具体服务实例（BaseService的子类）重写该启动过程实现
     *
     * @param listener 在上述停止过程的某些阶段调用该listener的回调方法，实现promise
     */
    protected void doStop(Listener listener) throws Throwable {
        //服务停止成功情况下
        listener.onSuccess();
        //服务停止失败情况下
        //listener.onFailure(cause);

    }

    /**
     * 开发者调用此方法可获得一个future对象，就可以用这个对象做同步或做监听
     * @return
     */
    @Override
    public CompletableFuture<Boolean> start() {
        FutureListener futureListener = new FutureListener(null,this.started);
        start(futureListener);
        return futureListener;
    }

    @Override
    public CompletableFuture<Boolean> stop() {
        FutureListener futureListener = new FutureListener(null, this.started);
        stop(futureListener);
        return futureListener;
    }

    @Override
    public boolean syncStart() {
        return start().join();
    }

    @Override
    public boolean syncStop() {
        return false;
    }

    @Override
    public void init() {

    }

    @Override
    public boolean isRunning() {
        return started.get();
    }

    /**
     * 具体服务实例（BaseService的子类）重写该启动过程实现
     *
     * @param listener 在上述启动过程的某些阶段调用该listener的回调方法，实现promise
     */
    protected void doStart(Listener listener) throws Throwable {
        listener.onSuccess();//默认简单实现，实际子类肯定会有一些过程
    }

    /**
     * 服务启动停止，超时时间, 默认是10s
     * 用于启动或停止服务时超过该时间则抛异常并进行相应处理
     *
     * @return 超时时间
     */
    protected int timeoutMillis() {
        return 1000 * 10;
    }

    @FunctionalInterface
    public interface ConsumerEx<T>{
        void accept(T t) throws Throwable;//强行做检查异常
    }
}
