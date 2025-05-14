package gals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SymbolTable {
    public static final int VARIABLE = 0;
    public static final int ARRAY = 1;
    public static final int PARAMETER = 2;
    public static final int FUNCTION = 3;

    public static class SymbolEntry {
        private String id;
        private int type;        // INT, FLO, CHA, STR, BOO, etc.
        private int modality;    // VARIABLE, ARRAY, PARAMETER, FUNCTION
        private String scope;    // global or function name
        private int position;    // position in source code
        private int size;        // for arrays

        public SymbolEntry(String id, int type, int modality, String scope, int position) {
            this.id = id;
            this.type = type;
            this.modality = modality;
            this.scope = scope;
            this.position = position;
            this.size = 0;  // Default size for non-arrays
        }

        public String getId() {
            return id;
        }

        public int getType() {
            return type;
        }

        public int getModality() {
            return modality;
        }

        public String getScope() {
            return scope;
        }

        public int getPosition() {
            return position;
        }

        public int getSize() {
            return size;
        }

        @Override
        public String toString() {
            String typeStr = "";
            switch (type) {
                case compile.SemanticTable.INT: typeStr = "INT"; break;
                case compile.SemanticTable.FLO: typeStr = "FLOAT"; break;
                case compile.SemanticTable.CHA: typeStr = "CHAR"; break;
                case compile.SemanticTable.STR: typeStr = "STRING"; break;
                case compile.SemanticTable.BOO: typeStr = "BOOL"; break;
                default: typeStr = "UNKNOWN"; break;
            }

            String modalityStr = "";
            switch (modality) {
                case VARIABLE: modalityStr = "VARIABLE"; break;
                case ARRAY: modalityStr = "ARRAY"; break;
                case PARAMETER: modalityStr = "PARAMETER"; break;
                case FUNCTION: modalityStr = "FUNCTION"; break;
                default: modalityStr = "UNKNOWN"; break;
            }

            return "ID: " + id +
                    ", Type: " + typeStr +
                    ", Modality: " + modalityStr +
                    ", Scope: " + scope +
                    (modality == ARRAY ? ", Size: " + size : "");
        }
    }

    private String currentScope = "global";
    private int currentType = -1;
    private boolean isArray = false;
    private int arraySize = 0;
    private final Map<String, List<SymbolEntry>> table = new HashMap<>();

    public void setCurrentScope(String scope) {
        this.currentScope = scope;
    }

    public void setArraySize(int size) {
        this.arraySize = size;
    }

    public int getArraySize() {
        return this.arraySize;
    }

    public String getCurrentScope() {
        return currentScope;
    }

    public void setCurrentType(int type) {
        this.currentType = type;
    }

    public int getCurrentType() {
        return currentType;
    }

    public void setArray(boolean isArray) {
        this.isArray = isArray;
    }

    public boolean isArray() {
        return isArray;
    }

    public void addSymbol(String id, int modality, int position) throws SemanticError {
        if (currentType == -1) {
            throw new SemanticError("Tipo não definido para identificador: " + id, position);
        }

        String key = id + "@" + currentScope;

        if (!table.containsKey(key)) {
            table.put(key, new ArrayList<>());
        }

        List<SymbolEntry> entries = table.get(key);
        for (SymbolEntry entry : entries) {
            if (entry.scope.equals(currentScope)) {
                throw new SemanticError("Identificador '" + id + "' já definido no escopo '" + currentScope + "'", position);
            }
        }

        SymbolEntry entry = new SymbolEntry(id, currentType, modality, currentScope, position);
        entries.add(entry);

        System.out.println("Adicionado à tabela de símbolos: " + entry);
    }

    public void addArray(String id, int position) throws SemanticError {
        if (currentType == -1) {
            throw new SemanticError("Tipo não definido para array: " + id, position);
        }

        String key = id + "@" + currentScope;

        if (!table.containsKey(key)) {
            table.put(key, new ArrayList<>());
        }

        List<SymbolEntry> entries = table.get(key);
        for (SymbolEntry entry : entries) {
            if (entry.scope.equals(currentScope)) {
                throw new SemanticError("Array '" + id + "' já definido no escopo '" + currentScope + "'", position);
            }
        }

        SymbolEntry entry = new SymbolEntry(id, currentType, ARRAY, currentScope, position);
        entry.size = arraySize;
        entries.add(entry);

        System.out.println("Array adicionado à tabela de símbolos: " + entry);
    }

    public SymbolEntry lookup(String id, String scope) {
        String key = id + "@" + scope;
        if (table.containsKey(key)) {
            List<SymbolEntry> entries = table.get(key);
            for (SymbolEntry entry : entries) {
                if (entry.scope.equals(scope)) {
                    return entry;
                }
            }
        }

        if (!scope.equals("global")) {
            key = id + "@global";
            if (table.containsKey(key)) {
                List<SymbolEntry> entries = table.get(key);
                for (SymbolEntry entry : entries) {
                    if (entry.scope.equals("global")) {
                        return entry;
                    }
                }
            }
        }

        return null;
    }

    public void printTable() {
        System.out.println("===================== TABELA DE SÍMBOLOS =====================");
        for (List<SymbolEntry> entries : table.values()) {
            for (SymbolEntry entry : entries) {
                System.out.println(entry);
            }
        }
        System.out.println("=============================================================");
    }
}