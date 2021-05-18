package com.icoder0.groom.ui

import com.icoder0.groom.component.EditorManager
import com.icoder0.groom.component.EditorManager.EditorManagerInternal.disposePanel
import com.icoder0.groom.ui.renderer.EditorExTableCellEditor
import com.icoder0.groom.ui.renderer.IconRendererEx
import com.icoder0.groom.ui.renderer.ObjectRendererEx
import com.icoder0.groom.ui.renderer.TextFieldTableCellRenderer
import com.intellij.execution.runners.ExecutionUtil
import com.intellij.icons.AllIcons
import com.intellij.openapi.Disposable
import com.intellij.openapi.actionSystem.ActionGroup
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.DataKey
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.wm.ex.ToolWindowEx
import com.intellij.ui.JBSplitter
import com.intellij.ui.SearchTextField
import com.intellij.ui.ToolbarDecorator
import com.intellij.ui.layout.panel
import com.intellij.ui.table.TableView
import com.intellij.util.castSafelyTo
import com.intellij.util.ui.ColumnInfo
import com.intellij.util.ui.JBUI.Panels.simplePanel
import com.intellij.util.ui.ListTableModel
import com.neovisionaries.ws.client.WebSocket
import com.neovisionaries.ws.client.WebSocketFactory
import icons.GroomIcons
import org.apache.commons.lang3.StringUtils
import org.apache.commons.lang3.tuple.MutablePair
import org.apache.commons.lang3.tuple.MutableTriple
import org.apache.commons.lang3.tuple.Triple
import java.awt.event.KeyEvent
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.swing.*
import javax.swing.event.ChangeEvent
import javax.swing.table.TableCellEditor
import javax.swing.table.TableCellRenderer
import javax.swing.table.TableRowSorter


/**
 * @author bofa1ex
 * @since  2021/3/12
 */
open class WebsocketClientView(project: Project, toolWindow: ToolWindowEx) : GroomToolWindowPanel("WebsocketClient", project, toolWindow) {

    var wsClientAddress : String? = null
    var wsClient: WebSocket? = null
    var wsPayloadMatch: String? = null
    var wsPayloadType: Int = allType
    val editor = EditorExTableCellEditor(project)

    companion object WebsocketManager {
        val webSocketFactory = WebSocketFactory()
        val WEBSOCKET_VIEW_KEY: DataKey<WebsocketClientView> = DataKey.create("WEBSOCKET_VIEW_KEY")
        const val inboundType = 0
        const val outboundType = 1
        const val allType = -1
    }

    open fun fireToggleTableView(isMarked: Boolean) {
        tableViewPanel.isVisible = isMarked
    }

    open fun fireToggleEditor(isMarked: Boolean) {
        wsRequestEditorWrapperPanel.isVisible = isMarked
        languageComboBox.isVisible = isMarked
        commitButton.isVisible = isMarked
    }

    open fun fireWebsocketPreConnect() {
        wsPayloadTableView.listTableModel?.items = mutableListOf()
    }

    open fun fireWebsocketConnected(newWebsocketClient: WebSocket?) {
        wsClient = newWebsocketClient
        commitButton.isEnabled = true
        WriteCommandAction.runWriteCommandAction(project) {
            with(toolWindow.contentManager.getContent(this)) {
                icon = ExecutionUtil.getLiveIndicator(icon)
                popupIcon = ExecutionUtil.getLiveIndicator(icon)
            }
        }
    }

    open fun fireMessageChanged(type: Int, message: String?) {
        wsPayloadTableView.listTableModel?.addRow(MutableTriple.of(
                message, if (type == inboundType) 0 else 1, LocalDateTime.now()
        ))
    }

    open fun fireWebsocketDisconnected() {
        commitButton.isEnabled = false
        WriteCommandAction.runWriteCommandAction(project) {
            with(toolWindow.contentManager.getContent(this)) {
                icon = GroomIcons.Socket
            }
        }
    }

    open fun firePayloadFilter(type: Int?, candidate: String?) {
        wsPayloadTableView.rowSorter?.castSafelyTo<TableRowSorter<ListTableModel<Triple<String, Int, LocalDateTime>>>>()!!.rowFilter = object : RowFilter<ListTableModel<Triple<String, Int, LocalDateTime>>, Int>() {
            override fun include(entry: Entry<out ListTableModel<Triple<String, Int, LocalDateTime>>, out Int>?): Boolean {
                if (type != null) {
                    wsPayloadType = type
                }
                if (candidate != null) {
                    wsPayloadMatch = candidate
                }
                val triple = entry?.getValue(0) as Triple<*, *, *>
                if (type != allType && triple.middle != type) {
                    return false
                }
                if (candidate == null || candidate.isBlank()) {
                    return true
                }
                val payload = (triple.left as String).trim()
                        .replace("\n", "")
                        .replace("\t", "")
                return payload.startsWith(candidate) || payload.endsWith(candidate) || payload.matches(Regex(candidate))
            }
        }
    }

    override fun initUI(): WebsocketClientView {
        with(ActionManager.getInstance()) {
            setContent(panel {
                row {
                    cell {
                        createActionToolbar(
                                "WebsocketViewMainToolbar",
                                getAction("WebsocketView.MainToolbar") as ActionGroup,
                                true
                        ).apply {
                            setTargetComponent(this@WebsocketClientView)
                        }.component(growX)
                    }
                    right { wsSearchTextField() }
                }
                row {
                    cell(isFullWidth = true) {
                        JBSplitter(true).apply {
                            firstComponent = tableViewPanel
                            secondComponent = wsRequestEditorWrapperPanel
                        }(grow, pushY).focused()
                    }
                    right { simplePanel(languageComboBox).addToRight(commitButton)() }
                }
            })
        }
        return this
    }

//    /* websocket#address文本框 */
//    private var wsAddressTextField = JBTextField("wss://socket.idcd.com:1443").apply { toolTipText = "wss://{ip}:{port}/" }

    private var commitButton = JButton(AllIcons.Actions.Commit).apply {
        this.addActionListener {
            kotlin.run {
                val text = wsRequestEditor.document.text
                if (StringUtils.isBlank(text)) {
                    Messages.showErrorDialog("Websocket request body could not be empty", "Websocket Request Error")
                    return@run
                }
                wsClient?.sendText(text)
                fireMessageChanged(outboundType, text)
            }
        }
    }.apply { isEnabled = false }

    private var wsRequestEditor = EditorManager.getEditor(this, "json")

    private var wsRequestEditorWrapperPanel = simplePanel(wsRequestEditor.component)

    private var wsSearchTextField = object : SearchTextField(true) {
        override fun preprocessEventForTextField(e: KeyEvent?): Boolean {
            if (KeyEvent.VK_ENTER == e?.keyCode || '\n' == e?.keyChar) {
                firePayloadFilter(wsPayloadType, this.text)
                e.consume()
                addCurrentTextToHistory()
            }
            return super.preprocessEventForTextField(e)
        }
    }

    private var languageComboBox = ComboBox(arrayOf("json", "xml", "html", "plainText")).apply {
        this.addActionListener {
            val oldText: String = wsRequestEditor.document.text
            wsRequestEditorWrapperPanel.remove(wsRequestEditor.component)
            wsRequestEditor = EditorManager.getEditor(this@WebsocketClientView, this.selectedItem as String)
            // Make the document change in the context of a write action.
            WriteCommandAction.runWriteCommandAction(project) {
                wsRequestEditor.document.replaceString(
                        0, wsRequestEditor.document.textLength, oldText
                )
            }
            wsRequestEditorWrapperPanel.add(wsRequestEditor.component)
            wsRequestEditorWrapperPanel.updateUI()
        }
    }

    /**
     * websocket payload tableview
     * @param string 文本内容
     * @param int   0:inbound,1:outbound
     * @param localDateTime 日期
     */
    var wsPayloadTableView = object : TableView<Triple<String, Int, LocalDateTime>>(ListTableModel(
            TypeColumnInfo("Type"),
            DataColumnInfo("Data"),
            TimeColumnInfo("Time")
    )) {
        override fun columnMarginChanged(e: ChangeEvent?) {
            val resizingColumn = if (tableHeader == null) null else tableHeader.resizingColumn
            // Need to do this here, before the parent's
            // layout manager calls getPreferredSize().
            // Need to do this here, before the parent's
            // layout manager calls getPreferredSize().
            if (resizingColumn != null && autoResizeMode == JTable.AUTO_RESIZE_OFF) {
                resizingColumn.preferredWidth = resizingColumn.width
            }
            resizeAndRepaint()
        }
    }

    private var tableViewPanel = with(wsPayloadTableView) {
        ToolbarDecorator.createDecorator(this).setAddAction {
            val inputPair = Messages.showInputDialogWithCheckBox(
                    "input payload", "Insert Payload Row", "In/Out", true,
                    true, AllIcons.Actions.Profile, "", null
            )
            listTableModel.addRow(MutableTriple.of(inputPair.first, if (inputPair.second) 0 else 1, LocalDateTime.now()))
        }.setRemoveAction {
            listTableModel.removeRow(selectedRow)
        }.disableUpDownActions().createPanel()
    }

    class TimeColumnInfo(name: String?) : ColumnInfo<Triple<String?, Int?, LocalDateTime?>, String>(name) {
        val renderer = ObjectRendererEx()

        override fun getWidth(table: JTable): Int {
            return table.getFontMetrics(table.font).stringWidth(" 00:00:00 ")
        }

        override fun getComparator(): Comparator<Triple<String?, Int?, LocalDateTime?>> {
            return Comparator { lt: Triple<String?, Int?, LocalDateTime?>, rt: Triple<String?, Int?, LocalDateTime?> ->
                if (lt.right == null) {
                    return@Comparator 0
                }
                return@Comparator lt.right!!.compareTo(rt.right)
            }
        }

        override fun getRenderer(item: Triple<String?, Int?, LocalDateTime?>?): TableCellRenderer? {
            return renderer
        }

        override fun getColumnClass(): Class<*> {
            return LocalDateTime::class.java
        }

        override fun valueOf(o: Triple<String?, Int?, LocalDateTime?>): String {
            return o.right!!.format(DateTimeFormatter.ofPattern("HH:mm:ss"))
        }
    }

    inner class DataColumnInfo(name: String?) : ColumnInfo<MutableTriple<String?, Int?, LocalDateTime?>, String>(name) {

        val renderer = TextFieldTableCellRenderer()

        override fun getEditor(item: MutableTriple<String?, Int?, LocalDateTime?>?): TableCellEditor? {
            return editor
        }

        override fun isCellEditable(item: MutableTriple<String?, Int?, LocalDateTime?>?): Boolean {
            return true
        }

        override fun getRenderer(item: MutableTriple<String?, Int?, LocalDateTime?>?): TableCellRenderer? {
            return renderer
        }

        override fun getColumnClass(): Class<*> {
            return MutablePair::class.java
        }

        override fun setValue(item: MutableTriple<String?, Int?, LocalDateTime?>, value: String?) {
            item.left = value
        }

        override fun valueOf(o: MutableTriple<String?, Int?, LocalDateTime?>): String? {
            return o.left
        }
    }


    class TypeColumnInfo(name: String?) : ColumnInfo<Triple<String?, Int?, LocalDateTime?>, Icon>(name) {

        val renderer = IconRendererEx()

        override fun getWidth(table: JTable): Int {
            return table.getFontMetrics(table.font).stringWidth(" 00:00:00 ")
        }

        override fun getColumnClass(): Class<*>? {
            return Icon::class.java
        }

        override fun getRenderer(item: Triple<String?, Int?, LocalDateTime?>?): TableCellRenderer? {
            return renderer
        }

        override fun valueOf(o: Triple<String?, Int?, LocalDateTime?>): Icon {
            return if (o.middle == 0) AllIcons.Ide.IncomingChangesOn else AllIcons.Ide.OutgoingChangesOn
        }
    }

    override fun getData(dataId: String): Any? {
        val data = super.getData(dataId)
        if (data == null) {
            if (WEBSOCKET_VIEW_KEY.`is`(dataId)) {
                return this
            }
        }
        return data
    }

    override fun dispose() {
        super.dispose()
        disposePanel(this)
        wsPayloadTableView.removeNotify()
        editor.dispose()
        wsClient?.disconnect(-1, "system dispose")
    }
}