package com.example.bd_students.data;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;

@Entity(
        tableName = "student_subject_cross_ref",
        primaryKeys = {"studentId", "subjectId"},
        indices = {
                @Index("studentId"),
                @Index("subjectId")
        },
        foreignKeys = {
                @ForeignKey(
                        entity = StudentEntity.class,
                        parentColumns = "studentId",
                        childColumns = "studentId",
                        onDelete = ForeignKey.CASCADE
                ),
                @ForeignKey(
                        entity = SubjectEntity.class,
                        parentColumns = "subjectId",
                        childColumns = "subjectId",
                        onDelete = ForeignKey.CASCADE
                )
        }
)
public class StudentSubjectCrossRef {

    private final int studentId;
    private final int subjectId;

    public StudentSubjectCrossRef(int studentId, int subjectId) {
        this.studentId = studentId;
        this.subjectId = subjectId;
    }

    public int getStudentId() {
        return studentId;
    }

    public int getSubjectId() {
        return subjectId;
    }
}
