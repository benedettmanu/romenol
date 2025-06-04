package gals;

import compile.SemanticTable;

import java.util.Stack;

public class Semantico implements Constants {
    private final SymbolTable symbolTable = new SymbolTable();
    private final Stack<Integer> typeStack = new Stack<>();
    private final Stack<String> identifierStack = new Stack<>();
    private final Stack<Integer> positionStack = new Stack<>();
    private final Stack<Integer> operatorStack = new Stack<>();
    private final Stack<String> scopeStack = new Stack<>();
    private Stack<String> operandStack = new Stack<>();
    private Stack<Boolean> constantStack = new Stack<>();
    private final StringBuilder codeGeneration;
    private int scopeCounter = 0;
    private boolean processingParameters = false;
    private boolean processingArrayParameter = false;
    private boolean inDeclarationContext = false;
    private boolean inAssignmentContext = false;
    private boolean isArray = false;
    private String lastIndex;

    public Semantico() {
        scopeStack.push("global");
        codeGeneration = new StringBuilder();
    }

    private void gera_cod(String instruction, String operand) {
        if (operand != null && !operand.isEmpty()) {
            codeGeneration.append("   ").append(instruction).append("     ").append(operand).append("\n");
        } else {
            codeGeneration.append("   ").append(instruction).append("\n");
        }
    }

    public String getGeneratedCode() {
        return codeGeneration.toString();
    }

    public void clearGeneratedCode() {
        codeGeneration.setLength(0);
    }

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
                isArray = true;
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

            case 10: // <Variable> <RelOp> #105 <Expr> #10
                inAssignmentContext = true;
                if (!identifierStack.isEmpty()) {
                    String id = identifierStack.pop();
                    int position = positionStack.pop();
                    verifyIdentifierDeclared(id, position);

                    int varType = symbolTable.lookup(id, symbolTable.getCurrentScope()).getType();
                    int exprType = typeStack.pop();

                    int resultType = SemanticTable.atribType(varType, exprType);
                    if (resultType == SemanticTable.ERR) {
                        throw new SemanticError("Tipos incompatíveis na atribuição para '" + id +
                                "'. Tipo da variável: " + getTypeName(varType) +
                                ", Tipo da expressão: " + getTypeName(exprType), position);
                    } else if (resultType == SemanticTable.WAR) {
                        System.out.println("AVISO: Possível perda de precisão na atribuição para '" + id +
                                "'. Tipo da variável: " + getTypeName(varType) +
                                ", Tipo da expressão: " + getTypeName(exprType) +
                                " (linha/posição: " + position + ")");
                    }

                    symbolTable.markAsUsed(id, symbolTable.getCurrentScope());
                    symbolTable.markAsInitialized(id, symbolTable.getCurrentScope());

                    if (!operandStack.isEmpty()) {
                        String operand = operandStack.pop();
                        boolean isConst = constantStack.pop();

                        if (isConst) {
                            gera_cod("LDI", operand);
                        } else if (!"ACC".equals(operand)) {
                            gera_cod("LD", operand);
                        }
                    }

                    if (isArray) {
                        gera_cod("LDI", lastIndex);
                        gera_cod("STO", "1000");
                        gera_cod("LDI", token.getLexeme());
                        gera_cod("STO", "1001");
                        gera_cod("LD", "1000");
                        gera_cod("STO", "$indr");
                        gera_cod("LD", "1001");
                        gera_cod("STOV", id);
                        isArray = false;
                    } else {
                        gera_cod("STO", id);
                    }
                }
                inAssignmentContext = false;
                break;

            case 11: // <Assignment> ::= <Variable> <AddOp> <Expr> #11
                inAssignmentContext = true;
                if (!identifierStack.isEmpty()) {
                    String id = identifierStack.pop();
                    int position = positionStack.pop();
                    verifyIdentifierDeclared(id, position);

                    int varType = symbolTable.lookup(id, symbolTable.getCurrentScope()).getType();
                    int exprType = typeStack.pop();
                    int op = operatorStack.isEmpty() ? SemanticTable.SUM : operatorStack.pop();

                    int resultType = SemanticTable.resultType(varType, exprType, op);
                    if (resultType == SemanticTable.ERR) {
                        throw new SemanticError("Operação incompatível para '" + id +
                                "'. Tipo da variável: " + getTypeName(varType) +
                                ", Tipo da expressão: " + getTypeName(exprType) +
                                ", Operador: " + getOperatorName(op), position);
                    }

                    int atribResult = SemanticTable.atribType(varType, resultType);
                    if (atribResult == SemanticTable.ERR) {
                        throw new SemanticError("Tipos incompatíveis na atribuição composta para '" + id +
                                "'. Tipo da variável: " + getTypeName(varType) +
                                ", Tipo resultante: " + getTypeName(resultType), position);
                    }

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

                    int varType = symbolTable.lookup(id, symbolTable.getCurrentScope()).getType();
                    int exprType = typeStack.pop();
                    int op = operatorStack.isEmpty() ? SemanticTable.MUL : operatorStack.pop();

                    int resultType = SemanticTable.resultType(varType, exprType, op);
                    if (resultType == SemanticTable.ERR) {
                        throw new SemanticError("Operação incompatível para '" + id +
                                "'. Tipo da variável: " + getTypeName(varType) +
                                ", Tipo da expressão: " + getTypeName(exprType) +
                                ", Operador: " + getOperatorName(op), position);
                    }

                    int atribResult = SemanticTable.atribType(varType, resultType);
                    if (atribResult == SemanticTable.ERR) {
                        throw new SemanticError("Tipos incompatíveis na atribuição composta para '" + id +
                                "'. Tipo da variável: " + getTypeName(varType) +
                                ", Tipo resultante: " + getTypeName(resultType), position);
                    }

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
                isArray = true;
                try {
                    int size = Integer.parseInt(token.getLexeme());
                    symbolTable.setArraySize(size);
                    lastIndex = token.getLexeme();
                } catch (NumberFormatException e) {
                    symbolTable.setArraySize(1);
                }
                symbolTable.setArray(true);
                break;

            case 15: // COLCHETE_DIREITO #15
                if (!identifierStack.isEmpty() && symbolTable.isArray() && inDeclarationContext) {
                    String id = identifierStack.pop();
                    int position = positionStack.pop();
                    try {
                        symbolTable.addArray(id, position);
                    } catch (SemanticError e) {
                        throw new SemanticError(e.getMessage(), position);
                    }
                }
                break;

            case 16: // <ArrayIDList> ::= <Variable> #16 | <ArrayIDList> , <Variable> #16
                isArray = false;
                if (!identifierStack.isEmpty()) {
                    String id = identifierStack.pop();
                    int position = positionStack.pop();
                    try {
                        if (symbolTable.isArray()) {
                            if (!symbolTable.alreadyDeclared(id)) {
                                symbolTable.addArray(id, position);
                            }
                        } else {
                            symbolTable.addSymbol(id, SymbolTable.VARIABLE, position);
                            if (inAssignmentContext || token.getLexeme().contains("=")) {
                                symbolTable.markAsInitialized(id, symbolTable.getCurrentScope());
                            }
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
                exitCurrentScope();
                break;

            case 20: // <Block1> ::= CHAVE_ESQUERDA CHAVE_DIREITA #20
                exitCurrentScope();
                break;

            case 21: // SE PARENTESES_ESQUERDO <Expr> #21 PARENTESES_DIREITO <Block1> #22
                int exprType = typeStack.pop();
                if (exprType != SemanticTable.BOO) {
                    throw new SemanticError("Expressão condicional deve ser do tipo booleano, encontrado: " +
                            getTypeName(exprType), token.getPosition());
                }
                enterNewScope("if");
                break;

            case 22: // Final do bloco IF (já tratado no case 19/20)
                break;
            
            case 24: // SENAO <Block1> #24
                enterNewScope("else");
                break;

            case 26: // ENQUANTO PARENTESES_ESQUERDO <Expr> #26 PARENTESES_DIREITO <Instruction> #27
                exprType = typeStack.pop();
                if (exprType != SemanticTable.BOO) {
                    throw new SemanticError("Expressão condicional deve ser do tipo booleano, encontrado: " +
                            getTypeName(exprType), token.getPosition());
                }
                enterNewScope("while"); // Entra em novo escopo para o WHILE
                break;

            case 27: // Final do while
                exitCurrentScope();
                break;
            
            case 28: // PARA - primeiro assignment no FOR
                enterNewScope("for");
                break;
            
            case 31: // Final do FOR (primeiro tipo)
                exitCurrentScope(); // Sai do escopo do for
                break;
            
            case 32: // PARA - assignment no FOR (segundo tipo)
                enterNewScope("for");
                break;
            
            case 35: // Final do FOR (segundo tipo)
                exitCurrentScope();
                break;
            
            case 36: // FACA no DO-WHILE
                enterNewScope("dowhile");
                break;
            
            case 38: // Final do DO-WHILE
                exitCurrentScope();
                break;

            case 39: // <InputStatement> ::= LEIA PARENTESES_ESQUERDO ID #39 PARENTESES_DIREITO #40
                verifyIdentifierDeclared(token.getLexeme(), token.getPosition());
                symbolTable.markAsInitialized(token.getLexeme(), symbolTable.getCurrentScope());

                gera_cod("LD", "$in_port");
                gera_cod("STO", token.getLexeme());
                break;

            case 41: // <InputStatement> ::= LEIA PARENTESES_ESQUERDO ID COLCHETE_ESQUERDO <Expr> #41 COLCHETE_DIREITO PARENTESES_DIREITO #42
                String arrayId = identifierStack.pop();
                int arrayPosition = positionStack.pop();

                verifyArrayDeclared(arrayId, arrayPosition);

                int indexType = typeStack.pop();
                if (indexType != SemanticTable.INT) {
                    throw new SemanticError("Índice de array deve ser do tipo inteiro, encontrado: " +
                            getTypeName(indexType), token.getPosition());
                }

                symbolTable.markAsInitialized(token.getLexeme(), symbolTable.getCurrentScope());

                gera_cod("LDI", token.getLexeme());
                gera_cod("STO", "$indr");
                gera_cod("LD", "$in_port");
                gera_cod("STOV", arrayId);

                break;

            case 47: // <OutputElement> ::= ID #47
                String id = token.getLexeme();
                int position = token.getPosition();
                verifyIdentifierDeclared(id, position);
                symbolTable.markAsUsed(id, symbolTable.getCurrentScope());

                checkIfInitialized(id, position);

                gera_cod("LD", token.getLexeme());
                gera_cod("STO", "$out_port");
                break;

            case 48: // <OutputElement> ::= ID COLCHETE_ESQUERDO <Expr> #48 COLCHETE_DIREITO #49
                id = identifierStack.pop();
                position = positionStack.pop();

                verifyArrayDeclared(id, position);

                indexType = typeStack.pop();
                if (indexType != SemanticTable.INT) {
                    throw new SemanticError("Índice de array deve ser do tipo inteiro, encontrado: " +
                            getTypeName(indexType), token.getPosition());
                }

                symbolTable.markAsUsed(id, symbolTable.getCurrentScope());
                checkIfInitialized(id, position);

                gera_cod("LDI", token.getLexeme());
                gera_cod("STO", "$indr");
                gera_cod("LDV", id);
                gera_cod("STO", "$out_port");
                break;

            case 56: // FUNCAO <Type> ID #56 ...
                try {
                    symbolTable.addSymbol(token.getLexeme(), SymbolTable.FUNCTION, token.getPosition());
                    enterNewScope("func_" + token.getLexeme());
                    processingParameters = true;
                } catch (SemanticError e) {
                    throw new SemanticError(e.getMessage(), token.getPosition());
                }
                break;

            case 57: // ... <Block1> #57 (end of function)
                exitCurrentScope();
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

            case 65: // <Expr> ::= <Expr> OU_LOGICO <Expr1> #65
                processOperatorExpression(SemanticTable.LOR);
                break;

            case 66: // <Expr1> ::= <Expr1> E_LOGICO <Expr2> #66
                processOperatorExpression(SemanticTable.LAND);
                break;

            case 67: // <Expr2> ::= <Expr2> OU_BIT <Expr3> #67
                //processOperatorExpression(SemanticTable.BOR);
                if (typeStack.size() < 2) {
                    throw new SemanticError("Erro na análise semântica: não há tipos suficientes na pilha", 0);
                }

                int rightType = typeStack.pop();
                int leftType = typeStack.pop();

                if (leftType != SemanticTable.INT || rightType != SemanticTable.INT) {
                    throw new SemanticError("Operações bit a bit só são permitidas com tipos inteiros. " +
                            "Encontrado: " + getTypeName(leftType) + " e " + getTypeName(rightType),
                            token.getPosition());
                }

                gera_cod("OR", ""); // OR bit a bit
                typeStack.push(SemanticTable.INT);
                break;

            case 68: // <Expr3> ::= <Expr3> XOR_BIT <Expr4> #68
                //processOperatorExpression(SemanticTable.XOR);
                if (typeStack.size() < 2) {
                    throw new SemanticError("Erro na análise semântica: não há tipos suficientes na pilha", 0);
                }

                rightType = typeStack.pop();
                leftType = typeStack.pop();

                if (leftType != SemanticTable.INT || rightType != SemanticTable.INT) {
                    throw new SemanticError("Operações bit a bit só são permitidas com tipos inteiros. " +
                            "Encontrado: " + getTypeName(leftType) + " e " + getTypeName(rightType),
                            token.getPosition());
                }

                gera_cod("XOR", ""); // XOR bit a bit
                typeStack.push(SemanticTable.INT);
                break;

            case 69: // <Expr4> ::= <Expr4> E_BIT <Expr5> #69
                //processOperatorExpression(SemanticTable.BAND);
                if (typeStack.size() < 2) {
                    throw new SemanticError("Erro na análise semântica: não há tipos suficientes na pilha", 0);
                }

                rightType = typeStack.pop();
                leftType = typeStack.pop();

                if (leftType != SemanticTable.INT || rightType != SemanticTable.INT) {
                    throw new SemanticError("Operações bit a bit só são permitidas com tipos inteiros. " +
                            "Encontrado: " + getTypeName(leftType) + " e " + getTypeName(rightType),
                            token.getPosition());
                }

                gera_cod("AND", ""); // AND bit a bit
                typeStack.push(SemanticTable.INT);
                break;

            case 70: // <Expr5> ::= <Expr5> <RelOp> <Expr6> #70
                processOperatorExpression(SemanticTable.REL);
                break;

            case 71: // <Expr6> ::= <Expr6> <ShiftOp> <Expr7> #71
                //processOperatorExpression(SemanticTable.SHL);
                if (typeStack.size() < 2) {
                    throw new SemanticError("Erro na análise semântica: não há tipos suficientes na pilha", 0);
                }

                rightType = typeStack.pop();
                leftType = typeStack.pop();
                int op = operatorStack.isEmpty() ? SemanticTable.SHL : operatorStack.pop();

                if (leftType != SemanticTable.INT || rightType != SemanticTable.INT) {
                    throw new SemanticError("Operações de deslocamento só são permitidas com tipos inteiros. " +
                            "Encontrado: " + getTypeName(leftType) + " e " + getTypeName(rightType),
                            token.getPosition());
                }

                if (op == SemanticTable.SHL) {
                    gera_cod("SLL", "");
                } else if (op == SemanticTable.SHR) {
                    gera_cod("SRL", "");
                }

                typeStack.push(SemanticTable.INT);
                break;

            case 72: // <Expr7> ::= <Expr7> <AddOp> <Expr8> #72
                //processOperatorExpression(SemanticTable.SUM);
                if (typeStack.size() < 2) {
                    throw new SemanticError("Erro na análise semântica: não há tipos suficientes na pilha", 0);
                }

                rightType = typeStack.pop();
                leftType = typeStack.pop();
                op = operatorStack.isEmpty() ? SemanticTable.SUM : operatorStack.pop();

                int resultType = SemanticTable.resultType(leftType, rightType, op);
                if (resultType == SemanticTable.ERR) {
                    throw new SemanticError("Operação incompatível entre tipos " +
                            getTypeName(leftType) + " e " + getTypeName(rightType) +
                            " com operador " + getOperatorName(op), token.getPosition());
                }

                String rightOp = operandStack.pop();
                boolean rightIsConst = constantStack.pop();
                String leftOp = operandStack.pop();
                boolean leftIsConst = constantStack.pop();

                if (leftIsConst) {
                    gera_cod("LDI", leftOp);
                } else {
                    gera_cod("LD", leftOp);
                }

                if (op == SemanticTable.SUM) {
                    if (rightIsConst) {
                        gera_cod("ADDI", rightOp);
                    } else {
                        gera_cod("ADD", rightOp);
                    }
                } else if (op == SemanticTable.SUB) {
                    if (rightIsConst) {
                        gera_cod("SUBI", rightOp);
                    } else {
                        gera_cod("SUB", rightOp);
                    }
                }

                operandStack.push("ACC");
                constantStack.push(false);

                typeStack.push(resultType);
                break;

            case 73: // <Expr8> ::= <Expr8> <MulOp> <Expr9> #73
                //processOperatorExpression(SemanticTable.MUL);
                if (typeStack.size() < 2) {
                    throw new SemanticError("Erro na análise semântica: não há tipos suficientes na pilha", 0);
                }

                rightType = typeStack.pop();
                leftType = typeStack.pop();
                op = operatorStack.isEmpty() ? SemanticTable.MUL : operatorStack.pop();

                resultType = SemanticTable.resultType(leftType, rightType, op);
                if (resultType == SemanticTable.ERR) {
                    throw new SemanticError("Operação incompatível entre tipos " +
                            getTypeName(leftType) + " e " + getTypeName(rightType) +
                            " com operador " + getOperatorName(op), token.getPosition());
                }

                rightOp = operandStack.pop();
                rightIsConst = constantStack.pop();
                leftOp = operandStack.pop();
                leftIsConst = constantStack.pop();

                if (leftIsConst) {
                    gera_cod("LDI", leftOp);
                } else {
                    gera_cod("LD", leftOp);
                }

                if (op == SemanticTable.MUL) {
                    if (rightIsConst) {
                        gera_cod("MULI", rightOp);
                    } else {
                        gera_cod("MUL", rightOp);
                    }
                } else if (op == SemanticTable.DIV) {
                    if (rightIsConst) {
                        gera_cod("DIVI", rightOp);
                    } else {
                        gera_cod("DIV", rightOp);
                    }
                } else if (op == SemanticTable.MOD) {
                    if (rightIsConst) {
                        gera_cod("MODI", rightOp);
                    } else {
                        gera_cod("MOD", rightOp);
                    }
                }

                operandStack.push("ACC");
                constantStack.push(false);

                typeStack.push(resultType);
                break;

            case 74: // <Expr9> ::= <UnOp> <Expr10> #74
                int type = typeStack.pop();
                op = operatorStack.pop();
                
                boolean valid = false;
                switch(op) {
                    case SemanticTable.SUB:
                        valid = (type == SemanticTable.INT || type == SemanticTable.FLO);
                        break;
                    case SemanticTable.BNOT:
                        valid = (type == SemanticTable.INT);
                        break;
                    case SemanticTable.LNOT:
                        valid = (type == SemanticTable.BOO);
                        break;
                    case SemanticTable.BAND:
                        valid = (type == SemanticTable.INT);
                        break;
                    default:
                        valid = false;
                        break;
                }

                if (!valid) {
                   throw new SemanticError("Operador unário incompatível com o tipo " +
                           getTypeName(type), token.getPosition());
                }
                
                typeStack.push(type);
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
                if (!isArray) {
                    //gera_cod("LDI", token.getLexeme());
                    operandStack.push(token.getLexeme());
                    constantStack.push(true);
                }
                typeStack.push(SemanticTable.INT);
                break;

            case 76: // <Expr10> ::= LITERAL_STRING_CARACTER #76
                //gera_cod("LDI", "'" + token.getLexeme() + "'");
                typeStack.push(SemanticTable.STR);
                break;

            case 77: // <Expr10> ::= LITERAL_CARACTER #77
                //gera_cod("LDI", "'" + token.getLexeme() + "'");
                typeStack.push(SemanticTable.CHA);
                break;

            case 78: // <Expr10> ::= LITERAL_REAL #78
                //gera_cod("LDI", token.getLexeme());
                typeStack.push(SemanticTable.FLO);
                break;

            case 79: // <Expr10> ::= ID #79
                id = token.getLexeme();
                position = token.getPosition();
                verifyIdentifierDeclaredAndPushType(id, position);
                //gera_cod("LD", id);

                operandStack.push(id);
                constantStack.push(false);

                if (!inAssignmentContext) {
                    symbolTable.markAsUsed(id, symbolTable.getCurrentScope());
                    checkIfInitialized(id, position);
                }
                break;

            case 80: // <Expr10> ::= ID COLCHETE_ESQUERDO <Expr> #80 COLCHETE_DIREITO #81
                id = token.getLexeme();
                position = token.getPosition();

                indexType = typeStack.pop();
                if (indexType != SemanticTable.INT) {
                    throw new SemanticError("Índice de array deve ser do tipo inteiro, encontrado: " +
                            getTypeName(indexType), token.getPosition());
                }

                verifyArrayDeclaredAndPushType(id, position);

                gera_cod("STO", "1000");

                if (inAssignmentContext) {
                    isArray = true;
                    identifierStack.push(id);
                    positionStack.push(position);
                    lastIndex = "1000";
                } else {
                    gera_cod("LD", "1000");
                    gera_cod("STO", "$indr");
                    gera_cod("LDV", id);
                    symbolTable.markAsUsed(id, symbolTable.getCurrentScope());
                    checkIfInitialized(id, position);
                }
                break;

            case 82: // <Expr10> ::= PARENTESES_ESQUERDO <Expr> #82 PARENTESES_DIREITO #83
                break;

            case 84: // <Expr10> ::= <FunctionCall> #84
                typeStack.push(SemanticTable.INT);
                break;

            case 85: // <RelOp> ::= MAIOR #85
            case 86: // <RelOp> ::= MENOR #86
            case 87: // <RelOp> ::= MAIOR_IGUAL #87
            case 88: // <RelOp> ::= MENOR_IGUAL #88
            case 89: // <RelOp> ::= IGUAL #89
            case 90: // <RelOp> ::= DIFERENTE #90
                operatorStack.push(SemanticTable.REL);
                break;

            case 91: // <RelOp> ::= RESULTADO #91 (operador =)
                inAssignmentContext = true;
                operatorStack.push(SemanticTable.REL);
                if (!identifierStack.isEmpty()) {
                    id = identifierStack.peek();
                    position = positionStack.peek();
                    verifyIdentifierDeclared(id, position);

                    symbolTable.markAsInitialized(id, symbolTable.getCurrentScope());
                }
                break;

            case 92: // <UnOp> ::= DIMINUICAO #92
                operatorStack.push(SemanticTable.SUB);
                break;

            case 93: // <UnOp> ::= NEGACAO_BIT #93
                operatorStack.push(SemanticTable.BNOT);
                break;

            case 94: // <UnOp> ::= NEGACAO #94
                operatorStack.push(SemanticTable.LNOT);
                break;

            case 95: // <ShiftOp> ::= SHIFT_ESQUERDA #95
                operatorStack.push(SemanticTable.SHL);
                break;

            case 96: // <ShiftOp> ::= SHIFT_DIREITA #96
                operatorStack.push(SemanticTable.SHR);
                break;

            case 97: // <AddOp> ::= SOMA #97
                operatorStack.push(SemanticTable.SUM);
                break;

            case 98: // <AddOp> ::= DIMINUICAO #98
                operatorStack.push(SemanticTable.SUB);
                break;

            case 99: // <MulOp> ::= MULTIPLICACAO #99
                operatorStack.push(SemanticTable.MUL);
                break;

            case 100: // <MulOp> ::= DIVISAO #100
                operatorStack.push(SemanticTable.DIV);
                break;

            case 101: // <MulOp> ::= MODULO #101
                operatorStack.push(SemanticTable.MOD);
                break;

            case 102: // "true"
                typeStack.push(SemanticTable.BOO);
                break;

            case 103: // "false"
                typeStack.push(SemanticTable.BOO);
                break;

            case 104: // ID #104 COLCHETE_ESQUERDO <Expr> #48 COLCHETE_DIREITO #49
                identifierStack.push(token.getLexeme());
                positionStack.push(token.getPosition());
                break;

            case 105: // <Variable> <RelOp> #105 <Expr> #10
                inAssignmentContext = true;
                break;

            default:
                break;
        }
    }

    private void processOperatorExpression(int operatorType) throws SemanticError {
        if (typeStack.size() < 2) {
            throw new SemanticError("Erro na análise semântica: não há tipos suficientes na pilha", 0);
        }

        int type2 = typeStack.pop();
        int type1 = typeStack.pop();
        int op = operatorStack.isEmpty() ? operatorType : operatorStack.pop();

        int resultType = SemanticTable.resultType(type1, type2, op);
        if (resultType == SemanticTable.ERR) {
            throw new SemanticError("Operação incompatível entre tipos " +
                    getTypeName(type1) + " e " + getTypeName(type2) +
                    " com operador " + getOperatorName(op), 0);
        }

        typeStack.push(resultType);
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
            System.out.println("AVISO: Variável '"+ id + 
            "'não inicializada no escopo '" + symbolTable.getCurrentScope() +
                    "' (linha/posição: " + position + ")");
        }
    }

    private void enterNewScope(String scopeType) {
        String newScope = scopeType + "_" + (++scopeCounter);
        scopeStack.push(newScope);
        symbolTable.setCurrentScope(newScope);
        System.out.println("Entrando no escopo: " + newScope);
    }
    
    private void exitCurrentScope() {
        if (scopeStack.size() > 1) {
            String exitingScope = scopeStack.pop();
            symbolTable.setCurrentScope(scopeStack.peek());
            System.out.println("Saindo do escopo: " + exitingScope);
        }
    }

    private String getTypeName(int type) {
        switch (type) {
            case SemanticTable.INT: return "INT";
            case SemanticTable.FLO: return "FLOAT";
            case SemanticTable.CHA: return "CHAR";
            case SemanticTable.STR: return "STRING";
            case SemanticTable.BOO: return "BOOL";
            default: return "UNKNOWN";
        }
    }

    private String getOperatorName(int op) {
        switch (op) {
            case SemanticTable.SUM:
                return "SOMA"; // +
            case SemanticTable.SUB:
                return "SUBTRACAO"; // -
            case SemanticTable.MUL:
                return "MULTIPLICACAO"; // *
            case SemanticTable.DIV:
                return "DIVISAO"; // /
            case SemanticTable.MOD:
                return "MODULO"; // %
            case SemanticTable.REL:
                return "RELACIONAL"; // ==, !=, >, <, >=, <=, etc.
            case SemanticTable.SHL:
                return "DESLOCAMENTO_ESQUERDA"; // <<
            case SemanticTable.SHR:
                return "DESLOCAMENTO_DIREITA"; // >>
            case SemanticTable.BOR:
                return "OU_BIT_A_BIT"; // |
            case SemanticTable.XOR:
                return "XOR_BIT_A_BIT"; // ^
            case SemanticTable.BAND:
                return "E_BIT_A_BIT"; // &
            case SemanticTable.LAND:
                return "E_LOGICO"; // &&
            case SemanticTable.LOR:
                return "OU_LOGICO"; // ||
            case SemanticTable.BNOT:
                return "NEGACAO_BIT"; // ~
            case SemanticTable.LNOT:
                return "NEGACAO_LOGICA"; // !
            default:
                return "UNKNOWN";
        }
    }

}