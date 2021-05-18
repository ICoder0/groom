package com.icoder0.groom.ui

import com.icoder0.groom.component.EditorManager
import com.intellij.openapi.Disposable
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.DataKey
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.SimpleToolWindowPanel
import com.intellij.openapi.util.Key
import com.intellij.openapi.wm.ex.ToolWindowEx
import org.jetbrains.annotations.Nullable
import java.util.*
import java.util.concurrent.atomic.AtomicInteger
import kotlin.reflect.jvm.internal.impl.load.kotlin.JvmType

/**
 * @author bofa1ex
 * @since  2021/4/17
 */
@SuppressWarnings("LeakingThisInConstructor")
abstract class GroomToolWindowPanel(var viewName: String, val project: Project, val toolWindow: ToolWindowEx) : SimpleToolWindowPanel(true), Disposable{

    companion object {
        val VIEW_NAME_KEY: DataKey<String> = DataKey.create("VIEW_NAME")
        val attribute = mutableMapOf<Class<GroomToolWindowPanel>, Pair<AtomicInteger, TreeSet<String>>>()
    }

    init {
        EditorManager.initPanel(this)
        var index : AtomicInteger? = attribute[this.javaClass]?.first
        var sortStack : TreeSet<String>? = attribute[this.javaClass]?.second
        synchronized(this::class.java){
            if (!attribute.containsKey(this::class.java)) {
                index = AtomicInteger(1)
                sortStack = sortedSetOf()
                attribute[this.javaClass] = Pair(
                        index!!, sortStack!!
                )
            }
        }
        with(sortStack!!){
            viewName = if(isNotEmpty()) pollFirst()!!
            else index!!.getAndIncrement().let {
                return@let when(it){
                    1 -> viewName
                    else -> "$viewName($it)"
                }
            }
        }
    }

    override fun dispose() {
        attribute.get(this::class.java)!!.second.add(viewName)
        this.removeAll()
    }

    abstract fun initUI(): GroomToolWindowPanel

    override fun getData(dataId: String): Any? {
        if (project.isDisposed) {
            return null
        }
        if (VIEW_NAME_KEY.`is`(dataId)){
            return viewName
        }
        return null
    }
}