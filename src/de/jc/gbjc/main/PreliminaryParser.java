/**
 * The GB-J Compiler
 * Copyright (C) 2019 robbi-blechdose
 * Licensed under GNU AGPLv3
 * (See LICENSE.txt for full license)
 */
package de.jc.gbjc.main;

import de.jc.gbjc.main.Util.Utils;
import de.jc.gbjc.main.Token.TokenType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * First pass over tokens to get a list of all methods (including return type)
 * that can later be checked against
 * Also gets a list of object types and their methods
 *
 * @author robbi-blechdose
 * 
 */
public class PreliminaryParser
{
    private List<Token> tokens;
    private int i;
    
    private List<String> staticMethods;
    
    private List<String> objectTypes; //<class name>
    private List<Integer> objectSizes; //<size (all variables added together)>
    private List<String> objectConstructors; //<class name>|<number of arguments>
    private String currentObject;
    private List<String> objectMethods; //<class name>|<function return type>|<function name>|<number of arguments>
    private List<String> objectVariablePositions; //<class name>|<variable name>|<position in bytes>
    private List<String> objectInstances; //<class name>|<instance name>
    
    /**
     * GIANT STATIC ARRAY OF DOOM™
     */
    private static final List<String> standardLibMethods = Arrays.asList
    (
        //Utils.asm
        "void|readPad|0",
        "char|isButtonPressed|1",
        "char|wasButtonPressed|1",
        "void|switchSpeed|0",
        //LCD.asm
        "void|lcdOff|0",
        "void|loadPalettes|2",
        //ScreenPrinter.asm
        "void|loadCharset|0",
        "void|clearScreen|0",
        "void|print|3",
        "void|printNumber|3",
        //Sprites.asm
        "void|dmaTransfer|0",
        "void|copyDMARoutine|0",
        "void|clearOAM|0",
        "void|setSpritePosition|3",
        "void|setSpriteTile|2",
        "void|setSpriteAttributes|2",
        //Tiles.asm
        "void|loadTiles|4",
        "void|setBGTile|4",
        "void|setWinTile|4",
        "void|loadBGMap|6",
        "void|loadWinMap|6",
        "void|setBGScroll|2",
        "void|setWinPosition|2",
        //GBPrinter.asm
        "char|initPrinter|0",
        "char|transferData|0",
        "char|getStatus|0",
        "char|startPrint|0"
    );
    
    public PreliminaryParser(List<Token> tokens)
    {
        this.tokens = tokens;
        this.staticMethods = new ArrayList<String>();
        this.objectTypes = new ArrayList<String>();
        this.objectSizes = new ArrayList<Integer>();
        this.objectConstructors = new ArrayList<String>();
        this.objectMethods = new ArrayList<String>();
        this.objectVariablePositions = new ArrayList<String>();
        this.objectInstances = new ArrayList<String>();
        this.i = 0;
    }
    
    /**
     * Parses a class / section
     * 
     * @param isStatic Determines if this is a section (static) or class (not static)
     * @return 
     */
    private int parseClass(boolean isStatic)
    {
        //Only used for objects
        int size = 0;
        i += 3;
        
        while(i < tokens.size())
        {
            //This is either a function or a variable declaration
            if(tokens.get(i).getType() == TokenType.IDENTIFIER && tokens.get(i + 1).getType() == TokenType.IDENTIFIER)
            {
                //It's a function
                if(tokens.get(i + 2).getType() == TokenType.LPAREN)
                {
                    String str = tokens.get(i).getString() + "|" + tokens.get(i + 1).getString() + "|";
                    i += 3;
                    
                    int funcArgs = 0;
                    
                    //Count function arguments
                    while(i < tokens.size())
                    {
                        if(tokens.get(i).getType() == TokenType.RPAREN)
                        {
                            i += 2; //Skip the RPAREN and the LBRACE
                            break;
                        }
                        else if(tokens.get(i).getType() == TokenType.IDENTIFIER && tokens.get(i + 1).getType() == TokenType.IDENTIFIER)
                        {
                            i++;
                            funcArgs++;
                        }
                        i++;
                    }
                    str += funcArgs;
                    
                    //Skip the rest of the function
                    int braces = 1;
                    while(i < tokens.size())
                    {
                        if(tokens.get(i).getType() == TokenType.LBRACE)
                        {
                            braces++;
                        }
                        else if(tokens.get(i).getType() == TokenType.RBRACE)
                        {
                            braces--;
                        }
                        
                        if(braces == 0)
                        {
                            break;
                        }
                        
                        i++;
                    }
                    
                    if(isStatic)
                    {
                        staticMethods.add(str);
                    }
                    else
                    {
                        objectMethods.add(currentObject + "|" + str);
                    }
                }
                //Variable declaration
                else
                {
                    if(tokens.get(i + 2).getType() == TokenType.SEMICOLON)
                    {
                        if(!isStatic)
                        {
                            objectVariablePositions.add(currentObject + "|" + tokens.get(i + 1).getString() + "|" + size);
                        }
                        
                        if(tokens.get(i).getString().equals("char"))
                        {
                            size++;
                        }
                        else if(tokens.get(i).getString().equals("int"))
                        {
                            size += 2;
                        }
                        else
                        {
                            objectInstances.add(tokens.get(i).getString() + "|" + tokens.get(i + 1).getString());
                            size += 2;
                        }
                    }
                }
            }
            //Skip constructors
            else if(tokens.get(i).getType() == TokenType.IDENTIFIER && tokens.get(i + 1).getType() == TokenType.LPAREN)
            {
                String str = tokens.get(i).getString() + "|";
                
                i += 2;
                
                int numArgs = 0;
                
                //Count arguments
                while(i < tokens.size())
                {
                    if(tokens.get(i).getType() == TokenType.IDENTIFIER && tokens.get(i + 1).getType() == TokenType.IDENTIFIER)
                    {
                        if(tokens.get(i + 2).getType() == TokenType.RPAREN)
                        {
                            numArgs++;
                            i += 4;
                            break;
                        }
                        else if(tokens.get(i + 2).getType() == TokenType.COMMA)
                        {
                            numArgs++;
                            i += 2; //Only +2 since we aren't breaking and there's an i++ below which makes it 3 "consumed" tokens
                        }
                    }
                    else if(tokens.get(i).getType() == TokenType.RPAREN)
                    {
                        i += 2; //Skip the RPAREN and the LBRACE
                        break;
                    }
                    i++;
                }
                
                str += numArgs;

                //Skip the rest of the constructor
                int braces = 1;
                while(i < tokens.size())
                {
                    if(tokens.get(i).getType() == TokenType.LBRACE)
                    {
                        braces++;
                    }
                    else if(tokens.get(i).getType() == TokenType.RBRACE)
                    {
                        braces--;
                    }

                    if(braces == 0)
                    {
                        break;
                    }

                    i++;
                }
                
                objectConstructors.add(str);
            }
            //Skip inlined ASM
            else if(tokens.get(i).getType() == TokenType.IDENTIFIER && tokens.get(i + 1).getType() == TokenType.LBRACE)
            {
                while(i < tokens.size())
                {
                    if(tokens.get(i).getType() == TokenType.RBRACE)
                    {
                        break;
                    }
                    
                    i++;
                }
            }
            //End of class reached
            else if(tokens.get(i).getType() == TokenType.RBRACE)
            {
                break;
            }
            
            i++;
        }
        
        return size;
    }
    
    public void addStdlibMethods()
    {
        staticMethods.addAll(standardLibMethods);
    }
    
    /**
     * Parses a file
     */
    public void parse()
    {
        while(i < tokens.size())
        {
            if(tokens.get(i).getType() == TokenType.IDENTIFIER && tokens.get(i).getString().equals("section"))
            {
                if(tokens.get(i + 2).getType() == TokenType.LBRACE)
                {
                    parseClass(true);
                }
            }
            else if(tokens.get(i).getType() == TokenType.IDENTIFIER && tokens.get(i).getString().equals("class"))
            {
                if(tokens.get(i + 2).getType() == TokenType.LBRACE)
                {
                    objectTypes.add(tokens.get(i + 1).getString());
                    currentObject = tokens.get(i + 1).getString();

                    objectSizes.add(parseClass(false));
                }
            }
            else if(tokens.get(i).getType() == TokenType.IDENTIFIER && tokens.get(i + 1).getType() == TokenType.STRING)
            {
                if(tokens.get(i).getString().equals("import") && tokens.get(i + 1).getString().endsWith(".gbj"))
                {
                    //Do the preliminary parsing for the imported file as well (the new pp will create other instances on further imports)
                    //This allows us to call methods from included files
                    PreliminaryParser pp = new PreliminaryParser(Lexer.lex(Utils.toAbsolutePath(tokens.get(i + 1).getString())));
                    pp.parse();
                    staticMethods.addAll(pp.getStaticMethods());
                    objectTypes.addAll(pp.getObjectTypes());
                    objectSizes.addAll(pp.getObjectSizes());
                    objectConstructors.addAll(pp.getObjectConstructors());
                    objectMethods.addAll(pp.getObjectMethods());
                    objectVariablePositions.addAll(pp.getObjectVariablePositions());
                    objectInstances.addAll(pp.getObjectInstances());
                    
                    i++;
                }
            }
            
            i++;
        }
    }
    
    public List<String> getStaticMethods()
    {
        return staticMethods;
    }
    
    public List<String> getObjectTypes()
    {
        return objectTypes;
    }
    
    public List<Integer> getObjectSizes()
    {
        return objectSizes;
    }
    
    public List<String> getObjectConstructors()
    {
        return objectConstructors;
    }
    
    public List<String> getObjectMethods()
    {
        return objectMethods;
    }
    
    public List<String> getObjectVariablePositions()
    {
        return objectVariablePositions;
    }
    
    public List<String> getObjectInstances()
    {
        return objectInstances;
    }
}