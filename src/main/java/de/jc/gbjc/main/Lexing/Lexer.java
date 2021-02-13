/**
 * The GB-J Compiler
 * Copyright (C) 2019 - 2020 robbi-blechdose
 * Licensed under GNU AGPLv3
 * (See LICENSE.txt for full license)
 */
package de.jc.gbjc.main.Lexing;

import de.jc.gbjc.main.Util.Logger;
import de.jc.gbjc.main.Lexing.Token.TokenType;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The first stage of the compilation process,
 * the lexer / tokenizer
 *
 * @author robbi-blechdose
 * 
 */
public class Lexer
{
    private static final Map<String, TokenType> keywords;
    
    static
    {
        keywords = new HashMap<String, TokenType>();
        keywords.put("section", TokenType.K_SECTION);
        keywords.put("include", TokenType.K_INCLUDE);
        keywords.put("package", TokenType.K_PACKAGE);
        keywords.put("char", TokenType.K_CHAR);
        keywords.put("int", TokenType.K_INT);
        keywords.put("ptr", TokenType.K_PTR);
    }
    
    /**
     * Lexes a given file into a token list
     * 
     * @param filePath The path to the file to read in
     * @return The list of tokens created from the file
     */
    public static List<Token> lex(String filePath)
    {
        List<Token> tokens = new ArrayList<Token>();
        
        try
        {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(filePath)));
            
            String line = bufferedReader.readLine();
            int lineNum = 0;
            
            while(line != null)
            {
                lineNum++;
                for(int i = 0; i < line.length(); i++)
                {
                    char c = line.charAt(i);
                    
                    //Discard whitespace tokens
                    if(!Character.isWhitespace(c))
                    {
                        //Discard comments
                        if(c == '/' && line.charAt(i + 1) == '/')
                        {
                            break;
                        }
                        //Number (decimal)
                        else if(Character.isDigit(c))
                        {
                            int number = 0;
                            
                            for(int j = i; j < line.length(); j++)
                            {
                                if(Character.isDigit(line.charAt(j)))
                                {
                                    number *= 10;
                                    number += line.charAt(j) - '0';
                                    i = j;
                                }
                                else
                                {
                                    //As soon as we found a non-digit character, break (don't "consume" it)
                                    break;
                                }
                            }
                            
                            tokens.add(new Token(TokenType.CONSTANT, Integer.toString(number), lineNum));
                        }
                        //Number (Hex)
                        else if(c == '$')
                        {
                            i++; //Discard $ sign
                            
                            String hexStr = "";
                            
                            for(int j = i; j < line.length(); j++)
                            {
                                char ch = line.charAt(j);
                                if(Character.isDigit(ch) ||
                                        ch == 'A' || ch == 'B' || ch == 'C' || ch == 'D' || ch == 'E' || ch == 'F' ||
                                        ch == 'a' || ch == 'b' || ch == 'c' || ch == 'd' || ch == 'e' || ch == 'f')
                                {
                                    hexStr += ch;
                                    i = j;
                                }
                                else
                                {
                                    //As soon as we found a non-digit character, break (don't "consume" it)
                                    break;
                                }
                            }
                            
                            tokens.add(new Token(TokenType.CONSTANT, Integer.toString(Integer.parseInt(hexStr, 16)), lineNum));
                        }
                        //Strings
                        else if(c == '"')
                        {
                            String str = "";
                            
                            for(int j = i + 1; j < line.length(); j++)
                            {
                                if(line.charAt(j) != '"')
                                {
                                    str += line.charAt(j);
                                }
                                else
                                {
                                    //Also "consume" the second quotation mark
                                    i = j;
                                    break;
                                }
                            }
                            
                            tokens.add(new Token(TokenType.STRING, str, lineNum));
                        }
                        //Identifier or keyword
                        else if(Character.isLetter(c) || c == '_')
                        {
                            String str = "";
                            
                            for(int j = i; j < line.length(); j++)
                            {
                                if(Character.isLetter(line.charAt(j)) || line.charAt(j) == '_' || Character.isDigit(line.charAt(j)))
                                {
                                    str += line.charAt(j);
                                }
                                else
                                {
                                    break;
                                }
                                i = j;
                            }
                            
                            if(keywords.containsKey(str))
                            {
                                tokens.add(new Token(keywords.get(str), "", lineNum));
                            }
                            else
                            {
                                tokens.add(new Token(TokenType.IDENTIFIER, str, lineNum));
                            }
                        }
                        //Semicolon;        Also, stop trolls replacing other people's semicolons with greek question marks
                        else if(c == ';' || c == ';')
                        {
                            tokens.add(new Token(TokenType.SEMICOLON, "", lineNum));
                        }
                        //Logical operators
                        else if(c == '|' && line.charAt(i + 1) == '|')
                        {
                            tokens.add(new Token(TokenType.OR, "", lineNum));
                            i++;
                        }
                        else if(c == '&' && line.charAt(i + 1) == '&')
                        {
                            tokens.add(new Token(TokenType.AND, "", lineNum));
                            i++;
                        }
                        else if(c == '.')
                        {
                            tokens.add(new Token(TokenType.DOT, "", lineNum));
                        }
                        else if(c == ':')
                        {
                            tokens.add(new Token(TokenType.COLON, "", lineNum));
                        }
                        else if(c == ',')
                        {
                            tokens.add(new Token(TokenType.COMMA, "", lineNum));
                        }
                        else if(c == '(')
                        {
                            tokens.add(new Token(TokenType.LPAREN, "", lineNum));
                        }
                        else if(c == ')')
                        {
                            tokens.add(new Token(TokenType.RPAREN, "", lineNum));
                        }
                        else if(c == '{')
                        {
                            tokens.add(new Token(TokenType.LBRACE, "", lineNum));
                        }
                        else if(c == '}')
                        {
                            tokens.add(new Token(TokenType.RBRACE, "", lineNum));
                        }
                        else if(c == '[')
                        {
                            tokens.add(new Token(TokenType.LBRACKET, "", lineNum));
                        }
                        else if(c == ']')
                        {
                            tokens.add(new Token(TokenType.RBRACKET, "", lineNum));
                        }
                        else if(c == '=')
                        {
                            tokens.add(new Token(TokenType.EQUALS, "", lineNum));
                        }
                        else if(c == '-')
                        {
                            tokens.add(new Token(TokenType.MINUS, "", lineNum));
                        }
                        else if(c == '+')
                        {
                            tokens.add(new Token(TokenType.PLUS, "", lineNum));
                        }
                        else if(c == '/')
                        {
                            tokens.add(new Token(TokenType.DIVIDE, "", lineNum));
                        }
                        else if(c == '*')
                        {
                            tokens.add(new Token(TokenType.MULTIPLY, "", lineNum));
                        }
                        else if(c == '%')
                        {
                            tokens.add(new Token(TokenType.MODULO, "", lineNum));
                        }
                        else if(c == '<')
                        {
                            tokens.add(new Token(TokenType.SMALLER, "", lineNum));
                        }
                        else if(c == '>')
                        {
                            tokens.add(new Token(TokenType.BIGGER, "", lineNum));
                        }
                        else if(c == '!')
                        {
                            tokens.add(new Token(TokenType.NOT, "", lineNum));
                        }
                        else
                        {
                            Logger.getInstance().log(Logger.WARNING, "Illegal character detected on line " + lineNum + ": \"" + c + "\". Skipping...");
                        }
                    }
                }
                
                line = bufferedReader.readLine();
            }
            bufferedReader.close();
        }
        catch(Exception e)
        {
            Logger.getInstance().log(Logger.ERROR, "Could not read in file.");
            Logger.getInstance().log(Logger.ERROR, e);
            System.exit(1);
        }
        
        return tokens;
    }
    
    /**
     * Debug method to print out the given token list
     * 
     * @param tokens The token list to be printed
     */
    public static void printTokenList(List<Token> tokens)
    {
        Logger.getInstance().log(Logger.FINE, "Token list:");
        for(int i = 0; i < tokens.size(); i++)
        {
            Logger.getInstance().log(Logger.FINE, tokens.get(i).toString());
        }
    }
}