package gals;

import compile.SemanticTable;

import java.util.Stack;

public class Semantico implements Constants {
    private final SymbolTable symbolTable = new SymbolTable();
    private final Stack<Integer> typeStack = new Stack<>();
    private final Stack<String> identifierStack = new Stack<>();
    private final Stack<Integer> positionStack = new Stack<>();
    private boolean processingParameters = false;
    private boolean processingArrayParameter = false;
    private boolean inDeclarationContext = false;
    private boolean inAssignmentContext = false;

    public void executeAction(int action, Token token) throws SemanticError {
        System.out.println("Ação #" + action + ", Token: " + token);

        switch (action) {
            case 1: // <Program> ::= <DeclarationList> #1
                symbolTable.checkUnusedIdentifiers();
                symbolTable.printTable();
                break;

            case 2: // | ID #2 COLCHETE_ESQUERDO <Expr> #14 COLCHETE_DIREITO #15
                identifierStack.push(token.getLexeme());
                positionStack.push(token.getPosition());
                break;

            case 3: // <Declaration> ::= <Type> <ArrayIDList> #3
                symbolTable.setArray(false);
                symbolTable.setArraySize(0);
                inDeclarationContext = false;
                break;

            case 4: // <Type> ::= INTEIRO #4
                symbolTable.setCurrentType(SemanticTable.INT);
                inDeclarationContext = true;
                break;

            case 5: // <Type> ::= REAL #5
                symbolTable.setCurrentType(SemanticTable.FLO);
                inDeclarationContext = true;
                break;

            case 6: // <Type> ::= CARACTER #6
                symbolTable.setCurrentType(SemanticTable.CHA);
                inDeclarationContext = true;
                break;

            case 7: // <Type> ::= STRING #7
                symbolTable.setCurrentType(SemanticTable.STR);
                inDeclarationContext = true;
                break;

            case 8: // <Type> ::= BOOL #8
                symbolTable.setCurrentType(SemanticTable.BOO);
                inDeclarationContext = true;
                break;

            case 9: // <Type> ::= VOID #9
                symbolTable.setCurrentType(-2);
                inDeclarationContext = true;
                break;

            case 10: // <Assignment> ::= <Variable> <RelOp> <Expr> #10
                inAssignmentContext = true;
                if (!identifierStack.isEmpty()) {
                    String id = identifierStack.pop();
                    int position = positionStack.pop();
                    verifyIdentifierDeclared(id, position);
                    symbolTable.markAsInitialized(id, symbolTable.getCurrentScope());
                }
                inAssignmentContext = false;
                break;

            case 11: // <Assignment> ::= <Variable> <AddOp> <Expr> #11
                inAssignmentContext = true;
                if (!identifierStack.isEmpty()) {
                    String id = identifierStack.pop();
                    int position = positionStack.pop();
                    verifyIdentifierDeclared(id, position);
                    symbolTable.markAsUsed(id, symbolTable.getCurrentScope());
                    symbolTable.markAsInitialized(id, symbolTable.getCurrentScope());
                }
                inAssignmentContext = false;
                break;

            case 12: // <Assignment> ::= <Variable> <MulOp> <Expr> #12
                inAssignmentContext = true;
                if (!identifierStack.isEmpty()) {
                    String id = identifierStack.pop();
                    int position = positionStack.pop();
                    verifyIdentifierDeclared(id, position);
                    symbolTable.markAsUsed(id, symbolTable.getCurrentScope());
                    symbolTable.markAsInitialized(id, symbolTable.getCurrentScope());
                }
                inAssignmentContext = false;
                break;

            case 13: // <Variable> ::= ID #13
                identifierStack.push(token.getLexeme());
                positionStack.push(token.getPosition());

                if (inDeclarationContext && !symbolTable.isArray() && !processingParameters && !processingArrayParameter) {
                    String id = identifierStack.pop();
                    int position = positionStack.pop();
                    try {
                        symbolTable.addSymbol(id, SymbolTable.VARIABLE, position);
                    } catch (SemanticError e) {
                        throw new SemanticError(e.getMessage(), position);
                    }
                }
                break;

            case 14: // ID COLCHETE_ESQUERDO <Expr> #14
                try {
                    int size = Integer.parseInt(token.getLexeme());
                    symbolTable.setArraySize(size);
                } catch (NumberFormatException e) {
                    symbolTable.setArraySize(1);
                }
                symbolTable.setArray(true);
                break;

            case 15: // COLCHETE_DIREITO #15
                if (!identifierStack.isEmpty() && symbolTable.isArray()) {
                    String id = identifierStack.pop();
                    int position = positionStack.pop();
                    try {
                        symbolTable.addArray(id, position);
                    } catch (SemanticError e) {
                        throw new SemanticError(e.getMessage(), position);
                    }
                }
                break;

            case 16: // <ArrayIDList> ::= <Variable> #16 | <ArrayIDList> VIRGULA <Variable> #16
                if (!identifierStack.isEmpty()) {
                    String id = identifierStack.pop();
                    int position = positionStack.pop();

                    try {
                        if (symbolTable.isArray()) {
                            symbolTable.addArray(id, position);
                        } else {
                            symbolTable.addSymbol(id, SymbolTable.VARIABLE, position);
                        }
                    } catch (SemanticError e) {
                        throw new SemanticError(e.getMessage(), position);
                    }
                }

                symbolTable.setArray(false);
                symbolTable.setArraySize(0);
                break;

            case 17: // <Block> ::= COMECO <InstructionList> FIM #17
                break;

            case 18: // <Block> ::= COMECO FIM #18
                break;

            case 19: // <Block1> ::= CHAVE_ESQUERDA <InstructionList> CHAVE_DIREITA #19
                break;

            case 20: // <Block1> ::= CHAVE_ESQUERDA CHAVE_DIREITA #20
                break;

            case 21: // SE PARENTESES_ESQUERDO <Expr> #21 PARENTESES_DIREITO <Block1> #22
                break;

            case 26: // ENQUANTO PARENTESES_ESQUERDO <Expr> #26 PARENTESES_DIREITO <Instruction> #27
                break;

            case 39: // <InputStatement> ::= LEIA PARENTESES_ESQUERDO ID #39 PARENTESES_DIREITO #40
                verifyIdentifierDeclared(token.getLexeme(), token.getPosition());
                symbolTable.markAsInitialized(token.getLexeme(), symbolTable.getCurrentScope());
                break;

            case 41: // <InputStatement> ::= LEIA PARENTESES_ESQUERDO ID COLCHETE_ESQUERDO <Expr> #41 COLCHETE_DIREITO PARENTESES_DIREITO #42
                verifyArrayDeclared(token.getLexeme(), token.getPosition());
                symbolTable.markAsInitialized(token.getLexeme(), symbolTable.getCurrentScope());
                break;

            case 47: // <OutputElement> ::= ID #47
                String id = token.getLexeme();
                int position = token.getPosition();
                verifyIdentifierDeclared(id, position);
                symbolTable.markAsUsed(id, symbolTable.getCurrentScope());

                checkIfInitialized(id, position);
                break;

            case 48: // <OutputElement> ::= ID COLCHETE_ESQUERDO <Expr> #48 COLCHETE_DIREITO #49
                id = token.getLexeme();
                position = token.getPosition();
                verifyArrayDeclared(id, position);
                symbolTable.markAsUsed(id, symbolTable.getCurrentScope());

                checkIfInitialized(id, position);
                break;

            case 56: // <Subroutine> ::= FUNCAO <Type> ID #56 ...
                try {
                    symbolTable.addSymbol(token.getLexeme(), SymbolTable.FUNCTION, token.getPosition());
                    symbolTable.setCurrentScope(token.getLexeme());
                    processingParameters = true;
                } catch (SemanticError e) {
                    throw new SemanticError(e.getMessage(), token.getPosition());
                }
                break;

            case 57: // ... <Block1> #57 (end of function)
                symbolTable.setCurrentScope("global");
                processingParameters = false;
                processingArrayParameter = false;
                break;

            case 58: // <Parameter> ::= <Type> <Variable> #58
                if (!identifierStack.isEmpty()) {
                    String paramId = identifierStack.pop();
                    int paramPosition = positionStack.pop();
                    try {
                        symbolTable.addSymbol(paramId, SymbolTable.PARAMETER, paramPosition);
                        symbolTable.markAsInitialized(paramId, symbolTable.getCurrentScope());
                    } catch (SemanticError e) {
                        throw new SemanticError(e.getMessage(), paramPosition);
                    }
                }
                break;

            case 59: // <Parameter> ::= VETOR <Type> ID COLCHETE_ESQUERDO COLCHETE_DIREITO #59
                processingArrayParameter = true;
                try {
                    symbolTable.addSymbol(token.getLexeme(), SymbolTable.PARAMETER, token.getPosition());
                    symbolTable.setArray(true);
                    symbolTable.markAsInitialized(token.getLexeme(), symbolTable.getCurrentScope());
                } catch (SemanticError e) {
                    throw new SemanticError(e.getMessage(), token.getPosition());
                }
                processingArrayParameter = false;
                break;

            case 60: // <FunctionCall> ::= ID #60 PARENTESES_ESQUERDO <OptionalArgumentList> #61 PARENTESES_DIREITO #62
                verifyFunctionDeclared(token.getLexeme(), token.getPosition());
                symbolTable.markAsUsed(token.getLexeme(), "global");
                break;

            case 75: // <Expr10> ::= LITERAL_INTEIRO #75
                if (symbolTable.isArray()) {
                    try {
                        int size = Integer.parseInt(token.getLexeme());
                        symbolTable.setArraySize(size);
                    } catch (NumberFormatException e) {
                        symbolTable.setArraySize(1);
                    }
                }
                typeStack.push(SemanticTable.INT);
                break;

            case 76: // <Expr10> ::= LITERAL_STRING_CARACTER #76
                typeStack.push(SemanticTable.STR);
                break;

            case 77: // <Expr10> ::= LITERAL_CARACTER #77
                typeStack.push(SemanticTable.CHA);
                break;

            case 78: // <Expr10> ::= LITERAL_REAL #78
                typeStack.push(SemanticTable.FLO);
                break;

            case 79: // <Expr10> ::= ID #79
                id = token.getLexeme();
                position = token.getPosition();
                verifyIdentifierDeclaredAndPushType(id, position);

                if (!inAssignmentContext) {
                    symbolTable.markAsUsed(id, symbolTable.getCurrentScope());
                    checkIfInitialized(id, position);
                }
                break;

            case 80: // <Expr10> ::= ID COLCHETE_ESQUERDO <Expr> #80 COLCHETE_DIREITO #81
                id = token.getLexeme();
                position = token.getPosition();
                verifyArrayDeclaredAndPushType(id, position);

                if (!inAssignmentContext) {
                    symbolTable.markAsUsed(id, symbolTable.getCurrentScope());
                    checkIfInitialized(id, position);
                }
                break;

            case 91: // <RelOp> ::= RESULTADO #91 (operador =)
                inAssignmentContext = true;
                if (!identifierStack.isEmpty()) {
                    id = identifierStack.peek();
                    position = positionStack.peek();
                    verifyIdentifierDeclared(id, position);
                }
                break;

            default:
                break;
        }
    }

    private void verifyIdentifierDeclared(String id, int position) throws SemanticError {
        SymbolTable.SymbolEntry entry = symbolTable.lookup(id, symbolTable.getCurrentScope());
        if (entry == null) {
            throw new SemanticError("Identificador '" + id + "' não declarado", position);
        }
    }

    private void verifyArrayDeclared(String id, int position) throws SemanticError {
        SymbolTable.SymbolEntry entry = symbolTable.lookup(id, symbolTable.getCurrentScope());
        if (entry == null) {
            throw new SemanticError("Array '" + id + "' não declarado", position);
        }
        if (entry.getModality() != SymbolTable.ARRAY) {
            throw new SemanticError("Identificador '" + id + "' não é um array", position);
        }
    }

    private void verifyFunctionDeclared(String id, int position) throws SemanticError {
        SymbolTable.SymbolEntry entry = symbolTable.lookup(id, "global");
        if (entry == null) {
            throw new SemanticError("Função '" + id + "' não declarada", position);
        }
        if (entry.getModality() != SymbolTable.FUNCTION) {
            throw new SemanticError("Identificador '" + id + "' não é uma função", position);
        }
    }

    private void verifyIdentifierDeclaredAndPushType(String id, int position) throws SemanticError {
        SymbolTable.SymbolEntry entry = symbolTable.lookup(id, symbolTable.getCurrentScope());
        if (entry == null) {
            throw new SemanticError("Identificador '" + id + "' não declarado", position);
        }
        typeStack.push(entry.getType());
    }

    private void verifyArrayDeclaredAndPushType(String id, int position) throws SemanticError {
        SymbolTable.SymbolEntry entry = symbolTable.lookup(id, symbolTable.getCurrentScope());
        if (entry == null) {
            throw new SemanticError("Array '" + id + "' não declarado", position);
        }
        if (entry.getModality() != SymbolTable.ARRAY) {
            throw new SemanticError("Identificador '" + id + "' não é um array", position);
        }
        typeStack.push(entry.getType());
    }

    private void checkIfInitialized(String id, int position) {
        SymbolTable.SymbolEntry entry = symbolTable.lookup(id, symbolTable.getCurrentScope());
        if (entry != null && !entry.isInitialized()) {
            System.out.println("AVISO: Possível uso de variável não inicializada '" + id +
                    "' no escopo '" + symbolTable.getCurrentScope() +
                    "' (linha/posição: " + position + ")");
        }
    }
}