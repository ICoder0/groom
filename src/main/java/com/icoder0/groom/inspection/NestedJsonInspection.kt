package com.icoder0.groom.inspection

import com.google.gson.*
import com.google.gson.JsonElement
import com.intellij.codeInsight.hint.HintManager
import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.json.psi.*
import com.intellij.largeFilesEditor.editor.LargeFileEditor
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.TextEditor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import org.jetbrains.annotations.NotNull
import kotlin.jvm.internal.Intrinsics
import org.apache.commons.lang3.StringEscapeUtils

/**
 * @author bofa1ex
 * @since  2021/5/23
 */
class NestedJsonInspection : LocalInspectionTool() {

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        Intrinsics.checkNotNullParameter(holder, "holder")
        return NestedJsonVisitor(holder, NestedJsonQuickFix(), NestedJsonQuickFixFully())
    }

    inner class NestedJsonVisitor(
        @NotNull val holder: ProblemsHolder,
        @NotNull val nestedJsonQuickFix: NestedJsonInspection.NestedJsonQuickFix,
        @NotNull val nestedJsonQuickFixAll: NestedJsonInspection.NestedJsonQuickFixFully
    ) : JsonElementVisitor() {
        init {
            Intrinsics.checkNotNullParameter(holder, "holder")
            Intrinsics.checkNotNullParameter(nestedJsonQuickFix, "nestedJsonQuickFix")
        }

        override fun visitStringLiteral(o: JsonStringLiteral) {
            Intrinsics.checkNotNull(o, "o")
            Intrinsics.checkNotNullExpressionValue(o.value, "o.value")
            if (isValidObjectOrArray(o.value)) {
                holder.registerProblem(
                    (o as PsiElement),
                    problemDescription,
                    nestedJsonQuickFix as LocalQuickFix,
                    nestedJsonQuickFixAll as LocalQuickFix
                )
            }
            super.visitStringLiteral(o)
        }
    }


    inner class NestedJsonQuickFix : LocalQuickFix {
        /**
         * @return text to appear in "Apply Fix" popup when multiple Quick Fixes exist (in the results of batch code inspection). For example,
         * if the name of the quickfix is "Create template &lt;filename&gt", the return value of getFamilyName() should be "Create template".
         * If the name of the quickfix does not depend on a specific element, simply return [.getName].
         */
        override fun getFamilyName(): String {
            return "Expand Nested JSON"
        }

        /**
         * Called to apply the fix.
         *
         *
         * Please call [com.intellij.profile.codeInspection.ProjectInspectionProfileManager.fireProfileChanged] if inspection profile is changed as result of fix.
         *
         * @param project    [Project]
         * @param descriptor problem reported by the tool which provided this quick fix action
         */
        override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
            Intrinsics.checkNotNullParameter(project, "project")
            Intrinsics.checkNotNullParameter(descriptor, "descriptor")
            val originalElement = descriptor.psiElement
            Intrinsics.checkNotNullExpressionValue(originalElement, "originalElement")
            var rawText = originalElement.text
            try {
                if (originalElement is JsonStringLiteral) {
                    rawText = JsonPsiUtil.stripQuotes(rawText)
                    Intrinsics.checkNotNullExpressionValue(rawText, "JsonPsiUtil.stripQuotes(rawText)")
                    val jsonValue = JsonElementGenerator(project).createValue<JsonValue>(prettify(rawText))
                    Intrinsics.checkNotNullExpressionValue(
                        jsonValue,
                        "JsonElementGenerator(pro…nUtils.prettify(content))"
                    )
                    WriteCommandAction.runWriteCommandAction(project) {
                        originalElement.replace(jsonValue)
                    }
                    return
                }
                handleFailure(project, descriptor)
            } catch (var7: Exception) {
                handleFailure(project, descriptor)
            }
        }
    }


    inner class NestedJsonQuickFixFully : LocalQuickFix {

        /**
         * @return text to appear in "Apply Fix" popup when multiple Quick Fixes exist (in the results of batch code inspection). For example,
         * if the name of the quickfix is "Create template &lt;filename&gt", the return value of getFamilyName() should be "Create template".
         * If the name of the quickfix does not depend on a specific element, simply return [.getName].
         */
        override fun getFamilyName(): String {
            return "Expand All Nested JSON"
        }

        override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
            Intrinsics.checkNotNullParameter(project, "project")
            Intrinsics.checkNotNullParameter(descriptor, "descriptor")
            val originalElement = descriptor.psiElement
            Intrinsics.checkNotNullExpressionValue(originalElement, "originalElement")
            var rawText = originalElement.text
            try {
                if (originalElement is JsonStringLiteral) {
                    rawText = JsonPsiUtil.stripQuotes(rawText)
                    Intrinsics.checkNotNullExpressionValue(rawText, "JsonPsiUtil.stripQuotes(rawText)")
                    val jsonValue =
                        JsonElementGenerator(project).createValue<JsonValue>(prettyGson.toJson(prettifyFully(rawText)))
                    Intrinsics.checkNotNullExpressionValue(
                        jsonValue,
                        "JsonElementGenerator(pro…nUtils.prettify(content))"
                    )
                    WriteCommandAction.runWriteCommandAction(project) {
                        originalElement.replace(jsonValue)
                    }
                    return
                }
                handleFailure(project, descriptor)
            } catch (var7: Exception) {
                handleFailure(project, descriptor)
            }
        }
    }

    fun handleFailure(project: Project, descriptor: ProblemDescriptor) {
        val var10000 = descriptor.psiElement
        Intrinsics.checkNotNullExpressionValue(var10000, "descriptor.psiElement")
        val var11 = var10000.containingFile
        Intrinsics.checkNotNullExpressionValue(var11, "descriptor.psiElement.containingFile")
        val virtualFile = var11.virtualFile
        val fileManager = FileEditorManager.getInstance(project)
        val var12 = fileManager.getSelectedEditor(virtualFile)
        if (var12 != null) {
            val var5: FileEditor = var12
            Intrinsics.checkNotNullExpressionValue(var5, "it")
            val editor: Editor = getEditorFromFileEditor(var5)
            HintManager.getInstance().showErrorHint(editor, "Could not apply quickfix")
        }
    }

    fun getEditorFromFileEditor(fileEditor: FileEditor): Editor {
        Intrinsics.checkNotNullParameter(fileEditor, "fileEditor")
        val var10000: Editor
        if (fileEditor is TextEditor) {
            var10000 = fileEditor.editor
            Intrinsics.checkNotNullExpressionValue(var10000, "fileEditor.editor")
        } else {
            if (fileEditor !is LargeFileEditor) {
                throw IllegalArgumentException("FileEditor does not have an editor object")
            }
            var10000 = fileEditor.editor
            Intrinsics.checkNotNullExpressionValue(var10000, "fileEditor.editor")
        }
        return var10000
    }


    fun isValidObjectOrArray(validJson: String): Boolean {
        Intrinsics.checkNotNullParameter(validJson, "validJson")
        try {
            with(JsonParser.parseString(validJson)) {
                return isJsonArray || isJsonObject
            }
        } catch (var4: JsonParseException) {
            return false
        }
    }

    fun prettify(json: String): String {
        Intrinsics.checkNotNullParameter(json, "json")
        return if (json.isBlank()) {
            ""
        } else {
            val unescapeJson = StringEscapeUtils.unescapeJson(json)
            try {
                return prettyGson.toJson(JsonParser.parseString(unescapeJson))
            } catch (e: JsonParseException) {
                throw e
            }
        }
    }

    fun prettifyFully(json: String): JsonElement? {
        Intrinsics.checkNotNullParameter(json, "json")
        return if (json.isBlank()) {
            null
        } else {
            val unescapeJson = StringEscapeUtils.unescapeJson(json)
            val root = JsonParser.parseString(unescapeJson)
            if (root.isJsonObject) {
                processNestedJsonObject(root.asJsonObject)
            }
            if (root.isJsonArray) {
                processNestedJsonArray(root.asJsonArray)
            }
            if (root.isJsonPrimitive) {
                return processNestedJson(root.asJsonPrimitive)
            }
            return root
        }
    }


    fun processNestedJson(nestedJson: JsonPrimitive): JsonElement? {
        if (nestedJson.isString && isValidObjectOrArray(nestedJson.asString)) {
            return prettifyFully(nestedJson.asString)
        }
        return nestedJson
    }


    fun processNestedJsonArray(root: com.google.gson.JsonArray) {
        root.forEachIndexed { _index, it ->
            if (it.isJsonArray) {
                processNestedJsonArray(it.asJsonArray)
            }
            if (it.isJsonObject) {
                processNestedJsonObject(it.asJsonObject)
            }
            if (it.isJsonPrimitive && it.asJsonPrimitive.isString) {
                root[_index] = processNestedJson(it.asJsonPrimitive)
            }
        }
    }

    fun processNestedJsonObject(root: com.google.gson.JsonObject) {
        root.entrySet().forEach {
            if (it.value.isJsonArray) {
                processNestedJsonArray(it.value.asJsonArray)
            }
            if (it.value.isJsonObject) {
                processNestedJsonObject(it.value.asJsonObject)
            }
            if (it.value.isJsonPrimitive) {
                it.setValue(processNestedJson(it.value.asJsonPrimitive))
            }
        }
    }

    companion object {
        const val problemDescription = "Nested JSON can be expanded"
        val prettyGson: Gson = GsonBuilder().setPrettyPrinting().serializeNulls().disableHtmlEscaping().create()
    }
}