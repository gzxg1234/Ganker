package com.sanron.ganker.data.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Created by sanron on 16-6-28.
 */
public class HistoryDates extends BaseData {

    @JsonProperty("results")
    public List<String> results;
}
