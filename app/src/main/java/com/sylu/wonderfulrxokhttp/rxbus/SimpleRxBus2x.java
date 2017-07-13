package com.sylu.wonderfulrxokhttp.rxbus;

/**
 * Created by Hudsvi on 2017/7/11.
 */

import org.reactivestreams.Subscriber;

import io.reactivex.Flowable;
import io.reactivex.processors.FlowableProcessor;
import io.reactivex.processors.PublishProcessor;

/**
 * TODO ------关于subject（2.x之前）：
 * 它可以是一个Observable同时也可以是一个Observer：一个Subject可以订阅一个Observable，就像一个观察者，并且它可以
 * 发射新的数据，或者传递它接受到的数据，就像一个Observable。作为一个Observable，观察者们或者其它Subject
 * 都可以订阅它，但同时，它的缺点是不支持BackPressure（当然，如果非要使用,还是有办法的，如onBackPressureBuffer这些操作）,
 * 如果Observable数据发射过快，可能会造成重要数据的丢失。
 * TODO ------关于processor(基于2.x):
 * 四个subject分别改为AsyncProcessor，BehaviorProcessor,PublishProcessor,ReplayProcessor，这四个的区别阅读者可自行
 * 博客了解他们的用法区别。2.x中，只有Flowable支持背压，背压策略有BUFFER,DROP,LATEST三种。对于Buffer，如果不指定缓存大
 * 小，则默认为128，如果指定，那么最小值不会低于16（如果小于16）。Drop则是丢弃后面那些因为溢出而添加的数据。Latest
 * 则是丢弃溢出部分，溢出后，直接发射溢出的最后一个数据。
 */
public class SimpleRxBus2x {
    private static SimpleRxBus2x instance;
    private final FlowableProcessor<Object> publishProssor;

    private SimpleRxBus2x() {
        /**
         * 用toSerialized（）代替了SerializedSubject类的转换*/
        publishProssor = PublishProcessor.create().toSerialized();
    }

    public static SimpleRxBus2x getInstance() {
        if (instance == null) {
            synchronized (SimpleRxBus2x.class) {
                if (instance == null)
                    instance = new SimpleRxBus2x();
            }
        }
        return instance;
    }

    public void post(Object o) {
        try {
            publishProssor.onNext(o);
        } catch (Exception e) {
            publishProssor.onError(e);
        } finally {
            publishProssor.onComplete();
        }
    }

    /**
     * 绑定特定类型并输出Flowable对象，ofType方法其实就是调用了map方法
     */
    public <T> Flowable<T> toFlowable(Class<T> type) {
        return publishProssor.ofType(type);
    }

    public Flowable<Object> toFlowable() {
        return publishProssor;
    }

    public boolean hasSubscribers() {
        return publishProssor.hasSubscribers();
    }
}
