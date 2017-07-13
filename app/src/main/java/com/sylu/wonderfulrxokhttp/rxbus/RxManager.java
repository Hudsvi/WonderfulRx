package com.sylu.wonderfulrxokhttp.rxbus;

import java.util.HashMap;
import java.util.Map;

import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.flowables.ConnectableFlowable;
import io.reactivex.functions.Consumer;
import io.reactivex.subscribers.ResourceSubscriber;

/**
 * Created by Hudsvi on 2017/7/11.
 */

public class RxManager {
    public static final int BACKPRESSURE_BUFFER = 1;
    public static final int BACKPRESSURE_DROP = 2;
    public static final int BACKPRESSURE_LATEST = 3;
    private static RxManager instance;
    private static RxBus2x bus = RxBus2x.getInstance();
    private final Map<Object, Flowable<?>> flowables = new HashMap<>();

    private RxManager() {

    }

    public static RxManager getInstance() {
        if (instance != null) {
            synchronized (RxManager.class) {
                if (instance != null) {
                    instance = new RxManager();
                }
            }
        }
        return instance;
    }

    public <T> void register(Object tag, ResourceSubscriber<T> subscriber) {
        Flowable<T> flowable = bus.register(tag);
        flowables.put(tag, flowable);
        flowable.observeOn(AndroidSchedulers.mainThread())
                .subscribe(subscriber);
    }

    /**
     * 你可能很疑惑为什么要使用包含type方法的函数调用,它与上面不指定type有何区别。
     * 其实原因很简单，它的作用就是将type类型对象（可以是任何一个对象类型）转换成Flowable对象，
     * 从而完成如下两个功能：1.完成数据的转发工作；2.发送自己的订阅事件。
     */
    public <T> void register(Object tag, Class<T> type, ResourceSubscriber<T> subscriber) {
        Flowable<T> flowable = bus.register(tag, type);
        flowables.put(tag, flowable);
        flowable.observeOn(AndroidSchedulers.mainThread())
                .subscribe(subscriber);
    }

    public <T> void register(Object tag, int strategy, ResourceSubscriber<T> subscriber) {
        ConnectableFlowable<T> flowable = bus.register(tag, strategy);
        flowables.put(tag, flowable);
        flowable.observeOn(AndroidSchedulers.mainThread())
                .subscribe(subscriber);
    }

    public void unregister() {
        for (Map.Entry<Object, Flowable<?>> set : flowables.entrySet()) {
            bus.unRegister(set.getKey(), set.getValue());
        }
    }

    /**
     * 不指定tag,默认getName
     */
    public void post(Object event) {
        if (event == null) return;
        bus.post(event);
    }

    public void post(@NonNull Object tag, Object event) {
        bus.post(tag, event);
    }
}
