package com.example.bd_students.data;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(
        entities = {StudentEntity.class, SubjectEntity.class, StudentSubjectCrossRef.class},
        version = 1,
        exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {
    public abstract SchoolDao schoolDao();
}
