/**
 * The GB-J Compiler
 * Copyright (C) 2019 robbi-blechdose
 * Licensed under GNU AGPLv3
 * (See LICENSE.txt for full license)
 */
package de.jc.gbjc.main.Codegen;

import de.jc.gbjc.main.Util.Logger;
import java.util.List;

/**
 *
 * @author robbi-blechdose
 * 
 */
public class Optimizer
{
    private List<String> code;
    private int i;
    
    private int cyclesSaved;
    private int bytesSaved;
    
    /**
     * Constructor
     * 
     * @param code The output of the CodeGenerator
     */
    public Optimizer(List<String> code)
    {
        this.code = code;
    }
    
    /**
     * The optimizing method of the optimizer optimizes the unoptimized code into optimized code
     */
    public void optimize()
    {
        Logger.getInstance().log(Logger.FINE, "Starting optimizer pass...");
        
        i = 0;
        cyclesSaved = 0;
        bytesSaved = 0;
        
        while(i < code.size())
        {   
            if(code.get(i).equals("cp 0\n"))
            {
                code.set(i, "and a\n");
                //Saves 1 byte, 1 cycle
                bytesSaved++;
                cyclesSaved++;
            }
            else if(code.get(i).equals("ld a, 0\n"))
            {
                code.set(i, "xor a\n");
                //Saves 1 byte, 1 cycle
                bytesSaved++;
                cyclesSaved++;
            }
            else if(code.get(i).equals("sub 1\n"))
            {
                code.set(i, "dec a\n");
                //Saves 1 byte, 1 cycle
                bytesSaved++;
                cyclesSaved++;
            }
            else if(code.get(i).equals("add 1\n"))
            {
                code.set(i, "inc a\n");
                //Saves 1 byte, 1 cycle
                bytesSaved++;
                cyclesSaved++;
            }
            else if(code.get(i).equals("add sp, 0\n"))
            {
                code.remove(i);
                //Saves 4 bytes, 2 cycles
                bytesSaved += 4;
                cyclesSaved += 2;
            }
            else if(i + 1 < code.size())
            {
                if(code.get(i + 1).startsWith("ld a, [w"))
                {
                    String end = code.get(i + 1).substring(8).replace("]\n", "");
                    if(code.get(i).equals("ld [w" + end + "], a\n"))
                    {
                        code.remove(i + 1);
                        //Saves 3 bytes, 4 cycles
                        bytesSaved += 3;
                        cyclesSaved += 4;
                    }
                }
                else if(code.get(i).startsWith("call ") && code.get(i + 1).equals("ret\n"))
                {
                    code.set(i, "jp " + code.get(i).substring(5));
                    code.remove(i + 1);
                    //Saves 1 byte, 6 cycles
                    bytesSaved++;
                    cyclesSaved += 6;
                }
                else if(code.get(i).equals("ld de, 0\n") && code.get(i + 1).equals("add hl, de\n"))
                {
                    code.remove(i);
                    code.remove(i); //Only "i" and not "i + 1" since the list is shifted to the left when an element is deleted
                    i--; //Make sure that we don't skip any elements
                    //Saves 4 bytes, 5 cycles
                    bytesSaved += 4;
                    cyclesSaved += 5;
                }
                else if(code.get(i).equals("ld de, 1\n") && code.get(i + 1).equals("add hl, de\n"))
                {
                    code.set(i, "inc hl\n");
                    code.remove(i + 1);
                    i--; //Make sure that we don't skip any elements
                    //Saves 3 bytes, 3 cycles
                    bytesSaved += 3;
                    cyclesSaved += 3;
                }
                else if(code.get(i).equals("ld c, a\n") && code.get(i + 1).equals("ld a, c\n"))
                {
                    code.remove(i);
                    code.remove(i); //Only "i" and not "i + 1" since the list is shifted to the left when an element is deleted
                    i--; //Make sure that we don't skip any elements
                    //Saves 2 bytes, 2 cycles
                    bytesSaved += 2;
                    cyclesSaved += 2;
                }
                else if(code.get(i).startsWith("ld e, ") && code.get(i + 1).equals("ld a, e\n"))
                {
                    code.set(i, "ld a, " + code.get(i).substring(6));
                    code.remove(i + 1);
                    i--; //Make sure that we don't skip any elements
                    //Saves 1 byte, 1 cycle
                    bytesSaved++;
                    cyclesSaved++;
                }
                else if(code.get(i).startsWith("ld c, ") && code.get(i + 1).equals("ld a, c\n"))
                {
                    code.set(i, "ld a, " + code.get(i).substring(6));
                    code.remove(i + 1);
                    i--; //Make sure that we don't skip any elements
                    //Saves 1 byte, 1 cycle
                    bytesSaved++;
                    cyclesSaved++;
                }
                else if(code.get(i).startsWith("add sp, ") && code.get(i + 1).startsWith("add sp, "))
                {
                    int value = Integer.parseInt(code.get(i).replace("\n", "").substring(8)) + Integer.parseInt(code.get(i + 1).replace("\n", "").substring(8));
                    code.remove(i + 1);
                    code.set(i, "add sp, " + value + "\n");
                    i--; //Make sure that we don't skip any elements
                    //Saves 2 bytes, 4 cycles
                    bytesSaved += 2;
                    cyclesSaved += 4;
                }
                else if(code.get(i).startsWith("ld e, ") && code.get(i + 1).startsWith("ld d, "))
                {
                    try
                    {
                        int a = Integer.parseInt(code.get(i).substring(6));
                        int b = Integer.parseInt(code.get(i + 1).substring(6));
                        
                        code.set(i, "ld de, (" + b + " << 8) + " + a);
                        code.remove(i + 1);
                        i--; //Make sure that we don't skip any elements
                        //Saves 1 byte, 1 cycle
                        bytesSaved++;
                        cyclesSaved++;
                    }
                    catch(Exception e)
                    {
                        //Not numbers, so we don't apply this optimization
                    }
                }
                else if(i + 2 < code.size())
                {
                    if(code.get(i).startsWith("ld e, ") && code.get(i + 2).endsWith(" e\n"))
                    {
                        boolean found = true;
                        
                        if(code.get(i + 2).equals("cp e\n"))
                        {
                            code.set(i + 2, "cp " + code.get(i).substring(6));
                        }
                        else if(code.get(i + 2).equals("sub e\n"))
                        {
                            code.set(i + 2, "sub " + code.get(i).substring(6));
                        }
                        else if(code.get(i + 2).equals("add e\n"))
                        {
                            code.set(i + 2, "add " + code.get(i).substring(6));
                        }
                        else
                        {
                            found = false;
                        }
                        
                        if(found)
                        {
                            code.remove(i);
                            i--;
                            //Saves 1 byte, 1 cycle
                            bytesSaved++;
                            cyclesSaved++;
                        }
                    }
                    
                    if(code.get(i).startsWith("ld c, ") && code.get(i + 2).equals("cp c\n"))
                    {
                        //"Optimizing" a away only leads to problems and is of no help
                        if(!code.get(i).substring(6).replace("\n", "").equals("a"))
                        {
                            code.set(i + 2, "cp " + code.get(i).substring(6));
                            code.remove(i);
                            i--; //Make sure that we don't skip any elements
                            //Saves 1 byte, 1 cycle
                            bytesSaved++;
                            cyclesSaved++;
                        }
                    }
                    else if(i + 3 < code.size())
                    {
                        if(code.get(i).equals("ld c, a\n") && code.get(i + 1).equals("ld e, 1\n") &&
                                code.get(i + 2).equals("ld a, c\n"))
                        {
                            if(code.get(i + 3).equals("add e\n"))
                            {
                                code.set(i, "inc a\n");
                            }
                            else if(code.get(i + 3).equals("sub e\n"))
                            {
                                code.set(i, "dec a\n");
                            }
                            code.remove(i + 3);
                            code.remove(i + 2);
                            code.remove(i + 1);
                            //Saves 5 bytes, 5 cycles
                            bytesSaved += 5;
                            cyclesSaved += 5;
                        }
                    }
                }
            }
            
            i++;
        }
        
        Logger.getInstance().log(Logger.FINE, "Optimizer pass saved " + bytesSaved + " bytes and " + cyclesSaved + " cycles.");
    }
}