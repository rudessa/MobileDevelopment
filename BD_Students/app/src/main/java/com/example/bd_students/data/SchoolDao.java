package com.example.bd_students.data;

import androidx.room.Dao;
import androidx.room.Query;

import java.util.List;

@Dao
public interface SchoolDao {

    @Query("SELECT * FROM students ORDER BY name")
    List<StudentEntity> getStudents();

    @Query("SELECT * FROM subjects ORDER BY title")
    List<SubjectEntity> getSubjects();

    @Query(
            "SELECT subjects.title " +
                    "FROM subjects " +
                    "INNER JOIN student_subject_cross_ref " +
                    "ON subjects.subjectId = student_subject_cross_ref.subjectId " +
                    "WHERE student_subject_cross_ref.studentId = :studentId " +
                    "ORDER BY subjects.title"
    )
    List<String> getSubjectTitlesForStudent(int studentId);

    @Query(
            "SELECT students.name " +
                    "FROM students " +
                    "INNER JOIN student_subject_cross_ref " +
                    "ON students.studentId = student_subject_cross_ref.studentId " +
                    "WHERE student_subject_cross_ref.subjectId = :subjectId " +
                    "ORDER BY students.name"
    )
    List<String> getStudentNamesForSubject(int subjectId);
}
