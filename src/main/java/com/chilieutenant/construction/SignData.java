package com.chilieutenant.construction;
import java.util.List;

public class SignData {

    private String[] frontLines;
    private String[] backLines;

    public SignData(String[] frontLines, String[] backLines) {
        this.frontLines = frontLines;
        this.backLines = backLines;
    }

    public String[] getFrontLines() {
        return frontLines;
    }

    public String[] getBackLines() {
        return backLines;
    }
    
}
