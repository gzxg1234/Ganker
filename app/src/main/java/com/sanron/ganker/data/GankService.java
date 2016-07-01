package com.sanron.ganker.data;

import com.sanron.ganker.data.entity.GankData;
import com.sanron.ganker.data.entity.HistoryDates;
import com.sanron.ganker.data.entity.SearchData;

import retrofit2.http.GET;
import retrofit2.http.Path;
import rx.Observable;

/**
 * Created by sanron on 16-6-28.
 */
public interface GankService {

    String BASE_URL = "http://gank.io/api/";

    /**
     * 搜索
     *
     * @param search   关键字
     * @param category 类别
     * @param count    数量
     * @param page     页码
     * @return
     */
    @GET("search/query/{search}/category/{category}/count/{count}/page/{page}")
    Observable<SearchData> search(@Path("search") String search, @Path("category") String category,
                                  @Path("count") int count, @Path("page") int page);


    /**
     * 获取特定日期干货
     *
     * @param year
     * @param month
     * @param day
     * @return
     */
    @GET("history/content/day/{year}/{month}/{day}")
    Observable<GankData> getByDate(@Path("year") int year, @Path("month") int month,
                                   @Path("day") int day);

    @GET("history")
    Observable<HistoryDates> getHistoryDates();

    @GET("data/{category}/{count}/{page}")
    Observable<GankData> getByCategory(@Path("category") String category,
                                       @Path("count") int count,
                                       @Path("page") int page);

    @GET("random/data/{category}/{count}")
    Observable<GankData> shuffleGank(@Path("category") String category,
                                     @Path("count") int count);
}
