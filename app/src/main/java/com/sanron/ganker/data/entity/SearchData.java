package com.sanron.ganker.data.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Created by sanron on 16-6-28.
 */
public class SearchData extends BaseData {

    @JsonProperty("results")
    public List<SearchGank> results;

    @JsonProperty("count")
    public int count;
}
