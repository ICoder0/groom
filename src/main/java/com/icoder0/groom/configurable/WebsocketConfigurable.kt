package com.icoder0.groom.configurable

import com.fasterxml.jackson.databind.util.BeanUtil
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
import com.jetbrains.rd.framework.base.deepClonePolymorphic
import com.sun.org.apache.xpath.internal.operations.Bool
import icons.GroomIcons
import org.codehaus.groovy.ast.tools.BeanUtils
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
class WebsocketConfigurable(selectedSettings: WebsocketSettingsManager.WebsocketConfigurationSetting?)
    : BoundSearchableConfigurable("Websocket-Configurable", "", "Websocket-Configurable"
) {

    var selectedSettingsDup = selectedSettings.deepClonePolymorphic()
    var allSettingsDup = WebsocketSettingsManager.allSettings.deepClonePolymorphic()

    init {
        if (selectedSettingsDup == null && allSettingsDup.isEmpty()) {
            selectedSettingsDup = WebsocketSettingsManager.WebsocketConfigurationSetting("Unnamed")
        }
    }

    val addressLabel = JBLabel("RequestURL:\t")
    val nameLabel = JBLabel("Name:\t")
    var addressTextField = JBTextField(selectedSettingsDup?.address)
    var nameTextField = JBTextField(selectedSettingsDup?.name)
    val allSettingsList = JBList<WebsocketSettingsManager.WebsocketConfigurationSetting>(
            if (allSettingsDup.isEmpty()) mutableListOf(selectedSettingsDup) else allSettingsDup
    ).apply {
        setSelectedValue(selectedSettingsDup, true)
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
                selectedSettingsDup?.name = nameTextField.text
                selectedSettingsDup?.address = addressTextField.text

                selectedSettingsDup = this
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
                                val newSettings = WebsocketSettingsManager.WebsocketConfigurationSetting("Unnamed")
                                (allSettingsList.model as DefaultListModel).addElement(newSettings)
                                allSettingsList.setSelectedValue(newSettings, true)
                                nameTextField.text = "Unnamed"; addressTextField.text = ""
                            }
                            .setRemoveAction {
                                val removeElement = (allSettingsList.model as DefaultListModel).elementAt((it.contextComponent as JBList<*>).selectedIndex)
                                (allSettingsList.model as DefaultListModel).removeElement(removeElement)
                                if (selectedSettingsDup == removeElement){
                                    selectedSettingsDup = null
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