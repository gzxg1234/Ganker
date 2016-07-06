package com.sanron.ganker.category_gank;

import com.sanron.ganker.data.GankService;

import dagger.Module;
import dagger.Provides;

/**
 * Created by sanron on 16-7-6.
 */
@Module
public class CategoricalGankModule {

    private String mCategory;

    public CategoricalGankModule(String category) {
        mCategory = category;
    }

    @Provides
    CategoricalGankContract.Presenter provideGankCategoryPresenter(GankService gankService) {
        return new CategoricalGankPresenter(gankService, mCategory);
    }
}
