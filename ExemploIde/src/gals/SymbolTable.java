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

    private static class SymbolEntry {
        String id;
        int type;        // INT, FLO, CHA, STR, BOO, etc.
        int modality;    // VARIABLE, ARRAY, PARAMETER, FUNCTION
        String scope;    // global or function name
        int position;    // position in source code
        int size;        // for arrays

        public SymbolEntry(String id, int type, int modality, String scope, int position) {
            this.id = id;
            this.type = type;
            this.modality = modality;
            this.scope = scope;
            this.position = position;
            this.size = 0;  // Default size for non-arrays
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
            throw new SemanticError("Type not defined for identifier: " + id, position);
        }

        String key = id + "@" + currentScope;

        if (!table.containsKey(key)) {
            table.put(key, new ArrayList<>());
        }

        List<SymbolEntry> entries = table.get(key);
        for (SymbolEntry entry : entries) {
            if (entry.scope.equals(currentScope)) {
                throw new SemanticError("Identifier '" + id + "' already defined in scope '" + currentScope + "'", position);
            }
        }

        SymbolEntry entry = new SymbolEntry(id, currentType, modality, currentScope, position);
        entries.add(entry);

        System.out.println("Added to symbol table: " + entry);
    }

    public void addArray(String id, int position) throws SemanticError {
        if (currentType == -1) {
            throw new SemanticError("Type not defined for array: " + id, position);
        }

        String key = id + "@" + currentScope;

        if (!table.containsKey(key)) {
            table.put(key, new ArrayList<>());
        }

        List<SymbolEntry> entries = table.get(key);
        for (SymbolEntry entry : entries) {
            if (entry.scope.equals(currentScope)) {
                throw new SemanticError("Array '" + id + "' already defined in scope '" + currentScope + "'", position);
            }
        }

        SymbolEntry entry = new SymbolEntry(id, currentType, ARRAY, currentScope, position);
        entry.size = arraySize;
        entries.add(entry);

        System.out.println("Added array to symbol table: " + entry);
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
        System.out.println("===================== SYMBOL TABLE =====================");
        for (List<SymbolEntry> entries : table.values()) {
            for (SymbolEntry entry : entries) {
                System.out.println(entry);
            }
        }
        System.out.println("=======================================================");
    }
}