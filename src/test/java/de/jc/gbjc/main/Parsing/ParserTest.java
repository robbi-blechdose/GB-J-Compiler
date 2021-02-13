/**
 * The GB-J Compiler
 * Copyright (C) 2019 - 2020 robbi-blechdose
 * Licensed under GNU AGPLv3
 * (See LICENSE.txt for full license)
 */
package de.jc.gbjc.main.Parsing;

import de.jc.gbjc.main.Lexing.Token;
import de.jc.gbjc.main.Lexing.Token.TokenType;
import de.jc.gbjc.main.Parsing.AbstractSyntaxTree.ASTType;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


/**
 *
 * @author robbi-blechdose
 * 
 */
public class ParserTest
{
    private Parser parser;
    private List<Token> tokens;
    
    @BeforeEach
    public void setUp()
    {
        tokens = new ArrayList<Token>();
    }

    @AfterEach
    public void tearDown()
    {
        tokens = null;
    }
    
    @Test
    public void testTryParseInclude()
    {
        String name = "includedfile.gbj";
        tokens.add(new Token(TokenType.K_INCLUDE, "", -1));
        tokens.add(new Token(TokenType.STRING, name, -1));
        tokens.add(new Token(TokenType.SEMICOLON, "", -1));
        parser = new Parser(tokens, null, false);
        
        AbstractSyntaxTree ast = parser.tryParseInclude();
        assertEquals(ast.getType(), ASTType.INCLUDE);
        assertEquals(ast.getContent(), name);
    }
    
    @Test
    public void testTryParseDeclaration()
    {
        String name = "field";
        tokens.add(new Token(TokenType.K_CHAR, "", -1));
        tokens.add(new Token(TokenType.IDENTIFIER, name, -1));
        tokens.add(new Token(TokenType.SEMICOLON, "", -1));
        parser = new Parser(tokens, null, false);
        
        AbstractSyntaxTree ast = parser.tryParseDeclaration();
        assertEquals(ast.getType(), ASTType.DECLARATION);
        assertEquals(ast.getContent(), new ASTCDeclaration(TokenType.K_CHAR, name));
    }
    
    //TODO: Test cases
}