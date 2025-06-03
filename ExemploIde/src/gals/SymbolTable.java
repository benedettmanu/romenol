package gals;

import java.util.*;

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
        private boolean used;    // flag indicating whether the identifier has been used
        private boolean initialized; // flag indicating whether the identifier has been initialized

        public SymbolEntry(String id, int type, int modality, String scope, int position) {
            this.id = id;
            this.type = type;
            this.modality = modality;
            this.scope = scope;
            this.position = position;
            this.size = 0;  // Default size for non-arrays
            this.used = false;

            this.initialized = (modality == PARAMETER || modality == FUNCTION);
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

        public boolean isUsed() {
            return used;
        }

        public void setUsed(boolean used) {
            this.used = used;
        }

        public boolean isInitialized() {
            return initialized;
        }

        public void setInitialized(boolean initialized) {
            this.initialized = initialized;
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
                    (modality == ARRAY ? ", Size: " + size : "") +
                    ", Used: " + used +
                    ", Initialized: " + initialized;
        }
    }

    private String currentScope = "global";
    private int currentType = -1;
    private boolean isArray = false;
    private int arraySize = 0;
    private final Map<String, List<SymbolEntry>> table = new LinkedHashMap<>();

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

    public boolean alreadyDeclared(String id) {
        String key = id + "@" + currentScope;
        return table.containsKey(key);
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
            if (scope.startsWith("if_") || scope.startsWith("else_") || 
                scope.startsWith("while_") || scope.startsWith("for_") || 
                scope.startsWith("dowhile_")) {
                
                for (String tableKey : table.keySet()) {
                    if (tableKey.startsWith(id + "@func_")) {
                        List<SymbolEntry> entries = table.get(tableKey);
                        for (SymbolEntry entry : entries) {
                            if (entry.scope.startsWith("func_")) {
                                return entry;
                            }
                        }
                    }
                }
            }
            
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

    public void markAsUsed(String id, String scope) {
        SymbolEntry entry = lookup(id, scope);
        if (entry != null) {
            entry.setUsed(true);
        }
    }

    public void markAsInitialized(String id, String scope) {
        SymbolEntry entry = lookup(id, scope);
        if (entry != null) {
            entry.setInitialized(true);
        }
    }

    public void checkUnusedIdentifiers() {
        System.out.println("\n========== VERIFICAÇÃO DE IDENTIFICADORES NÃO UTILIZADOS ==========");
        boolean found = false;

        for (List<SymbolEntry> entries : table.values()) {
            for (SymbolEntry entry : entries) {
                if (!entry.isUsed()) {
                    if (entry.getModality() == FUNCTION) {
                        System.out.println("AVISO: Função '" + entry.getId() + "' declarada mas nunca chamada...");
                    } else {
                        System.out.println("AVISO: Identificador '" + entry.getId() + "' declarado mas não utilizado...");
                    }
                    found = true;
                }
            }
        }

        if (!found) {
            System.out.println("Nenhum identificador declarado sem uso encontrado.");
        }
        System.out.println("=================================================================");
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

    public Map<String, List<SymbolEntry>> getTable() {
        return table;
    }
}