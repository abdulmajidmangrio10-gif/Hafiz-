package com.example.data.local

import com.example.data.model.Project
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ProjectRepository(private val projectDao: ProjectDao) {

    val allProjects: Flow<List<Project>> = projectDao.getAllProjects().map { list ->
        list.map { ProjectJsonMapper.toDomain(it) }
    }

    suspend fun getProjectById(id: String): Project? {
        val entity = projectDao.getProjectById(id) ?: return null
        return ProjectJsonMapper.toDomain(entity)
    }

    suspend fun saveProject(project: Project) {
        val entity = ProjectJsonMapper.toEntity(project.copy(modifiedAt = System.currentTimeMillis()))
        projectDao.insertProject(entity)
    }

    suspend fun deleteProject(id: String) {
        projectDao.deleteProjectById(id)
    }

    suspend fun renameProject(id: String, newName: String) {
        projectDao.renameProject(id, newName, System.currentTimeMillis())
    }
}
