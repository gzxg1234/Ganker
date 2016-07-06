package com.sanron.ganker.data;

import android.app.Application;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sanron.ganker.util.Common;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import okhttp3.Cache;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.jackson.JacksonConverterFactory;

/**
 * Created by sanron on 16-7-6.
 */
@Singleton
@Module
public class GankerDataModule {

    private Application mApplication;

    public GankerDataModule(Application application) {
        mApplication = application;
    }

    @Singleton
    @Provides
    OkHttpClient provideOkHttpClient() {

        File cacheFile = new File(mApplication.getCacheDir(), "http_cache");
        if (!cacheFile.exists()) {
            cacheFile.mkdirs();
        }

        final int CACHE_MAX_SIZE = 2 * 1024 * 1024;//2MB
        Interceptor CACHE_INTERCEPTOR = new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Response response = chain.proceed(chain.request());
                if (Common.isNetworkAvaialable(mApplication)) {
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

        return new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .cache(new Cache(cacheFile, CACHE_MAX_SIZE))
                .addInterceptor(CACHE_INTERCEPTOR)
                .addNetworkInterceptor(CACHE_INTERCEPTOR)
                .build();
    }

    @Singleton
    @Provides
    Retrofit provideRetrofit(OkHttpClient okHttpClient) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return new Retrofit.Builder()
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .addConverterFactory(JacksonConverterFactory.create(objectMapper))
                .baseUrl(GankService.BASE_URL)
                .client(okHttpClient)
                .build();
    }

    @Singleton
    @Provides
    GankService provideGankService(Retrofit retrofit) {
        return retrofit.create(GankService.class);
    }
}
