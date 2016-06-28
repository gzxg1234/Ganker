package com.sanron.ganker.model.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Created by sanron on 16-6-28.
 */
public class DailyData extends BaseData {

    @JsonProperty("results")
    public Results results;

    @JsonProperty("category")
    public List<String> category;

    public static class Results {
        @JsonProperty("Android")
        public List<Gank> androidResults;
        @JsonProperty("iOS")
        public List<Gank> iosResults;
        @JsonProperty("休息视频")
        public List<Gank> videoResults;
        @JsonProperty("拓展资源")
        public List<Gank> expandResults;
        @JsonProperty("瞎推荐")
        public List<Gank> recommandResults;
        @JsonProperty("福利")
        public List<Gank> fuliResults;
    }
}
