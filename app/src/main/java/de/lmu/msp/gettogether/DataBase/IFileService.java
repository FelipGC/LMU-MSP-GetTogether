package de.lmu.msp.gettogether.DataBase;

import java.io.File;

public interface IFileService {
    void put(String fileName, File file);

    File getFileFor(String fileName);
}
