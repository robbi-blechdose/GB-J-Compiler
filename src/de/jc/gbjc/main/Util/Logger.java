/**
 * The GB-J Compiler
 * Copyright (C) 2019 robbi-blechdose
 * Licensed under GNU AGPLv3
 * (See LICENSE.txt for full license)
 */
package de.jc.gbjc.main.Util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.fusesource.jansi.Ansi;
import org.fusesource.jansi.AnsiConsole;

/**
 * Simple logger implementation utilizing the JANSI library for colored output
 *
 * @author robbi-blechdose
 * 
 */
public class Logger
{
    public static Logger instance;
    
    private SimpleDateFormat sdf =  new SimpleDateFormat("yyyy.MM.dd HH.mm.ss");
    
    public static final int DIRECT = -1;
    public static final int INFO = 0;
    public static final int FINE = 1;
    public static final int WARNING = 2;
    public static final int ERROR = 3;
    
    public Logger()
    {
        AnsiConsole.systemInstall();
    }
    
    public void log(int level, String s)
    {
        String toLog = sdf.format(new Date()) + " ";
        
        switch(level)
        {
            case DIRECT:
            {
                toLog = s;
                break;
            }
            case INFO:
            {
                toLog += "INFO: " + Ansi.ansi().fgDefault().a(s).reset().toString();
                break;
            }
            case FINE:
            {
                toLog += "FINE: " + Ansi.ansi().fgBrightGreen().a(s).reset().toString();
                break;
            }
            case WARNING:
            {
                toLog += "WARN: " + Ansi.ansi().fgBrightYellow().a(s).reset().toString();
                break;
            }
            case ERROR:
            {
                toLog += "ERR:  " + Ansi.ansi().fgBrightRed().a(s).reset().toString();
                break;
            }
        }
        
        System.out.println(toLog);
    }
    
    public void log(int level, Exception e)
    {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        log(level, sw.toString());
    }
    
    public static Logger getInstance()
    {
        if(instance == null)
        {
            instance = new Logger();
        }
        return instance;
    }
    
    public void close()
    {
        AnsiConsole.systemUninstall();
    }
}