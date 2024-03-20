package com.icoder0.groom.dialog

import com.icoder0.groom.component.WebsocketSettingsManager
import com.icoder0.groom.configurable.WebsocketConfigurable
import com.icoder0.groom.util.IdeUtils
import com.intellij.notification.NotificationType
import com.intellij.openapi.options.ex.SingleConfigurableEditor
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.MessageType
import com.intellij.openapi.ui.ValidationInfo
import javax.swing.DefaultListModel

/**
 * @author bofa1ex
 * @since 2021/5/10
 */
open class WebsocketManagerDialog(project: Project?, selectedSetting: WebsocketSettingsManager.WebsocketConfigurationSetting?
) : SingleConfigurableEditor(project, WebsocketConfigurable(selectedSetting), "Websocket-Configurable-DimensionKey", false) {

    init {
        this.title = "Websocket Configuration"
    }

    override fun doHelpAction() {
        IdeUtils.notify("Input RequestURL\n e.g. wss://example.com:8080/path?query=value#fragment", MessageType.INFO)
    }

    override fun doValidate(): ValidationInfo? {
        with(configurable as WebsocketConfigurable) {
            for (element in (allSettingsList.model as DefaultListModel).elements()) {
                if (element.address.isEmpty()){
                    continue
                }
                if (element.address.matches(Regex("^(wss?://)([^:/]+)(:\\d+)?(/[^?#]*)?(\\?[^#]*)?(#.*)?\$"))) {
                    continue
                }
                return ValidationInfo("Wrong specification + [" + element.address + "]", addressTextField)
            }
            if (addressTextField.text.isEmpty()){
                return null
            }
            if (addressTextField.text.matches(Regex("^(wss?://)([^:/]+)(:\\d+)?(/[^?#]*)?(\\?[^#]*)?(#.*)?\$"))) {
                return null
            }
            return ValidationInfo("Wrong specification + [" + addressTextField.text + "]", addressTextField)
        }
    }
}