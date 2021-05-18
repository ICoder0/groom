package com.icoder0.groom.configurable

import com.icoder0.groom.component.WebsocketSettingsManager
import com.icoder0.groom.ui.WebsocketClientView
import com.intellij.openapi.options.BoundSearchableConfigurable
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.ui.JBSplitter
import com.intellij.ui.ToolbarDecorator
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBList
import com.intellij.ui.components.JBTextField
import com.intellij.ui.layout.panel
import com.intellij.util.ui.JBSwingUtilities
import com.sun.org.apache.xpath.internal.operations.Bool
import icons.GroomIcons
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import javax.swing.DefaultListModel
import javax.swing.DefaultListSelectionModel
import javax.swing.JTextField
import javax.swing.SwingConstants.LEADING

/**
 * @author bofa1ex
 * @since  2021/5/14
 */
class WebsocketConfigurable(
        val allSettings: MutableList<WebsocketSettingsManager.WebsocketConfigurationSetting>,
        var selectedSettings: WebsocketSettingsManager.WebsocketConfigurationSetting?) : BoundSearchableConfigurable("Websocket-Configurable", "", "Websocket-Configurable"
) {
    init {
        if (selectedSettings == null && allSettings.isEmpty()) {
            selectedSettings = WebsocketSettingsManager.WebsocketConfigurationSetting("Uname")
        }
    }
    val addressLabel = JBLabel("RequestURL:\t")
    val nameLabel = JBLabel("Name:\t")
    var addressTextField = JBTextField(selectedSettings?.address)
    var nameTextField = JBTextField(selectedSettings?.name)
    val allSettingsList = JBList<WebsocketSettingsManager.WebsocketConfigurationSetting>(
            if (allSettings.isEmpty()) mutableListOf(selectedSettings) else allSettings
    ).apply {
        setSelectedValue(selectedSettings, true)
        installCellRenderer {
            JBLabel(it.name, GroomIcons.Socket, LEADING)
        }
        selectionModel.addListSelectionListener {
            addressLabel.isVisible = true; nameLabel.isVisible = true
            addressTextField.isVisible = true; nameTextField.isVisible = true
            if (this@apply.selectedValue == null) {
                addressLabel.isVisible = false; nameLabel.isVisible = false
                addressTextField.isVisible = false; nameTextField.isVisible = false
                return@addListSelectionListener
            }
            with(this@apply.selectedValue) {
                // before change selectedSettings point, update current presentation
                selectedSettings?.name = nameTextField.text
                selectedSettings?.address = addressTextField.text

                selectedSettings = this
                nameTextField.text = name
                addressTextField.text = address
            }
        }
    }

    override fun createPanel(): DialogPanel {
        nameTextField.addKeyListener(object : KeyAdapter() {
            override fun keyReleased(e: KeyEvent?) {
                allSettingsList.selectedValue?.name = nameTextField.text
                allSettingsList.cellRenderer.getListCellRendererComponent(
                        allSettingsList, allSettingsList.selectedValue, allSettingsList.selectedIndex, true, true
                )
                allSettingsList.updateUI()
            }
        })
        return panel {
            row {
                JBSplitter(false, 0.3f).apply {
                    firstComponent = ToolbarDecorator.createDecorator(allSettingsList)
                            .setAddAction {
                                val newSettings = WebsocketSettingsManager.WebsocketConfigurationSetting("Uname")
                                (allSettingsList.model as DefaultListModel).addElement(newSettings)
                                allSettingsList.setSelectedValue(newSettings, true)
                                nameTextField.text = "Uname"; addressTextField.text = ""
                            }
                            .setRemoveAction {
                                val removeElement = (allSettingsList.model as DefaultListModel).elementAt((it.contextComponent as JBList<*>).selectedIndex)
                                (allSettingsList.model as DefaultListModel).removeElement(removeElement)
                                if (selectedSettings == removeElement){
                                    selectedSettings = null
                                }
                                nameTextField.text = ""; addressTextField.text = ""
                            }
                            .disableUpDownActions()
                            .createPanel()
                    secondComponent = com.intellij.ui.layout.panel {
                        row {
                            cell(isFullWidth = true) {
                                nameLabel()
                                nameTextField(growX).focused()
                            }
                            cell(isFullWidth = true) {
                                addressLabel()
                                addressTextField(growX)
                            }
                        }
                    }
                }(grow, pushY)
            }
        }.withPreferredSize(500, 300)
    }

}