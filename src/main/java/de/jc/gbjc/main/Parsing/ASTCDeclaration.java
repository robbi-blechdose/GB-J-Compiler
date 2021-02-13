/**
 * The GB-J Compiler
 * Copyright (C) 2019 - 2021 robbi-blechdose
 * Licensed under GNU AGPLv3
 * (See LICENSE.txt for full license)
 */
package de.jc.gbjc.main.Parsing;

import de.jc.gbjc.main.Lexing.Token.TokenType;
import java.util.Objects;

/**
 * AbstractSyntaxTree Content Declaration
 * 
 * A content object for ASTS representing a declaration
 *
 * @author robbi-blechdose
 *
 */
public class ASTCDeclaration
{
    private TokenType type;
    private String name;
    
    public ASTCDeclaration(TokenType type, String name)
    {
        this.type = type;
        this.name = name;
    }

    public TokenType getType()
    {
        return type;
    }

    public String getName()
    {
        return name;
    }

    @Override
    public boolean equals(Object obj)
    {
        if(this == obj)
        {
            return true;
        }
        else if(obj == null)
        {
            return false;
        }
        else if(getClass() != obj.getClass())
        {
            return false;
        }
        
        final ASTCDeclaration other = (ASTCDeclaration) obj;
        if(!Objects.equals(this.name, other.name))
        {
            return false;
        }
        if(this.type != other.type)
        {
            return false;
        }
        return true;
    }
}