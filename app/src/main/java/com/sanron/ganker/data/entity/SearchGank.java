package com.sanron.ganker.data.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by sanron on 16-6-30.
 */
public class SearchGank extends Gank {

    private String gankId;

    @JsonProperty("ganhuo_id")
    @Override
    public String getGankId() {
        return gankId;
    }

    @Override
    public void setGankId(String gankId) {
        this.gankId = gankId;
    }
}
