package com.icoder0.groom.dialog

import com.icoder0.groom.component.NotificationManager
import com.icoder0.groom.component.WebsocketSettingsManager
import com.icoder0.groom.configurable.WebsocketConfigurable
import com.intellij.notification.NotificationType
import com.intellij.openapi.options.ex.SingleConfigurableEditor
import com.intellij.openapi.project.Project
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
        NotificationManager.notify(project, NotificationType.INFORMATION,
                "Input RequestURL\n e.g. ws://{ws.server.ip/domain}:{ws.server.port}/{path}"
        )
    }

    override fun doValidate(): ValidationInfo? {
        with(configurable as WebsocketConfigurable) {
            for (element in (allSettingsList.model as DefaultListModel).elements()) {
                if (element.address.isEmpty()){
                    continue
                }
                if (element.address.matches(Regex("^(ws|wss)://([a-zA-Z][a-zA-Z0-9_]*[.]?)+(:\\d{1,4})?(\\/[a-zA-Z0-9_]+)*(\\?[a-zA-Z0-9_]+)*(=[a-zA-Z0-9_&]+)*\$"))) {
                    continue
                }

                if (element.address.matches(Regex("^(ws|wss)://((2(5[0-5]|[0-4]\\d))|[0-1]?\\d{1,2})(\\.((2(5[0-5]|[0-4]\\d))|[0-1]?\\d{1,2})){3}(:\\d{1,4})?(\\/[a-zA-Z0-9_]+)*\$"))) {
                    continue
                }
                return ValidationInfo("Wrong specification + [" + element.address + "]", addressTextField)
            }
            if (addressTextField.text.isEmpty()){
                return null
            }
            if (addressTextField.text.matches(Regex("^(ws|wss)://([a-zA-Z][a-zA-Z0-9_]*[.]?)+(:\\d{1,4})?(\\/[a-zA-Z0-9_]+)*(\\?[a-zA-Z0-9_]+)*(=[a-zA-Z0-9_&]+)*\$"))) {
                return null
            }
            if (addressTextField.text.matches(Regex("^(ws|wss)://((2(5[0-5]|[0-4]\\d))|[0-1]?\\d{1,2})(\\.((2(5[0-5]|[0-4]\\d))|[0-1]?\\d{1,2})){3}(:\\d{1,4})?(\\/[a-zA-Z0-9_]+)*\$"))) {
                return null
            }
            return ValidationInfo("Wrong specification + [" + addressTextField.text + "]", addressTextField)
        }
    }
}