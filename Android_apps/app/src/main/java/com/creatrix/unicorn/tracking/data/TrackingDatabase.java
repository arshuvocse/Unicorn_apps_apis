package com.creatrix.unicorn.tracking.data;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

/**
 * The offline queue. There is deliberately no {@code fallbackToDestructiveMigration()}: a schema
 * change without a migration must fail loudly during development rather than silently wipe
 * locations that have not been uploaded yet.
 */
@Database(entities = {TrackedLocation.class}, version = 1, exportSchema = true)
public abstract class TrackingDatabase extends RoomDatabase {

    private static final String NAME = "unicorn_tracking.db";
    private static volatile TrackingDatabase instance;

    public abstract LocationDao locationDao();

    public static TrackingDatabase get(Context context) {
        if (instance == null) {
            synchronized (TrackingDatabase.class) {
                if (instance == null) {
                    instance = Room.databaseBuilder(context.getApplicationContext(), TrackingDatabase.class, NAME)
                            .build();
                }
            }
        }
        return instance;
    }
}
