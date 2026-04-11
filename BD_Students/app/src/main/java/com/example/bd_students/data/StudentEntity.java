package com.example.bd_students.data;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "students")
public class StudentEntity {

    @PrimaryKey
    private final int studentId;
    @NonNull
    private final String name;

    public StudentEntity(int studentId, @NonNull String name) {
        this.studentId = studentId;
        this.name = name;
    }

    public int getStudentId() {
        return studentId;
    }

    @NonNull
    public String getName() {
        return name;
    }
}
