package com.creatrix.unicorn.DBInfo;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.creatrix.unicorn.Model.UserTracking;

import java.util.ArrayList;
import java.util.List;

public class TrackingQueueDbHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "tracking_queue.db";
    // v2: latitude/longitude became nullable ("no location" points are queued too).
    private static final int DATABASE_VERSION = 2;
    private static final String TABLE_NAME = "pending_tracking";

    public TrackingQueueDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createTable(db, TABLE_NAME);
    }

    private static void createTable(SQLiteDatabase db, String name) {
        db.execSQL("CREATE TABLE " + name + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "emp_info_id INTEGER NOT NULL," +
                "latitude TEXT," +
                "longitude TEXT," +
                "address TEXT," +
                "time_str TEXT," +
                "track_date TEXT," +
                "created_at INTEGER NOT NULL)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            // SQLite cannot drop NOT NULL in place: rebuild the table and keep the queued rows.
            db.beginTransaction();
            try {
                createTable(db, TABLE_NAME + "_v2");
                db.execSQL("INSERT INTO " + TABLE_NAME + "_v2 (id, emp_info_id, latitude, longitude, address, time_str, track_date, created_at) " +
                        "SELECT id, emp_info_id, latitude, longitude, address, time_str, track_date, created_at FROM " + TABLE_NAME);
                db.execSQL("DROP TABLE " + TABLE_NAME);
                db.execSQL("ALTER TABLE " + TABLE_NAME + "_v2 RENAME TO " + TABLE_NAME);
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }
        }
    }

    public long enqueue(UserTracking tracking) {
        ContentValues values = new ContentValues();
        values.put("emp_info_id", tracking.getEmpInfoId());
        values.put("latitude", tracking.getLatValue());
        values.put("longitude", tracking.getLongValue());
        values.put("address", tracking.getAddressName());
        values.put("time_str", tracking.getTimeStr());
        values.put("track_date", tracking.getTrackDate());
        values.put("created_at", System.currentTimeMillis());
        return getWritableDatabase().insertOrThrow(TABLE_NAME, null, values);
    }

    /** Oldest-first rows with id greater than afterId (lets the uploader skip a row the server rejects). */
    public List<PendingTracking> getPending(long afterId, int limit) {
        List<PendingTracking> result = new ArrayList<>();
        try (Cursor cursor = getReadableDatabase().query(
                TABLE_NAME,
                new String[]{"id", "emp_info_id", "latitude", "longitude", "address", "time_str", "track_date", "created_at"},
                "id > ?",
                new String[]{String.valueOf(afterId)},
                null,
                null,
                "id ASC",
                String.valueOf(limit))) {
            while (cursor.moveToNext()) {
                UserTracking tracking = new UserTracking();
                tracking.setEmpInfoId(cursor.getInt(1));
                tracking.setLatValue(cursor.getString(2));
                tracking.setLongValue(cursor.getString(3));
                tracking.setAddressName(cursor.getString(4));
                tracking.setTimeStr(cursor.getString(5));
                tracking.setTrackDate(cursor.getString(6));
                result.add(new PendingTracking(cursor.getLong(0), tracking, cursor.getLong(7)));
            }
        }
        return result;
    }

    public List<PendingTracking> getPending(int limit) {
        return getPending(0, limit);
    }

    public void delete(long id) {
        getWritableDatabase().delete(TABLE_NAME, "id = ?", new String[]{String.valueOf(id)});
    }

    public static class PendingTracking {
        private final long id;
        private final UserTracking tracking;
        private final long createdAt;

        PendingTracking(long id, UserTracking tracking, long createdAt) {
            this.id = id;
            this.tracking = tracking;
            this.createdAt = createdAt;
        }

        public long getId() {
            return id;
        }

        public UserTracking getTracking() {
            return tracking;
        }

        public long getCreatedAt() {
            return createdAt;
        }
    }
}
