package com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Utility;


public final class FixedSizeList {

    private final Long[] fixedSizeList = new Long[35];
    private int indexPos = 0;

    public void add(Long nr){
        fixedSizeList[(indexPos++)%35] = nr;
    }
    public boolean contains(Long nr){
        for (int i = 0; i < fixedSizeList.length; i++) {
            if(fixedSizeList[i] == nr)
                return true;
        }
        return false;
    }
}
