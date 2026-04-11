package com.example.bd_students.data;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "subjects")
public class SubjectEntity {

    @PrimaryKey
    private final int subjectId;
    @NonNull
    private final String title;

    public SubjectEntity(int subjectId, @NonNull String title) {
        this.subjectId = subjectId;
        this.title = title;
    }

    public int getSubjectId() {
        return subjectId;
    }

    @NonNull
    public String getTitle() {
        return title;
    }
}
