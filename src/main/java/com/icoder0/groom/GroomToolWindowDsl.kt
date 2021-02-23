package com.icoder0.groom

import com.icoder0.groom.websocket.WebsocketArchetypeClient
import com.icoder0.groom.websocket.WebsocketConstant
import com.intellij.icons.AllIcons
import com.intellij.ide.ui.fullRow
import com.intellij.json.JsonFileType
import com.intellij.json.JsonLanguage
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.Messages
import com.intellij.ui.AnActionButton
import com.intellij.ui.LanguageTextField.SimpleDocumentCreator
import com.intellij.ui.TabbedPaneImpl
import com.intellij.ui.ToolbarDecorator
import com.intellij.ui.components.JBTextField
import com.intellij.ui.layout.panel
import com.intellij.ui.table.TableView
import com.intellij.util.castSafelyTo
import com.intellij.util.ui.ColumnInfo
import com.intellij.util.ui.ListTableModel
import org.apache.commons.lang3.StringUtils
import org.apache.commons.lang3.tuple.MutableTriple
import org.apache.commons.lang3.tuple.Triple
import sun.plugin2.message.Message
import java.awt.event.*
import java.beans.PropertyChangeEvent
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit
import javax.swing.*
import javax.swing.SwingConstants.TOP
import javax.swing.event.ChangeEvent
import javax.swing.table.TableRowSorter
import kotlin.Comparator

/**
 * @author bofa1ex
 * @since  2021/2/20
 */
class GroomToolWindowDsl(var project: Project) {

    lateinit var websocketArchetypeClient: WebsocketArchetypeClient

    var assistTextField : JBTextField = JBTextField().apply { isVisible = false }
    /* 启动websocket按钮 */
    private var connectButton: JButton = JButton(AllIcons.Actions.Execute).apply {
        this.addActionListener {
            kotlin.run {
                websocketArchetypeClient = WebsocketArchetypeClient.start(wsAddressTextField.text, this@GroomToolWindowDsl)!!
                try {
                    websocketArchetypeClient.connectBlocking(10, TimeUnit.SECONDS)
                    if (websocketArchetypeClient.isClosed) {
                        Messages.showErrorDialog("Websocket连接失败, 请检查连接地址", "Websocket异常")
                        return@run
                    }
                } catch (e: InterruptedException) {
                    Messages.showErrorDialog("Websocket连接失败, 请尝试重新连接", "Websocket异常")
                    return@run
                }
                fireConnectCallback()
            }
        }
    }

    /* 关闭websocket按钮 */
    private var disconnectButton: JButton = JButton(AllIcons.Actions.Suspend).apply {
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
    private var wsAddressTextField: JBTextField = JBTextField("wss://socket.idcd.com:1443").apply { toolTipText = "wss://{ip}:{port}/" }

    /* 提交websocket request按钮 */
    private var commitButton: JButton = JButton(AllIcons.Actions.Commit).apply {
        this.addActionListener{
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
    }

    /* 查看websocket request 历史记录按钮 */
    private var showButton: JButton = JButton(AllIcons.Actions.Show).apply {
        toolTipText = "暂不支持请求报文历史记录功能, 请关注后续版本变更."
    }

    /* 筛选websocket payload类型ComboBox */
    private var typeComboBox: ComboBox<String> = ComboBox(arrayOf("All", "In", "Out")).apply {
        this.addActionListener {
            kotlin.run {
                wsPayloadTableView.rowSorter.castSafelyTo<TableRowSorter<ListTableModel<Triple<String, Int, LocalDateTime>>>>()!!.rowFilter = object : RowFilter<ListTableModel<Triple<String, Int, LocalDateTime>>, Int>() {
                    override fun include(entry: Entry<out ListTableModel<Triple<String, Int, LocalDateTime>>, out Int>): Boolean {
                        if (selectedIndex == 0){
                            return true
                        }
                        val triple = entry.getValue(0) as Triple<*, *, *>
                        return triple.middle == if (selectedIndex == 1) 0 else 1
                    }
                }
            }
        }
    }

    /* 筛选websocket payload文本框 */
    private var wsPayloadSearchTextField: JBTextField = JBTextField().apply {
        addKeyListener(object : KeyAdapter() {
            override fun keyTyped(e: KeyEvent) {
                if (e.keyCode == KeyEvent.VK_ENTER) {
                    fireFilterTableViewCallback()
                }
            }
        })
    }

    /* 搜索websocket payload按钮 */
    private var wsPayloadSearchButton: JButton = JButton(AllIcons.Actions.Search).apply {
        this.addActionListener{
            run {
                fireFilterTableViewCallback()
            }
        }
    }

    private var wsRequestDocument: Document = SimpleDocumentCreator().createDocument("", JsonLanguage.INSTANCE, project)

    /* websocket request 编辑器 */
    private var wsRequestEditor: Editor = EditorFactory.getInstance().createEditor(wsRequestDocument, project, JsonFileType.INSTANCE, false).apply {
        this.settings.apply {
            additionalLinesCount = 53
            additionalColumnsCount = 0
            isCaretRowShown = false
            isRightMarginShown = false
            isAdditionalPageAtBottom = false
            isLineMarkerAreaShown = false
        }
    }
    private var typeColumnInfo: TypeColumnInfo = TypeColumnInfo("Type")
    private var dataColumnInfo: DataColumnInfo = DataColumnInfo("Data")
    private var lengthColumnInfo: LengthColumnInfo = LengthColumnInfo("Length")
    private var timeColumnInfo: TimeColumnInfo = TimeColumnInfo("Time")

    /* websocket payload tableview */
    private var wsPayloadTableView: TableView<Triple<String, Int, LocalDateTime>> = TableView(ListTableModel<Triple<String, Int, LocalDateTime>>(
            typeColumnInfo,
            dataColumnInfo,
            lengthColumnInfo,
            timeColumnInfo
    ))

    fun getMainPanel(): JComponent {
        val mainPane = TabbedPaneImpl(TOP)
        mainPane.addTab("wsClient", panel {
            titledRow("Control Viewport") {}
            fullRow {
                component(connectButton)
                component(disconnectButton).enabled(false)
                component(wsAddressTextField).constraints(growX).focused()
            }
            fullRow {
                component(commitButton).enabled(false)
                component(showButton).enabled(false)
                component(typeComboBox)
                component(wsPayloadSearchTextField).constraints(growX)
                component(wsPayloadSearchButton)
            }
            titledRow("Payload Viewport") {}
            fullRow {
                /* 编辑 websocket request */
                component(wsRequestEditor.component).constraints(grow)
                /* 显示websocket payload视图 */
                component(decoratorTableView(wsPayloadTableView)).constraints(grow)
            }
        })
        mainPane.addTab("reverse", panel {})
        return mainPane
    }

    fun fireConnectCallback() {
        commitButton.isEnabled = true
        connectButton.isEnabled = false
        disconnectButton.isEnabled = true
        wsAddressTextField.isEditable = false
        wsPayloadTableView.listTableModel.items = mutableListOf()
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
        val text = wsPayloadSearchTextField.text
        wsPayloadTableView.rowSorter.castSafelyTo<TableRowSorter<ListTableModel<Triple<String, Int, LocalDateTime>>>>()!!.rowFilter = object : RowFilter<ListTableModel<Triple<String, Int, LocalDateTime>>, Int>() {
            override fun include(entry: Entry<out ListTableModel<Triple<String, Int, LocalDateTime>>, out Int>): Boolean {
                if (StringUtils.isBlank(text)) {
                    return true
                }
                val triple = entry.getValue(0) as Triple<*, *, *>
                val payload = triple.left as String
                return payload.startsWith(text) || payload.endsWith(text) || payload.matches(Regex(text))
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
        }.createPanel()
    }

    class TimeColumnInfo(name: String?) : ColumnInfo<Triple<String?, Int?, LocalDateTime?>, String>(name) {

        override fun getWidth(table: JTable?): Int {
            return 200
        }

        override fun getComparator(): Comparator<Triple<String?, Int?, LocalDateTime?>> {
            return Comparator { lt: Triple<String?, Int?, LocalDateTime?>, rt: Triple<String?, Int?, LocalDateTime?> ->
                lt.right!!.compareTo(rt.right)
            }
        }

        override fun getColumnClass(): Class<*> {
            return LocalDateTime::class.java
        }

        override fun valueOf(o: Triple<String?, Int?, LocalDateTime?>): String {
            return o.right!!.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
        }
    }

    class LengthColumnInfo(name: String?) : ColumnInfo<Triple<String?, Int?, LocalDateTime?>, Int>(name) {
        override fun getWidth(table: JTable?): Int {
            return 80
        }

        override fun getComparator(): Comparator<Triple<String?, Int?, LocalDateTime?>> {
            return Comparator.comparingInt { obj: Triple<String?, Int?, LocalDateTime?> -> obj.left!!.length }
        }

        override fun getColumnClass(): Class<*> {
            return Int::class.java
        }

        override fun valueOf(o: Triple<String?, Int?, LocalDateTime?>): Int {
            return o.left!!.length
        }
    }

    class DataColumnInfo(name: String?) : ColumnInfo<MutableTriple<String?, Int?, LocalDateTime?>, String>(name) {
        override fun setValue(item: MutableTriple<String?, Int?, LocalDateTime?>?, value: String?) {
            item!!.left = value
            item.right = LocalDateTime.now()
        }

        override fun isCellEditable(item: MutableTriple<String?, Int?, LocalDateTime?>?): Boolean {
            return true
        }

        override fun valueOf(o: MutableTriple<String?, Int?, LocalDateTime?>): String? {
            return o.left
        }
    }

    class TypeColumnInfo(name: String?) : ColumnInfo<Triple<String?, Int?, LocalDateTime?>, Icon>(name) {
        override fun getColumnClass(): Class<*>? {
            return Icon::class.java
        }

        override fun getWidth(table: JTable?): Int {
            return 60
        }

        override fun getComparator(): Comparator<Triple<String?, Int?, LocalDateTime?>> {
            return Comparator.comparingInt { obj: Triple<String?, Int?, LocalDateTime?> -> obj.middle!! }
        }

        override fun valueOf(o: Triple<String?, Int?, LocalDateTime?>): Icon {
            return if (o.middle == 0) AllIcons.Ide.IncomingChangesOn else AllIcons.Ide.OutgoingChangesOn
        }
    }
}