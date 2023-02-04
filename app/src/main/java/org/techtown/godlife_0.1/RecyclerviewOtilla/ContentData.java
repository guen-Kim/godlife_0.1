package org.techtown.withotilla2.RecyclerviewOtilla;

import android.graphics.Bitmap;

public class ContentData {
    public Bitmap Bmp;
    public String FileName;
    public String FilePath;
    public String FileSize;

    public ContentData()
    {

    }

    public ContentData(Bitmap Bmp, String FileName, String FilePath, String FileSize)
    {
        this.Bmp = Bmp;
        this.FileName = FileName;
        this.FilePath = FilePath;
        this.FileSize = FileSize;
    }
}