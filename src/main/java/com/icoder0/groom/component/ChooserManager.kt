package com.icoder0.groom.component

import com.icoder0.groom.action.WebsocketPanelViewFilterAction.FilterObjectKind.EDITOR_VIEW
import com.icoder0.groom.action.WebsocketTableViewFilterAction.FilterObjectKind.INBOUND
import com.icoder0.groom.action.WebsocketTableViewFilterAction.FilterObjectKind.OUTBOUND
import com.icoder0.groom.action.WebsocketPanelViewFilterAction.FilterObjectKind.TABLE_VIEW
import com.intellij.icons.AllIcons
import com.intellij.ide.util.ElementsChooser
import com.intellij.ide.util.MultiStateElementsChooser
import com.intellij.ui.table.JBTable
import java.awt.Dimension
import java.util.function.Supplier
import java.util.function.UnaryOperator
import javax.swing.Icon
import javax.swing.JPanel

/**
 * @author bofa1ex
 * @since  2021/3/5
 */
open class ChooserManager {
    companion object ChooserManagerInternal {
        val viewChooserDisplaySupplier: Supplier<ElementsChooser<String?>> = Supplier {
            return@Supplier object : ElementsChooser<String?>(true) {}.apply {
                addElement(TABLE_VIEW, true, object : MultiStateElementsChooser.ElementProperties {
                    override fun getIcon(): Icon {
                        return AllIcons.Nodes.Type
                    }
                })
                addElement(EDITOR_VIEW, true, object : MultiStateElementsChooser.ElementProperties {
                    override fun getIcon(): Icon {
                        return AllIcons.Nodes.Enum
                    }
                })
                with(component as JBTable) {
                    this@apply.preferredSize = Dimension(150, rowHeight * rowCount + 10)
                }
            }
        }

        val payloadChooserDisplaySupplier: Supplier<ElementsChooser<String?>> = Supplier {
            return@Supplier object : ElementsChooser<String?>(true) {}.apply {
                addElement(INBOUND, true, object : MultiStateElementsChooser.ElementProperties {
                    override fun getIcon(): Icon {
                        return AllIcons.Ide.IncomingChangesOn
                    }
                })
                addElement(OUTBOUND, true, object : MultiStateElementsChooser.ElementProperties {
                    override fun getIcon(): Icon {
                        return AllIcons.Ide.OutgoingChangesOn
                    }
                })
                with(component as JBTable) {
                    this@apply.preferredSize = Dimension(150, rowHeight * rowCount + 10)
                }
            }
        }

        val viewChooserDisplayMap: MutableMap<JPanel, ElementsChooser<String?>> = HashMap()

        val payloadChooserDisplayMap: MutableMap<JPanel, ElementsChooser<String?>> = HashMap()

        fun getViewChooser(panel: JPanel, applyFunction: UnaryOperator<ElementsChooser<String?>>): ElementsChooser<String?> {
            return viewChooserDisplayMap.compute(panel) { _: JPanel, u: ElementsChooser<String?>? ->
                if (u == null) {
                    return@compute viewChooserDisplaySupplier.get().apply {
                        applyFunction.apply(this)
                    }
                }
                return@compute u
            }!!
        }

        fun getPayloadChooser(panel: JPanel, applyFunction: UnaryOperator<ElementsChooser<String?>>): ElementsChooser<String?> {
            return payloadChooserDisplayMap.compute(panel) { _: JPanel, u: ElementsChooser<String?>? ->
                if (u == null) {
                    return@compute payloadChooserDisplaySupplier.get().apply {
                        applyFunction.apply(this)
                    }
                }
                return@compute u
            }!!
        }
    }
}