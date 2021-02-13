/**
 * The GB-J Compiler
 * Copyright (C) 2019 robbi-blechdose
 * Licensed under GNU AGPLv3
 * (See LICENSE.txt for full license)
 */
package de.jc.gbjc.main.Codegen;

import de.jc.gbjc.main.Codegen.CodeGenerator.VariableType;
import de.jc.gbjc.main.Constants;
import de.jc.gbjc.main.Parsing.AbstractSyntaxTree;
import de.jc.gbjc.main.Parsing.AbstractSyntaxTree.ASTType;
import de.jc.gbjc.main.VarHelper;
import java.util.List;

/**
 * Generates calls to standard library functions
 *
 * @author robbi-blechdose
 * 
 */
public class StdlibCallGenerator
{
//    private static StdlibCallGenerator instance;
//    
//    /**
//     * Loads the specified register with a value that the tree determines
//     * 
//     * a and hl may be trashed
//     * 
//     * @param tree The AST we're generating the code from
//     * @param code The arraylist the generated code is written into
//     * @param reg1 "b", "d", "h"
//     * @param reg2 "c", "e", "l", "a"
//     * @param bitNum The bitNum of the variable
//     * @param codegen The CodeGenerator instance
//     */
//    private void generateRegisterLoad(AbstractSyntaxTree tree, List<String> code, String reg1, String reg2, int bitNum, CodeGenerator codegen)
//    {
//        if(tree.getType() == ASTType.NUMBER)
//        {
//            int constant = Integer.parseInt(tree.getContent());
//
//            //Check for out of bounds
//            if((bitNum == 8 && (constant > 255 || constant < -128)) || (bitNum == 16 && (constant > 65535 || constant < -32768)))
//            {
//                codegen.numberTooBigError(tree.getLine());
//            }
//
//            if(bitNum == 8)
//            {
//                code.add("ld " + reg2 + ", " + constant + "\n");
//            }
//            else if(bitNum == 16)
//            {
//                code.add("ld " + reg1 + reg2 + ", " + constant + "\n");
//            }
//        }
//        else if(tree.getType() == ASTType.VARIABLE)
//        {
//            if(Constants.getContants().containsKey(tree.getContent()))
//            {
//                if(bitNum == 8)
//                {
//                    code.add("ld " + reg2 + ", " + Constants.getContants().get(tree.getContent()) + "\n");
//                }
//                else
//                {
//                    code.add("ld " + reg1 + reg2 + ", " + Constants.getContants().get(tree.getContent()) + "\n");
//                }
//                return;
//            }
//            
//            VarHelper vh2 = codegen.doVariableAccess(tree, false);
//            int bitNum2 = vh2.getBitNum();
//            VariableType varType2 = vh2.getVarType();
//            if(bitNum2 == 0)
//            {
//                vh2 = codegen.doVariableAccess(tree, true);
//                bitNum2 = vh2.getBitNum();
//                varType2 = vh2.getVarType();
//            }
//            
//            if(varType2 == VariableType.FUNC_ARG || varType2 == VariableType.STACK_VAR)
//            {
//                if(bitNum == 8 && bitNum2 == 8)
//                {
//                    code.add("ld hl, sp + " + codegen.stackOffset + "\n");
//                    code.add("ld " + reg2 + ", [hl]\n");
//                }
//                else if(bitNum == 16 && bitNum2 == 16)
//                {
//                    code.add("ld hl, sp + " + codegen.stackOffset + "\n");
//                    code.add("ld a, [hl+]\n");
//                    code.add("ld " + reg1 + ", [hl]\n");
//                    code.add("ld " + reg2 + ", a\n");
//                }
//                else if(bitNum == 16 && bitNum2 == 8)
//                {
//                    code.add("ld hl, sp + " + codegen.stackOffset + "\n");
//                    code.add("ld " + reg1 + ", [hl]\n");
//                    code.add("ld " + reg2 + ", 0\n");
//                }
//                else
//                {
//                    codegen.incorrectAssignmentError(tree.getLine());
//                }
//            }
//            else if(varType2 == VariableType.STATIC)
//            {
//                if(bitNum == 8 && bitNum2 == 8)
//                {
//                    code.add("ld a, [w" + tree.getContent() + "]\n");
//                    code.add("ld " + reg2 +", a\n");
//                }
//                else if(bitNum == 16 && bitNum2 == 16)
//                {
//                    code.add("ld a, [w" + tree.getContent() + "]\n");
//                    code.add("ld " + reg2 + ", a\n");
//                    code.add("ld a, [w" + tree.getContent() + " + 1]\n");
//                    code.add("ld " + reg1 + ", a\n");
//                }
//                else if(bitNum == 16 && bitNum2 == 8)
//                {
//                    code.add("ld a, [w" + tree.getContent() + "]\n");
//                    code.add("ld " + reg2 + ", a\n");
//                    code.add("ld " + reg1 + ", 0\n");
//                }
//                else
//                {
//                    codegen.incorrectAssignmentError(tree.getLine());
//                }
//            }
//            else if(varType2 == VariableType.OBJECT)
//            {
//                //Preserve de so we have 2 more regs to work with!
//                code.add("push de\n");
//                codegen.generateAddressForObjectVariable(code, tree.getContent(), 0);
//                code.add("pop de\n");
//
//                if(bitNum == 8 && bitNum2 == 8)
//                {
//                    code.add("ld " + reg2 + ", [hl]\n");
//                }
//                else if(bitNum == 16 && bitNum2 == 16)
//                {
//                    code.add("ld a, [hl+]\n");
//                    code.add("ld " + reg2 + ", a\n");
//                    code.add("ld " + reg1 + ", [hl]\n");
//                }
//                else if(bitNum == 16 && bitNum2 == 8)
//                {
//                    code.add("ld " + reg2 + ", [hl]\n");
//                    code.add("ld " + reg1 + ", 0\n");
//                }
//                else
//                {
//                    codegen.incorrectAssignmentError(tree.getLine());
//                }
//            }
//        }
//        //TODO
//        else if(false) //tree.getType() == TreeNodeType.ARRAY_ACCESS)
//        {
//            if(!reg2.equals("c"))
//            {
//                code.add("push bc\n");
//            }
//            //Method returns in c or bc
//            VarHelper vh3 = codegen.doVariableAccess(tree, true);
//            codegen.generateArrayAccess(code, tree, vh3.getBitNum(), true, false);
//            if(!reg2.equals("c"))
//            {
//                code.add("ld " + reg2 + ", c\n");
//            }
//            if(!reg1.equals("b") && bitNum == 16)
//            {
//                code.add("ld " + reg1 + ", b\n");
//            }
//            if(!reg2.equals("c"))
//            {
//                code.add("pop bc\n");
//            }
//        }
//        //TODO
//        else if(false) //tree.getType() == TreeNodeType.CALL || tree.getType() == TreeNodeType.OBJECT_CALL)
//        {
//            if(!reg2.equals("c"))
//            {
//                code.add("push bc\n");
//            }
//            //Method returns in c or bc
//            codegen.generateMethodCall(code, tree, 0);
//            if(!reg2.equals("c"))
//            {
//                code.add("ld " + reg2 + ", c\n");
//            }
//            if(!reg1.equals("b") && bitNum == 16)
//            {
//                code.add("ld " + reg1 + ", b\n");
//            }
//            if(!reg2.equals("c"))
//            {
//                code.add("pop bc\n");
//            }
//        }
//        //TODO
//        else if(false) //tree.getType() == TreeNodeType.ASM_REFERENCE)
//        {
//            code.add("ld " + reg1 + reg2 + ", " + tree.getContent() + "\n");
//        }
//        //TODO
//        else if(false) //tree.getType() == TreeNodeType.STRING)
//        {
//            code.add("ld " + reg1 + reg2 + ", _g" + Math.abs(tree.hashCode()) + "\n");
//            //Put data into data part of file
//            codegen.getData().add("_g" + Math.abs(tree.hashCode()) + ":\n");
//            codegen.getData().add("DB \"" + tree.getContent() + "\", 0\n");
//        }
//    }
//    
//    /**
//     * Generates calls to stdlib methods, but only those using arguments
//     * Calls without arguments require no special case and are thus handled normally
//     * 
//     * @param tree The AST we're generating the code from
//     * @param code The arraylist the generated code is written into
//     * @param codegen The CodeGenerator instance
//     * @param mbcVariant The MBC variant the game uses
//     * @return 
//     */
//    public int tryGenerateStdlibCall(AbstractSyntaxTree tree, List<String> code, CodeGenerator codegen, int mbcVariant)
//    {
//        AbstractSyntaxTree args = tree.getChildren().get(0);
//        
//        switch(tree.getContent())
//        {
//            //Utils.asm
//            case "isButtonPressed":
//            {
//                if(args.getChildren().size() == 1)
//                {
//                    generateRegisterLoad(args.getChildren().get(0), code, "", "c", 8, codegen);
//                    code.add("call isButtonPressed\n");
//                }
//                else
//                {
//                    codegen.mismatchedArgumentNumberError(tree.getLine(), 0, args.getChildren().size());
//                }
//                return 8;
//            }
//            case "wasButtonPressed":
//            {
//                if(args.getChildren().size() == 1)
//                {
//                    generateRegisterLoad(args.getChildren().get(0), code, "", "c", 8, codegen);
//                    code.add("call wasButtonPressed\n");
//                }
//                else
//                {
//                    codegen.mismatchedArgumentNumberError(tree.getLine(), 0, args.getChildren().size());
//                }
//                return 8;
//            }
//            //LCD.asm
//            case "loadPalettes":
//            {
//                if(args.getChildren().size() == 2)
//                {
//                    //This is probably fine, since palettes can only be loaded via ASM references
//                    //Okay, the user COULD use a variable, but that makes rather little sense.
//                    generateRegisterLoad(args.getChildren().get(0), code, "", "a", 8, codegen);
//                    generateRegisterLoad(args.getChildren().get(1), code, "h", "l", 16, codegen);
//                    code.add("call loadPalettes\n");
//                }
//                else
//                {
//                    codegen.mismatchedArgumentNumberError(tree.getLine(), 0, args.getChildren().size());
//                }
//                return 0;
//            }
//            //ScreenPrinter.asm
//            case "print":
//            {
//                if(args.getChildren().size() == 3)
//                {
//                    generateRegisterLoad(args.getChildren().get(0), code, "", "e", 8, codegen);
//                    generateRegisterLoad(args.getChildren().get(1), code, "", "c", 8, codegen);
//                    generateRegisterLoad(args.getChildren().get(2), code, "h", "l", 16, codegen);
//                    code.add("call print\n");
//                }
//                else
//                {
//                    codegen.mismatchedArgumentNumberError(tree.getLine(), 0, args.getChildren().size());
//                }
//                return 0;
//            }
//            case "printNumber":
//            {
//                if(args.getChildren().size() == 3)
//                {
//                    generateRegisterLoad(args.getChildren().get(0), code, "", "c", 8, codegen);
//                    generateRegisterLoad(args.getChildren().get(2), code, "", "d", 8, codegen);
//                    generateRegisterLoad(args.getChildren().get(1), code, "", "l", 8, codegen);
//                    code.add("call printNumber\n");
//                }
//                else
//                {
//                    codegen.mismatchedArgumentNumberError(tree.getLine(), 0, args.getChildren().size());
//                }
//                return 0;
//            }
//            //Sprites.asm
//            case "setSpritePosition":
//            {
//                if(args.getChildren().size() == 3)
//                {
//                    generateRegisterLoad(args.getChildren().get(1), code, "", "b", 8, codegen);
//                    generateRegisterLoad(args.getChildren().get(2), code, "", "c", 8, codegen);
//                    //Generate A last, since A might be trashed when generating any other register!
//                    generateRegisterLoad(args.getChildren().get(0), code, "", "a", 8, codegen);
//                    code.add("call setSpritePosition\n");
//                }
//                else
//                {
//                    codegen.mismatchedArgumentNumberError(tree.getLine(), 0, args.getChildren().size());
//                }
//                return 0;
//            }
//            case "setSpriteTile":
//            {
//                if(args.getChildren().size() == 2)
//                {
//                    generateRegisterLoad(args.getChildren().get(1), code, "", "b", 8, codegen);
//                    //Generate A last, since A might be trashed when generating any other register!
//                    generateRegisterLoad(args.getChildren().get(0), code, "", "a", 8, codegen);
//                    code.add("call setSpriteTile\n");
//                }
//                else
//                {
//                    codegen.mismatchedArgumentNumberError(tree.getLine(), 0, args.getChildren().size());
//                }
//                return 0;
//            }
//            case "setSpriteAttributes":
//            {
//                if(args.getChildren().size() == 2)
//                {
//                    generateRegisterLoad(args.getChildren().get(1), code, "", "b", 8, codegen);
//                    //Generate A last, since A might be trashed when generating any other register!
//                    generateRegisterLoad(args.getChildren().get(0), code, "", "a", 8, codegen);
//                    code.add("call setSpriteAttributes\n");
//                }
//                else
//                {
//                    codegen.mismatchedArgumentNumberError(tree.getLine(), 0, args.getChildren().size());
//                }
//                return 0;
//            }
//            //Tiles.asm
//            //loadTiles has too many arguments
//            case "placeBGTile":
//            {
//                if(args.getChildren().size() == 4)
//                {
//                    generateRegisterLoad(args.getChildren().get(1), code, "", "b", 8, codegen);
//                    generateRegisterLoad(args.getChildren().get(2), code, "", "c", 8, codegen);
//                    generateRegisterLoad(args.getChildren().get(3), code, "", "d", 8, codegen);
//                    //Generate A last, since A might be trashed when generating any other register!
//                    generateRegisterLoad(args.getChildren().get(0), code, "", "a", 8, codegen);
//                    code.add("call setBGTile\n");
//                }
//                else
//                {
//                    codegen.mismatchedArgumentNumberError(tree.getLine(), 0, args.getChildren().size());
//                }
//                return 0;
//            }
//            case "placeWinTile":
//            {
//                if(args.getChildren().size() == 4)
//                {
//                    generateRegisterLoad(args.getChildren().get(1), code, "", "b", 8, codegen);
//                    generateRegisterLoad(args.getChildren().get(2), code, "", "c", 8, codegen);
//                    generateRegisterLoad(args.getChildren().get(3), code, "", "d", 8, codegen);
//                    //Generate A last, since A might be trashed when generating any other register!
//                    generateRegisterLoad(args.getChildren().get(0), code, "", "a", 8, codegen);
//                    code.add("call setWinTile\n");
//                }
//                else
//                {
//                    codegen.mismatchedArgumentNumberError(tree.getLine(), 0, args.getChildren().size());
//                }
//                return 0;
//            }
//            //loadBGMap has too many arguments
//            //loadWinMap has too many arguments
//            case "setBGScroll":
//            {
//                if(args.getChildren().size() == 2)
//                {
//                    generateRegisterLoad(args.getChildren().get(1), code, "", "b", 8, codegen);
//                    //Generate A last, since A might be trashed when generating any other register!
//                    generateRegisterLoad(args.getChildren().get(0), code, "", "a", 8, codegen);
//                    code.add("call setBGScroll\n");
//                }
//                else
//                {
//                    codegen.mismatchedArgumentNumberError(tree.getLine(), 0, args.getChildren().size());
//                }
//                return 0;
//            }
//            case "setWinPosition":
//            {
//                if(args.getChildren().size() == 2)
//                {
//                    generateRegisterLoad(args.getChildren().get(1), code, "", "b", 8, codegen);
//                    //Generate A last, since A might be trashed when generating any other register!
//                    generateRegisterLoad(args.getChildren().get(0), code, "", "a", 8, codegen);
//                    code.add("call setWinPosition\n");
//                }
//                else
//                {
//                    codegen.mismatchedArgumentNumberError(tree.getLine(), 0, args.getChildren().size());
//                }
//                return 0;
//            }
//            case "switchBank":
//            {
//                if(args.getChildren().size() == 1)
//                {
//                    generateRegisterLoad(args.getChildren().get(0), code, "", "a", 8, codegen);
//                    
//                    if(mbcVariant == 5 || mbcVariant == 3)
//                    {
//                        //MBC5 code (also works for MBC3)
//                        code.add("ld [rROMB0], a\n");
//                    }
//                    else if(mbcVariant == 1)
//                    {
//                        //MBC1 code
//                        //Split a - lower 5 bits go into b, high 2 bits (yes, not 3) go into c
//                        code.add("ld b, a\n");
//                        code.add("and $1F");
//                        code.add("ld [rROMB0], a\n");
//                        code.add("ld a, b\n");
//                        code.add("swap a\n"); //Swap upper 4 and lower 4 bits
//                        code.add("rra\n");
//                        code.add("and $03\n");
//                        code.add("ld [rRAMB0], a\n");
//                    }
//                }
//                else
//                {
//                    codegen.mismatchedArgumentNumberError(tree.getLine(), 0, args.getChildren().size());
//                }
//                return 0;
//            }
//            default:
//            {
//                //-1 means we did not generate a method call here
//                return -1;
//            }
//        }
//    }
//    
//    public static StdlibCallGenerator getInstance()
//    {
//        if(instance == null)
//        {
//            instance = new StdlibCallGenerator();
//        }
//        return instance;
//    }
}