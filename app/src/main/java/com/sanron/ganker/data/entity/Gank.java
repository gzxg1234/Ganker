package com.sanron.ganker.data.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by sanron on 16-6-28.
 */
public class Gank implements Serializable {

    public static final String CATEGORY_ANDROID = "Android";
    public static final String CATEGORY_IOS = "iOS";
    public static final String CATEGORY_EXPAND = "拓展资源";
    public static final String CATEGORY_FRONT_END = "前端";
    public static final String CATEGORY_FULI = "福利";
    public static final String CATEGORY_VEDIO = "休息视频";
    public static final String CATEGORY_ALL = "all";

    @JsonProperty("desc")
    public String desc;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",timezone = "Asia/Shanghai")
    @JsonProperty("createdAt")
    public Date createdAt;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",timezone = "Asia/Shanghai")
    @JsonProperty("publishedAt")
    public Date publishedAt;
    @JsonProperty("type")
    public String type;
    @JsonProperty("url")
    public String url;
    @JsonProperty("who")
    public String who;
    @JsonProperty("_id")
    public String gankId;
}
