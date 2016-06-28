package com.sanron.ganker.model.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by sanron on 16-6-28.
 */
public class Gank implements Serializable {

    @JsonProperty("desc")
    public String desc;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    @JsonProperty("createdAt")
    public Date createdAt;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    @JsonProperty("publishedAt")
    public Date publishedAt;
    @JsonProperty("readability")
    public String readability;
    @JsonProperty("type")
    public String type;
    @JsonProperty("url")
    public String url;
    @JsonProperty("who")
    public String who;
    @JsonProperty("_id")
    public String id;
    @JsonProperty("source")
    public String source;
    @JsonProperty("used")
    public boolean used;
}
