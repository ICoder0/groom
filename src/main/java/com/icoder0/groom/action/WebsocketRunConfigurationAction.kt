package com.icoder0.groom.action

import com.google.common.base.Strings
import com.icoder0.groom.component.WebsocketSettingsManager
import com.icoder0.groom.configurable.WebsocketConfigurable
import com.icoder0.groom.dialog.WebsocketManagerDialog
import com.icoder0.groom.ui.WebsocketClientView
import com.intellij.execution.ExecutionBundle
import com.intellij.icons.AllIcons
import com.intellij.idea.ActionsBundle
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.actionSystem.ex.ComboBoxAction
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.roots.ui.configuration.actions.IconWithTextAction
import icons.GroomIcons
import javax.swing.DefaultListModel
import javax.swing.JComponent

/**
 * @author bofa1ex
 * @since  2021/5/18
 */
class WebsocketRunConfigurationAction : ComboBoxAction() {

    var selectedSettingMap: MutableMap<WebsocketClientView, WebsocketSettingsManager.WebsocketConfigurationSetting?> = mutableMapOf()

    override fun update(e: AnActionEvent) {
        val presentation = e.presentation
        val project = e.getData(CommonDataKeys.PROJECT)
        val clientView = e.getData(WebsocketClientView.WEBSOCKET_VIEW_KEY) as WebsocketClientView
        val selectedSetting = selectedSettingMap.getOrPut(clientView,
                {WebsocketSettingsManager.allSettings.firstOrNull()}
        )
        if (project == null || project.isDisposed || !project.isOpen) {
            updatePresentation(null, presentation, e.place, selectedSetting)
            presentation.isEnabled = false
        } else {
            clientView.wsClientAddress = selectedSetting?.address
            updatePresentation(project, presentation, e.place, selectedSetting)
            presentation.isEnabled = true
        }
    }

    fun updatePresentation(project: Project?,
                           presentation: Presentation,
                           actionPlace: String,
                           selectedSetting: WebsocketSettingsManager.WebsocketConfigurationSetting?) {
        if (project != null && selectedSetting != null) {
            presentation.setText(
                    if (selectedSetting.name.length > 10) selectedSetting.name.substring(0,7) + "..."
                    else selectedSetting.name, false
            )
            presentation.icon = GroomIcons.Socket
        } else {
            presentation.setText(ExecutionBundle.messagePointer("action.presentation.RunConfigurationsComboBoxAction.text"))
            presentation.description = ActionsBundle.actionDescription(IdeActions.ACTION_EDIT_RUN_CONFIGURATIONS)
            if (ActionPlaces.TOUCHBAR_GENERAL == actionPlace) presentation.icon = AllIcons.General.Add else presentation.icon = null
        }
    }

    override fun createPopupActionGroup(button: JComponent?): DefaultActionGroup {
        val allActionsGroup = DefaultActionGroup()
        allActionsGroup.add(EditWebsocketConfigurationAction())
        allActionsGroup.addSeparator()
        for (setting in WebsocketSettingsManager.allSettings) {
            allActionsGroup.add(ComboBoxInternalAction(setting))
        }
        return allActionsGroup
    }


    inner class ComboBoxInternalAction(val setting: WebsocketSettingsManager.WebsocketConfigurationSetting) : IconWithTextAction(setting.name, "", GroomIcons.Socket) {
        override fun actionPerformed(e: AnActionEvent) {
            selectedSettingMap[e.getData(WebsocketClientView.WEBSOCKET_VIEW_KEY)!!] = setting
        }
    }

    inner class EditWebsocketConfigurationAction : DumbAwareAction("Edit Websocket Configurations...") {
        override fun actionPerformed(e: AnActionEvent) {
            var project = e.getData(CommonDataKeys.PROJECT)
            val websocketClientView = e.getData(WebsocketClientView.WEBSOCKET_VIEW_KEY)
            if (project != null && project.isDisposed) {
                return
            }
            if (project == null) {
                //setup template project configurations
                project = ProjectManager.getInstance().defaultProject
            }
            object : WebsocketManagerDialog(project, selectedSettingMap.get(websocketClientView)) {
                override fun doOKAction() {
                    with((configurable as WebsocketConfigurable)) {
                        WebsocketSettingsManager.allSettings.clear()
                        for (element in (allSettingsList.model as DefaultListModel).elements()) {
                            WebsocketSettingsManager.allSettings.add(element)
                        }
                        // update selected configuration.
                        selectedSettingsDup?.name = nameTextField.text
                        selectedSettingsDup?.address = addressTextField.text
                        selectedSettingMap[websocketClientView!!] = selectedSettingsDup
                        configurable.disposeUIResources()
                    }
                    super.doOKAction()
                }
            }.show()

        }
    }
}