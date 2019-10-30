/**
 * The GB-J Compiler
 * Copyright (C) 2019 robbi-blechdose
 * Licensed under GNU AGPLv3
 * (See LICENSE.txt for full license)
 */
package de.jc.gbjc.main.Parsing;

import java.util.ArrayList;
import java.util.List;

/**
 * An AbstractSyntaxTree<br>
 * One instance of this class is one node of the tree which can have multiple children
 *
 * @author robbi-blechdose
 * 
 */
public class AbstractSyntaxTree
{
    public static enum TreeNodeType
    {
        PROGRAM,
        CLASS, SECTION,
        IMPORT, PACKAGE,
        CONSTRUCTOR, INSTANTIATION,
        DECLARATION, ASM_REFERENCE, ARRAY_DECLARATION,
        ASSIGNMENT,
        OPERATION,
        CONSTANT, STRING, VARIABLE, ARRAY_ACCESS,
        FUNCTION, FUNC_ARG, FUNC_ARGS,
        CALL, OBJECT_CALL, RETURN,
        IF, ELSE, SWITCH, CASE, WHILE, CONDITION, BODY,
        INLINE_ASM;
    }
    
    private TreeNodeType type;
    private String content;
    private int line; //Line of code this was generated from (if applicable)
    private List<AbstractSyntaxTree> children;
    
    /**
     * Constructor
     * 
     * @param type The type of this node
     * @param content The content of this node (refer to the parser for how this is formatted)
     * @param line The line of code this was generated from, if applicable (otherwise -1)
     */
    public AbstractSyntaxTree(TreeNodeType type, String content, int line)
    {
        this.type = type;
        this.content = content;
        this.line = line;
        this.children = new ArrayList<AbstractSyntaxTree>();
    }
    
    /**
     * Adds another AST as child of this one
     * 
     * @param ast
     */
    public void addChild(AbstractSyntaxTree ast)
    {
        children.add(ast);
    }

    /**
     * Returns the list of children ASTs
     * 
     * @return 
     */
    public List<AbstractSyntaxTree> getChildren()
    {
        return children;
    }

    public TreeNodeType getType()
    {
        return type;
    }

    /**
     * @return 
     */
    public String getContent()
    {
        return content;
    }
    
    /**
     * @return 
     */
    public int getLine()
    {
        return line;
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
    
    /**
     * @param s 
     */
    public void setContent(String s)
    {
        this.content = s;
    }
}