/**
 * The GB-J Compiler
 * Copyright (C) 2019 - 2021 robbi-blechdose
 * Licensed under GNU AGPLv3
 * (See LICENSE.txt for full license)
 */
package de.jc.gbjc.main.Util;

/**
 *
 * @author robbi-blechdose
 *
 */
public class CompilerOutput
{
    public static enum CompilerOutputType {
        MISSING_OPENBRACE, MISSING_CLOSEBRACE,
        MISSING_OPENBRACKET, MISSING_CLOSEBRACKET,
        MISSING_OPENPAREN, MISSING_CLOSEPAREN,
        MISSING_SEMICOLON,
        EXPECTEDBUTGOT,
        MALFORMED_LOGIC,
        ILLEGAL_TOKEN
    }
    
    /**
     * Outputs a syntax error and terminates the program
     * 
     * @param t The error type
     * @param line The line the error occured on
     */
    public static void syntaxError(CompilerOutputType t, int line)
    {
        syntaxError(t, line, null);
    }
    
    /**
     * Outputs a syntax error and terminates the program
     * 
     * @param t The error type
     * @param line The line the error occured on
     * @param info Additional info, if necessary
     */
    public static void syntaxError(CompilerOutputType t, int line, String info)
    {
        String message = "Syntax error on line " + line;
        
        switch(t)
        {
            case MISSING_SEMICOLON:
            {
                message += ": Missing semicolon.";
                break;
            }
            case ILLEGAL_TOKEN:
            {
                message += ": Illegal token \"" + info + "\".";
                break;
            }
            default:
            {
                message += ".";
                break;
            }
        }
        
        Logger.getInstance().log(Logger.ERROR, message);
        System.exit(-1);
    }
}