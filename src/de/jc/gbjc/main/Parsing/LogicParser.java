/**
 * The GB-J Compiler
 * Copyright (C) 2019 robbi-blechdose
 * Licensed under GNU AGPLv3
 * (See LICENSE.txt for full license)
 */
package de.jc.gbjc.main.Parsing;

import de.jc.gbjc.main.Parsing.AbstractSyntaxTree.TreeNodeType;
import de.jc.gbjc.main.Token;
import de.jc.gbjc.main.Token.TokenType;
import java.util.List;

/**
 *
 * @author robbi-blechdose
 * 
 */
public class LogicParser
{
    private List<Token> tokens;
    private int i;
    private int endIndex;
    
    public LogicParser(List<Token> tokens)
    {
        this.tokens = tokens;
    }
    
    public AbstractSyntaxTree parseCondition(int startIndex, int endIndex)
    {
        this.endIndex = endIndex;
        AbstractSyntaxTree ast = new AbstractSyntaxTree(TreeNodeType.CONDITION, "", -1);
        
        i = startIndex;
        while(i <= endIndex)
        {
            if(tokens.get(i).getType() == TokenType.LPAREN)
            {
                ast.addChild(parseLogicalExpression());
            }
        }
        
        return ast;
    }
    
    public AbstractSyntaxTree parseLogicalExpression()
    {
        AbstractSyntaxTree ast = null;
        
        while(i <= endIndex)
        {
            if(tokens.get(i).getType() == TokenType.LPAREN)
            {
                ast.addChild(parseLogicalExpression());
            }
            else if(tokens.get(i).getType() == TokenType.IDENTIFIER)
            {
                
            }
        }
        
        return ast;
    }
}