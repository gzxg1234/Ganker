package com.sanron.ganker.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.sanron.ganker.data.entity.Gank;
import com.sanron.ganker.db.entity.SaveGank;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;

import rx.Observable;

/**
 * Created by sanron on 16-7-4.
 */
public class HistoryTableHelper extends BaseHelper {

    private static final int MAX_HISTORY_COUNT = 200;

    public HistoryTableHelper(Context context) {
        super(context);
    }

    public static void onCreate(SQLiteDatabase db) {
        String sql = "create table if not exists " + SaveGank.TABLE_HISTORY + "(" +
                SaveGank.COLUMN_ID + " integer primary key," +
                SaveGank.COLUMN_ADD_TIME + " integer," +
                SaveGank.COLUMN_GANK_ID + " text," +
                SaveGank.COLUMN_WHO + " text," +
                SaveGank.COLUMN_DESC + " text," +
                SaveGank.COLUMN_PUBLISH_TIME + " integer," +
                SaveGank.COLUMN_URL + " text," +
                SaveGank.COLUMN_TYPE + " text)";
        db.execSQL(sql);
    }


    public Observable<List<SaveGank>> getAll() {
        return createObserver(new Callable<List<SaveGank>>() {
            @Override
            public List<SaveGank> call() throws Exception {
                List<SaveGank> result = new ArrayList<>();
                Cursor c = getDataBase().query(SaveGank.TABLE_HISTORY,
                        null, null, null, null, null,
                        SaveGank.COLUMN_ADD_TIME + " desc");
                while (c.moveToNext()) {
                    result.add(toSaveGank(c));
                }
                c.close();
                return result;
            }
        });
    }


    public Observable<List<SaveGank>> getByPage(final int pageSize, final int page) {
        return createObserver(new Callable<List<SaveGank>>() {
            @Override
            public List<SaveGank> call() throws Exception {
                List<SaveGank> result = new ArrayList<>();
                Cursor c = getDataBase().query(SaveGank.TABLE_HISTORY,
                        null, null, null, null, null,
                        SaveGank.COLUMN_ADD_TIME + " desc",
                        ((page - 1) * pageSize) + "," + pageSize);
                while (c.moveToNext()) {
                    result.add(toSaveGank(c));
                }
                c.close();
                return result;
            }
        });
    }

    public Observable<Long> add(final Gank gank) {
        return createObserver(new Callable<Long>() {
            @Override
            public Long call() throws Exception {
                long result = -1;
                ContentValues values = new ContentValues();
                values.put(SaveGank.COLUMN_ADD_TIME, System.currentTimeMillis());
                String gankId = gank.getGankId();
                String selection = SaveGank.COLUMN_GANK_ID + "=?";

                SQLiteDatabase db = getDataBase();
                Cursor c1 = db.query(SaveGank.TABLE_HISTORY, null, selection,
                        new String[]{gankId}, null, null, null);
                if (c1.moveToFirst()) {
                    //存在此数据,则只需要更新时间即可
                    if (db.update(SaveGank.TABLE_HISTORY, values, selection, new String[]{gankId}) > 0) {
                        result = c1.getLong(c1.getColumnIndex(SaveGank.COLUMN_ID));
                    }
                } else {
                    //否则添加数据
                    values.put(SaveGank.COLUMN_DESC, gank.getDesc());
                    values.put(SaveGank.COLUMN_GANK_ID, gank.getGankId());
                    values.put(SaveGank.COLUMN_TYPE, gank.getType());
                    values.put(SaveGank.COLUMN_PUBLISH_TIME, gank.getPublishedAt().getTime());
                    values.put(SaveGank.COLUMN_URL, gank.getUrl());
                    values.put(SaveGank.COLUMN_WHO, gank.getWho());
                    result = db.insert(SaveGank.TABLE_HISTORY, null, values);
                    if (result > -1) {
                        //删除浏览时间最久远的数据
                        deleteOldest(db);
                    }
                }
                c1.close();
                return result;
            }
        });
    }

    private void deleteOldest(SQLiteDatabase db) {
        String queryCount = "select count(1) from " + SaveGank.TABLE_HISTORY;
        Cursor c = db.rawQuery(queryCount, null);
        if (c.moveToFirst()) {
            int count = c.getInt(0);
            int limit = count - MAX_HISTORY_COUNT;
            if (limit > 0) {
                String sql = "delete from " + SaveGank.TABLE_HISTORY + " where " + SaveGank.COLUMN_ID + " in"
                        + "(select " + SaveGank.COLUMN_ID + " from " + SaveGank.TABLE_HISTORY
                        + " order by " + SaveGank.COLUMN_ADD_TIME + " asc limit " + limit + ")";
                db.execSQL(sql);
            }
        }
        c.close();
    }


    public Observable<Boolean> deleteById(final long id) {
        return createObserver(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                boolean result = false;
                String selection = SaveGank.COLUMN_ID + "=?";
                int delCount = getDataBase().delete(SaveGank.TABLE_HISTORY,
                        selection,
                        new String[]{id + ""});
                if (delCount > 0) {
                    result = true;
                }
                return result;
            }
        });
    }

    public Observable<Integer> deleteAll() {
        return createObserver(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                return getDataBase().delete(SaveGank.TABLE_HISTORY, null, null);
            }
        });
    }

    private static SaveGank toSaveGank(Cursor cursor) {
        SaveGank gank = new SaveGank();
        gank.id = cursor.getLong(cursor.getColumnIndex(SaveGank.COLUMN_ID));
        gank.addTime = cursor.getLong(cursor.getColumnIndex(SaveGank.COLUMN_ADD_TIME));
        gank.setDesc(cursor.getString(cursor.getColumnIndex(SaveGank.COLUMN_DESC)));
        gank.setGankId(cursor.getString(cursor.getColumnIndex(SaveGank.COLUMN_GANK_ID)));
        gank.setPublishedAt(new Date(cursor.getLong(cursor.getColumnIndex(SaveGank.COLUMN_PUBLISH_TIME))));
        gank.setType(cursor.getString(cursor.getColumnIndex(SaveGank.COLUMN_TYPE)));
        gank.setWho(cursor.getString(cursor.getColumnIndex(SaveGank.COLUMN_WHO)));
        gank.setUrl(cursor.getString(cursor.getColumnIndex(SaveGank.COLUMN_URL)));
        return gank;
    }

}
