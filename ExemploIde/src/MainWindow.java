import gals.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.plaf.basic.BasicSplitPaneUI;
import javax.swing.plaf.basic.BasicSplitPaneDivider;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.List;
import java.util.Map;
import java.lang.reflect.Field;

/**
 *
 * @author eas
 */
public class MainWindow extends javax.swing.JFrame {
    private JTextArea sourceInput;
    private JTextArea console;
    private JButton buttonCompile;
    private JButton buttonDownloadDocs;
    private JButton buttonShowSymbols;
    private JButton buttonShowAsm;
    private JLabel flagLabel;
    private JLabel catLabel;
    private JLabel sadCatLabel;
    private JLabel sleepCatLabel;
    private JScrollPane scrollPaneSource;
    private JScrollPane scrollPaneConsole;
    private CardLayout catCardLayout;
    private JPanel catContainer;
    private JTable symbolTable;
    private JScrollPane symbolTableScrollPane;
    private DefaultTableModel symbolTableModel;
    private JSplitPane rightSplitPane;
    private boolean symbolTableVisible = false;
    private Semantico lastSemantico;

    private final int CAT_WIDTH = 64;
    private final int CAT_HEIGHT = 64;

    Color rosa1 = new Color(255, 230, 244);
    Color rosa2 = new Color(255, 182, 213);
    Color rosa3 = new Color(255, 105, 180);
    Color rosa4 = new Color(219, 112, 147);
    Color rosa5 = new Color(255, 140, 200);
    Color background = new Color(255, 245, 250);
    Color error = new Color(255, 51, 102);

    Font titleFont = new Font("Monospaced", Font.BOLD, 14);
    Font codeFont = new Font("Monospaced", Font.PLAIN, 14);
    Font globalFont = new Font("Consolas", Font.PLAIN, 14);
    Font boldGlobalFont = new Font("Consolas", Font.BOLD, 14);

    public MainWindow() {
        initComponents();
    }

    @SuppressWarnings("unchecked")
    private void initComponents() {
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setTitle("Romenol");
        setFont(titleFont);

        sourceInput = new JTextArea();
        console = new JTextArea();
        buttonCompile = new JButton("Compile");
        buttonDownloadDocs = new JButton("Documentation");
        buttonShowSymbols = new JButton("Symbol Table");
        buttonShowAsm = new JButton("Show ASM");

        catLabel = new JLabel();
        sadCatLabel = new JLabel();
        sleepCatLabel = new JLabel();

        catLabel.setVisible(false);
        sadCatLabel.setVisible(false);
        sleepCatLabel.setVisible(false);

        try {
            ImageIcon catIcon = loadAndResizeImage("/resources/pixel_cat.gif", CAT_WIDTH, CAT_HEIGHT);
            catLabel.setIcon(catIcon);

            ImageIcon sadCatIcon = loadAndResizeImage("/resources/sad_cat.gif", CAT_WIDTH, CAT_HEIGHT);
            sadCatLabel.setIcon(sadCatIcon);

            ImageIcon sleepCatIcon = loadAndResizeImage("/resources/sleep_cat.gif", CAT_WIDTH, CAT_HEIGHT);
            sleepCatLabel.setIcon(sleepCatIcon);

            catLabel.setHorizontalAlignment(SwingConstants.CENTER);
            sadCatLabel.setHorizontalAlignment(SwingConstants.CENTER);
            sleepCatLabel.setHorizontalAlignment(SwingConstants.CENTER);

        } catch (Exception e) {
            System.err.println("Erro ao carregar imagem do gatinho: " + e.getMessage());
        }

        buttonCompile.setFont(globalFont);
        buttonCompile.setBackground(rosa3);
        buttonCompile.setForeground(Color.WHITE);
        buttonCompile.setFocusPainted(false);
        buttonCompile.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));

        buttonDownloadDocs.setFont(globalFont);
        buttonDownloadDocs.setBackground(rosa5);
        buttonDownloadDocs.setForeground(Color.WHITE);
        buttonDownloadDocs.setFocusPainted(false);
        buttonDownloadDocs.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));

        buttonShowSymbols.setFont(globalFont);
        buttonShowSymbols.setBackground(rosa4);
        buttonShowSymbols.setForeground(Color.WHITE);
        buttonShowSymbols.setFocusPainted(false);
        buttonShowSymbols.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));

        buttonShowAsm.setFont(globalFont);
        buttonShowAsm.setBackground(rosa2);
        buttonShowAsm.setForeground(Color.WHITE);
        buttonShowAsm.setFocusPainted(false);
        buttonShowAsm.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));

        ImageIcon flagIcon = new ImageIcon(getClass().getResource("/resources/romenia_flag.jpeg"));
        Image originalImage = flagIcon.getImage();
        Image scaledImage = originalImage.getScaledInstance(30, 18, Image.SCALE_SMOOTH);
        ImageIcon scaledIcon = new ImageIcon(scaledImage);
        flagLabel = new JLabel(scaledIcon);
        flagLabel.setBorder(BorderFactory.createLineBorder(rosa2, 1));

        JLabel titleLabel = new JLabel("<html><div style='font-family: \"Monospaced\", cursive; font-size: 20px; font-weight: lighter; color: #FF69B4;'>Romenol IDE</div></html>");

        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        titlePanel.setBackground(rosa1);
        titlePanel.add(flagLabel);
        titlePanel.add(titleLabel);

        sourceInput.setEditable(true);
        sourceInput.setColumns(20);
        sourceInput.setFont(codeFont);
        sourceInput.setRows(10);
        sourceInput.setBackground(background);
        sourceInput.setForeground(rosa4);
        sourceInput.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 1, 1, 1, rosa2),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        scrollPaneSource = new JScrollPane(sourceInput);
        scrollPaneSource.setBorder(BorderFactory.createEmptyBorder());

        console.setEditable(false);
        console.setColumns(20);
        console.setFont(globalFont);
        console.setRows(6);
        console.setBackground(background);
        console.setForeground(rosa4);
        console.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 1, 1, 1, rosa2),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        scrollPaneConsole = new JScrollPane(console);
        scrollPaneConsole.setBorder(BorderFactory.createEmptyBorder());
        scrollPaneConsole.setPreferredSize(new Dimension(0, 120));
        scrollPaneConsole.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));

        initializeSymbolTable();

        buttonCompile.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                buttonCompileActionPerformed(evt);
            }
        });

        buttonDownloadDocs.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                buttonDownloadDocsActionPerformed(evt);
            }
        });

        buttonShowSymbols.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                toggleSymbolTable();
            }
        });

        buttonShowAsm.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                showAsmDialog();
            }
        });

        JPanel consoleAndCatPanel = new JPanel(new BorderLayout());
        consoleAndCatPanel.setBackground(rosa1);

        consoleAndCatPanel.add(scrollPaneConsole, BorderLayout.CENTER);

        catCardLayout = new CardLayout();
        catContainer = new JPanel(catCardLayout);
        catContainer.setBackground(rosa1);
        catContainer.add(sleepCatLabel, "sleep");
        catContainer.add(catLabel, "happy");
        catContainer.add(sadCatLabel, "sad");

        catCardLayout.show(catContainer, "sleep");

        JPanel catPanel = new JPanel(new BorderLayout());
        catPanel.setBackground(rosa1);
        catPanel.setPreferredSize(new Dimension(CAT_WIDTH + 10, CAT_HEIGHT + 10));
        catPanel.add(catContainer, BorderLayout.CENTER);

        consoleAndCatPanel.add(catPanel, BorderLayout.EAST);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(rosa1);
        buttonPanel.add(buttonDownloadDocs);
        buttonPanel.add(buttonShowAsm);
        buttonPanel.add(buttonShowSymbols);
        buttonPanel.add(buttonCompile);

        JPanel bottomSectionPanel = new JPanel(new BorderLayout());
        bottomSectionPanel.setBackground(rosa1);
        bottomSectionPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        bottomSectionPanel.add(consoleAndCatPanel, BorderLayout.CENTER);
        bottomSectionPanel.add(buttonPanel, BorderLayout.SOUTH);

        rightSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, scrollPaneSource, symbolTableScrollPane);
        rightSplitPane.setResizeWeight(0.7);
        rightSplitPane.setOneTouchExpandable(true);
        rightSplitPane.setBackground(rosa1);

        setDividerColor(rightSplitPane, rosa1);

        symbolTableScrollPane.setVisible(false);
        rightSplitPane.setBottomComponent(null);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(rosa1);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        mainPanel.add(titlePanel, BorderLayout.NORTH);
        mainPanel.add(rightSplitPane, BorderLayout.CENTER);
        mainPanel.add(bottomSectionPanel, BorderLayout.SOUTH);

        getContentPane().add(mainPanel);
        pack();
    }

    private void initializeSymbolTable() {
        String[] columnNames = {"ID", "TYPE", "MODALITY", "SCOPE", "POSITION", "SIZE"};
        symbolTableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        symbolTable = new JTable(symbolTableModel);
        symbolTable.setFont(globalFont);
        symbolTable.setRowHeight(25);
        symbolTable.setForeground(rosa4);
        symbolTable.setSelectionBackground(rosa2);
        symbolTable.setShowGrid(true);
        symbolTable.setGridColor(rosa2);

        JTableHeader header = symbolTable.getTableHeader();
        DefaultTableCellRenderer headerRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(
                        table, value, isSelected, hasFocus, row, column);
                label.setFont(boldGlobalFont);
                label.setHorizontalAlignment(JLabel.CENTER);
                label.setBackground(rosa2);
                label.setForeground(Color.WHITE);
                return label;
            }
        };

        for (int i = 0; i < symbolTable.getColumnCount(); i++) {
            symbolTable.getColumnModel().getColumn(i).setHeaderRenderer(headerRenderer);
        }

        header.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, rosa2));

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus,
                                                           int row, int column) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                if (!isSelected) {
                    label.setBackground(row % 2 == 0 ? background : rosa1);
                }
                label.setHorizontalAlignment(JLabel.CENTER);

                return label;
            }
        };

        for (int i = 0; i < columnNames.length; i++) {
            symbolTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        symbolTableScrollPane = new JScrollPane(symbolTable);
        symbolTableScrollPane.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 1, 1, 1, rosa2),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        symbolTableScrollPane.getViewport().setBackground(background);
    }

    private void toggleSymbolTable() {
        symbolTableVisible = !symbolTableVisible;
        
        if (symbolTableVisible) {
            if (lastSemantico != null) {
                updateSymbolTableDisplay();
            }
            symbolTableScrollPane.setVisible(true);
            rightSplitPane.setBottomComponent(symbolTableScrollPane);
            rightSplitPane.setDividerLocation(0.7);
            buttonShowSymbols.setText("Hide Table");
        } else {
            symbolTableScrollPane.setVisible(false);
            rightSplitPane.setBottomComponent(null);
            buttonShowSymbols.setText("Symbol Table");
        }
        
        revalidate();
        repaint();
    }

    private void setDividerColor(JSplitPane splitPane, final Color color) {
        splitPane.setUI(new BasicSplitPaneUI() {
            @Override
            public BasicSplitPaneDivider createDefaultDivider() {
                BasicSplitPaneDivider divider = new BasicSplitPaneDivider(this) {
                    @Override
                    public void paint(Graphics g) {
                        g.setColor(color);
                        g.fillRect(0, 0, getWidth(), getHeight());
                    }
                };
                divider.setBackground(color);
                return divider;
            }
        });
    }

    private void updateSymbolTableDisplay() {
        if (lastSemantico == null) return;
        
        symbolTableModel.setRowCount(0);
        
        try {
            Field symbolTableField = Semantico.class.getDeclaredField("symbolTable");
            symbolTableField.setAccessible(true);
            SymbolTable symTable = (SymbolTable) symbolTableField.get(lastSemantico);
            
            if (symTable != null) {
                Field tableField = SymbolTable.class.getDeclaredField("table");
                tableField.setAccessible(true);
                @SuppressWarnings("unchecked")
                Map<String, List<Object>> table = (Map<String, List<Object>>) tableField.get(symTable);
                
                Class<?>[] innerClasses = SymbolTable.class.getDeclaredClasses();
                Class<?> symbolEntryClass = null;
                for (Class<?> innerClass : innerClasses) {
                    if (innerClass.getSimpleName().equals("SymbolEntry")) {
                        symbolEntryClass = innerClass;
                        break;
                    }
                }
                
                if (symbolEntryClass != null && table != null) {
                    Field idField = symbolEntryClass.getDeclaredField("id");
                    Field typeField = symbolEntryClass.getDeclaredField("type");
                    Field modalityField = symbolEntryClass.getDeclaredField("modality");
                    Field scopeField = symbolEntryClass.getDeclaredField("scope");
                    Field positionField = symbolEntryClass.getDeclaredField("position");
                    Field sizeField = symbolEntryClass.getDeclaredField("size");
                    
                    idField.setAccessible(true);
                    typeField.setAccessible(true);
                    modalityField.setAccessible(true);
                    scopeField.setAccessible(true);
                    positionField.setAccessible(true);
                    sizeField.setAccessible(true);
                    
                    for (List<Object> entries : table.values()) {
                        for (Object entry : entries) {
                            String id = (String) idField.get(entry);
                            int type = (Integer) typeField.get(entry);
                            int modality = (Integer) modalityField.get(entry);
                            String scope = (String) scopeField.get(entry);
                            int position = (Integer) positionField.get(entry);
                            int size = (Integer) sizeField.get(entry);
                            
                            String typeStr = getTypeString(type);
                            String modalityStr = getModalityString(modality);
                            String sizeStr = modalityStr.equals("ARRAY") ? String.valueOf(size) : "N/A";
                            
                            symbolTableModel.addRow(new Object[]{id, typeStr, modalityStr, scope, position, sizeStr});
                        }
                    }
                }
            }
        } catch (Exception e) {
            console.append("Error accessing symbol table: " + e.getMessage() + "\n");
            e.printStackTrace();
        }
    }

    private void showAsmDialog() {
        if (lastSemantico == null) {
            JOptionPane.showMessageDialog(this, "Compile algo primeiro!", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            Field symbolTableField = Semantico.class.getDeclaredField("symbolTable");
            symbolTableField.setAccessible(true);
            SymbolTable symbolTable = (SymbolTable) symbolTableField.get(lastSemantico);

            if (symbolTable == null) {
                JOptionPane.showMessageDialog(this, "Tabela de símbolos vazia.", "Erro", JOptionPane.ERROR_MESSAGE);
                return;
            }

            CodeGenerator generator = new CodeGenerator(symbolTable);
            generator.generateCode();
            String semanticCode = lastSemantico.getGeneratedCode();
            generator.appendGeneratedCode(semanticCode);

            String asmCode = generator.getGeneratedCode();

            JTextArea textArea = new JTextArea(asmCode);
            textArea.setFont(codeFont);
            textArea.setBackground(background);
            textArea.setForeground(rosa4);
            textArea.setCaretColor(rosa3);
            textArea.setEditable(false);
            textArea.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, rosa2));

            JScrollPane scrollPane = new JScrollPane(textArea);
            scrollPane.setPreferredSize(new Dimension(600, 400));
            scrollPane.getViewport().setBackground(background);
            scrollPane.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(2, 2, 2, 2, rosa2),
                    BorderFactory.createEmptyBorder(5, 5, 5, 5)
            ));

            JButton copyButton = new JButton("Copiar Código");
            copyButton.setFont(globalFont);
            copyButton.setBackground(rosa3);
            copyButton.setForeground(Color.WHITE);
            copyButton.setFocusPainted(false);
            copyButton.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
            copyButton.addActionListener(e -> {
                Toolkit.getDefaultToolkit()
                        .getSystemClipboard()
                        .setContents(new StringSelection(asmCode), null);
                JOptionPane.showMessageDialog(this, "Código copiado para a área de transferência!", "Copiado", JOptionPane.INFORMATION_MESSAGE);
            });

            JPanel panel = new JPanel(new BorderLayout());
            panel.setBackground(rosa1);
            panel.add(scrollPane, BorderLayout.CENTER);

            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            buttonPanel.setBackground(rosa1);
            buttonPanel.add(copyButton);
            panel.add(buttonPanel, BorderLayout.SOUTH);

            JDialog dialog = new JDialog(this, "Código ASM", true);
            dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
            dialog.setContentPane(panel);
            dialog.pack();
            dialog.setLocationRelativeTo(this);
            dialog.setVisible(true);

        } catch (Exception e) {
            console.append("Erro ao gerar ASM: " + e.getMessage() + "\n");
            e.printStackTrace();
        }
    }

    private String getTypeString(int type) {
        try {
            Class<?> semanticTableClass = Class.forName("compile.SemanticTable");
            if (type == semanticTableClass.getField("INT").getInt(null)) return "INT";
            if (type == semanticTableClass.getField("FLO").getInt(null)) return "FLOAT";
            if (type == semanticTableClass.getField("CHA").getInt(null)) return "CHAR";
            if (type == semanticTableClass.getField("STR").getInt(null)) return "STRING";
            if (type == semanticTableClass.getField("BOO").getInt(null)) return "BOOL";
        } catch (Exception e) {
            switch (type) {
                case 1: return "INT";
                case 2: return "FLOAT";
                case 3: return "CHAR";
                case 4: return "STRING";
                case 5: return "BOOL";
            }
        }
        return "UNKNOWN";
    }
    
    private String getModalityString(int modality) {
        switch (modality) {
            case SymbolTable.VARIABLE: return "VARIABLE";
            case SymbolTable.ARRAY: return "ARRAY";
            case SymbolTable.PARAMETER: return "PARAMETER";
            case SymbolTable.FUNCTION: return "FUNCTION";
            default: return "UNKNOWN";
        }
    }

    private ImageIcon loadAndResizeImage(String path, int width, int height) {
        try {
            ImageIcon originalIcon = new ImageIcon(getClass().getResource(path));

            JLabel tempLabel = new JLabel(originalIcon);
            tempLabel.setPreferredSize(new Dimension(width, height));

            Image img = originalIcon.getImage();
            Image resizedImg = img.getScaledInstance(width, height, Image.SCALE_FAST);
            return new ImageIcon(resizedImg);

        } catch (Exception e) {
            System.err.println("Erro ao redimensionar imagem: " + e.getMessage());
            return new ImageIcon();
        }
    }

    private void buttonDownloadDocsActionPerformed(ActionEvent evt) {
        try {
            InputStream in = getClass().getResourceAsStream("/resources/documentacao_romenol.pdf");
            if (in == null) {
                console.setForeground(error);
                console.append("Documentação não encontrada.\n");
                return;
            }

            File tempFile = File.createTempFile("documentacao_romenol", ".pdf");
            tempFile.deleteOnExit();

            Files.copy(in, tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            in.close();

            Desktop.getDesktop().open(tempFile);

            console.setForeground(rosa4);
            console.append("Documentação aberta com sucesso.\n");
        } catch (Exception e) {
            console.setForeground(error);
            console.append("Falha ao abrir a documentação: " + e.getMessage() + "\n");
        }
    }

    private void buttonCompileActionPerformed(java.awt.event.ActionEvent evt) {
        Lexico lex = new Lexico();
        Sintatico sint = new Sintatico();
        Semantico sem = new Semantico();
        
        lastSemantico = sem;

        lex.setInput(sourceInput.getText());

        PrintStream originalOut = System.out;
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PrintStream customPrintStream = new PrintStream(outputStream);
        System.setOut(customPrintStream);

        try {
            sint.parse(lex, sem);

            System.setOut(originalOut);

            String capturedOutput = outputStream.toString();

            console.setForeground(rosa4);
            console.setText("Sucesso!\n\n");

            if (capturedOutput.contains("AVISO:")) {
                console.append("=== WARNINGS ===\n");
                String[] lines = capturedOutput.split("\n");
                for (String line : lines) {
                    if (line.contains("AVISO:")) {
                        console.append(line + "\n");
                    }
                }
            }

            catCardLayout.show(catContainer, "happy");
            
            if (symbolTableVisible) {
                updateSymbolTableDisplay();
            }

        } catch (Exception ex) {
            System.setOut(originalOut);

            console.setForeground(error);
            if (ex instanceof LexicalError)
                console.setText("Problema léxico: " + ex.getLocalizedMessage());
            else if (ex instanceof SyntacticError)
                console.setText("Problema sintático: " + ex.getLocalizedMessage());
            else
                console.setText("Problema semântico: " + ex.getLocalizedMessage());

            catCardLayout.show(catContainer, "sad");

            Logger.getLogger(MainWindow.class.getName()).log(Level.SEVERE, null, ex);
        }

        revalidate();
        repaint();
    }

    public static void main(String args[]) {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(MainWindow.class.getName()).log(Level.SEVERE, null, ex);
        }

        java.awt.EventQueue.invokeLater(() -> new MainWindow().setVisible(true));
    }
}