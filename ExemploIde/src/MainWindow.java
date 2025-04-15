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
    private JScrollPane scrollPaneSource;
    private JScrollPane scrollPaneConsole;

    public MainWindow() {
        initComponents();
    }

    @SuppressWarnings("unchecked")
    private void initComponents() {
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setTitle("Romenol");
        setFont(new Font("Monospaced", Font.BOLD, 14));

        sourceInput = new JTextArea();
        console = new JTextArea();
        buttonCompile = new JButton("Compile");
        buttonDownloadDocs = new JButton("Documentation");

        buttonCompile.setFont(new Font("Consolas", Font.PLAIN, 14));
        buttonCompile.setBackground(new Color(54, 145, 80));
        buttonCompile.setForeground(Color.WHITE);
        buttonCompile.setFocusPainted(false);
        buttonCompile.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));

        buttonDownloadDocs.setFont(new Font("Consolas", Font.PLAIN, 14));
        buttonDownloadDocs.setBackground(new Color(130, 86, 141));
        buttonDownloadDocs.setForeground(Color.WHITE);
        buttonDownloadDocs.setFocusPainted(false);
        buttonDownloadDocs.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));

        ImageIcon flagIcon = new ImageIcon(getClass().getResource("/resources/romenia_flag.jpeg"));
        Image originalImage = flagIcon.getImage();
        Image scaledImage = originalImage.getScaledInstance(30, 18, Image.SCALE_SMOOTH);
        ImageIcon scaledIcon = new ImageIcon(scaledImage);
        flagLabel = new JLabel(scaledIcon);
        flagLabel.setBorder(BorderFactory.createLineBorder(new Color(147, 163, 177), 1));

        JLabel titleLabel = new JLabel("<html><div style='font-family: \"Monospaced\", cursive; font-size: 20px; font-weight: lighter; color: #dcdcdc;'>Romenol IDE</div></html>");

        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        titlePanel.setBackground(new Color(52, 55, 57));
        titlePanel.add(flagLabel);
        titlePanel.add(titleLabel);

        sourceInput.setEditable(true);
        sourceInput.setColumns(20);
        sourceInput.setFont(new Font("Monospaced", Font.PLAIN, 14));
        sourceInput.setRows(10);
        sourceInput.setBackground(new Color(38, 38, 38));
        sourceInput.setForeground(new Color(246, 135, 34));
        sourceInput.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 1, 1, 1, new Color(80, 80, 80)),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        scrollPaneSource = new JScrollPane(sourceInput);
        scrollPaneSource.setBorder(BorderFactory.createEmptyBorder());

        console.setEditable(false);
        console.setColumns(20);
        console.setFont(new Font("Consolas", Font.PLAIN, 14));
        console.setRows(6);
        console.setBackground(new Color(38,38,38));
        console.setForeground(new Color(147, 163, 177));
        console.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 1, 1, 1, new Color(80, 80, 80)),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        scrollPaneConsole = new JScrollPane(console);
        scrollPaneConsole.setBorder(BorderFactory.createEmptyBorder());

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

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(52, 55, 57));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        mainPanel.add(titlePanel, BorderLayout.NORTH);

        JPanel editorPanel = new JPanel(new BorderLayout());
        editorPanel.setBackground(new Color(52, 55, 57));
        editorPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        editorPanel.add(scrollPaneSource, BorderLayout.CENTER);
        mainPanel.add(editorPanel, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBackground(new Color(52, 55, 57));
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        bottomPanel.add(scrollPaneConsole, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(new Color(52, 55, 57));
        buttonPanel.add(buttonDownloadDocs);
        buttonPanel.add(buttonCompile);
        bottomPanel.add(buttonPanel, BorderLayout.SOUTH);

        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        getContentPane().add(mainPanel);
        pack();
    }

    private void buttonDownloadDocsActionPerformed(ActionEvent evt) {
        try {
            InputStream in = getClass().getResourceAsStream("/resources/documentacao_romenol.pdf");
            if (in == null) {
                console.setForeground(new Color(200, 81, 79));
                console.append("Documentação não encontrada.\n");
                return;
            }

            File tempFile = File.createTempFile("documentacao_romenol", ".pdf");
            tempFile.deleteOnExit();

            Files.copy(in, tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            in.close();

            Desktop.getDesktop().open(tempFile);

            console.setForeground(new Color(147, 163, 177));
            console.append("Documentação aberta com sucesso.\n");
        } catch (Exception e) {
            console.setForeground(new Color(200, 81, 79));
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
            console.setForeground(new Color(147, 163, 177));
            console.setText("Sucesso!");
        } catch (LexicalError ex) {
            console.setForeground(new Color(200, 81, 79));
            console.setText("Problema léxico: "+ex.getLocalizedMessage());
            Logger.getLogger(MainWindow.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SyntacticError ex) {
            console.setForeground(new Color(200, 81, 79));
            console.setText("Problema sintático: "+ex.getLocalizedMessage());
            Logger.getLogger(MainWindow.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SemanticError ex) {
            console.setForeground(new Color(200, 81, 79));
            console.setText("Problema semântico: "+ex.getLocalizedMessage());
            Logger.getLogger(MainWindow.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public static void main(String args[]) {

        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(MainWindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(MainWindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(MainWindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(MainWindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }

        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new MainWindow().setVisible(true);
            }
        });
    }
}
