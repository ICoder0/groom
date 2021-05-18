package com.icoder0.groom.component

import com.intellij.configurationStore.APP_CONFIG
import com.intellij.configurationStore.deserializeInto
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.RoamingType
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.util.xmlb.XmlSerializer
import org.jdom.Element

/**
 * @author bofa1ex
 * @since  2021/5/17
 */
@State(name = "ConfigProviderState", storages = [Storage(
        "$APP_CONFIG\$/configProvider.xml",
        roamingType = RoamingType.DISABLED)
])
class WebsocketSettingsManager : PersistentStateComponent<Element> {

    companion object {
        var allSettings = mutableListOf<WebsocketConfigurationSetting>()
    }

    class WebsocketConfigurationSetting(var name: String = "", var address: String = "") {
        override fun toString(): String {
            return name
        }
    }

    override fun getState(): Element? {
        val state: Element = XmlSerializer.serialize(this)
        for (setting in allSettings) {
            val content = Element("setting")
            content.setAttribute("name", setting.name)
            content.setAttribute("address", setting.address)
            state.addContent(content)
        }
        return state
    }

    override fun loadState(state: Element) {
        state.deserializeInto(this)
        for (element in state.getChildren("setting")) {
            allSettings.add(WebsocketConfigurationSetting(
                    element.getAttributeValue("name"),
                    element.getAttributeValue("address")
            ))
        }
    }
}