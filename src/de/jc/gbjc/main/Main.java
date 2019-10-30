/**
 * The GB-J Compiler
 * Copyright (C) 2019 robbi-blechdose
 * Licensed under GNU AGPLv3
 * (See LICENSE.txt for full license)
 */
package de.jc.gbjc.main;

import de.jc.gbjc.main.Parsing.AbstractSyntaxTree;
import de.jc.gbjc.main.Parsing.Parser;
import de.jc.gbjc.main.Util.Logger;
import de.jc.gbjc.main.Util.Utils;
import de.jc.gbjc.main.Codegen.CodeGenerator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * The GB-J Compiler
 *
 * @author robbi-blechdose
 * 
 */
public class Main
{
    /**
     * Main method
     * 
     * @param args 
     */
    public static void main(String[] args)
    {
        boolean runAssembler = true;
        boolean debug = false;
        int optimizerPasses = 2;
        int mbcVariant = 5;
        boolean objectWarn = true;
        
        Logger.getInstance().log(Logger.INFO, "GB-J Compiler Copyright (C) 2019 robbi-blechdose");
        Logger.getInstance().log(Logger.INFO, "Starting GB-J Compiler.");
        
        long startTime = System.currentTimeMillis();
        
        //Get the files to compile as well as the arguments
        List<String> fileNames = new ArrayList<String>();
        List<String> filePaths = new ArrayList<String>();
        List<String> arguments = new ArrayList<String>();
        
        for(String s : args)
        {
            if(s.startsWith("-"))
            {
                if(s.equals("-help"))
                {
                    displayHelp();
                    System.exit(0);
                }
                else if(s.equals("-debug"))
                {
                    debug = true;
                }
                else if(s.equals("-noasm"))
                {
                    runAssembler = false;
                }
                else if(s.equals("-noObjWarn"))
                {
                    objectWarn = false;
                }
                else if(s.startsWith("-O"))
                {
                    optimizerPasses = Integer.parseInt(s.substring(2));
                }
                else if(s.startsWith("-mbc"))
                {
                    mbcVariant = Integer.parseInt(s.substring(4));
                }
                else
                {
                    arguments.add(s);
                }
            }
            else
            {
                String path = Utils.toAbsolutePath(s);
                Logger.getInstance().log(Logger.INFO, path);
                filePaths.add(path);
                
                fileNames.add(s.substring(0, s.length() - 4));
            }
        }
        
        if(fileNames.isEmpty())
        {
            Logger.getInstance().log(Logger.FINE, "No files to compile. Try the -help option to learn about the usage of this compiler.");
            Logger.getInstance().log(Logger.FINE, "Exiting...");
            System.exit(0);
        }
        
        for(int i = 0; i < fileNames.size(); i++)
        {
            List<Token> tokens = Lexer.lex(filePaths.get(i));
            if(debug)
            {
                Lexer.printTokenList(tokens);
            }

            PreliminaryParser pp = new PreliminaryParser(tokens);
            pp.addStdlibMethods();
            pp.parse();
            
            if(debug)
            {
                Logger.getInstance().log(Logger.INFO, "Static Methods: " + Arrays.toString(pp.getStaticMethods().toArray()));
                Logger.getInstance().log(Logger.INFO, "Object Types: " + Arrays.toString(pp.getObjectTypes().toArray()));
                Logger.getInstance().log(Logger.INFO, "Object Sizes: " + Arrays.toString(pp.getObjectSizes().toArray()));
                Logger.getInstance().log(Logger.INFO, "Object Constructors: " + Arrays.toString(pp.getObjectConstructors().toArray()));
                Logger.getInstance().log(Logger.INFO, "Object Methods: " + Arrays.toString(pp.getObjectMethods().toArray()));
                Logger.getInstance().log(Logger.INFO, "Object Variable Positions: " + Arrays.toString(pp.getObjectVariablePositions().toArray()));
                Logger.getInstance().log(Logger.INFO, "Object Instances: " + Arrays.toString(pp.getObjectInstances().toArray()));
            }

            Parser p = new Parser(tokens, pp.getObjectTypes(), objectWarn);
            AbstractSyntaxTree ast = p.parse();
            if(debug)
            {
                Logger.getInstance().log(Logger.INFO, ast.toString());
            }

            CodeGenerator cg = new CodeGenerator(ast, fileNames.get(i), pp, optimizerPasses, mbcVariant);
            //"runAssembler" also determines whether the ROM header is included
            cg.generateAsmFile(runAssembler);
        }
        
        String carttype = "";
        String ramsize = "";
        String gbtype = "";
        String romname = "";
        
        for(String s : arguments)
        {
            if(s.startsWith("-c"))
            {
                carttype = s.substring(2);
            }
            else if(s.startsWith("-r"))
            {
                ramsize = s.substring(2);
            }
            else if(s.startsWith("-g"))
            {
                gbtype = s.substring(2);
            }
            else if(s.startsWith("-n"))
            {
                romname = s.substring(2);
            }
        }
        
        if(runAssembler)
        {
            AssemblerRunner.runAssembler(fileNames.toArray(new String[0]), romname, carttype, ramsize, gbtype);
        }
        
        Logger.getInstance().log(Logger.FINE, "Compilation finished in " + (System.currentTimeMillis() - startTime) + "ms.");
        
        Logger.getInstance().close();
    }
    
    /**
     * Prints out the entire help
     */
    private static void displayHelp()
    {
        Logger.getInstance().log(Logger.INFO, "Usage: java -jar GB-J-Compiler.jar"
                + " [-cCarttype] [-rRamsize] [-gGBType] [-nROMName] [-mbcX] [-ONumberOfOptimizerPasses] [-debug] [-noasm] sourcefile [...]");
        Logger.getInstance().log(Logger.INFO, "");
        Logger.getInstance().log(Logger.INFO, "Available cart types:");
        Logger.getInstance().log(Logger.INFO, "ROM");
        Logger.getInstance().log(Logger.INFO, "ROM_MBC1");
        Logger.getInstance().log(Logger.INFO, "ROM_MBC1_RAM");
        Logger.getInstance().log(Logger.INFO, "ROM_MBC1_RAM_BAT");
        Logger.getInstance().log(Logger.INFO, "ROM_MBC2");
        Logger.getInstance().log(Logger.INFO, "ROM_MBC2_BAT");
        Logger.getInstance().log(Logger.INFO, "ROM_RAM");
        Logger.getInstance().log(Logger.INFO, "ROM_RAM_BAT");
        Logger.getInstance().log(Logger.INFO, "ROM_MBC3_BAT_RTC");
        Logger.getInstance().log(Logger.INFO, "ROM_MBC3_RAM_BAT_RTC");
        Logger.getInstance().log(Logger.INFO, "ROM_MBC3");
        Logger.getInstance().log(Logger.INFO, "ROM_MBC3_RAM");
        Logger.getInstance().log(Logger.INFO, "ROM_MBC3_RAM_BAT");
        Logger.getInstance().log(Logger.INFO, "ROM_MBC5");
        Logger.getInstance().log(Logger.INFO, "ROM_MBC5_BAT");
        Logger.getInstance().log(Logger.INFO, "ROM_MBC5_RAM_BAT");
        Logger.getInstance().log(Logger.INFO, "ROM_MBC5_RUMBLE");
        Logger.getInstance().log(Logger.INFO, "ROM_MBC5_RAM_RUMBLE");
        Logger.getInstance().log(Logger.INFO, "ROM_MBC5_RAM_BAT_RUMBLE");
        Logger.getInstance().log(Logger.INFO, "ROM_MBC7_RAM_BAT_GYRO");
        Logger.getInstance().log(Logger.INFO, "ROM_POCKET_CAMERA");
        Logger.getInstance().log(Logger.INFO, "");
        Logger.getInstance().log(Logger.INFO, "Available RAM sizes:");
        Logger.getInstance().log(Logger.INFO, "NONE");
        Logger.getInstance().log(Logger.INFO, "16K");
        Logger.getInstance().log(Logger.INFO, "64K");
        Logger.getInstance().log(Logger.INFO, "256K");
        Logger.getInstance().log(Logger.INFO, "1M");
        Logger.getInstance().log(Logger.INFO, "");
        Logger.getInstance().log(Logger.INFO, "Available GB Types:");
        Logger.getInstance().log(Logger.INFO, "DMG");
        Logger.getInstance().log(Logger.INFO, "DMG_GBC");
        Logger.getInstance().log(Logger.INFO, "GBC");
        Logger.getInstance().log(Logger.INFO, "");
        Logger.getInstance().log(Logger.INFO, "Available MBC variants:");
        Logger.getInstance().log(Logger.INFO, "MBC5 (up to 255 banks @ 16K)");
        Logger.getInstance().log(Logger.INFO, "MBC3 (up to 127 banks @ 16K)");
        Logger.getInstance().log(Logger.INFO, "MBC1 (up to 125 banks @ 16K)");
        Logger.getInstance().log(Logger.INFO, "");
        Logger.getInstance().log(Logger.INFO, "-debug is for debugging the compiler, it will output the token list and the AST.");
        Logger.getInstance().log(Logger.INFO, "-noasm skips running the assembler. It is used internally, but you can use it if you have some reason to.");
        Logger.getInstance().log(Logger.INFO, "-noObjWarn disables the warning when using objects. This should only be used if you know what you're doing.");
    }
}