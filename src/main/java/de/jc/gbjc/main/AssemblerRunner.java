/**
 * The GB-J Compiler
 * Copyright (C) 2019 robbi-blechdose
 * Licensed under GNU AGPLv3
 * (See LICENSE.txt for full license)
 */
package de.jc.gbjc.main;

import de.jc.gbjc.main.Util.Logger;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author robbi-blechdose
 * 
 */
public class AssemblerRunner
{
    /**
     * Derived from the equates in hardware.inc<br>
     * Parses a cartridge type as string to an int that can be passed to rbgfix
     * 
     * @param cartType
     * @return
     */
    private static int parseCartType(String cartType)
    {
        switch(cartType)
        {
            case "ROM":
                return 0;
            case "ROM_MBC1":
                return 1;
            case "ROM_MBC1_RAM":
                return 2;
            case "ROM_MBC1_RAM_BAT":
                return 3;
            case "ROM_MBC2":
                return 5;
            case "ROM_MBC2_BAT":
                return 6;
            case "ROM_RAM":
                return 8;
            case "ROM_RAM_BAT":
                return 9;
            case "ROM_MBC3_BAT_RTC":
                return 15;
            case "ROM_MBC3_RAM_BAT_RTC":
                return 16;
            case "ROM_MBC3":
                return 17;
            case "ROM_MBC3_RAM":
                return 18;
            case "ROM_MBC3_RAM_BAT":
                return 19;
            case "ROM_MBC5":
                return 25;
            case "ROM_MBC5_BAT":
                return 26;
            case "ROM_MBC5_RAM_BAT":
                return 27;
            case "ROM_MBC5_RUMBLE":
                return 28;
            case "ROM_MBC5_RAM_RUMBLE":
                return 29;
            case "ROM_MBC5_RAM_BAT_RUMBLE":
                return 30;
            case "ROM_MBC7_RAM_BAT_GYRO":
                return 34;
            case "ROM_POCKET_CAMERA":
                return 252;
            default:
                Logger.getInstance().log(Logger.ERROR, "Incorrect cart type entered: \"" + cartType + "\". Exiting.");
                System.exit(0);
                return 0;
        }
    }
    
    /**
     * Derived from the equates in hardware.inc<br>
     * Parses a cartridge RAM size as string to an int that can be passed to rbgfix
     * 
     * @param ramSize
     * @return 
     */
    private static int parseRamSize(String ramSize)
    {
        switch(ramSize)
        {
            case "NONE":
                return 0;
            case "16K":
                return 1;
            case "64K":
                return 2;
            case "256K":
                return 3;
            case "1M":
                return 4;
            default:
                Logger.getInstance().log(Logger.ERROR, "Incorrect cart ram size entered: \"" + ramSize + "\". Exiting.");
                System.exit(0);
                return 0;
        }
    }
    
    /**
     * Runs the entire RGBDS toolchain
     * 
     * @param filenames The names of the compiled .asm files to be assembled
     * @param romname The name of the ROM to be produced
     * @param cartType The cartridge type set in the ROM
     * @param ramSize The catridge RAM size set in the ROM
     * @param gbcCompat Compatability flag for GBC
     */
    public static void runAssembler(String[] filenames, String romname, String cartType, String ramSize, String gbcCompat)
    {
        String compilerDir = getCompilerDir();
        
        int cart = parseCartType(cartType);
        int ram = parseRamSize(ramSize);
        int gbc = 0;
        
        boolean success = true;
        
        if(gbcCompat.equals("DMG"))
        {
            gbc = 0;
        }
        else if(gbcCompat.equals("DMG_GBC"))
        {
            gbc = 1;
        }
        if(gbcCompat.equals("GBC"))
        {
            gbc = 2;
        }
        
        for(String s : filenames)
        {
            List<String> assemblerCommand = new ArrayList<String>();
            assemblerCommand.add("\"" + compilerDir + "/RGBDS/rgbasm.exe\"");
            assemblerCommand.add("\"-i" + compilerDir + "/\"");
            assemblerCommand.add("-obuild/" + s + ".o");
            assemblerCommand.add("build/" + s + ".asm");
            int exitCode = runProcess(assemblerCommand, Logger.INFO);
            
            if(exitCode != 0)
            {
                success = false;
            }
        }
        
        String fileEnding = ".gb";
        if(gbc == 2)
        {
            fileEnding = ".gbc";
        }
        
        List<String> linkerCommand = new ArrayList<String>();
        linkerCommand.add("\"" + compilerDir + "/RGBDS/rgblink.exe\"");
        linkerCommand.add("-nbuild/" + romname + ".sym");
        linkerCommand.add("-obuild/" + romname + fileEnding);

        for(String s : filenames)
        {
            linkerCommand.add("build/" + s + ".o");
        }

        int exitCode = runProcess(linkerCommand, Logger.INFO);
        if(exitCode != 0)
        {
            success = false;
        }
        
        List<String> fixerCommand = new ArrayList<String>();
        fixerCommand.add("\"" + compilerDir + "/RGBDS/rgbfix.exe\"");
        fixerCommand.add("-p0xFF");
        fixerCommand.add("-v");
        fixerCommand.add("-m" + cart);
        fixerCommand.add("-r" + ram);
        if(gbc == 2)
        {
            fixerCommand.add("-C");
        }
        else if(gbc == 1)
        {
            fixerCommand.add("-c");
        }
        
        fixerCommand.add("build/" + romname + fileEnding);
        
        int exitCodeLink = runProcess(fixerCommand, Logger.INFO);
        if(exitCodeLink != 0)
        {
            success = false;
        }
        
        if(!success)
        {
            Logger.getInstance().log(Logger.ERROR, "RGBDS failed.");
            System.exit(0);
        }
    }
    
    /**
     * Runs the GB-J compiler on another .gbj file<br>
     * Used for .gbj includes
     * 
     * @param name The name of the file to run the compiler on
     */
    public static void runCompiler(String name)
    {
        Logger.getInstance().log(Logger.INFO, "Attempting to run compiler on file " + name + ".");
        
        List<String> command = new ArrayList<String>();
        command.add("java");
        command.add("-jar");
        command.add("\"" + getCompilerDir() + "/GB-J-Compiler.jar\"");
        command.add("-n" + name.substring(0, name.length() - 4));
        command.add("-noasm");
        command.add(name);
        
        int exitCode = runProcess(command, Logger.DIRECT);
        
        if(exitCode != 0)
        {
            Logger.getInstance().log(Logger.ERROR, "Running compiler on file " + name + " failed.");
            System.exit(0);
        }
    }
    
    /**
     * Returns the directory the GB-J Compiler is located in
     * 
     * @return 
     */
    private static String getCompilerDir()
    {
        try
        {
            return new File(Main.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getParent().replace("\\", "/");
        }
        catch(Exception e)
        {
            Logger.getInstance().log(Logger.ERROR, "Error occured while getting compiler dir.");
            Logger.getInstance().log(Logger.ERROR, e);
            System.exit(0);
            return null;
        }
    }
    
    /**
     * Basic idea taken from https://stackoverflow.com/questions/13991007/execute-external-program-in-java (Solution by Steven)<br>
     * Runs a process, captures its output and logs it
     * 
     * @param command
     * @param logLevel
     * @return 
     */
    private static int runProcess(List<String> command, int logLevel)
    {
        if(!System.getProperty("os.name").toLowerCase().contains("win"))
        {
            String str = "";
            for(String s : command)
            {
                str += " " + s.replace(".exe", "");
            }
            command.clear();
            
            command.add(0, "bash");
            command.add(1, "-c");
            command.add(2, str);
        }
        
        Logger.getInstance().log(Logger.INFO, "Attempting to execute command:");
        String str = "";
        for(String s : command)
        {
            str += s + " ";
        }
        Logger.getInstance().log(Logger.INFO, str);
        
        try
        {
            Process process = new ProcessBuilder(command).start();
            
            new Thread(new Runnable()
            {
                public void run()
                {
                    BufferedReader input = new BufferedReader(new InputStreamReader(process.getErrorStream()));
                    String line = null; 

                    try
                    {
                        while((line = input.readLine()) != null)
                        {
                            Logger.getInstance().log(logLevel, line);
                        }
                    }
                    catch (IOException e)
                    {
                        Logger.getInstance().log(Logger.ERROR, e);
                    }
                }
            }).start();
            
            new Thread(new Runnable()
            {
                public void run()
                {
                    BufferedReader input = new BufferedReader(new InputStreamReader(process.getInputStream()));
                    String line = null; 

                    try
                    {
                        while((line = input.readLine()) != null)
                        {
                            Logger.getInstance().log(logLevel, line);
                        }
                    }
                    catch (IOException e)
                    {
                        Logger.getInstance().log(Logger.ERROR, e);
                    }
                }
            }).start();

            process.waitFor();
            
            return process.exitValue();
        }
        catch(Exception e)
        {
            Logger.getInstance().log(Logger.ERROR, "Failed to invoke RGBDS.");
            Logger.getInstance().log(Logger.ERROR, e);
            System.exit(0);
            return 0;
        }
    }
}