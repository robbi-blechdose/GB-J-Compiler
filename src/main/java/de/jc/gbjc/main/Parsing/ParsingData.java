/**
 * The GB-J Compiler
 * Copyright (C) 2019 - 2020 robbi-blechdose
 * Licensed under GNU AGPLv3
 * (See LICENSE.txt for full license)
 */
package de.jc.gbjc.main.Parsing;

import de.jc.gbjc.main.Lexing.Token;
import java.util.List;

/**
 *
 * @author robbi-blechdose
 *
 */
public class ParsingData
{
    public List<Token> tokens;
    public List<String> objectTypes;
    
    public int i;
    
    public boolean objectWarn;
    
    public ParsingData(List<Token> tokens, List<String> objectTypes, boolean objectWarn)
    {
        this.tokens = tokens;
        this.objectTypes = objectTypes;
        i = 0;
        this.objectWarn = objectWarn;
    }
}