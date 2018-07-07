package de.lmu.msp.gettogether.DataBase;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class FileService extends Service implements IFileService {
    private IBinder binder = new FileServiceBinder();
    private Map<String, File> files = new HashMap<>();

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void put(String fileName, File file) {
        files.put(fileName, file);
    }

    @Nullable
    @Override
    public File getFileFor(String fileName) {
        if (!files.containsKey(fileName)) {
            return null;
        }
        return files.get(fileName);
    }

    private class FileServiceBinder extends Binder implements IFileServiceBinder {

        @Override
        public IFileService getService() {
            return FileService.this;
        }
    }
}
