/**
 * The GB-J Compiler
 * Copyright (C) 2019 robbi-blechdose
 * Licensed under GNU AGPLv3
 * (See LICENSE.txt for full license)
 */
package de.jc.gbjc.main.Parsing;

import de.jc.gbjc.main.Util.Logger;
import de.jc.gbjc.main.Parsing.AbstractSyntaxTree.TreeNodeType;
import de.jc.gbjc.main.Token;
import de.jc.gbjc.main.Token.TokenType;
import java.util.List;

/**
 * The second stage of the compilation process,
 * the parser
 *
 * @author robbi-blechdose
 * 
 */
public class Parser
{
    private List<Token> tokens;
    private int i;
    private int j;
    
    private List<String> objectTypes;
    
    private boolean objectWarn;
    
    /**
     * Instantiates a parser object
     * 
     * @param tokens The token list to be parsed into an AST
     * @param objectTypes The object types to be used for reference
     */
    public Parser(List<Token> tokens, List<String> objectTypes, boolean objectWarn)
    {
        this.tokens = tokens;
        this.objectTypes = objectTypes;
        this.i = 0;
        this.objectWarn = objectWarn;
    }
    
    /**
     * Outputs a syntax error and terminates the program
     * 
     * @param line The line the error occured on
     * @param message A description of the error
     */
    private void syntaxError(int line, String message)
    {
        Logger.getInstance().log(Logger.ERROR, "Syntax error on line " + line + ": " + message);
        System.exit(0);
    }
    
    private void missingOpeningBraceError(int line)
    {
        syntaxError(line, "Missing opening brace.");
    }
    
    private void missingClosingBraceError(int line)
    {
        syntaxError(line, "Missing closing brace.");
    }
    
    private void missingClosingBracketError(int line)
    {
        syntaxError(line, "Missing closing bracket.");
    }
    
    private void missingSemicolonError(int line)
    {
        syntaxError(line, "Missing semicolon.");
    }
    
    private void noArraysAllowedError(int line)
    {
        syntaxError(line, "Arrays are not allowed in objects.");
    }
    
    /**
     * Grammar: Asm{{@literal <}string{@literal >}<sup>*</sup>} → AST
     * 
     * @return 
     */
    private AbstractSyntaxTree parseInlineAsm()
    {
        i += 2;
        
        String str = "";
        int line = tokens.get(i).getLine();
        
        while(i < tokens.size())
        {
            if(tokens.get(i).getType() == TokenType.RBRACE)
            {
                break;
            }
            else if(tokens.get(i).getType() == TokenType.STRING)
            {
                str += tokens.get(i).getString() + "\n";
            }
            else
            {
                syntaxError(line, "Unexpected token in inline ASM block.");
            }
            
            i++;
        }
        
        return new AbstractSyntaxTree(TreeNodeType.INLINE_ASM, str, line);
    }
    
    /**
     * Grammar: {@literal <}data type{@literal >}{@literal [}{@literal <}array size{@literal >}{@literal ]}; → AST
     * 
     * @return 
     */
    private AbstractSyntaxTree parseArrayDeclaration()
    {
        AbstractSyntaxTree ast = new AbstractSyntaxTree(TreeNodeType.ARRAY_DECLARATION,
                tokens.get(i).getString() + "|" + tokens.get(i + 2).getString() + "|" + tokens.get(i + 4).getString(), tokens.get(i).getLine());
        i += 4;
        
        return ast;
    }
    
    /**
     * Grammar: {@literal <}array name{@literal >}{@literal [}{@literal <}index{@literal >}{@literal ]} → AST
     * 
     * @return 
     */
    private AbstractSyntaxTree parseArrayAccess()
    {
        AbstractSyntaxTree ast = new AbstractSyntaxTree(TreeNodeType.ARRAY_ACCESS, tokens.get(i).getString(), tokens.get(i).getLine());
        if(tokens.get(i + 2).getType() == TokenType.CONSTANT)
        {
            ast.addChild(new AbstractSyntaxTree(TreeNodeType.CONSTANT, tokens.get(i + 2).getString(), tokens.get(i + 2).getLine()));
        }
        else if(tokens.get(i + 2).getType() == TokenType.IDENTIFIER)
        {
            ast.addChild(new AbstractSyntaxTree(TreeNodeType.VARIABLE, tokens.get(i + 2).getString(), tokens.get(i + 2).getLine()));
        }
        else
        {
            syntaxError(tokens.get(i).getLine(), "Array indices can only be constants and variables. What the hell do you think you're doing?");
        }
        i += 3;
        
        return ast;
    }
    
    /**
     * Grammar: new {@literal <}class name{@literal >}(<b>[</b>{@literal <}function argument{@literal >}
     * <b>(</b>,{@literal <}function argument{@literal >}<b>)</b><sup>*</sup><b>]</b>) → AST
     * 
     * @return 
     */
    private AbstractSyntaxTree parseInstantiation()
    {
        AbstractSyntaxTree ast = new AbstractSyntaxTree(TreeNodeType.INSTANTIATION, tokens.get(i + 1).getString(), tokens.get(i + 1).getLine());
        i += 3;
        
        AbstractSyntaxTree args = new AbstractSyntaxTree(TreeNodeType.FUNC_ARGS, "", -1);
        ast.addChild(args);
        
        while(i < tokens.size())
        {
            if(tokens.get(i).getType() == TokenType.RPAREN)
            {
                break;
            }
            else if(tokens.get(i).getType() == TokenType.CONSTANT)
            {
                args.addChild(new AbstractSyntaxTree(TreeNodeType.CONSTANT, tokens.get(i).getString(), tokens.get(i).getLine()));
            }
            else if(tokens.get(i).getType() == TokenType.IDENTIFIER)
            {
                if(tokens.get(i + 1).getType() == TokenType.LPAREN)
                {
                    j = i;
                    args.addChild(parseFunctionCall(true));
                    i = j;
                }
                else if(tokens.get(i).getString().equals("Asm") && 
                        tokens.get(i + 1).getType() == TokenType.DOT && tokens.get(i + 2).getType() == TokenType.IDENTIFIER)
                {
                    AbstractSyntaxTree asm = new AbstractSyntaxTree(TreeNodeType.ASM_REFERENCE, tokens.get(i + 2).getString(), tokens.get(i).getLine());
                    i += 2;
                    args.addChild(asm);
                }
                else
                {
                    args.addChild(new AbstractSyntaxTree(TreeNodeType.VARIABLE, tokens.get(i).getString(), tokens.get(i).getLine()));
                }
            }
            //Object function call
            else if(tokens.get(i).getType() == TokenType.IDENTIFIER && tokens.get(i + 1).getType() == TokenType.DOT &&
                    tokens.get(i + 2).getType() == TokenType.IDENTIFIER && tokens.get(i + 3).getType() == TokenType.LPAREN)
            {
                j = i;
                ast.addChild(parseFunctionCall(false));
                i = j;
                i++;
                if(tokens.get(i).getType() != TokenType.SEMICOLON)
                {
                    missingSemicolonError(tokens.get(i - 1).getLine());
                }
            }
            else if(tokens.get(i).getType() == TokenType.STRING)
            {
                args.addChild(new AbstractSyntaxTree(TreeNodeType.STRING, tokens.get(i).getString(), tokens.get(i).getLine()));
            }
            
            i++;
        }
        
        return ast;
    }

    /**
     * Grammar: import {@literal <}string{@literal >}; → AST
     * 
     * @return 
     */
    private AbstractSyntaxTree parseImport()
    {
        AbstractSyntaxTree ast = new AbstractSyntaxTree(TreeNodeType.IMPORT, tokens.get(i + 1).getString(), tokens.get(i + 1).getLine());
        
        if(tokens.get(i + 2).getType() != TokenType.SEMICOLON)
        {
            missingSemicolonError(tokens.get(i + 1).getLine());
        }
        
        i += 2;
        
        return ast;
    }
    
    /**
     * Grammar: package {@literal <}string{@literal >}; → AST
     * 
     * @return 
     */
    private AbstractSyntaxTree parsePackageDeclaration()
    {
        AbstractSyntaxTree ast = new AbstractSyntaxTree(TreeNodeType.PACKAGE, tokens.get(i + 1).getString(), tokens.get(i + 1).getLine());
        
        if(tokens.get(i + 2).getType() != TokenType.SEMICOLON)
        {
            missingSemicolonError(tokens.get(i + 1).getLine());
        }
        
        i += 2;
        
        return ast;
    }
    
    /**
     * Grammar: {@literal <}data type{@literal >} {@literal <}variable name{@literal >}; → AST
     * 
     * @return 
     */
    private AbstractSyntaxTree parseDeclaration()
    {
        AbstractSyntaxTree ast = null;
        
        if(tokens.get(i).getString().equals("char") || tokens.get(i).getString().equals("int") || objectTypes.contains(tokens.get(i).getString()))
        {
            ast = new AbstractSyntaxTree(TreeNodeType.DECLARATION, tokens.get(i).getString() + "|" + tokens.get(i + 1).getString(),
                    tokens.get(i + 1).getLine());
        }
        else
        {
            syntaxError(tokens.get(i).getLine(), "Unknown type " + tokens.get(i).getString() + ".");
        }
        
        i += 2;
        
        return ast;
    }
    
    /**
     * Grammar: return {@literal <}variable or constant{@literal >} → AST
     * 
     * @return 
     */
    private AbstractSyntaxTree parseReturn()
    {
        AbstractSyntaxTree ast = new AbstractSyntaxTree(TreeNodeType.RETURN, "", tokens.get(i).getLine());
        i++;
        
        if(tokens.get(i).getType() == TokenType.IDENTIFIER)
        {
            if(tokens.get(i + 1).getType() == TokenType.SEMICOLON)
            {
                ast.addChild(new AbstractSyntaxTree(TreeNodeType.VARIABLE, tokens.get(i).getString(), tokens.get(i).getLine()));
                i++;
            }
            else if(tokens.get(i + 1).getType() == TokenType.LBRACKET && tokens.get(i + 3).getType() == TokenType.RBRACKET)
            {
                ast.addChild(parseArrayAccess());
            }
            else
            {
                missingSemicolonError(tokens.get(i).getLine());
            }
        }
        else if(tokens.get(i).getType() == TokenType.CONSTANT)
        {
            if(tokens.get(i + 1).getType() == TokenType.SEMICOLON)
            {
                ast.addChild(new AbstractSyntaxTree(TreeNodeType.CONSTANT, tokens.get(i).getString(), tokens.get(i).getLine()));
                i++;
            }
            else
            {
                missingSemicolonError(tokens.get(i).getLine());
            }
        }
        else
        {
            syntaxError(tokens.get(i).getLine(), "Return type not supported.");
        }
        
        return ast;
    }
    
    /**
     * Grammar: {@literal <}variable or constant{@literal >} {@literal <}operation{@literal >} {@literal <}variable or constant{@literal >} → AST
     * 
     * @param type Either -, +, /, * or %
     * @return 
     */
    private AbstractSyntaxTree parseMathsOperation(String type)
    {
        AbstractSyntaxTree ast = new AbstractSyntaxTree(TreeNodeType.OPERATION, type, -1);
        
        if(tokens.get(i).getType() == TokenType.IDENTIFIER)
        {
            ast.addChild(new AbstractSyntaxTree(TreeNodeType.VARIABLE, tokens.get(i).getString(), tokens.get(i).getLine()));
        }
        else if(tokens.get(i).getType() == TokenType.CONSTANT)
        {
            ast.addChild(new AbstractSyntaxTree(TreeNodeType.CONSTANT, tokens.get(i).getString(), tokens.get(i).getLine()));
        }
        //Array access
        else if(tokens.get(i).getType() == TokenType.RBRACKET && tokens.get(i - 2).getType() == TokenType.LBRACKET &&
                tokens.get(i - 3).getType() == TokenType.IDENTIFIER)
        {
            i -= 3;
            ast.addChild(parseArrayAccess());
        }
        
        if(tokens.get(i + 2).getType() == TokenType.IDENTIFIER)
        {
            //Array access
            if(tokens.get(i + 3).getType() == TokenType.LBRACKET && tokens.get(i + 5).getType() == TokenType.RBRACKET)
            {
                i += 2;
                ast.addChild(parseArrayAccess());
                i++;
                if(tokens.get(i).getType() != TokenType.SEMICOLON)
                {
                    syntaxError(tokens.get(i).getLine(), "Only 2 operands are allowed.");
                }
                return ast;
            }
            else
            {
                ast.addChild(new AbstractSyntaxTree(TreeNodeType.VARIABLE, tokens.get(i + 2).getString(), tokens.get(i + 2).getLine()));
            }
        }
        else if(tokens.get(i + 2).getType() == TokenType.CONSTANT)
        {
            ast.addChild(new AbstractSyntaxTree(TreeNodeType.CONSTANT, tokens.get(i + 2).getString(), tokens.get(i + 2).getLine()));
        }
        
        if(tokens.get(i + 3).getType() != TokenType.SEMICOLON)
        {
            syntaxError(tokens.get(i + 3).getLine(), "Only 2 operands are allowed.");
        }
            
        i += 2;
        
        return ast;
    }
    
    /**
     * Grammar: {@literal <}variable or array access{@literal >} = {@literal <}variable or constant or maths operation{@literal >} → AST
     * 
     * @param isArray Determines if the target of the assignment is a variable or an array access
     * @return 
     */
    //TODO: Add function calls here
    //TODO: Add function calls to assignments in codegen too
    private AbstractSyntaxTree parseAssignment(boolean isArray)
    {
        AbstractSyntaxTree ast;
        if(isArray)
        {
            ast = new AbstractSyntaxTree(TreeNodeType.ASSIGNMENT, "", -1);
            ast.addChild(parseArrayAccess());
        }
        else
        {
            ast = new AbstractSyntaxTree(TreeNodeType.ASSIGNMENT, "", -1);
            ast.addChild(new AbstractSyntaxTree(TreeNodeType.VARIABLE, tokens.get(i).getString(), tokens.get(i).getLine()));
            i+= 2;
        }
        
        while(i < tokens.size())
        {
            if(tokens.get(i + 1).getType() == TokenType.PLUS)
            {
                ast.addChild(parseMathsOperation("+"));
                break;
            }
            else if(tokens.get(i + 1).getType() == TokenType.MINUS)
            {
                ast.addChild(parseMathsOperation("-"));
                break;
            }
            else if(tokens.get(i + 1).getType() == TokenType.MULTIPLY)
            {
                ast.addChild(parseMathsOperation("*"));
                break;
            }
            else if(tokens.get(i + 1).getType() == TokenType.DIVIDE)
            {
                ast.addChild(parseMathsOperation("/"));
                break;
            }
            else if(tokens.get(i + 1).getType() == TokenType.MODULO)
            {
                ast.addChild(parseMathsOperation("%"));
                break;
            }
            else if(tokens.get(i).getType() == TokenType.CONSTANT)
            {
                ast.addChild(new AbstractSyntaxTree(TreeNodeType.CONSTANT, tokens.get(i).getString(), tokens.get(i).getLine()));
                break;
            }
            else if(tokens.get(i).getType() == TokenType.IDENTIFIER)
            {
                //Object instantiation
                if(tokens.get(i).getString().equals("new"))
                {
                    ast.addChild(parseInstantiation());
                }
                //Could be an array access
                else if(tokens.get(i + 1).getType() == TokenType.LBRACKET)
                {
                    if(tokens.get(i + 3).getType() == TokenType.RBRACKET)
                    {
                        if(tokens.get(i + 4).getType() == TokenType.SEMICOLON)
                        {
                            //Is an array access
                            ast.addChild(parseArrayAccess());
                            break;
                        }
                        else if(tokens.get(i + 4).getType() == TokenType.PLUS)
                        {
                            i += 3;
                            ast.addChild(parseMathsOperation("+"));
                            break;
                        }
                        else if(tokens.get(i + 4).getType() == TokenType.MINUS)
                        {
                            i += 3;
                            ast.addChild(parseMathsOperation("-"));
                            break;
                        }
                        else if(tokens.get(i + 4).getType() == TokenType.MULTIPLY)
                        {
                            i += 3;
                            ast.addChild(parseMathsOperation("*"));
                            break;
                        }
                        else if(tokens.get(i + 4).getType() == TokenType.DIVIDE)
                        {
                            i += 3;
                            ast.addChild(parseMathsOperation("/"));
                            break;
                        }
                        else if(tokens.get(i + 4).getType() == TokenType.MODULO)
                        {
                            i += 3;
                            ast.addChild(parseMathsOperation("%"));
                            break;
                        }
                        else
                        {
                            missingSemicolonError(tokens.get(i).getLine());
                        }
                    }
                    else
                    {
                        missingClosingBracketError(tokens.get(i).getLine());
                    }
                }
                //Could be a function call
                else if(tokens.get(i + 1).getType() == TokenType.LPAREN)
                {
                    //TODO
                }
                //It's a variable
                else
                {
                    ast.addChild(new AbstractSyntaxTree(TreeNodeType.VARIABLE, tokens.get(i).getString(), tokens.get(i).getLine()));
                }
            }
            else if(tokens.get(i).getType() == TokenType.SEMICOLON)
            {
                break;
            }
            i++;
        }
        
        return ast;
    }
    
    /**
     * Grammar: case {@literal <}constant{@literal >}: {{@literal <}statement sequence{@literal >}} → AST
     * 
     * @param isStatic  Determines if this is a section (static) or an object class
     * @return 
     */
    private AbstractSyntaxTree parseSwitchCase(boolean isStatic)
    {
        AbstractSyntaxTree ast = new AbstractSyntaxTree(TreeNodeType.CASE, tokens.get(i + 1).getString(), tokens.get(i + 1).getLine());
        i += 2;
        
        if(tokens.get(i).getType() != TokenType.COLON)
        {
            syntaxError(tokens.get(i).getLine(), "Missing colon for switch case.");
        }
        i++;
        if(tokens.get(i).getType() != TokenType.LBRACE)
        {
            missingOpeningBraceError(tokens.get(i).getLine());
        }
        i++;
        
        parseStatementSequence(ast, isStatic);
        
        return ast;
    }
    
    /**
     * Grammar: switch({@literal <}variable{@literal >}) {{@literal <}switch case{@literal >}<sup>*</sup>} → AST
     * 
     * @param isStatic Determines if this is a section (static) or an object class
     * @return 
     */
    private AbstractSyntaxTree parseSwitchStatement(boolean isStatic)
    {
        AbstractSyntaxTree ast = new AbstractSyntaxTree(TreeNodeType.SWITCH, "", -1);
        i += 2;
        
        if(tokens.get(i).getType() == TokenType.IDENTIFIER)
        {
            ast.addChild(new AbstractSyntaxTree(TreeNodeType.VARIABLE, tokens.get(i).getString(), tokens.get(i).getLine()));
            i++;
        }
        else
        {
            syntaxError(tokens.get(i).getLine(), "No variable provided for switch case.");
        }
        
        if(tokens.get(i).getType() != TokenType.RPAREN)
        {
            syntaxError(tokens.get(i).getLine(), "Missing closing parenthesis.");
        }
        i++;
        if(tokens.get(i).getType() != TokenType.LBRACE)
        {
            missingOpeningBraceError(tokens.get(i).getLine());
        }
        i++;
        
        while(i < tokens.size())
        {
            if(tokens.get(i).getType() == TokenType.RBRACE)
            {
                break;
            }
            else if(tokens.get(i).getType() == TokenType.IDENTIFIER && tokens.get(i).getString().equals("case") &&
                    tokens.get(i + 1).getType() == TokenType.CONSTANT)
            {
                ast.addChild(parseSwitchCase(isStatic));
            }
            else
            {
                syntaxError(tokens.get(i).getLine(), "Bad statement detected inside switch case.");
            }
            
            i++;
        }
        
        return ast;
    }
    
    /**
     * Parses statements from i to a right brace (})
     * 
     * Grammar: {@literal <}statement{@literal >}<sup>*</sup>} → AST
     * 
     * @param ast The AST where the statements are attached
     * @param isStatic Determines if this is a section (static) or an object class
     */
    private void parseStatementSequence(AbstractSyntaxTree ast, boolean isStatic)
    {
        while(i < tokens.size())
        {
            if(tokens.get(i).getType() == TokenType.IDENTIFIER && tokens.get(i).getString().equals("return"))
            {
                ast.addChild(parseReturn());
            }
            else if(tokens.get(i).getType() == TokenType.IDENTIFIER && tokens.get(i + 1).getType() == TokenType.IDENTIFIER)
            {
                if(tokens.get(i + 2).getType() == TokenType.SEMICOLON)
                {
                    ast.addChild(parseDeclaration());
                }
                else
                {
                    missingSemicolonError(tokens.get(i).getLine());
                }
            }
            else if(tokens.get(i).getType() == TokenType.IDENTIFIER && tokens.get(i + 1).getType() == TokenType.EQUALS)
            {
                ast.addChild(parseAssignment(false));
            }
            //Assignment to an element in an array
            else if(tokens.get(i).getType() == TokenType.IDENTIFIER && tokens.get(i + 1).getType() == TokenType.LBRACKET)
            {
                if(isStatic)
                {
                    if(tokens.get(i + 3).getType() == TokenType.RBRACKET)
                    {
                        if(tokens.get(i + 4).getType() == TokenType.EQUALS)
                        {
                            ast.addChild(parseAssignment(true));
                        }
                    }
                    else
                    {
                        missingClosingBracketError(tokens.get(i).getLine());
                    }
                }
                else
                {
                    noArraysAllowedError(tokens.get(i).getLine());
                }
            }
            //Throw error on assignment to constant
            else if(tokens.get(i).getType() == TokenType.CONSTANT && tokens.get(i + 1).getType() == TokenType.EQUALS)
            {
                syntaxError(tokens.get(i).getLine(), "Assignment to a constant. Are you drunk or what?");
            }
            //Function call or if/else chain
            else if(tokens.get(i).getType() == TokenType.IDENTIFIER && tokens.get(i + 1).getType() == TokenType.LPAREN)
            {
                if(tokens.get(i).getString().equals("if"))
                {
                    ast.addChild(parseIfElseChain(isStatic));
                }
                else if(tokens.get(i).getString().equals("while"))
                {
                    ast.addChild(parseWhileLoop(isStatic));
                }
                else if(tokens.get(i).getString().equals("switch"))
                {
                    ast.addChild(parseSwitchStatement(isStatic));
                }
                else
                {
                    j = i;
                    ast.addChild(parseFunctionCall(true));
                    i = j;
                    i++;
                    if(tokens.get(i).getType() != TokenType.SEMICOLON)
                    {
                        missingSemicolonError(tokens.get(i - 1).getLine());
                    }
                }
            }
            //Object function call
            else if(tokens.get(i).getType() == TokenType.IDENTIFIER && tokens.get(i + 1).getType() == TokenType.DOT &&
                    tokens.get(i + 2).getType() == TokenType.IDENTIFIER && tokens.get(i + 3).getType() == TokenType.LPAREN)
            {
                if(tokens.get(i).getString().equals("this"))
                {
                    if(isStatic)
                    {
                        syntaxError(tokens.get(i).getLine(), "\"this\" may only be used in object classes.");
                    }
                }
                j = i;
                ast.addChild(parseFunctionCall(false));
                i = j;
                i++;
                if(tokens.get(i).getType() != TokenType.SEMICOLON)
                {
                    missingSemicolonError(tokens.get(i - 1).getLine());
                }
            }
            else if(tokens.get(i).getType() == TokenType.RBRACE)
            {
                break;
            }
            i++;
        }
    }
    
    /**
     * Grammar: {@literal <}cvaf{@literal >} {@literal <}condition{@literal >} {@literal <}cvaf{@literal >} → AST<br>
     * {@literal <} | {@literal <}= | == | {@literal >} | {@literal >}= → {@literal <}condition{@literal >}<br>
     * {@literal <}constant{@literal >} | {@literal <}variable{@literal >} | {@literal <}array access{@literal >} | {@literal <}function call{@literal >}
     * → {@literal <}cvaf{@literal >}
     * 
     * @param startIndex The index where the element before the condition begins
     * @return 
     */
    private AbstractSyntaxTree parseCondition(int startIndex)
    {
        int iOld = i;
        
        AbstractSyntaxTree ast = null;
        
        if(tokens.get(i).getType() == TokenType.SMALLER)
        {
            // <=
            if(tokens.get(i + 1).getType() == TokenType.EQUALS)
            {
                ast = new AbstractSyntaxTree(TreeNodeType.CONDITION, "<=", tokens.get(i).getLine());
                i += 2;
            }
            // <
            else
            {
                ast = new AbstractSyntaxTree(TreeNodeType.CONDITION, "<", tokens.get(i).getLine());
                i++;
            }
        }
        else if(tokens.get(i).getType() == TokenType.EQUALS)
        {
            // ==
            if(tokens.get(i + 1).getType() == TokenType.EQUALS)
            {
                ast = new AbstractSyntaxTree(TreeNodeType.CONDITION, "==", tokens.get(i).getLine());
                i += 2;
            }
            //Syntax error
            else
            {
                syntaxError(tokens.get(i).getLine(), "\"=\" found. Did you mean \"==\"?");
            }
        }
        else if(tokens.get(i).getType() == TokenType.BIGGER)
        {
            // >=
            if(tokens.get(i + 1).getType() == TokenType.EQUALS)
            {
                ast = new AbstractSyntaxTree(TreeNodeType.CONDITION, ">=", tokens.get(i).getLine());
                i += 2;
            }
            // >
            else
            {
                ast = new AbstractSyntaxTree(TreeNodeType.CONDITION, ">", tokens.get(i).getLine());
                i++;
            }
        }
        else if(tokens.get(i).getType() == TokenType.NOT)
        {
            // !=
            if(tokens.get(i + 1).getType() == TokenType.EQUALS)
            {
                ast = new AbstractSyntaxTree(TreeNodeType.CONDITION, "!=", tokens.get(i).getLine());
                i += 2;
            }
            //Syntax error
            else
            {
                syntaxError(tokens.get(i).getLine(), "\"!\" found. Did you mean \"!=\"?");
            }
        }
        
        //Parse stuff before condition
        j = startIndex;
        while(j < iOld)
        {
            if(tokens.get(j).getType() == TokenType.CONSTANT)
            {
                ast.addChild(new AbstractSyntaxTree(TreeNodeType.CONSTANT, tokens.get(j).getString(), tokens.get(j).getLine()));
            }
            //Array access
            else if(tokens.get(j).getType() == TokenType.IDENTIFIER && tokens.get(j + 1).getType() == TokenType.LBRACKET)
            {
                if(tokens.get(j + 3).getType() == TokenType.RBRACKET)
                {
                    int temp = i;
                    i = j;
                    ast.addChild(parseArrayAccess());
                    j = i;
                    i = temp;
                }
                else
                {
                    missingClosingBracketError(tokens.get(j).getLine());
                }
            }
            else if(tokens.get(j).getType() == TokenType.IDENTIFIER && tokens.get(j + 1).getType() == TokenType.DOT &&
                    tokens.get(j + 2).getType() == TokenType.IDENTIFIER && tokens.get(j + 3).getType() == TokenType.LPAREN)
            {
                ast.addChild(parseFunctionCall(false));
            }
            else if(tokens.get(j).getType() == TokenType.IDENTIFIER)
            {
                //Function call
                if(tokens.get(j + 1).getType() == TokenType.LPAREN)
                {
                    ast.addChild(parseFunctionCall(true));
                }
                //Variable
                else
                {
                    ast.addChild(new AbstractSyntaxTree(TreeNodeType.VARIABLE, tokens.get(j).getString(), tokens.get(j).getLine()));
                }
            }
            
            j++;
        }
        
        //Parse stuff after condition
        j = i;
        while(j < tokens.size())
        {
            if(tokens.get(j).getType() == TokenType.RPAREN)
            {
                break;
            }
            //Array access
            else if(tokens.get(j).getType() == TokenType.IDENTIFIER && tokens.get(j + 1).getType() == TokenType.LBRACKET)
            {
                if(tokens.get(j + 3).getType() == TokenType.RBRACKET)
                {
                    int temp = i;
                    i = j;
                    ast.addChild(parseArrayAccess());
                    j = i;
                    i = temp;
                }
                else
                {
                    missingClosingBracketError(tokens.get(j).getLine());
                }
            }
            else if(tokens.get(j).getType() == TokenType.CONSTANT)
            {
                ast.addChild(new AbstractSyntaxTree(TreeNodeType.CONSTANT, tokens.get(j).getString(), tokens.get(j).getLine()));
            }
            else if(tokens.get(j).getType() == TokenType.IDENTIFIER && tokens.get(j + 1).getType() == TokenType.DOT &&
                    tokens.get(j + 2).getType() == TokenType.IDENTIFIER && tokens.get(j + 3).getType() == TokenType.LPAREN)
            {
                ast.addChild(parseFunctionCall(false));
            }
            else if(tokens.get(j).getType() == TokenType.IDENTIFIER)
            {
                //Function call
                if(tokens.get(j + 1).getType() == TokenType.LPAREN)
                {
                    ast.addChild(parseFunctionCall(true));
                }
                //Variable
                else
                {
                    ast.addChild(new AbstractSyntaxTree(TreeNodeType.VARIABLE, tokens.get(j).getString(), tokens.get(j).getLine()));
                }
            }
            
            j++;
        }
        
        i = j - 1; //-1 because we get an off-by-one otherwise
        
        return ast;
    }
    
    /**
     * Grammar: if({@literal <}condition{@literal >}) {{@literal <}statement sequence{@literal >}}
     * <sup>(</sup>else if {{@literal <}statement sequence{@literal >}}<sup>)*</sup>
     * <sup>[</sup>else {{@literal <}statement sequence{@literal >}}<sup>]</sup> → AST
     * 
     * @param isStatic Determines if this is a section (static) or an object class
     * @return 
     */
    private AbstractSyntaxTree parseIfElseChain(boolean isStatic)
    {
        AbstractSyntaxTree ast = new AbstractSyntaxTree(TreeNodeType.IF, "", -1);
        i += 2;
        
        int startIndex = i;
        
        //Parse Condition
        while(i < tokens.size())
        {
            //Find condition, it'll take care of the rest (including the closing parentheses)
            if(tokens.get(i).getType() == TokenType.SMALLER || tokens.get(i).getType() == TokenType.EQUALS || tokens.get(i).getType() == TokenType.BIGGER ||
                    tokens.get(i).getType() == TokenType.NOT)
            {
                ast.addChild(parseCondition(startIndex));
                break;
            }
            
            i++;
        }
        
        //Parse body
        AbstractSyntaxTree body = new AbstractSyntaxTree(TreeNodeType.BODY, "", -1);
        ast.addChild(body);
        parseStatementSequence(body, isStatic);
        
        //Do we have an else if/else?
        if(tokens.get(i + 1).getType() == TokenType.IDENTIFIER && tokens.get(i + 1).getString().equals("else"))
        {
            i += 2;
            //We have an else if
            if(tokens.get(i).getType() == TokenType.IDENTIFIER && tokens.get(i).getString().equals("if"))
            {
                ast.addChild(parseIfElseChain(isStatic));
            }
            //We have an else block
            else
            {
                AbstractSyntaxTree astElse = new AbstractSyntaxTree(TreeNodeType.ELSE, "", -1);
                parseStatementSequence(astElse, isStatic);
                ast.addChild(astElse);
            }
        }
        
        return ast;
    }
    
    /**
     * Grammar: while({@literal <}condition{@literal >}) {{@literal <}statement sequence{@literal >}} → AST
     * 
     * @param isStatic Determines if this is a section (static) or an object class
     * @return 
     */
    private AbstractSyntaxTree parseWhileLoop(boolean isStatic)
    {
        AbstractSyntaxTree ast = new AbstractSyntaxTree(TreeNodeType.WHILE, "", -1);
        i += 2;
        
        int startIndex = i;
        
        //Parse condition
        while(i < tokens.size())
        {
            //Find condition, it'll take care of the rest (including the closing parentheses)
            if(tokens.get(i).getType() == TokenType.SMALLER || tokens.get(i).getType() == TokenType.EQUALS || tokens.get(i).getType() == TokenType.BIGGER ||
                    tokens.get(i).getType() == TokenType.NOT)
            {
                ast.addChild(parseCondition(startIndex));
                break;
            }
            
            i++;
        }
        
        i += 2;
        if(tokens.get(i).getType() != TokenType.LBRACE)
        {
            missingOpeningBraceError(tokens.get(i - 1).getLine());
        }
        
        AbstractSyntaxTree body = new AbstractSyntaxTree(TreeNodeType.BODY, "", -1);
        parseStatementSequence(body, isStatic);
        ast.addChild(body);
        
        return ast;
    }
    
    /**
     * Grammar: {@literal <}void, char or int{@literal >} {@literal <}function name{@literal >}(<b>[</b>{@literal <}function argument{@literal >}
     * <b>(</b>,{@literal <}function argument{@literal >}<b>)</b><sup>*</sup><b>]</b>) {{@literal <}statement sequence{@literal >}} → AST
     * 
     * @param isStatic Determines if this is a section (static) or an object class
     * @return 
     */
    private AbstractSyntaxTree parseFunction(boolean isStatic)
    {
        AbstractSyntaxTree ast = new AbstractSyntaxTree(TreeNodeType.FUNCTION, tokens.get(i + 1).getString(), tokens.get(i + 1).getLine());
        i += 3;
        
        //Parse arguments
        while(i < tokens.size())
        {
            if(tokens.get(i).getType() == TokenType.IDENTIFIER && tokens.get(i + 1).getType() == TokenType.IDENTIFIER)
            {
                if(tokens.get(i + 2).getType() == TokenType.RPAREN)
                {
                    ast.addChild(new AbstractSyntaxTree(TreeNodeType.FUNC_ARG, tokens.get(i).getString() + "|" + tokens.get(i + 1).getString(),
                            tokens.get(i + 1).getLine()));
                    i += 3;
                    break;
                }
                else if(tokens.get(i + 2).getType() == TokenType.COMMA)
                {
                    ast.addChild(new AbstractSyntaxTree(TreeNodeType.FUNC_ARG, tokens.get(i).getString() + "|" + tokens.get(i + 1).getString(),
                            tokens.get(i + 1).getLine()));
                    i += 2; //Only +2 since we aren't breaking and there's an i++ below which makes it 3 "consumed" tokens
                }
            }
            //When we have no arguments, this is triggered
            else if(tokens.get(i).getType() == TokenType.RPAREN)
            {
                i++;
                break;
            }
            
            i++;
        }
        
        if(tokens.get(i).getType() != TokenType.LBRACE)
        {
            missingOpeningBraceError(tokens.get(i - 1).getLine());
        }
        
        //Parse the function body
        parseStatementSequence(ast, isStatic);
        
        return ast;
    }
    
    //TODO: Check if argument type is correct!
    /**
     * Grammar: {@literal <}function name{@literal >}(<b>[</b>{@literal <}function argument{@literal >}
     * <b>(</b>,{@literal <}function argument{@literal >}<b>)</b><sup>*</sup><b>]</b>); → AST
     * 
     * @param isStatic Determines if this is a section (static) or an object class
     * @return 
     */
    private AbstractSyntaxTree parseFunctionCall(boolean isStatic)
    {
        AbstractSyntaxTree ast;
        if(isStatic)
        {
            ast = new AbstractSyntaxTree(TreeNodeType.CALL, tokens.get(j).getString(), tokens.get(j).getLine());
            j += 2;
        }
        else
        {
            ast = new AbstractSyntaxTree(TreeNodeType.OBJECT_CALL , tokens.get(j).getString() + "|" + tokens.get(j + 2).getString(), tokens.get(j + 2).getLine());
            j += 4;
        }
        
        AbstractSyntaxTree args = new AbstractSyntaxTree(TreeNodeType.FUNC_ARGS, "", -1);
        ast.addChild(args);
        
        while(j < tokens.size())
        {
            if(tokens.get(j).getType() == TokenType.RPAREN)
            {
                break;
            }
            else if(tokens.get(j).getType() == TokenType.CONSTANT)
            {
                args.addChild(new AbstractSyntaxTree(TreeNodeType.CONSTANT, tokens.get(j).getString(), tokens.get(j).getLine()));
            }
            //Object function call
            else if(tokens.get(j).getType() == TokenType.IDENTIFIER && tokens.get(j + 1).getType() == TokenType.DOT &&
                    tokens.get(j + 2).getType() == TokenType.IDENTIFIER && tokens.get(j + 3).getType() == TokenType.LPAREN)
            {
                args.addChild(parseFunctionCall(false));
            }
            else if(tokens.get(j).getType() == TokenType.IDENTIFIER)
            {
                if(tokens.get(j + 1).getType() == TokenType.LPAREN)
                {
                    args.addChild(parseFunctionCall(true));
                }
                else if(tokens.get(j).getString().equals("Asm") && 
                        tokens.get(j + 1).getType() == TokenType.DOT && tokens.get(j + 2).getType() == TokenType.IDENTIFIER)
                {
                    AbstractSyntaxTree asm = new AbstractSyntaxTree(TreeNodeType.ASM_REFERENCE, tokens.get(j + 2).getString(), tokens.get(j).getLine());
                    args.addChild(asm);
                    j += 2;
                }
                //Could be an array access
                else if(tokens.get(j + 1).getType() == TokenType.LBRACKET)
                {
                    if(tokens.get(j + 3).getType() == TokenType.RBRACKET)
                    {
                        int temp = i;
                        i = j;
                        //Is an array access
                        args.addChild(parseArrayAccess());
                        j = i;
                        i = temp;
                    }
                    else
                    {
                        missingClosingBracketError(tokens.get(i).getLine());
                    }
                }
                else
                {
                    args.addChild(new AbstractSyntaxTree(TreeNodeType.VARIABLE, tokens.get(j).getString(), tokens.get(j).getLine()));
                }
            }
            else if(tokens.get(j).getType() == TokenType.STRING)
            {
                args.addChild(new AbstractSyntaxTree(TreeNodeType.STRING, tokens.get(j).getString(), tokens.get(j).getLine()));
            }
            
            j++;
            
            //Sort of a hack, but it works
            if(tokens.get(j).getType() == TokenType.RPAREN)
            {
                break;
            }
            
            if(tokens.get(j).getType() != TokenType.COMMA)
            {
                syntaxError(tokens.get(j).getLine(), "Missing comma between two function arguments. Please add one. :)");
            }
            
            j++;
        }
        
        return ast;
    }
    
    /**
     * Grammar: {@literal <}class name{@literal >}(<b>[</b>{@literal <}function argument{@literal >}
     * <b>(</b>,{@literal <}function argument{@literal >}<b>)</b><sup>*</sup><b>]</b>) {{@literal <}statement sequence{@literal >}} → AST<br>
     * 
     * @param className The name of the class this constructor is in
     * @return 
     */
    private AbstractSyntaxTree parseConstructor(String className)
    {
        if(!className.equals(tokens.get(i).getString()))
        {
            syntaxError(tokens.get(i).getLine(), "Constructor has incorrect name.");
        }
        
        AbstractSyntaxTree ast = new AbstractSyntaxTree(TreeNodeType.CONSTRUCTOR, "", tokens.get(i).getLine());
        i += 2;
        
        //Parse arguments
        while(i < tokens.size())
        {
            if(tokens.get(i).getType() == TokenType.IDENTIFIER && tokens.get(i + 1).getType() == TokenType.IDENTIFIER)
            {
                if(tokens.get(i + 2).getType() == TokenType.RPAREN)
                {
                    ast.addChild(new AbstractSyntaxTree(TreeNodeType.FUNC_ARG, tokens.get(i).getString() + "|" + tokens.get(i + 1).getString(),
                            tokens.get(i + 1).getLine()));
                    i += 3;
                    break;
                }
                else if(tokens.get(i + 2).getType() == TokenType.COMMA)
                {
                    ast.addChild(new AbstractSyntaxTree(TreeNodeType.FUNC_ARG, tokens.get(i).getString() + "|" + tokens.get(i + 1).getString(),
                            tokens.get(i + 1).getLine()));
                    i += 2; //Only +2 since we aren't breaking and there's an i++ below which makes it 3 "consumed" tokens
                }
            }
            //When we have no arguments, this is triggered
            else if(tokens.get(i).getType() == TokenType.RPAREN)
            {
                i++;
                break;
            }
            
            i++;
        }
        
        if(tokens.get(i).getType() != TokenType.LBRACE)
        {
            missingOpeningBraceError(tokens.get(i - 1).getLine());
        }
        
        //Parse the function body
        parseStatementSequence(ast, false);
        
        return ast;
    }
    
    /**
     * Parses a section
     * 
     * Grammar: section {@literal <}name{@literal >} {{@literal <}fdapi{@literal >}<sup>*</sup>} → AST<br>
     * {@literal <}function{@literal >} | {@literal <}variable declaration{@literal >} | {@literal <}array declaration{@literal >} |
     * {@literal <}package declaration{@literal >} | {@literal <}inline assembly{@literal >} → {@literal <}fdapi{@literal >}
     * 
     * @return 
     */
    private AbstractSyntaxTree parseStaticClass()
    {
        AbstractSyntaxTree ast = new AbstractSyntaxTree(TreeNodeType.SECTION, tokens.get(i + 1).getString(), tokens.get(i + 1).getLine());
        i += 3;

        while(i < tokens.size())
        {
            //This is either a function or a variable declaration
            if(tokens.get(i).getType() == TokenType.IDENTIFIER && tokens.get(i + 1).getType() == TokenType.IDENTIFIER)
            {
                //It's a function
                if(tokens.get(i + 2).getType() == TokenType.LPAREN)
                {
                    ast.addChild(parseFunction(true));
                    //i has been incremented by the parse call
                    if(tokens.get(i).getType() != TokenType.RBRACE)
                    {
                        missingClosingBraceError(tokens.get(i).getLine());
                    }
                }
                else
                {
                    if(tokens.get(i + 2).getType() == TokenType.SEMICOLON)
                    {
                        ast.addChild(parseDeclaration());
                    }
                    else
                    {
                        missingSemicolonError(tokens.get(i).getLine());
                    }
                }
            }
            //Array declaration
            else if(tokens.get(i).getType() == TokenType.IDENTIFIER && tokens.get(i + 1).getType() == TokenType.LBRACKET &&
                    tokens.get(i + 2).getType() == TokenType.CONSTANT && tokens.get(i + 3).getType() == TokenType.RBRACKET &&
                    tokens.get(i + 4).getType() == TokenType.IDENTIFIER)
            {
                if(tokens.get(i + 5).getType() == TokenType.SEMICOLON)
                {
                    ast.addChild(parseArrayDeclaration());
                }
                else
                {
                    missingSemicolonError(tokens.get(i).getLine());
                }
            }
            //Package declaration
            else if(tokens.get(i).getType() == TokenType.IDENTIFIER && tokens.get(i + 1).getType() == TokenType.STRING)
            {
                ast.addChild(parsePackageDeclaration());
            }
            else if(tokens.get(i).getType() == TokenType.IDENTIFIER && tokens.get(i).getString().equals("Asm") &&
                    tokens.get(i + 1).getType() == TokenType.LBRACE)
            {
                ast.addChild(parseInlineAsm());
            }
            //End of class reached
            else if(tokens.get(i).getType() == TokenType.RBRACE)
            {
                break;
            }

            i++;
        }
        
        return ast;
    }
    
    /**
     * Parses an (object) class
     * 
     * Grammar: section {@literal <}name{@literal >} {{@literal <}fdcpi{@literal >}<sup>*</sup>} → AST<br>
     * {@literal <}function{@literal >} | {@literal <}variable declaration{@literal >} | {@literal <}constructor{@literal >} |
     * {@literal <}package declaration{@literal >} | {@literal <}inline assembly{@literal >} → {@literal <}fdapi{@literal >}
     * 
     * @return 
     */
    private AbstractSyntaxTree parseClass()
    {
        String className = tokens.get(i + 1).getString();
        
        AbstractSyntaxTree ast = new AbstractSyntaxTree(TreeNodeType.CLASS, tokens.get(i + 1).getString(), tokens.get(i + 1).getLine());
        i += 3;
        
        while(i < tokens.size())
        {
            //This is either a function or a variable declaration
            if(tokens.get(i).getType() == TokenType.IDENTIFIER && tokens.get(i + 1).getType() == TokenType.IDENTIFIER)
            {
                //It's a function
                if(tokens.get(i + 2).getType() == TokenType.LPAREN)
                {
                    ast.addChild(parseFunction(false));
                    //i has been incremented by the parse call
                    if(tokens.get(i).getType() != TokenType.RBRACE)
                    {
                        missingClosingBraceError(tokens.get(i).getLine());
                    }
                }
                else
                {
                    if(tokens.get(i + 2).getType() == TokenType.SEMICOLON)
                    {
                        ast.addChild(parseDeclaration());
                    }
                    else
                    {
                        missingSemicolonError(tokens.get(i).getLine());
                    }
                }
            }
            //Array declaration - but we don't allow arrays in objects!
            else if(tokens.get(i).getType() == TokenType.IDENTIFIER && tokens.get(i + 1).getType() == TokenType.LBRACKET &&
                    tokens.get(i + 2).getType() == TokenType.CONSTANT && tokens.get(i + 3).getType() == TokenType.RBRACKET &&
                    tokens.get(i + 4).getType() == TokenType.IDENTIFIER)
            {
//                if(tokens.get(i + 5).getType() == TokenType.SEMICOLON)
//                {
//                    ast.addChild(parseArrayDeclaration());
//                }
//                else
//                {
//                    missingSemicolonError(tokens.get(i).getLine());
//                }
                noArraysAllowedError(tokens.get(i).getLine());
            }
            //Package declaration
            else if(tokens.get(i).getType() == TokenType.IDENTIFIER && tokens.get(i + 1).getType() == TokenType.STRING)
            {
                ast.addChild(parsePackageDeclaration());
            }
            //Constructor
            else if(tokens.get(i).getType() == TokenType.IDENTIFIER && tokens.get(i + 1).getType() == TokenType.LPAREN)
            {
                ast.addChild(parseConstructor(className));
            }
            else if(tokens.get(i).getType() == TokenType.IDENTIFIER && tokens.get(i).getString().equals("Asm") &&
                    tokens.get(i + 1).getType() == TokenType.LBRACE)
            {
                ast.addChild(parseInlineAsm());
            }
            //End of class reached
            else if(tokens.get(i).getType() == TokenType.RBRACE)
            {
                break;
            }

            i++;
        }
        
        return ast;
    }
    
    /**
     * Parses an entire program (token list was supplied in constructor)
     * 
     * @return The AST of the program
     */
    public AbstractSyntaxTree parse()
    {
        AbstractSyntaxTree ast = new AbstractSyntaxTree(TreeNodeType.PROGRAM, "", -1);
        
        while(i < tokens.size())
        {
            if(tokens.get(i).getType() == TokenType.IDENTIFIER && tokens.get(i).getString().equals("section"))
            {
                if(tokens.get(i + 2).getType() == TokenType.LBRACE)
                {
                    ast.addChild(parseStaticClass());
                    
                    //i has been incremented by the parse call
                    if(tokens.get(i).getType() != TokenType.RBRACE)
                    {
                        missingClosingBraceError(tokens.get(i).getLine());
                    }
                }
                else
                {
                    missingOpeningBraceError(tokens.get(i).getLine());
                }
            }
            else if(tokens.get(i).getType() == TokenType.IDENTIFIER && tokens.get(i).getString().equals("class"))
            {
                if(tokens.get(i + 2).getType() == TokenType.LBRACE)
                {
                    ast.addChild(parseClass());
                    
                    //i has been incremented by the parse call
                    if(tokens.get(i).getType() != TokenType.RBRACE)
                    {
                        missingClosingBraceError(tokens.get(i).getLine());
                    }
                }
                else
                {
                    missingOpeningBraceError(tokens.get(i).getLine());
                }
                
                //Object usage warning
                if(objectWarn)
                {
                    Logger.getInstance().log(Logger.WARNING, "Object class definition detected. Object classes have greatly reduced performance compared to static"
                            + " sections, only use them if necessary. This warning can be disabled with the option -noObjWarn");
                }
            }
            else if(tokens.get(i).getType() == TokenType.IDENTIFIER && tokens.get(i + 1).getType() == TokenType.STRING)
            {
                if(tokens.get(i).getString().equals("import"))
                {
                    ast.addChild(parseImport());
                }
            }
            
            i++;
        }
        
        return ast;
    }
}