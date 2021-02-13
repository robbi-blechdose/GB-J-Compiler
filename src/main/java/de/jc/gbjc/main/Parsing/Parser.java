/**
 * The GB-J Compiler
 * Copyright (C) 2019 - 2020 robbi-blechdose
 * Licensed under GNU AGPLv3
 * (See LICENSE.txt for full license)
 */
package de.jc.gbjc.main.Parsing;

import de.jc.gbjc.main.Parsing.AbstractSyntaxTree.ASTType;
import de.jc.gbjc.main.Lexing.Token;
import de.jc.gbjc.main.Lexing.Token.TokenType;
import de.jc.gbjc.main.Util.CompilerOutput;
import java.util.List;

/**
 *
 * @author robbi-blechdose
 *
 */
public class Parser
{
    private ParsingData pd;
    
    //Sub-Parsers
    
    public Parser(List<Token> tokens, List<String> objectTypes, boolean objectWarn)
    {
        pd = new ParsingData(tokens, objectTypes, objectWarn);
    }
    
    protected AbstractSyntaxTree tryParseInclude()
    {
        //include "XXXX"
        if(pd.tokens.get(pd.i).getType() == TokenType.K_INCLUDE &&
                pd.tokens.get(pd.i + 1).getType() == TokenType.STRING)
        {
            //;
            if(pd.tokens.get(pd.i + 2).getType() == TokenType.SEMICOLON)
            {
                AbstractSyntaxTree ast = new AbstractSyntaxTree(ASTType.INCLUDE, pd.tokens.get(pd.i + 1).getString(), pd.tokens.get(pd.i + 1).getLine());
                pd.i += 3; //Consume tokens
                return ast;
            }
            else
            {
                CompilerOutput.syntaxError(CompilerOutput.CompilerOutputType.MISSING_SEMICOLON, pd.tokens.get(pd.i + 1).getLine());
            }
        }
        
        return null;
    }
    
    protected AbstractSyntaxTree tryParseDeclaration()
    {
        //type (char/int/ptr)
        TokenType type = pd.tokens.get(pd.i).getType();
        
        if(type == TokenType.K_CHAR || type == TokenType.K_INT || type == TokenType.K_PTR ||
                (type == TokenType.IDENTIFIER && pd.objectTypes.contains(pd.tokens.get(pd.i).getString())))
        {
            //name
            if(pd.tokens.get(pd.i + 1).getType() == TokenType.IDENTIFIER)
            {
                //;
                if(pd.tokens.get(pd.i + 2).getType() == TokenType.SEMICOLON)
                {
                    AbstractSyntaxTree ast = new AbstractSyntaxTree(ASTType.DECLARATION, new ASTCDeclaration(type, pd.tokens.get(pd.i + 1).getString()), pd.tokens.get(pd.i).getLine());
                    pd.i += 3; //Consume tokens
                    return ast;
                }
                else
                {
                    CompilerOutput.syntaxError(CompilerOutput.CompilerOutputType.MISSING_SEMICOLON, pd.tokens.get(pd.i + 1).getLine());
                }
            }
        }
        
        return null;
    }
    
    private AbstractSyntaxTree tryParseFunction()
    {
        return null;
    }
    
    private AbstractSyntaxTree tryParseSection()
    {
        //section XXXX {
        if(pd.tokens.get(pd.i).getType() == TokenType.K_SECTION &&
                pd.tokens.get(pd.i + 1).getType() == TokenType.IDENTIFIER &&
                pd.tokens.get(pd.i + 2).getType() == TokenType.LBRACE)
        {
            AbstractSyntaxTree ast = new AbstractSyntaxTree(ASTType.SECTION);
            pd.i += 3; //Consume tokens
            
            while(pd.i < pd.tokens.size())
            {
                //}
                if(pd.tokens.get(pd.i).getType() == TokenType.RBRACE)
                {
                    pd.i++;
                    return ast;
                }
                else if(!(ast.addChild(tryParseInclude()) || ast.addChild(tryParseDeclaration()) || ast.addChild(tryParseFunction())))
                {
                    //Illegal token found
                    //TODO
                }
            }
        }
        
        return null;
    }
    
    public AbstractSyntaxTree parse()
    {
        AbstractSyntaxTree ast = new AbstractSyntaxTree(ASTType.PROGRAM);
        
        while(pd.i < pd.tokens.size())
        {
            ast.addChild(tryParseSection());
        }
        
        return ast;
    }
}