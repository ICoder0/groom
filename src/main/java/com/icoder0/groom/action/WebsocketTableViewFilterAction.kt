package com.icoder0.groom.action

import com.icoder0.groom.component.ChooserManager
import com.icoder0.groom.ui.WebsocketClientView
import com.intellij.execution.runners.ExecutionUtil
import com.intellij.icons.AllIcons
import com.intellij.ide.util.ElementsChooser
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.ui.popup.JBPopupFactory
import java.util.function.UnaryOperator

/**
 * @author bofa1ex
 * @since  2021/5/5
 */
class WebsocketTableViewFilterAction : DumbAwareAction() {

    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.EDT
    }

    companion object FilterObjectKind {
        const val INBOUND = "Inbound"
        const val OUTBOUND = "Outbound"
    }

    override fun update(e: AnActionEvent) {
        val isModify = e.getData(WebsocketClientView.IS_TABLE_VIEW_MODIFY_KEY)
        e.presentation.text = "Filter Inbound/Outbound message"
        e.presentation.icon = if(isModify?:false) ExecutionUtil.getLiveIndicator(AllIcons.General.Filter)
        else AllIcons.General.Filter
    }

    override fun actionPerformed(e: AnActionEvent) {
        val websocketClientView: WebsocketClientView? = e.getData<WebsocketClientView>(WebsocketClientView.WEBSOCKET_VIEW_KEY)
        val chooser = websocketClientView?.let {
            ChooserManager.getPayloadChooser(it, UnaryOperator { t ->
                t.addElementsMarkListener(ElementsChooser.ElementsMarkListener { element, isMarked ->
                    if (t.markedElements.size == 0) {
                        t.setElementMarked(element, true)
                        return@ElementsMarkListener
                    }
                    websocketClientView.fireTableViewModify(t.markedElements.size != 2)
                    if (t.markedElements.size == 2) {
                        websocketClientView.firePayloadFilter(WebsocketClientView.allType, websocketClientView.wsPayloadMatch)
                        return@ElementsMarkListener
                    }
                    when (element) {
                        INBOUND -> if (isMarked) websocketClientView.firePayloadFilter(0, websocketClientView.wsPayloadMatch) else websocketClientView.firePayloadFilter(1, websocketClientView.wsPayloadMatch)
                        OUTBOUND -> if (isMarked) websocketClientView.firePayloadFilter(1, websocketClientView.wsPayloadMatch) else websocketClientView.firePayloadFilter(0, websocketClientView.wsPayloadMatch)
                    }
                })
                return@UnaryOperator t
            })
        }


        JBPopupFactory.getInstance().createComponentPopupBuilder(chooser!!,null)
                .setRequestFocus(false)
                .setFocusable(false)
                .setResizable(true)
                .createPopup()
                .showUnderneathOf(e.inputEvent.component)
    }
}