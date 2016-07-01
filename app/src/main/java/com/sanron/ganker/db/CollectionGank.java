package com.sanron.ganker.db;

import com.sanron.ganker.data.entity.Gank;

/**
 * Created by sanron on 16-6-29.
 */
public class CollectionGank extends Gank {

    public long id;

    public long addTime;

    public static final String TABLE_NAME = "collections";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_ADD_TIME = "add_time";
    public static final String COLUMN_GANK_ID = "gank_id";
    public static final String COLUMN_DESC = "desc";
    public static final String COLUMN_PUBLISH_TIME = "publish_time";
    public static final String COLUMN_TYPE = "type";
    public static final String COLUMN_URL = "url";
    public static final String COLUMN_WHO = "who";
}
