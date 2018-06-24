package com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Connection;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

public class JsonConnectionMessageFactory implements IConnectionMessageFactory {
    private final String TAG = "JsonConMessageFactory";

    @Override
    public String buildFileData(long id, String fileName) {
        try {
            JSONObject json = new JSONObject();
            json.put("type", "fileData");
            json.put("payloadId", id);
            json.put("fileName", fileName);
            return json.toString();
        } catch (JSONException e) {
            Log.w(TAG, e.getMessage());
        }
        return null;
    }
}
