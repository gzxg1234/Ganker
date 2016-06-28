package com.sanron.ganker.util;

import rx.Observable;
import rx.subjects.PublishSubject;
import rx.subjects.SerializedSubject;
import rx.subjects.Subject;

/**
 * Created by sanron on 16-6-28.
 */
public class RxBus {

    private static class HOLDER {
        private static final RxBus INSTANCE = new RxBus();
    }

    public static RxBus get() {
        return HOLDER.INSTANCE;
    }

    private Subject mSubject;

    public RxBus() {
        super();
        mSubject = new SerializedSubject<>(PublishSubject.create());
    }

    public void post(Object o) {
        mSubject.onNext(o);
    }

    public <T> Observable<T> toObserverable(Class<T> eventType) {
        return mSubject.ofType(eventType);
    }
}
