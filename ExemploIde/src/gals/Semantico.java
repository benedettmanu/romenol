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

    public void executeAction(int action, Token token) throws SemanticError {
        System.out.println("Ação #" + action + ", Token: " + token);

        switch (action) {
            case 1: // <Program> ::= <DeclarationList> #1
                symbolTable.printTable();
                break;

            case 2: // | ID #2 COLCHETE_ESQUERDO <Expr> #14 COLCHETE_DIREITO #15
                identifierStack.push(token.getLexeme());
                positionStack.push(token.getPosition());
                break;

            case 3: // <Declaration> ::= <Type> <ArrayIDList> #3
                symbolTable.setArray(false);
                symbolTable.setArraySize(0);
                break;

            case 4: // <Type> ::= INTEIRO #4
                symbolTable.setCurrentType(SemanticTable.INT);
                break;

            case 5: // <Type> ::= REAL #5
                symbolTable.setCurrentType(SemanticTable.FLO);
                break;

            case 6: // <Type> ::= CARACTER #6
                symbolTable.setCurrentType(SemanticTable.CHA);
                break;

            case 7: // <Type> ::= STRING #7
                symbolTable.setCurrentType(SemanticTable.STR);
                break;

            case 8: // <Type> ::= BOOL #8
                symbolTable.setCurrentType(SemanticTable.BOO);
                break;

            case 9: // <Type> ::= VOID #9
                symbolTable.setCurrentType(-2);
                break;

            case 13: // <Variable> ::= ID #13
                identifierStack.push(token.getLexeme());
                positionStack.push(token.getPosition());

                if (!symbolTable.isArray() && !processingParameters && !processingArrayParameter) {
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
                    String id = identifierStack.pop();
                    int position = positionStack.pop();
                    try {
                        symbolTable.addSymbol(id, SymbolTable.PARAMETER, position);
                    } catch (SemanticError e) {
                        throw new SemanticError(e.getMessage(), position);
                    }
                }
                break;

            case 59: // <Parameter> ::= VETOR <Type> ID COLCHETE_ESQUERDO COLCHETE_DIREITO #59
                processingArrayParameter = true;
                try {
                    symbolTable.addSymbol(token.getLexeme(), SymbolTable.PARAMETER, token.getPosition());
                    symbolTable.setArray(true);
                } catch (SemanticError e) {
                    throw new SemanticError(e.getMessage(), token.getPosition());
                }
                processingArrayParameter = false;
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
                break;

            default:
                break;
        }
    }
}