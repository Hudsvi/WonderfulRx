package com.sylu.wonderfulrxokhttp.rxbus;

/**
 * Created by Hudsvi on 2017/7/11.
 */

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import io.reactivex.Flowable;
import io.reactivex.annotations.NonNull;
import io.reactivex.flowables.ConnectableFlowable;
import io.reactivex.processors.FlowableProcessor;
import io.reactivex.processors.PublishProcessor;

/**
 * TODO 你可能会问我为什么还要使用RxBus，直接使用SimpleRxBus不是更方便吗？
 * 其实原因很简单，因为SimpleRxBus不便于管理，如unSubscriber,而RxBus则容易多了，RxBus通过将每个
 * 不同标签的Flowable添加到ConcurrentHashMap中,而相同标签时则存入List中，List再存入Map中。
 * 这样一来，对于以后的管理工作将变得十分容易。
 */
public class RxBus2x {

    //    private final FlowableProcessor<Object> publishProcessor;
    private static RxBus2x instance;
    /**
     * ConcurrentHashMap和HashTable很相似，但有些方法并不是线程安全的，有时候效率可能会更好，例如get等方法。
     */
    private ConcurrentHashMap<Object, List<PublishProcessor>> processorMap = new ConcurrentHashMap<>();

    /**
     * 这里使用了toSerialized处理，可保证并发时的同步，但如果你在使用时不关心同步问题，可不使用
     * toSerialized()。 这个类将实现类似Eventbus的功能。
     */
    private RxBus2x() {
//        publishProcessor = PublishProcessor.create().toSerialized();
    }

    public static RxBus2x getInstance() {
        if (instance == null) {
            synchronized (RxBus2x.class) {
                if (instance == null) {
                    instance = new RxBus2x();
                }
            }
        }
        return instance;
    }

    /**
     * 不绑定数据的类型。
     */
    public <T> Flowable<T> register(@NonNull Object tag) {
        List<PublishProcessor> list = processorMap.get(tag);
        if (list == null) {
            list = new ArrayList<>();
            processorMap.put(tag, list);
        }
        PublishProcessor<T> publishP;
        list.add(publishP = PublishProcessor.<T>create());
        return publishP;
    }

    /**
     * 绑定数据的类型
     */
    public <T> Flowable<T> register(@NonNull Object tag, @NonNull Class<T> type) {
        List<PublishProcessor> list = processorMap.get(tag);
        if (list == null) {
            list = new ArrayList<>();
            processorMap.put(tag, list);
        }
        PublishProcessor<T> publish;
        list.add(publish = PublishProcessor.<T>create());
        return publish.ofType(type);
    }

    public <T> ConnectableFlowable<T> register(@NonNull Object tag, int strategy) {
        List<PublishProcessor> list = processorMap.get(tag);
        if (list == null) {
            list = new ArrayList<>();
            processorMap.put(tag, list);
        }
        PublishProcessor<T> publish;
        list.add(publish = PublishProcessor.<T>create());
        switch (strategy) {
            case RxManager.BACKPRESSURE_BUFFER:
                publish.onBackpressureBuffer
                        (Integer.MAX_VALUE,true,true);
                break;
            case RxManager.BACKPRESSURE_DROP:
                publish.onBackpressureDrop();
                break;
            case RxManager.BACKPRESSURE_LATEST:
                publish.onBackpressureLatest();
                break;
        }
        return publish.publish();
    }

    public void unregister(@NonNull Object tag) {
        List<PublishProcessor> list = processorMap.get(tag);
        if (list != null) {
            processorMap.remove(list);
        }
    }

    public RxBus2x unRegister(@NonNull Object tag, Flowable<?> flowable) {
        if (flowable == null) {
            return this;
        }
        List<PublishProcessor> list = processorMap.get(tag);
        if (list != null) {
            list.remove(flowable);
            if (list.isEmpty()) {
                processorMap.remove(tag);
            }
        }
        return this;
    }

    /**
     * post一个事件，但不指定tag。
     */
    public void post(Object event) {
        post(event.getClass().getName(), event);
    }

    public  void post(@NonNull Object tag, Object event) {
        List<PublishProcessor> list = processorMap.get(tag);
        if (!isEmpty(list)) {
            for (int i = 0; i < list.size(); i++) {
                try {
                    list.get(i).onNext(event);
                } catch (Exception e) {
                    list.get(i).onError(e);
                } finally {
                    list.get(i).onComplete();
                }
            }
        }

    }

    private boolean isEmpty(List<PublishProcessor> list) {
        return list == null || list.isEmpty();
    }


}
