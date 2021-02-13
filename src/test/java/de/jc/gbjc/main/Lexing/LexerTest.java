/**
 * The GB-J Compiler
 * Copyright (C) 2019 - 2020 robbi-blechdose
 * Licensed under GNU AGPLv3
 * (See LICENSE.txt for full license)
 */
package de.jc.gbjc.main.Lexing;

import de.jc.gbjc.main.Lexing.Token.TokenType;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
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
public class LexerTest
{
    private File temp;
    private BufferedWriter bw;
    
    @BeforeEach
    public void setUp() throws IOException
    {
        temp = File.createTempFile("tempCode", ".gbj");
        bw = new BufferedWriter(new FileWriter(temp));
    }

    @AfterEach
    public void tearDown() throws IOException
    {
        temp.delete();
    }
    
    @Test
    public void testWhitespace() throws IOException
    {
        bw.write(" ");
        bw.write("\n");
        bw.write("\t");
        bw.close();
        
        List<Token> tokens = Lexer.lex(temp.getAbsolutePath());
        assertEquals(true, tokens.isEmpty(), "Lexer failed: Whitespace handling.");
    }
    
    @Test
    public void testComment() throws IOException
    {
        bw.write("//rtgr8ztvgwrz\n");
        bw.write("// int a;\n");
        bw.write("//x#äüö\n");
        bw.close();
        
        List<Token> tokens = Lexer.lex(temp.getAbsolutePath());
        assertEquals(true, tokens.isEmpty(), "Lexer failed: Comment handling.");
    }
    
    @Test
    public void testNumber() throws IOException
    {
        //Decimal numbers
        bw.write("12\n");
        bw.write("9462445\n");
        bw.write("01\n");
        //Hex numbers
        bw.write("$FF\n");
        bw.write("$01\n");
        bw.write("$DEAD\n");
        bw.close();
        
        List<Token> tokens = Lexer.lex(temp.getAbsolutePath());
        
        for(Token t : tokens)
        {
            assertEquals(t.getType(), TokenType.CONSTANT, "Lexer failed: Number handling.");
        }
        
        assertEquals("12", tokens.get(0).getString(), "Lexer failed: Number handling (Decimal 1).");
        assertEquals("9462445", tokens.get(1).getString(), "Lexer failed: Number handling (Decimal 2).");
        assertEquals("1", tokens.get(2).getString(), "Lexer failed: Number handling (Decimal 3).");
        
        assertEquals("255", tokens.get(3).getString(), "Lexer failed: Number handling (Hex 1).");
        assertEquals("1", tokens.get(4).getString(), "Lexer failed: Number handling (Hex 2).");
        assertEquals("57005", tokens.get(5).getString(), "Lexer failed: Number handling (Hex 3).");
    }
    
    @Test
    public void testString() throws IOException
    {
        bw.write("\"test\"\n");
        bw.write("\"12bc%+üö#\"");
        bw.close();
        
        List<Token> tokens = Lexer.lex(temp.getAbsolutePath());
        
        assertEquals("test", tokens.get(0).getString(), "Lexer failed: String handling.");
        assertEquals("12bc%+üö#", tokens.get(1).getString(), "Lexer failed: String handling.");
    }
    
    @Test
    public void testKeyword() throws IOException
    {
        bw.write("section\n");
        bw.write("include\n");
        bw.write("package\n");
        bw.write("char\n");
        bw.write("int\n");
        bw.write("ptr\n");
        bw.close();
        
        List<Token> tokens = Lexer.lex(temp.getAbsolutePath());
        
        assertEquals(TokenType.K_SECTION, tokens.get(0).getType(), "Lexer failed: Keyword handling (1).");
        assertEquals(TokenType.K_INCLUDE, tokens.get(1).getType(), "Lexer failed: Keyword handling (2).");
        assertEquals(TokenType.K_PACKAGE, tokens.get(2).getType(), "Lexer failed: Keyword handling (3).");
        assertEquals(TokenType.K_CHAR, tokens.get(3).getType(), "Lexer failed: Keyword handling (4).");
        assertEquals(TokenType.K_INT, tokens.get(4).getType(), "Lexer failed: Keyword handling (5).");
        assertEquals(TokenType.K_PTR, tokens.get(5).getType(), "Lexer failed: Keyword handling (6).");
    }
    
    @Test
    public void testIdentifier() throws IOException
    {
        bw.write("kappa\n");
        bw.write("KäNgUrU\n");
        bw.close();
        
        List<Token> tokens = Lexer.lex(temp.getAbsolutePath());
        
        assertEquals(TokenType.IDENTIFIER, tokens.get(0).getType(), "Lexer failed: Identifier handling (1).");
        assertEquals("kappa", tokens.get(0).getString(), "Lexer failed: Identifier handling (1).");
        assertEquals(TokenType.IDENTIFIER, tokens.get(1).getType(), "Lexer failed: Identifier handling (2).");
        assertEquals("KäNgUrU", tokens.get(1).getString(), "Lexer failed: Identifier handling (2).");
    }
    
    @Test
    public void testSymbols() throws IOException
    {
        bw.write(";||&&.:,(){}[]=-+/*%<>!");
        bw.close();
        
        List<Token> tokens = Lexer.lex(temp.getAbsolutePath());
        
        assertEquals(TokenType.SEMICOLON, tokens.get(0).getType(), "Lexer failed: Symbol handling (;).");
        assertEquals(TokenType.OR, tokens.get(1).getType(), "Lexer failed: Symbol handling (||).");
        assertEquals(TokenType.AND, tokens.get(2).getType(), "Lexer failed: Symbol handling (&&).");
        assertEquals(TokenType.DOT, tokens.get(3).getType(), "Lexer failed: Symbol handling (.).");
        assertEquals(TokenType.COLON, tokens.get(4).getType(), "Lexer failed: Symbol handling (:).");
        assertEquals(TokenType.COMMA, tokens.get(5).getType(), "Lexer failed: Symbol handling (,).");
        assertEquals(TokenType.LPAREN, tokens.get(6).getType(), "Lexer failed: Symbol handling (().");
        assertEquals(TokenType.RPAREN, tokens.get(7).getType(), "Lexer failed: Symbol handling ()).");
        assertEquals(TokenType.LBRACE, tokens.get(8).getType(), "Lexer failed: Symbol handling ({).");
        assertEquals(TokenType.RBRACE, tokens.get(9).getType(), "Lexer failed: Symbol handling (}).");
        assertEquals(TokenType.LBRACKET, tokens.get(10).getType(), "Lexer failed: Symbol handling ([).");
        assertEquals(TokenType.RBRACKET, tokens.get(11).getType(), "Lexer failed: Symbol handling (]).");
        assertEquals(TokenType.EQUALS, tokens.get(12).getType(), "Lexer failed: Symbol handling (=).");
        assertEquals(TokenType.MINUS, tokens.get(13).getType(), "Lexer failed: Symbol handling (-).");
        assertEquals(TokenType.PLUS, tokens.get(14).getType(), "Lexer failed: Symbol handling (+).");
        assertEquals(TokenType.DIVIDE, tokens.get(15).getType(), "Lexer failed: Symbol handling (/).");
        assertEquals(TokenType.MULTIPLY, tokens.get(16).getType(), "Lexer failed: Symbol handling (*).");
        assertEquals(TokenType.MODULO, tokens.get(17).getType(), "Lexer failed: Symbol handling (%).");
        assertEquals(TokenType.SMALLER, tokens.get(18).getType(), "Lexer failed: Symbol handling (<).");
        assertEquals(TokenType.BIGGER, tokens.get(19).getType(), "Lexer failed: Symbol handling (>).");
        assertEquals(TokenType.NOT, tokens.get(20).getType(), "Lexer failed: Symbol handling (!).");
    }
}