/**
 * The GB-J Compiler
 * Copyright (C) 2019 robbi-blechdose
 * Licensed under GNU AGPLv3
 * (See LICENSE.txt for full license)
 */
package de.jc.gbjc.main.Parsing;

import de.jc.gbjc.main.Parsing.AbstractSyntaxTree.TreeNodeType;
import de.jc.gbjc.main.Token;
import java.util.List;

/**
 * Parses mathematical expressions
 *
 * @author robbi-blechdose
 *
 */
public class MathsParser
{
    private List<Token> tokens;
    private int i;
    
    public MathsParser(List<Token> tokens)
    {
        this.tokens = tokens;
    }
    
    public AbstractSyntaxTree parseMathsOperation(int startIndex, boolean isControlStructure)
    {
        AbstractSyntaxTree ast = new AbstractSyntaxTree(TreeNodeType.OPERATION, "", -1);
        
        i = startIndex;
        while(i < tokens.size())
        {
            
        }
        
        return ast;
    }
}