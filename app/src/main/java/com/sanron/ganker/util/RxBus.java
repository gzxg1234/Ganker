package com.sanron.ganker.util;

import rx.Observable;
import rx.subjects.PublishSubject;
import rx.subjects.Subject;

/**
 * Created by sanron on 16-7-5.
 */
public class RxBus {
    private static class HOLDER {
        private static final RxBus INSTANCE = new RxBus();
    }

    public static RxBus getDefault() {
        return HOLDER.INSTANCE;
    }

    private Subject mSubject;

    public RxBus() {
        mSubject = PublishSubject.create();
    }

    public void post(Object event) {
        mSubject.onNext(event);
    }

    public <T> Observable<T> toObservable(Class<T> clazz) {
        return mSubject.ofType(clazz);
    }
}
