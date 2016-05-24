package com.totalboron.jay.labeled;

import java.io.File;
import java.io.Serializable;

/**
 * Created by Jay on 20/05/16.
 */
public class DisplayObject implements Serializable
{
    private File imageFile;
    private File labelFile;
    public DisplayObject(File imageFile, File labelFile)
    {
        this.imageFile = imageFile;
        this.labelFile = labelFile;
    }

    public File getImageFile()
    {
        return imageFile;
    }

    public File getLabelFile()
    {
        return labelFile;
    }

}
