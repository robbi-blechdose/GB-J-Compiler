/**
 * The GB-J Compiler
 * Copyright (C) 2019 robbi-blechdose
 * Licensed under GNU AGPLv3
 * (See LICENSE.txt for full license)
 */
package de.jc.gbjc.main;

import de.jc.gbjc.main.Codegen.CodeGenerator.VariableType;

/**
 * Small helper class to improve handling of these two classes which very often are both needed
 *
 * @author robbi-blechdose
 * 
 */
public class VarHelper
{
    private int bitNum;
    private VariableType varType;

    /**
     * Constructor
     * 
     * @param bitNum The number of bits this variable has
     * @param varType The type of this variable
     */
    public VarHelper(int bitNum, VariableType varType)
    {
        this.bitNum = bitNum;
        this.varType = varType;
    }

    public int getBitNum()
    {
        return bitNum;
    }

    public VariableType getVarType()
    {
        return varType;
    }
}