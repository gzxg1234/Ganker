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

    private String desc;
    private Date publishedAt;
    private String type;
    private String url;
    private String who;
    private String gankId;

    @JsonProperty("desc")
    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Shanghai")
    @JsonProperty("publishedAt")
    public Date getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(Date publishedAt) {
        this.publishedAt = publishedAt;
    }

    @JsonProperty("type")
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @JsonProperty("url")
    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @JsonProperty("who")
    public String getWho() {
        return who;
    }

    public void setWho(String who) {
        this.who = who;
    }

    @JsonProperty("_id")
    public String getGankId() {
        return gankId;
    }

    public void setGankId(String gankId) {
        this.gankId = gankId;
    }
}
