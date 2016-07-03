package com.sanron.ganker.data;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sanron.ganker.Ganker;
import com.sanron.ganker.util.CommonUtil;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Cache;
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

    private static final int CACHE_MAX_SIZE = 2 * 1024 * 1024;//2MB

    private static final Interceptor CACHE_INTERCEPTOR = new Interceptor() {
        @Override
        public Response intercept(Chain chain) throws IOException {
            Response response = chain.proceed(chain.request());
            if (CommonUtil.isNetworkAvaialable(Ganker.get())) {
                String control = response.header("Cache-Control");
                if (control == null) {
                    return response.newBuilder()
                            .addHeader("Cache-Control", "public,max-age=60")
                            .build();
                }
            } else {
                return response.newBuilder()
                        .addHeader("Cache-Control", "public, only-if-cached, max-stale=" + Integer.MAX_VALUE)
                        .build();
            }
            return response;
        }
    };

    private GankerRetrofit() {
        File cacheFile = new File(Ganker.get().getCacheDir(), "http_cache");
        if (!cacheFile.exists()) {
            cacheFile.mkdirs();
        }
        OkHttpClient httpClient = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .cache(new Cache(cacheFile, CACHE_MAX_SIZE))
                .readTimeout(30, TimeUnit.SECONDS)
                .addInterceptor(CACHE_INTERCEPTOR)
                .addNetworkInterceptor(CACHE_INTERCEPTOR)
                .build();

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mRetrofit = new Retrofit.Builder()
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .addConverterFactory(JacksonConverterFactory.create(objectMapper))
                .baseUrl(GankService.BASE_URL)
                .client(httpClient)
                .build();

        mGankService = mRetrofit.create(GankService.class);
    }


    public GankService getGankService() {
        return mGankService;
    }
}
