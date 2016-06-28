package com.sanron.ganker.model.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by sanron on 16-6-28.
 */
public class SearchData extends GankData {

    @JsonProperty("count")
    public int count;
}
