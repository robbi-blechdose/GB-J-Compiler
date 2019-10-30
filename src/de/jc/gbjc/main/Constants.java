/**
 * The GB-J Compiler
 * Copyright (C) 2019 robbi-blechdose
 * Licensed under GNU AGPLv3
 * (See LICENSE.txt for full license)
 */
package de.jc.gbjc.main;

import java.util.HashMap;
import java.util.Map;

/**
 * This is basically just one giant map
 *
 * @author robbi-blechdose
 * 
 */
public class Constants
{
    private static final Map<String, String> constants;
    
    static
    {
        constants = new HashMap<String, String>();
        constants.put("BUTTON_A", "%00000001");
        constants.put("BUTTON_B", "%00000010");
        constants.put("BUTTON_SELECT", "%00000100");
        constants.put("BUTTON_START", "%00001000");
        constants.put("BUTTON_RIGHT", "%00010000");
        constants.put("BUTTON_LEFT", "%00100000");
        constants.put("BUTTON_UP", "%01000000");
        constants.put("BUTTON_DOWN", "%10000000");
        //Constructed from hardware.inc
        constants.put("LCD_OFF", "0");
        constants.put("LCD_ON", "1");
        constants.put("WIN_MAP_9800", "0");
        constants.put("WIN_MAP_9C00", "1");
        constants.put("WIN_OFF", "0");
        constants.put("WIN_ON", "1");
        constants.put("BG_TILES_8800", "0");
        constants.put("BG_TILES_8000", "1");
        constants.put("BG_MAP_9800", "0");
        constants.put("BG_MAP_9C00", "1");
        constants.put("OBJ8", "0");
        constants.put("OBJ16", "1");
        constants.put("OBJ_OFF", "0");
        constants.put("OBJ_ON", "1");
        constants.put("BG_OFF", "0");
        constants.put("BG_ON", "1");
        //Palettes
        constants.put("PAL_BG", "0");
        constants.put("PAL_OBJ", "1");
    }
    
    /**
     * @return The map of constants that can be used in .gbj source files
     */
    public static Map<String, String> getContants()
    {
        return constants;
    }
    
    public static final int CHAR_SIZE = 8;
    public static final int INT_SIZE = 16;
    public static final int POINTER_SIZE = 16;
}