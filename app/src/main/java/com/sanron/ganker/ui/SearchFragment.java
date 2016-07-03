package com.sanron.ganker.ui;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.sanron.ganker.Ganker;
import com.sanron.ganker.R;
import com.sanron.ganker.data.GankerRetrofit;
import com.sanron.ganker.data.entity.Gank;
import com.sanron.ganker.data.entity.SearchData;
import com.sanron.ganker.ui.base.BaseFragment;
import com.sanron.ganker.util.CommonUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTextChanged;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.subjects.PublishSubject;

/**
 * Created by sanron on 16-6-30.
 */
public class SearchFragment extends BaseFragment implements TextView.OnEditorActionListener {

    @BindView(R.id.et_word) EditText etWord;
    @BindView(R.id.iv_back) View ivBack;
    @BindView(R.id.id_clear) View clear;
    @BindView(R.id.list_search_history) RecyclerView mListSearchHistory;
    @BindView(R.id.view_search_result) View mViewSearchResult;
    @BindView(R.id.view_pager) ViewPager mViewPager;
    @BindView(R.id.tab_layout) TabLayout mTabLayout;

    private PublishSubject<String> mTextChangeSubject = PublishSubject.create();
    private InputMethodManager mInputManager;
    private LocalPagerAdapter mSearchPagerAdapter;
    private HistoryAdapter mHistoryAdapter;
    private static final String[] PAGE_CATEGORIES = {Gank.CATEGORY_ANDROID,
            Gank.CATEGORY_IOS, Gank.CATEGORY_FRONT_END, Gank.CATEGORY_EXPAND};

    @Override
    public int getLayoutId() {
        return R.layout.fragment_search;
    }

    public static SearchFragment newInstance() {
        return new SearchFragment();
    }

    @Override
    public void initView(View root, Bundle savedInstanceState) {
        super.initView(root, savedInstanceState);
        mInputManager = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        mHistoryAdapter = new HistoryAdapter();
        etWord.setOnEditorActionListener(this);
        mListSearchHistory.setLayoutManager(new LinearLayoutManager(getContext()));
        mListSearchHistory.setAdapter(mHistoryAdapter);
        mTextChangeSubject.debounce(500, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String s) {
                        search(s);
                    }
                });
        Ganker.get().getSearchHistory()
                .subscribe(new Action1<List<String>>() {
                    @Override
                    public void call(List<String> strings) {
                        mHistoryAdapter.setData(strings);
                    }
                });
    }


    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_SEARCH
                || (event != null && event.getAction() == KeyEvent.KEYCODE_ENTER)) {
            final String word = v.getText().toString();
            if (!TextUtils.isEmpty(word)) {
                if (!mHistoryAdapter.getData().contains(word)) {
                    //保存搜索历史
                    List<String> items = new ArrayList<>(mHistoryAdapter.getData());
                    items.add(word);
                    Ganker.get().saveSearchHistory(items)
                            .subscribe(new Action1<Boolean>() {
                                @Override
                                public void call(Boolean aBoolean) {
                                    if (aBoolean) {
                                        mHistoryAdapter.add(word);
                                    }
                                }
                            });
                }
                mInputManager.hideSoftInputFromWindow(v.getWindowToken(), 0);
            }
            return true;
        }
        return false;
    }


    @OnTextChanged(R.id.et_word)
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        if (TextUtils.isEmpty(s)) {
            clear.setVisibility(View.INVISIBLE);
            mViewSearchResult.setVisibility(View.INVISIBLE);
            mListSearchHistory.setVisibility(View.VISIBLE);
        } else {
            clear.setVisibility(View.VISIBLE);
            mViewSearchResult.setVisibility(View.VISIBLE);
            mListSearchHistory.setVisibility(View.INVISIBLE);
            mTextChangeSubject.onNext(s.toString());
        }
    }

    public void search(String word) {
        if (mSearchPagerAdapter == null) {
            mSearchPagerAdapter = new LocalPagerAdapter(getChildFragmentManager(), word);
            mViewPager.setAdapter(mSearchPagerAdapter);
            mTabLayout.setupWithViewPager(mViewPager);
        } else {
            mSearchPagerAdapter.setWord(word);
        }
    }


    @OnClick(R.id.iv_back)
    public void onBack() {
        getFragmentManager()
                .popBackStack(getClass().getName(), FragmentManager.POP_BACK_STACK_INCLUSIVE);
    }

    @OnClick(R.id.id_clear)
    public void onClear(View v) {
        if (v.isShown()) {
            etWord.setText(null);
        }
    }


    public static class LocalPagerAdapter extends FragmentPagerAdapter {

        public static class SearchGankCreator extends GankPagerFragment.ObservableCreator {
            private String category;
            private String word;

            public SearchGankCreator(String category, String word) {
                this.category = category;
                this.word = word;
            }

            @Override
            public Observable<List<? extends Gank>> onLoad(int pageSize, int page) {
                return GankerRetrofit.get()
                        .getGankService()
                        .search(word, category, pageSize, page)
                        .map(new Func1<SearchData, List<? extends Gank>>() {
                            @Override
                            public List<? extends Gank> call(SearchData searchData) {
                                return searchData.results;
                            }
                        });
            }
        }

        private String mWord;
        //存储已经初始化的fragment
        private SparseArray<GankPagerFragment> mInitedFragment = new SparseArray<>();

        public LocalPagerAdapter(FragmentManager fm, String word) {
            super(fm);
            mWord = word;
        }

        public void setWord(String word) {
            if (word.equals(mWord)) {
                return;
            }
            mWord = word;
            for (int i = 0; i < mInitedFragment.size(); i++) {
                //更新ObservableCreator
                mInitedFragment.valueAt(i).setObservableCreator(
                        new SearchGankCreator(PAGE_CATEGORIES[i], mWord));
            }
        }


        @Override
        public Fragment getItem(final int position) {
            GankPagerFragment gankPagerFragment = GankPagerFragment.newInstance(
                    new SearchGankCreator(PAGE_CATEGORIES[position], mWord));
            mInitedFragment.put(position, gankPagerFragment);
            return gankPagerFragment;
        }

        @Override
        public int getCount() {
            return PAGE_CATEGORIES.length;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return PAGE_CATEGORIES[position];
        }
    }


    public class HistoryAdapter extends RecyclerView.Adapter {

        private List<String> mItems = new ArrayList<>();
        private final int TYPE_ITEM = 0;
        private final int TYPE_CLEAR = 1;
        final int TEXT_COLOR = getContext().getResources().getColor(R.color.textColorSecondary);

        public List<String> getData() {
            return mItems;
        }

        public void add(String item) {
            mItems.add(item);
            notifyItemInserted(getItemCount());
        }

        public void setData(List<String> items) {
            mItems.clear();
            if (items != null) {
                mItems.addAll(items);
            }
            notifyDataSetChanged();
        }

        public void remove(int position) {
            mItems.remove(position);
            notifyItemRangeRemoved(position, mItems.size() == 0 ? 2 : 1);
        }

        @Override
        public int getItemViewType(int position) {
            if (position > 0
                    && position == getItemCount() - 1) {
                return TYPE_CLEAR;
            }
            return TYPE_ITEM;
        }

        private View createClearView() {
            TextView tvClear = new TextView(getContext());
            tvClear.setTextSize(15);
            tvClear.setText("清除搜索记录");
            final int padding = CommonUtil.dpToPx(getContext(), 8);
            tvClear.setPadding(padding, padding, padding, padding);
            tvClear.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));
            tvClear.setGravity(Gravity.CENTER);
            tvClear.setTextColor(getResources().getColor(R.color.colorPrimary));
            return tvClear;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            if (viewType == TYPE_CLEAR) {
                return new FooterHolder(createClearView());
            } else {
                View view = LayoutInflater.from(getContext()).inflate(R.layout.list_history_item, parent, false);
                Holder holder = new Holder(view);
                DrawableCompat.setTint(holder.ivHistory.getDrawable().mutate(), TEXT_COLOR);
                DrawableCompat.setTint(holder.del.getDrawable().mutate(), TEXT_COLOR);
                return holder;
            }
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
            if (holder instanceof FooterHolder) {
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Ganker.get()
                                .saveSearchHistory(new ArrayList<String>())
                                .subscribe(new Action1<Boolean>() {
                                    @Override
                                    public void call(Boolean aBoolean) {
                                        if (aBoolean) {
                                            setData(null);
                                        }
                                    }
                                });
                    }
                });
            } else if (holder instanceof Holder) {
                ((Holder) holder).setWord(mItems.get(position));
                ((Holder) holder).del.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        List<String> items = new ArrayList<>(mHistoryAdapter.getData());
                        items.remove(position);
                        Ganker.get()
                                .saveSearchHistory(items)
                                .subscribe(new Action1<Boolean>() {
                                    @Override
                                    public void call(Boolean aBoolean) {
                                        if (aBoolean) {
                                            remove(position);
                                        }
                                    }
                                });
                    }
                });
                ((Holder) holder).itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mInputManager.hideSoftInputFromWindow(v.getWindowToken(), 0);
                        etWord.setText(mItems.get(position));
                    }
                });
            }
        }

        @Override
        public int getItemCount() {
            int itemSize = mItems.size();
            return itemSize == 0 ? 0 : itemSize + 1;
        }

        class FooterHolder extends RecyclerView.ViewHolder {
            public FooterHolder(View itemView) {
                super(itemView);
            }
        }

        class Holder extends RecyclerView.ViewHolder {

            @BindView(R.id.iv_history)
            ImageView ivHistory;
            @BindView(R.id.tv_word)
            TextView tvWord;
            @BindView(R.id.iv_del)
            ImageView del;
            String word;

            public Holder(View itemView) {
                super(itemView);
                ButterKnife.bind(this, itemView);
            }

            public void setWord(String word) {
                this.word = word;
                tvWord.setText(word);
            }
        }
    }
}
