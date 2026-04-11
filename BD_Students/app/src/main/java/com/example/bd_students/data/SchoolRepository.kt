package com.example.bd_students.data

class SchoolRepository(
    private val dao: SchoolDao
) {
    fun getSelectableItems(mode: LookupMode): List<SelectableItem> {
        return when (mode) {
            LookupMode.STUDENT -> dao.getStudents().map { SelectableItem(it.studentId, it.name) }
            LookupMode.SUBJECT -> dao.getSubjects().map { SelectableItem(it.subjectId, it.title) }
        }
    }

    fun getRelatedTitles(mode: LookupMode, id: Int): List<String> {
        return when (mode) {
            LookupMode.STUDENT -> dao.getSubjectTitlesForStudent(id)
            LookupMode.SUBJECT -> dao.getStudentNamesForSubject(id)
        }
    }
}
