package com.sanron.ganker.data.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

/**
 * Created by sanron on 16-6-28.
 */
public class BaseData implements Serializable {

    @JsonProperty("error")
    public boolean error;
}
