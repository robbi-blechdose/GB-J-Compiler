/**
 * The GB-J Compiler
 * Copyright (C) 2019 robbi-blechdose
 * Licensed under GNU AGPLv3
 * (See LICENSE.txt for full license)
 */
package de.jc.gbjc.main.Util;

import java.nio.file.FileSystems;

/**
 *
 * @author robbi-blechdose
 * 
 */
public class Utils
{
    /**
     * Small utility method used for resolving paths of imported files
     * 
     * @param p
     * @return 
     */
    public static String toAbsolutePath(String p)
    {
        return FileSystems.getDefault().getPath(p).normalize().toAbsolutePath().toString();
    }
}