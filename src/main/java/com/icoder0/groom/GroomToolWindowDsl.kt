package com.icoder0.groom

import com.icoder0.groom.renderer.EditorExTableCellEditor
import com.icoder0.groom.component.EditorManager
import com.icoder0.groom.renderer.IconRendererEx
import com.icoder0.groom.renderer.ObjectRendererEx
import com.icoder0.groom.renderer.TextFieldTableCellRenderer
import com.icoder0.groom.websocket.WebsocketArchetypeClient
import com.icoder0.groom.websocket.WebsocketConstant
import com.intellij.icons.AllIcons
import com.intellij.ide.ui.fullRow
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.ui.popup.*
import com.intellij.ui.AnActionButton
import com.intellij.ui.TabbedPaneImpl
import com.intellij.ui.ToolbarDecorator
import com.intellij.ui.components.JBTextField
import com.intellij.ui.layout.panel
import com.intellij.ui.table.TableView
import com.intellij.util.castSafelyTo
import com.intellij.util.ui.ColumnInfo
import com.intellij.util.ui.ListTableModel
import com.intellij.util.ui.UIUtil
import com.intellij.util.ui.components.BorderLayoutPanel
import org.apache.commons.lang3.StringUtils
import org.apache.commons.lang3.tuple.MutablePair
import org.apache.commons.lang3.tuple.MutableTriple
import org.apache.commons.lang3.tuple.Triple
import java.awt.Dimension
import java.awt.event.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit
import javax.swing.*
import javax.swing.SwingConstants.TOP
import javax.swing.event.ChangeEvent
import javax.swing.table.TableCellEditor
import javax.swing.table.TableCellRenderer
import javax.swing.table.TableRowSorter

/**
 * @author bofa1ex
 * @since  2021/2/20
 */
class GroomToolWindowDsl(var project: Project) {

    lateinit var websocketArchetypeClient: WebsocketArchetypeClient

    private var connectProgressBar = JProgressBar(0, 0, 10)

    /* 启动websocket按钮 */
    private var connectButton = JButton(AllIcons.Actions.Execute).apply {
        this.addActionListener {
            kotlin.run {
                websocketArchetypeClient = WebsocketArchetypeClient.start(wsAddressTextField.text, this@GroomToolWindowDsl)!!
                var connectState = false
                try {
                    val connectPopup = JBPopupFactory.getInstance().createBalloonBuilder(connectProgressBar)
                            .setTitle("正在连接Websocket")
                            .setAnimationCycle(0)
                            .setHideOnClickOutside(true)
                            .setHideOnKeyOutside(true)
                            .setHideOnAction(false)
                            .setFillColor(UIUtil.getControlColor())
//                            .setCloseButtonEnabled(true)
//                            .setClickHandler({
//                                connectState = true
//                            }, true)
                            .createBalloon()
                    connectPopup.showInCenterOf(this)
                    val connectFuture = CompletableFuture.runAsync {
                        connectProgressBar.value = connectProgressBar.minimum
                        while (connectProgressBar.value < 10) {
                            if (connectState) {
                                break
                            }
                            connectProgressBar.value++
                            TimeUnit.SECONDS.sleep(1)
                        }
                        if (websocketArchetypeClient.isOpen) {
                            connectProgressBar.value = connectProgressBar.maximum
                            fireConnectCallback()
                        }
                        TimeUnit.MILLISECONDS.sleep(100)
                        connectPopup.hide()
                    }
                    if (!websocketArchetypeClient.connectBlocking(10, TimeUnit.SECONDS)) {
                        connectFuture.cancel(true)
                        Messages.showErrorDialog("Websocket连接失败, 请尝试重新连接", "Websocket异常")
                        return@run
                    }

                } catch (e: InterruptedException) {
                    Messages.showErrorDialog("Websocket连接失败, 请尝试重新连接", "Websocket异常")
                    return@run
                } finally {
                    connectState = true
                }
            }
        }
    }

    /* 关闭websocket按钮 */
    private var disconnectButton = JButton(AllIcons.Actions.Suspend).apply {
        this.addActionListener {
            kotlin.run {
                try {
                    websocketArchetypeClient.closeBlocking()
                } catch (e: InterruptedException) {
                    Messages.showErrorDialog("Websocket关闭连接失败", "Websocket请求异常");
                    return@run
                }
                fireDisconnectCallback()
            }
        }
    }

    /* websocket#address文本框 */
    private var wsAddressTextField = JBTextField("wss://socket.idcd.com:1443").apply { toolTipText = "wss://{ip}:{port}/" }

    /* 提交websocket request按钮 */
    private var commitButton = JButton(AllIcons.Actions.Commit).apply {
        this.addActionListener {
            kotlin.run {
                val text = wsRequestEditor.document.text
                if (StringUtils.isBlank(text)) {
                    Messages.showErrorDialog("Websocket请求不可为空", "Websocket请求异常");
                    return@run
                }
                if (!websocketArchetypeClient.isOpen) {
                    Messages.showWarningDialog("Websocket connect is not open!", "Websocket请求异常");
                    return@run
                }
                websocketArchetypeClient.send(text)
                messageCallback(WebsocketConstant.outboundType, text)
            }
        }
    }.apply { isEnabled = false }

    /* 查看websocket request 历史记录按钮 */
    private var showButton = JButton(AllIcons.Actions.Show).apply {
        toolTipText = "暂不支持请求报文历史记录功能, 请关注后续版本变更."
    }.apply { isEnabled = false }

    /* 筛选websocket payload类型ComboBox */
    private var typeComboBox = ComboBox(arrayOf("All", "In", "Out")).apply {
        this.addActionListener {
            kotlin.run {
                wsPayloadTableView.rowSorter.castSafelyTo<TableRowSorter<ListTableModel<Triple<String, Int, LocalDateTime>>>>()!!.rowFilter = object : RowFilter<ListTableModel<Triple<String, Int, LocalDateTime>>, Int>() {
                    override fun include(entry: Entry<out ListTableModel<Triple<String, Int, LocalDateTime>>, out Int>): Boolean {
                        return typeComboBoxInclude(entry) && searchTextFieldInclude(entry)
                    }
                }
            }
        }
    }

    /* 筛选websocket payload文本框 */
    private var wsPayloadSearchTextField = JBTextField().apply {
        addKeyListener(object : KeyAdapter() {
            override fun keyTyped(e: KeyEvent) {
                if (e.keyCode == KeyEvent.VK_ENTER) {
                    fireFilterTableViewCallback()
                }
            }
        })
    }

    /* 搜索websocket payload按钮 */
    private var wsPayloadSearchButton = JButton(AllIcons.Actions.Search).apply {
        this.addActionListener {
            run {
                fireFilterTableViewCallback()
            }
        }
    }

    /* websocket request 编辑器 */
    private var wsRequestEditor = EditorManager.getEditor("json")

    private var languageComboBox = ComboBox(arrayOf("json", "xml", "html", "java", "plainText")).apply {
        this.addActionListener {
            val oldText: String = wsRequestEditor.document.text
            wsRequestEditorWrapperPanel.remove(wsRequestEditor.component)
            wsRequestEditor = EditorManager.getEditor(this.selectedItem as String)
            // Make the document change in the context of a write action.
            WriteCommandAction.runWriteCommandAction(project) { wsRequestEditor.document.replaceString(
                    0, wsRequestEditor.document.textLength, oldText
            ) }
            wsRequestEditorWrapperPanel.add(wsRequestEditor.component)
        }
    }

    private var wsRequestEditorWrapperPanel = BorderLayoutPanel().apply {
        addToCenter(wsRequestEditor.component)
    }

    private var dataColumnInfo = DataColumnInfo("Data")
    private var typeColumnInfo = TypeColumnInfo("Type")
    private var timeColumnInfo = TimeColumnInfo("Time")

    /**
     * websocket payload tableview
     * @param string 文本内容
     * @param int   0:inbound,1:outbound
     * @param localDateTime 日期
     */
    private var wsPayloadTableView = object : TableView<Triple<String, Int, LocalDateTime>>(ListTableModel(
            typeColumnInfo,
            dataColumnInfo,
            timeColumnInfo
    )){
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
    }.apply {
        fillsViewportHeight = true
        rowSorter.sortKeys = arrayListOf()
    }

    fun getMainPanel(): JComponent {
        val mainPane = TabbedPaneImpl(TOP)
        mainPane.addTab("wsClient", panel {
            titledRow("Control Viewport") {}
            fullRow {
                connectButton()
                disconnectButton().enabled(false)
                wsAddressTextField(growX).focused()
            }
            fullRow {
                typeComboBox()
                wsPayloadSearchTextField(growX)
                wsPayloadSearchButton()
            }
            titledRow("Payload Viewport") {}
            fullRow {
                decoratorTableView(wsPayloadTableView)(grow)
            }
            fullRow {
                wsRequestEditorWrapperPanel(grow)
            }
            row {
                right {
                    BorderLayoutPanel().addToRight(commitButton).addToCenter(languageComboBox)()
                }
            }
        })
        mainPane.addTab("reverse", panel {})
        return mainPane
    }

    fun typeComboBoxInclude(entry: RowFilter.Entry<out ListTableModel<Triple<String, Int, LocalDateTime>>, out Int>): Boolean {
        if (typeComboBox.selectedIndex == 0) {
            return true
        }
        val triple = entry.getValue(0) as Triple<*, *, *>
        return triple.middle == if (typeComboBox.selectedIndex == 1) 0 else 1
    }

    fun searchTextFieldInclude(entry: RowFilter.Entry<out ListTableModel<Triple<String, Int, LocalDateTime>>, out Int>): Boolean {
        val text = wsPayloadSearchTextField.text
        if (StringUtils.isBlank(text)) {
            return true
        }
        val triple = entry.getValue(0) as Triple<*, *, *>
        val payload = (triple.left as String).trim()
                .replace("\n", "")
                .replace("\t", "")
        return payload.startsWith(text) || payload.endsWith(text) || payload.matches(Regex(text))
    }

    fun fireConnectCallback() {
        commitButton.isEnabled = true
        connectButton.isEnabled = false
        disconnectButton.isEnabled = true
        wsAddressTextField.isEditable = false
        wsPayloadTableView.listTableModel.items = mutableListOf()
        dataColumnInfo.reset()
    }

    fun fireDisconnectCallback() {
        commitButton.isEnabled = false;
        connectButton.isEnabled = true;
        disconnectButton.isEnabled = false;
        wsAddressTextField.isEditable = true;
    }

    fun messageCallback(type: Int, message: String) {
        wsPayloadTableView.listTableModel.addRow(MutableTriple.of(
                message, if (type == WebsocketConstant.inboundType) 0 else 1, LocalDateTime.now()
        ))
    }

    fun fireFilterTableViewCallback(): Unit {
        wsPayloadTableView.rowSorter.castSafelyTo<TableRowSorter<ListTableModel<Triple<String, Int, LocalDateTime>>>>()!!.rowFilter = object : RowFilter<ListTableModel<Triple<String, Int, LocalDateTime>>, Int>() {
            override fun include(entry: Entry<out ListTableModel<Triple<String, Int, LocalDateTime>>, out Int>): Boolean {
                return typeComboBoxInclude(entry) && searchTextFieldInclude(entry)
            }
        }
    }

    fun decoratorTableView(tableView: TableView<Triple<String, Int, LocalDateTime>>): JComponent {
        return ToolbarDecorator.createDecorator(tableView).setAddAction { t: AnActionButton? ->
            val inputPair = Messages.showInputDialogWithCheckBox(
                    "input payload", "Insert Payload Row", "in/out", true,
                    true, AllIcons.Actions.Profile, "", null
            )
            tableView.listTableModel.addRow(MutableTriple.of(inputPair.first, if (inputPair.second) 0 else 1, LocalDateTime.now()))
        }.setRemoveAction { t: AnActionButton? ->
            tableView.listTableModel.removeRow(tableView.selectedRow)
        }.setMinimumSize(Dimension(tableView.width, 300))
                .disableUpDownActions()
                .createPanel()
    }

    class TimeColumnInfo(name: String?) : ColumnInfo<Triple<String?, Int?, LocalDateTime?>, String>(name) {
        val renderer = ObjectRendererEx()

        override fun getWidth(table: JTable): Int {
            return table.getFontMetrics(table.font).stringWidth(" 00:00:00 ")
        }

        override fun getComparator(): Comparator<Triple<String?, Int?, LocalDateTime?>> {
            return Comparator { lt: Triple<String?, Int?, LocalDateTime?>, rt: Triple<String?, Int?, LocalDateTime?> ->
                lt.right!!.compareTo(rt.right)
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

        val editor = EditorExTableCellEditor(project)

        val renderer = TextFieldTableCellRenderer()

        fun reset() {
            renderer.reset()
        }

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

        override fun getComparator(): Comparator<Triple<String?, Int?, LocalDateTime?>> {
            return Comparator.comparingInt { obj: Triple<String?, Int?, LocalDateTime?> -> obj.middle!! }
        }

        override fun valueOf(o: Triple<String?, Int?, LocalDateTime?>): Icon {
            return if (o.middle == 0) AllIcons.Ide.IncomingChangesOn else AllIcons.Ide.OutgoingChangesOn
        }
    }
}