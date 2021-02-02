package com.github.bofa1ex.groom.services

import com.github.bofa1ex.groom.MyBundle
import com.intellij.openapi.project.Project

class MyProjectService(project: Project) {

    init {
        println(MyBundle.message("projectService", project.name))
    }
}
