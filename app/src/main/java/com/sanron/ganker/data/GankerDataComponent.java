package com.sanron.ganker.data;

import javax.inject.Singleton;

import dagger.Component;

/**
 * Created by sanron on 16-7-6.
 */

@Singleton
@Component(
        modules = {
                GankerDataModule.class
        }
)
public interface GankerDataComponent {
    GankService getGankService();
}
