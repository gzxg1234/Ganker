package com.sanron.ganker.model;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.jackson.JacksonConverterFactory;

/**
 * Created by sanron on 16-6-28.
 */
public class GankerRetrofit {

    private static class HOLDER {
        private static final GankerRetrofit INSTANCE = new GankerRetrofit();
    }

    public static GankerRetrofit get() {
        return HOLDER.INSTANCE;
    }

    private Retrofit mRetrofit;
    private GankService mGankService;

    public GankerRetrofit() {
        OkHttpClient httpClient = new OkHttpClient.Builder()
                .connectTimeout(5, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .addInterceptor(new Interceptor() {
                    @Override
                    public Response intercept(Chain chain) throws IOException {
                        System.out.println(chain.request().url());
                        return chain.proceed(chain.request());
                    }
                })
                .build();


        mRetrofit = new Retrofit.Builder()
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .addConverterFactory(JacksonConverterFactory.create())
                .baseUrl(GankService.BASE_URL)
                .client(httpClient)
                .build();

        mGankService = mRetrofit.create(GankService.class);
    }


    public GankService getGankService() {
        return mGankService;
    }
}
