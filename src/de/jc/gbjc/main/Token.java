/**
 * The GB-J Compiler
 * Copyright (C) 2019 robbi-blechdose
 * Licensed under GNU AGPLv3
 * (See LICENSE.txt for full license)
 */
package de.jc.gbjc.main;

/**
 * A single token representing part of the original source code
 *
 * @author robbi-blechdose
 * 
 */
public class Token
{
    public static enum TokenType
    {
        CONSTANT, IDENTIFIER, STRING,
        SEMICOLON, DOT, COLON, COMMA, LPAREN, RPAREN, LBRACE, RBRACE, LBRACKET, RBRACKET,
        EQUALS, PLUS, MINUS, DIVIDE, MULTIPLY, MODULO,
        BIGGER, SMALLER, NOT,
        PIPE, AND;
    }
    
    private TokenType type;
    private String string;
    private int line;
    
    /**
     * Constructor
     * 
     * @param type Token type
     * @param string Additional information may be saved here, interpretation depends on token type
     * @param line Source code line this token was created from
     */
    public Token(TokenType type, String string, int line)
    {
        this.type = type;
        this.string = string;
        this.line = line;
    }

    public TokenType getType()
    {
        return type;
    }

    public String getString()
    {
        return string;
    }
    
    public int getLine()
    {
        return line;
    }

    @Override
    public String toString()
    {
        return "Token{" + "type=" + type + ", string=" + string + ", line=" + line + "}";
    }
}