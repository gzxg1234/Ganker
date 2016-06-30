package com.sanron.ganker.data.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Created by sanron on 16-6-30.
 */
public class SearchGank extends Gank {

    @JsonProperty("ganhuo_id")
    public String gankId;
}
