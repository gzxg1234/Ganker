package com.sanron.ganker.category_gank;

import com.sanron.ganker.data.GankerDataComponent;
import com.sanron.ganker.scope.FragmentScope;

import dagger.Component;

/**
 * Created by sanron on 16-7-6.
 */
@FragmentScope
@Component(
        dependencies = {
                GankerDataComponent.class
        },
        modules = {
                CategoricalGankModule.class
        }
)
public interface CategoricalGankComponent {
    void inject(Fragment categoricalGankFragment);
}
