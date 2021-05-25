package com.icoder0.groom.language

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.patterns.PlatformPatterns
import com.intellij.util.ProcessingContext


/**
 * @author bofa1ex
 * @since  2021/5/21
 */
class SimpleCompletionContributor : CompletionContributor() {
    init {
        extend(CompletionType.BASIC, PlatformPatterns.psiElement(), object : CompletionProvider<CompletionParameters>() {
            override fun addCompletions(parameters: CompletionParameters,
                                        context: ProcessingContext,
                                        resultSet: CompletionResultSet) {
                resultSet.addElement(LookupElementBuilder.create("Hello"))
            }
        })
    }
}