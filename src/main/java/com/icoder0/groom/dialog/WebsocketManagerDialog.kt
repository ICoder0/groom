package com.icoder0.groom.dialog

import com.icoder0.groom.component.NotificationManager
import com.icoder0.groom.component.WebsocketSettingsManager
import com.icoder0.groom.configurable.WebsocketConfigurable
import com.intellij.openapi.options.ex.SingleConfigurableEditor
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ValidationInfo
import javax.swing.DefaultListModel
import javax.swing.JComponent

/**
 * @author bofa1ex
 * @since 2021/5/10
 */
open class WebsocketManagerDialog(project: Project?,
                                  val allSettings: MutableList<WebsocketSettingsManager.WebsocketConfigurationSetting>,
                                  var selectedSetting: WebsocketSettingsManager.WebsocketConfigurationSetting?
) : SingleConfigurableEditor(project, WebsocketConfigurable(allSettings, selectedSetting), "Websocket-Configurable-DimensionKey", false) {
    init {
        this.title = "Websocket Configuration"
    }

    override fun doHelpAction() {
        NotificationManager.notifyInfo(project,
                "Input RequestURL\n" +
                        "e.g. ws://{ws.server.ip/domain}:{ws.server.port}/{path}"
        )
    }

    override fun doValidate(): ValidationInfo? {
        with(configurable as WebsocketConfigurable) {
            for (element in (allSettingsList.model as DefaultListModel).elements()) {
                if (element.address.isEmpty()){
                    continue
                }
                if (element.address.matches(Regex("^(ws|wss)://\\w+:\\d{1,4}.*$"))) {
                    continue
                }
                if (element.address.matches(Regex("^(ws|wss)://((2(5[0-5]|[0-4]\\d))|[0-1]?\\d{1,2})(\\.((2(5[0-5]|[0-4]\\d))|[0-1]?\\d{1,2})){3}:\\d{1,4}.*$"))) {
                    continue
                }
                return ValidationInfo("Wrong specification + [" + element.address + "]", addressTextField)
            }
            if (addressTextField.text.isEmpty()){
                return ValidationInfo("RequestURL must not be null", addressTextField)
            }
            if (addressTextField.text.matches(Regex("^(ws|wss)://\\w+:\\d{1,4}.*$"))) {
                return null
            }
            if (addressTextField.text.matches(Regex("^(ws|wss)://((2(5[0-5]|[0-4]\\d))|[0-1]?\\d{1,2})(\\.((2(5[0-5]|[0-4]\\d))|[0-1]?\\d{1,2})){3}:\\d{1,4}.*$"))) {
                return null
            }
            return ValidationInfo("Wrong specification + [" + addressTextField.text + "]", addressTextField)
        }
    }
}