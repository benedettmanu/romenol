import gals.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author eas
 */
public class MainWindow extends javax.swing.JFrame {
    private JTextArea sourceInput;
    private JTextArea console;
    private JButton buttonCompile;
    private JButton buttonDownloadDocs;
    private JLabel flagLabel;
    private JLabel catLabel;
    private JLabel sadCatLabel;
    private JLabel sleepCatLabel;
    private JScrollPane scrollPaneSource;
    private JScrollPane scrollPaneConsole;
    private CardLayout catCardLayout;
    private JPanel catContainer;

    private final int CAT_WIDTH = 64;
    private final int CAT_HEIGHT = 64;

    public MainWindow() {
        initComponents();
    }

    @SuppressWarnings("unchecked")
    private void initComponents() {
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setTitle("Romenol");
        setFont(new Font("Monospaced", Font.BOLD, 14));

        Color rosa1 = new Color(255, 230, 244);
        Color rosa2 = new Color(255, 182, 213);
        Color rosa3 = new Color(255, 105, 180);
        Color rosa4 = new Color(219, 112, 147);

        sourceInput = new JTextArea();
        console = new JTextArea();
        buttonCompile = new JButton("Compile");
        buttonDownloadDocs = new JButton("Documentation");

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

        buttonCompile.setFont(new Font("Consolas", Font.PLAIN, 14));
        buttonCompile.setBackground(rosa3);
        buttonCompile.setForeground(Color.WHITE);
        buttonCompile.setFocusPainted(false);
        buttonCompile.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));

        buttonDownloadDocs.setFont(new Font("Consolas", Font.PLAIN, 14));
        buttonDownloadDocs.setBackground(rosa4);
        buttonDownloadDocs.setForeground(Color.WHITE);
        buttonDownloadDocs.setFocusPainted(false);
        buttonDownloadDocs.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));

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
        sourceInput.setFont(new Font("Monospaced", Font.PLAIN, 14));
        sourceInput.setRows(10);
        sourceInput.setBackground(new Color(255, 245, 250));
        sourceInput.setForeground(rosa4);
        sourceInput.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 1, 1, 1, rosa2),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        scrollPaneSource = new JScrollPane(sourceInput);
        scrollPaneSource.setBorder(BorderFactory.createEmptyBorder());

        console.setEditable(false);
        console.setColumns(20);
        console.setFont(new Font("Consolas", Font.PLAIN, 14));
        console.setRows(6);
        console.setBackground(new Color(255, 245, 250));
        console.setForeground(new Color(219, 112, 147));
        console.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 1, 1, 1, rosa2),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        scrollPaneConsole = new JScrollPane(console);
        scrollPaneConsole.setBorder(BorderFactory.createEmptyBorder());
        scrollPaneConsole.setPreferredSize(new Dimension(0, 120));
        scrollPaneConsole.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));

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
        buttonPanel.add(buttonCompile);

        JPanel bottomSectionPanel = new JPanel(new BorderLayout());
        bottomSectionPanel.setBackground(rosa1);
        bottomSectionPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        bottomSectionPanel.add(consoleAndCatPanel, BorderLayout.CENTER);
        bottomSectionPanel.add(buttonPanel, BorderLayout.SOUTH);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(rosa1);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        mainPanel.add(titlePanel, BorderLayout.NORTH);
        mainPanel.add(scrollPaneSource, BorderLayout.CENTER);
        mainPanel.add(bottomSectionPanel, BorderLayout.SOUTH);

        getContentPane().add(mainPanel);
        pack();
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
                console.setForeground(new Color(255, 51, 102));
                console.append("Documentação não encontrada.\n");
                return;
            }

            File tempFile = File.createTempFile("documentacao_romenol", ".pdf");
            tempFile.deleteOnExit();

            Files.copy(in, tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            in.close();

            Desktop.getDesktop().open(tempFile);

            console.setForeground(new Color(219, 112, 147));
            console.append("Documentação aberta com sucesso.\n");
        } catch (Exception e) {
            console.setForeground(new Color(255, 51, 102));
            console.append("Falha ao abrir a documentação: " + e.getMessage() + "\n");
        }
    }

    private void buttonCompileActionPerformed(java.awt.event.ActionEvent evt) {
        Lexico lex = new Lexico();
        Sintatico sint = new Sintatico();
        Semantico sem = new Semantico();

        lex.setInput(sourceInput.getText());

        try {
            sint.parse(lex, sem);
            console.setForeground(new Color(219, 112, 147));
            console.setText("Sucesso!");

            catCardLayout.show(catContainer, "happy");

        } catch (Exception ex) {
            console.setForeground(new Color(255, 51, 102));
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