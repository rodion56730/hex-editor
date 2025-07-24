package com.example.hexeditor;

import javax.swing.JPanel;
import javax.swing.SwingWorker;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.JScrollPane;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.ListSelectionModel;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.GridLayout;
import java.awt.Font;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Color;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import static java.awt.Color.WHITE;
import static java.awt.Color.YELLOW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Главное окно HEX-редактора для просмотра и редактирования бинарных файлов.
 * Предоставляет функциональность для:
 * - Отображения файла в hex-формате с адресацией
 * - Редактирования содержимого файла
 * - Поиска данных по шаблону
 * - Копирования/вставки данных
 * - Интерпретации данных в различных форматах (int, float и т.д.)
 */
public class HexEditorFrame extends JFrame {
    /** Ширина окна по умолчанию */
    public static final int WIDTH = 900;
    /** Высота окна по умолчанию */
    public static final int HEIGHT = 600;
    /** Количество байт в одной строке */
    public static final int BYTES_PER_ROW = 16;
    /** Размер шрифта для отображения */
    public static final int FONT_SIZE = 12;
    /** Цвет выделения ячеек */
    public static final Color SELECTED_BLUE_COLOR = new Color(173, 216, 230);

    private File currentFile;
    private HexFileModel fileModel;
    private JTable table;
    private JLabel statusLabel;
    private JLabel valueLabel;
    private byte[][] clipboard = null;
    private int clipboardRows = 0;
    private int clipboardCols = 0;
    HexTableModel tableModel = null;
    private static final Logger logger = LoggerFactory.getLogger(HexEditorFrame.class);

    /**
     * Создает новый экземпляр редактора для указанного файла.
     *
     * @param file файл для редактирования
     * @throws IOException если произошла ошибка при открытии файла
     */
    public HexEditorFrame(File file) throws IOException {
        logger.info("Создание HexEditorFrame для файла: {}", file.getAbsolutePath());
        this.currentFile = file;
        initializeEditor();
    }

    /**
     * Инициализирует компоненты редактора.
     *
     * @throws IOException если произошла ошибка при инициализации
     */
    private void initializeEditor() throws IOException {
        logger.debug("Инициализация редактора...");
        this.fileModel = new HexFileModel(currentFile);
        setTitle("HEX редактор - " + currentFile.getName());
        setSize(WIDTH, HEIGHT);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        getContentPane().removeAll();

        int rowCount = (int) (fileModel.getLength()/(BYTES_PER_ROW - 1));

        tableModel = new HexTableModel(fileModel, BYTES_PER_ROW);
        table = new JTable(tableModel) {
            @Override
            public String getToolTipText(MouseEvent e) {
                int row = rowAtPoint(e.getPoint());
                int col = columnAtPoint(e.getPoint());
                if (row < 0 || col < 0) return null;

                long offset = (long) row * getColumnCount() + col;
                try {
                    int value = fileModel.readByte(offset);
                    logger.info("Файл успешно загружен: {} (размер: {} байт)",
                            currentFile.getName(), fileModel.getLength());
                    int unsigned = value & 0xFF;

                    return "<html>" + String.format("Offset: %08X\nHex: %02X\nSigned: %d\nUnsigned: %d\n", offset, unsigned, value, unsigned).replace("\n", "<br>") + "</html>";
                } catch (IOException ex) {
                    logger.error("Ошибка инициализации редактора для файла {} {}", currentFile.getName(), e);
                    return "Ошибка чтения байта";
                }
            }
        };


        table.setFont(new Font("Monospaced", Font.PLAIN, FONT_SIZE));
        table.setCellSelectionEnabled(true);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        table.getSelectionModel().addListSelectionListener(e -> updateStatus());
        table.getColumnModel().getSelectionModel().addListSelectionListener(e -> updateStatus());

        JScrollPane scrollPane = new JScrollPane(table);

        JTable rowHeader = new JTable(new RowHeaderTableModel(rowCount, BYTES_PER_ROW));
        rowHeader.setPreferredScrollableViewportSize(new Dimension(80, 0));
        rowHeader.setDefaultRenderer(Object.class, table.getTableHeader().getDefaultRenderer());
        rowHeader.setRowHeight(table.getRowHeight());
        rowHeader.setEnabled(false);
        scrollPane.setRowHeaderView(rowHeader);

        add(scrollPane, BorderLayout.CENTER);

        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table,
                                                           Object value,
                                                           boolean isSelected,
                                                           boolean hasFocus,
                                                           int row,
                                                           int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                HexTableModel model = (HexTableModel) table.getModel();
                int bytesPerRow = table.getColumnCount();
                int offset = row * bytesPerRow + column;

                if (isSelected) {
                    c.setBackground(SELECTED_BLUE_COLOR);
                } else if (model.isHighlighted(offset)) {
                    c.setBackground(YELLOW);
                } else {
                    c.setBackground(WHITE);
                }

                return c;
            }
        });

        statusLabel = new JLabel("");
        statusLabel.setFont(new Font("Monospaced", Font.PLAIN, FONT_SIZE));
        valueLabel = new JLabel(" ");
        valueLabel.setFont(new Font("Monospaced", Font.PLAIN, FONT_SIZE));
        JPanel infoPanel = new JPanel(new GridLayout(2, 1));
        infoPanel.add(statusLabel);
        infoPanel.add(valueLabel);
        add(infoPanel, BorderLayout.SOUTH);

        JMenuBar menuBar = new JMenuBar();
        menuBar.add(buildFileMenu());
        menuBar.add(buildSearchMenu());
        menuBar.add(buildEditMenu());
        setJMenuBar(menuBar);

        revalidate();
        repaint();
    }

    /**
     * Создает меню поиска.
     *
     * @return созданное меню поиска
     */
    private JMenu buildSearchMenu() {
        logger.trace("Создание меню 'Поиск'");
        JMenu searchMenu = new JMenu("Поиск");
        JMenuItem findItem = new JMenuItem("Найти...");
        JMenuItem clearHighlights = new JMenuItem("Снять выделение");
        clearHighlights.addActionListener(e -> {
            tableModel.clearSearchHighlights();
            table.repaint();
        });

        searchMenu.add(clearHighlights);
        findItem.addActionListener(e -> {
            SearchDialog dialog = new SearchDialog(this);
            dialog.setVisible(true);
            if (!dialog.isConfirmed()) return;

            byte[] pattern = dialog.getHexBytes();
            byte[] mask = dialog.getMaskBytes();

            if ((pattern == null || pattern.length == 0) && (mask == null || mask.length == 0)) {
                showError("Введите байты или маску для поиска.");
                return;
            }

            if (mask == null) {
                mask = new byte[pattern.length];
                Arrays.fill(mask, (byte) 0xFF);
            }

            byte[] finalPattern = pattern;
            byte[] finalMask = mask;

            new SwingWorker<List<Integer>, Void>() {
                @Override
                protected List<Integer> doInBackground() throws Exception {
                    SearchController searcher = new SearchController(fileModel);
                    return searcher.searchWithMask(finalPattern, finalMask);
                }

                @Override
                protected void done() {
                    try {
                        List<Integer> result = get();

                        SwingUtilities.invokeLater(() -> {
                            tableModel.setHighlightedOffsets(result);
                            table.repaint();
                        });

                    } catch (Exception ex) {
                        ex.printStackTrace();
                        showError("Ошибка поиска: " + ex.getMessage());
                    }
                }
            }.execute();
        });


        searchMenu.add(findItem);
        return searchMenu;
    }

    /**
     * Создает меню файла.
     *
     * @return созданное меню файла
     */
    private JMenu buildFileMenu() {
        logger.trace("Создание меню 'Файл'");
        JMenu fileMenu = new JMenu("Файл");

        JMenuItem openItem = getJMenuItem();

        JMenuItem saveItem = new JMenuItem("Сохранить");
        saveItem.addActionListener(e -> {
            try {
                byte[] all = fileModel.readAll();
                fileModel.close();

                try (OutputStream out = Files.newOutputStream(currentFile.toPath())) {
                    out.write(all);
                }

                fileModel = new HexFileModel(currentFile);
                tableModel = new HexTableModel(fileModel, BYTES_PER_ROW);
                table.setModel(tableModel);
                tableModel.fireTableStructureChanged();

                statusLabel.setText("Файл сохранен: " + currentFile.getName());
            } catch (IOException ex) {
                showError("Ошибка сохранения: " + ex.getMessage());
            }
        });

        JMenuItem saveAsItem = getMenuItem();

        fileMenu.add(openItem);
        fileMenu.add(saveItem);
        fileMenu.add(saveAsItem);
        return fileMenu;
    }

    private JMenuItem getMenuItem() {
        JMenuItem saveAsItem = new JMenuItem("Сохранить как...");
        saveAsItem.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                File newFile = chooser.getSelectedFile();
                try (OutputStream out = Files.newOutputStream(newFile.toPath())) {
                    byte[] all = fileModel.readAll();
                    out.write(all);
                    currentFile = newFile;
                    setTitle("HEX редактор - " + currentFile.getName());
                    statusLabel.setText("Сохранено как: " + newFile.getName());
                } catch (IOException ex) {
                    showError("Ошибка при сохранении: " + ex.getMessage());
                }
            }
        });
        return saveAsItem;
    }

    private JMenuItem getJMenuItem() {
        JMenuItem openItem = new JMenuItem("Открыть...");
        openItem.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                try {
                    currentFile = chooser.getSelectedFile();
                    initializeEditor();
                } catch (IOException ex) {
                    showError("Ошибка открытия: " + ex.getMessage());
                }
            }
        });
        return openItem;
    }

    /**
     * Создает меню правки.
     *
     * @return созданное меню правки
     */
    private JMenu buildEditMenu() {
        logger.trace("Создание меню 'Правка'");
        JMenu editMenu = new JMenu("Правка");

        JMenuItem deleteItem = getItem();

        JMenuItem deleteShiftItem = getDeleteShiftItem();

        JMenuItem copyItem = new JMenuItem("Копировать");
        copyItem.addActionListener(e -> {
            try {
                clipboard = getSelectedBytes();
                statusLabel.setText("Скопировано: " + (clipboardRows * clipboardCols) + " байт");
            } catch (IOException ex) {
                showError("Ошибка при копировании: " + ex.getMessage());
            }
        });

        JMenuItem pasteReplace = getPasteReplace();

        JMenuItem pasteShift = getPasteShift();

        editMenu.add(copyItem);
        editMenu.add(pasteReplace);
        editMenu.add(pasteShift);
        editMenu.add(deleteItem);
        editMenu.add(deleteShiftItem);
        return editMenu;
    }

    private JMenuItem getPasteShift() {
        JMenuItem pasteShift = new JMenuItem("Вставить со сдвигом");
        pasteShift.addActionListener(e -> {
            if (clipboard == null) {
                showError("Буфер обмена пуст");
                return;
            }
            int row = table.getSelectedRow();
            int col = table.getSelectedColumn();
            try {
                byte[] flat = new byte[clipboardRows * clipboardCols];
                int index = 0;
                for (byte[] line : clipboard) {
                    for (byte b : line) {
                        flat[index++] = b;
                    }
                }
                long offset = (long) row * table.getColumnCount() + col;
                fileModel.insertBytes(offset, flat, false);
                table.repaint();
            } catch (IOException ex) {
                showError("Ошибка вставки со сдвигом: " + ex.getMessage());
            }
        });
        return pasteShift;
    }

    private JMenuItem getPasteReplace() {
        JMenuItem pasteReplace = new JMenuItem("Вставить с заменой");
        pasteReplace.addActionListener(e -> {
            if (clipboard == null) {
                showError("Буфер обмена пуст");
                return;
            }
            int row = table.getSelectedRow();
            int col = table.getSelectedColumn();
            try {
                for (int i = 0; i < clipboardRows; i++) {
                    for (int j = 0; j < clipboardCols; j++) {
                        long offset = (long)(row + i) * table.getColumnCount() + (col + j);
                        if (offset < fileModel.getLength()) {
                            fileModel.writeByte(offset, clipboard[i][j]);
                        }
                    }
                }

                table.repaint();
            } catch (IOException ex) {
                showError("Ошибка вставки: " + ex.getMessage());
            }
        });
        return pasteReplace;
    }

    private JMenuItem getDeleteShiftItem() {
        JMenuItem deleteShiftItem = new JMenuItem("Удалить (со сдвигом)");
        deleteShiftItem.addActionListener(e -> {
            int[] rows = table.getSelectedRows();
            int[] cols = table.getSelectedColumns();
            if (rows.length == 0 || cols.length == 0) return;

            int rowMin = rows[0];
            int colMin = cols[0];
            int rowMax = rows[rows.length - 1];
            int colMax = cols[cols.length - 1];

            long from = (long) rowMin * table.getColumnCount() + colMin;
            long to = (long) rowMax * table.getColumnCount() + colMax;
            int length = (int) (to - from + 1);

            try {
                fileModel.deleteBytes(from, length, true);
                table.repaint();
                statusLabel.setText("Удалено со сдвигом: " + length + " байт");
            } catch (IOException ex) {
                showError("Ошибка удаления: " + ex.getMessage());
            }
        });
        return deleteShiftItem;
    }

    private JMenuItem getItem() {
        JMenuItem deleteItem = new JMenuItem("Удалить (обнулить)");
        deleteItem.addActionListener(e -> {
            int[] rows = table.getSelectedRows();
            int[] cols = table.getSelectedColumns();
            if (rows.length == 0 || cols.length == 0) return;

            try {
                for (int r : rows) {
                    for (int c : cols) {
                        long offset = (long) r * table.getColumnCount() + c;
                        if (offset < fileModel.getLength()) {
                            fileModel.writeByte(offset, (byte) 0x00);
                        }
                    }
                }
                table.repaint();
                statusLabel.setText("Удалено (обнулено)");
            } catch (IOException ex) {
                showError("Ошибка удаления: " + ex.getMessage());
            }
        });
        return deleteItem;
    }

    /**
     * Получает выбранные байты из таблицы.
     *
     * @return двумерный массив выбранных байт
     * @throws IOException если произошла ошибка чтения
     */
    private byte[][] getSelectedBytes() throws IOException {
        logger.debug("Получение выбранных байт (строк: {}, колонок: {})", clipboardRows, clipboardCols);
        int[] rows = table.getSelectedRows();
        int[] cols = table.getSelectedColumns();
        clipboardRows = rows.length;
        clipboardCols = cols.length;
        byte[][] data = new byte[clipboardRows][clipboardCols];
        for (int i = 0; i < clipboardRows; i++) {
            for (int j = 0; j < clipboardCols; j++) {
                long offset = (long) rows[i] * table.getColumnCount() + cols[j];
                data[i][j] = fileModel.readByte(offset);
            }
        }
        return data;
    }

    /**
     * Показывает диалоговое окно с сообщением об ошибке.
     *
     * @param msg текст сообщения об ошибке
     */
    private void showError(String msg) {
        logger.error(msg);
        JOptionPane.showMessageDialog(this, msg, "Ошибка", JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Обновляет статусную строку с информацией о текущей позиции.
     */
    private void updateStatus() {
        logger.trace("Обновление статусной строки");
        int row = table.getSelectedRow();
        int col = table.getSelectedColumn();

        long offset = (long) row * table.getColumnCount() + col;
        try {
            StringBuilder sb = new StringBuilder();
            byte[][] selectedBytes = getSelectedBytes();
            int available = (int) (fileModel.getLength() - offset);
            if (selectedBytes.length > 0) {
                available = selectedBytes.length * selectedBytes[0].length;
            }
            if (available >= 2) {
                sb.append(String.format("int16: %d  uint16: %d  ",
                        DataInterpreter.toShort(selectedBytes, true),
                        DataInterpreter.toShort(selectedBytes, false) & 0xFFFF));
            }
            if (available >= 4) {
                sb.append(String.format("int32: %d  uint32: %d  float: %.6f  ",
                        DataInterpreter.toInt(selectedBytes, true),
                        DataInterpreter.toUnsignedInt(selectedBytes),
                        DataInterpreter.toFloat(selectedBytes)));
            }
            if (available >= 8) {
                sb.append(String.format("int64: %d  double: %.16E",
                        DataInterpreter.toLong(selectedBytes),
                        DataInterpreter.toDouble(selectedBytes)));
            }

            valueLabel.setText(sb.toString());
            logger.debug("Отображение данных: {}", sb);
        } catch (IOException e) {
            statusLabel.setText("Read error at offset: " + offset);
            logger.warn("Ошибка обновления статуса", e);
        }
    }

}
