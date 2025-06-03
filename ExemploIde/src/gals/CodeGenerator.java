package gals;

import java.util.HashMap;
import java.util.Map;
import java.util.List;

public class CodeGenerator {
    private final SymbolTable symbolTable;
    private final StringBuilder dataSection;
    private final StringBuilder textSection;
    private final Map<String, String> variableMap;

    public CodeGenerator(SymbolTable symbolTable) {
        this.symbolTable = symbolTable;
        this.dataSection = new StringBuilder();
        this.textSection = new StringBuilder();
        this.variableMap = new HashMap<>();

        initializeSections();
    }

    private void initializeSections() {
        dataSection.append(".data\n");
        textSection.append(".text\n");
    }

    public void generateCode() {
        generateDataSection();
        generateHalt();
    }

    private void generateDataSection() {
        for (Map.Entry<String, List<SymbolTable.SymbolEntry>> scopeEntry : symbolTable.getTable().entrySet()) {
            List<SymbolTable.SymbolEntry> entries = scopeEntry.getValue();

            for (SymbolTable.SymbolEntry entry : entries) {
                if ("global".equals(entry.getScope())) {
                    String symbolName = entry.getId();

                    if (entry.getModality() == SymbolTable.VARIABLE) {
                        generateVariableDeclaration(symbolName, entry);
                    } else if (entry.getModality() == SymbolTable.ARRAY) {
                        generateArrayDeclaration(symbolName, entry);
                    }
                }
            }
        }

        dataSection.append("\n");
    }

    private void generateVariableDeclaration(String varName, SymbolTable.SymbolEntry entry) {
        String defaultValue = getDefaultValue(entry.getType());
        dataSection.append("   ").append(varName).append(" : ").append(defaultValue).append("\n");
        variableMap.put(varName, varName);
    }

    private void generateArrayDeclaration(String arrayName, SymbolTable.SymbolEntry entry) {
        String defaultValue = getDefaultValue(entry.getType());
        int size = entry.getSize() > 0 ? entry.getSize() : 1;

        dataSection.append("   ").append(arrayName).append(" : ");

        for (int i = 0; i < size; i++) {
            dataSection.append(defaultValue);
            if (i < size - 1) {
                dataSection.append(",");
            }
        }
        dataSection.append("\n");
        variableMap.put(arrayName, arrayName);
    }

    private String getDefaultValue(int type) {
        return "0";
    }

    private void generateHalt() {
        textSection.append("   HLT     0\n");
    }

    public String getGeneratedCode() {
        StringBuilder fullCode = new StringBuilder();
        fullCode.append(dataSection.toString());
        fullCode.append(textSection.toString());
        return fullCode.toString();
    }

    public void appendGeneratedCode(String code) {
        String currentText = textSection.toString();
        if (currentText.contains("   HLT     0")) {
            textSection.setLength(0);
            textSection.append(currentText.replace("   HLT     0\n", ""));
            textSection.append(code);
            textSection.append("   HLT     0\n");
        } else {
            textSection.append(code);
        }
    }

    public void printGeneratedCode() {
        System.out.println("=== CODIGO BIPIDE GERADO ===");
        System.out.println(getGeneratedCode());
        System.out.println("=============================");
    }
}