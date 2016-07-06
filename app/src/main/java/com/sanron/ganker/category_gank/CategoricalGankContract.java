package com.sanron.ganker.category_gank;

import com.sanron.ganker.BasePresenter;
import com.sanron.ganker.data.entity.Gank;
import com.sanron.ganker.view.BaseView;

import java.util.List;

/**
 * Created by sanron on 16-7-6.
 */
public interface CategoricalGankContract {
    interface View extends BaseView {

        void onLoadDataSuccess(List<Gank> ganks);

        void onRefreshData(List<Gank> ganks);

        void onHasNoMore();

        void onLoadDataError();
    }

    interface Presenter extends BasePresenter<View> {
        void loadData();

        void refresh();

        void onDestory();
    }
}
