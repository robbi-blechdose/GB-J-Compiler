/**
 * The GB-J Compiler
 * Copyright (C) 2019 - 2021 robbi-blechdose
 * Licensed under GNU AGPLv3
 * (See LICENSE.txt for full license)
 */
package de.jc.gbjc.main.Parsing;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author robbi-blechdose
 *
 */
public class AbstractSyntaxTree
{
    public static enum ASTType
    {
        PROGRAM,
        SECTION,
        INCLUDE, DECLARATION, FUNCTION,
        TYPE, HEADER, BLOCK,
        IF, ELSEIF, ELSE, WHILE,
        ASSIGNMENT, EXPRESSION,
        ADD, SUB, MUL, DIV, MOD,
        NUMBER, FUNCTIONCALL, VARIABLE
    }
    
    private ASTType type;
    private Object content;
    private int line; //Line of code this was generated from (if applicable)
    private List<AbstractSyntaxTree> children;
    
    private static final int NO_LINE = -1;
    
    public AbstractSyntaxTree(ASTType type)
    {
        this(type, null, NO_LINE);
    }
    
    public AbstractSyntaxTree(ASTType type, Object content)
    {
        this(type, content, NO_LINE);
    }
    
    /**
     * Creates a new AST
     * 
     * @param type
     * @param content
     * @param line 
     */
    public AbstractSyntaxTree(ASTType type, Object content, int line)
    {
        this.type = type;
        this.content = content;
        this.line = line;
        this.children = new ArrayList<AbstractSyntaxTree>();
    }

    /**
     * Adds an AST as child of this one
     * 
     * @param ast 
     */
    public boolean addChild(AbstractSyntaxTree ast)
    {
        if(ast != null)
        {
            children.add(ast);
            return true;
        }
        return false;
    }
    
    public ASTType getType()
    {
        return type;
    }

    public Object getContent()
    {
        return content;
    }

    public int getLine()
    {
        return line;
    }

    public List<AbstractSyntaxTree> getChildren()
    {
        return children;
    }
    
    @Override
    public String toString()
    {
        return "\n" + print("", true);
    }
    
    /**
     * Taken from: https://stackoverflow.com/questions/4965335/how-to-print-binary-tree-diagram
     * 
     * @param prefix
     * @param isTail 
     * @return
     */
    public String print(String prefix, boolean isTail)
    {
        String str = prefix + (isTail ? "└── " : "├── ") + type + "|" + content + "\n";
        for(int i = 0; i < children.size() - 1; i++)
        {
            str += children.get(i).print(prefix + (isTail ? "    " : "│   "), false);
        }
        if(children.size() > 0)
        {
            str += children.get(children.size() - 1).print(prefix + (isTail ?"    " : "│   "), true);
        }
        return str;
    }
}