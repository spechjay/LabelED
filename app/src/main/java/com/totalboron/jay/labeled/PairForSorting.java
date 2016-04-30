package com.totalboron.jay.labeled;

import java.io.File;
import java.util.Comparator;

/**
 * Created by Jay on 20/04/16.
 */
public class PairForSorting implements Comparable
{
    public File fi;
    public long time;
    public PairForSorting(File fi)
    {
        this.fi=fi;
        time=fi.lastModified();
    }

    @Override
    public int compareTo(Object object)
    {
        long t=((PairForSorting)object).time;
        return time<t?1:time==t?0:-1;
    }
}
