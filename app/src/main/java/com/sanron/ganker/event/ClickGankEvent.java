package com.sanron.ganker.event;

import com.sanron.ganker.model.entity.Gank;

/**
 * Created by sanron on 16-6-28.
 */
public class ClickGankEvent {
    public Gank gank;

    public ClickGankEvent(Gank gank) {
        this.gank = gank;
    }
}
