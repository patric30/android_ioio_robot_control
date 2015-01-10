package com.muehlbauer.myrobi;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

class SpreadsheetDataFeed extends AsyncTask<String, Void, String> {

    private static final String TAG = "SpreadsheetDataFeed";

    protected String doInBackground(String... urls)  {
        try {
            URL url= new URL(urls[0]);

            InputStream stream = url.openStream();

            BufferedReader bReader = new BufferedReader(new InputStreamReader(stream, "iso-8859-1"), 8);
            StringBuilder sBuilder = new StringBuilder();

            String line = null;
            while ((line = bReader.readLine()) != null) {
                sBuilder.append(line + "\n");
            }

            stream.close();
            return sBuilder.toString();

        } catch (IOException e) {
            Log.d(TAG, "Spreadsheet Feed Error: " + e.toString());
            return null;
        }
    }

    protected void onPostExecute(String result) {
        //parse JSON data
        result = result.substring(result.indexOf("(") + 1, result.length() - 1);
        try {
            Log.d(TAG, result);
            JSONObject jObject = new JSONObject(result);
            jObject = jObject.getJSONObject("feed");
            JSONArray jArray = jObject.getJSONArray("entry");
            Log.d(TAG, jArray.toString());

            for(int i=0; i < jArray.length(); i++) {

                JSONObject jArrObject = jArray.getJSONObject(i);
                jArrObject = jArrObject.getJSONObject("content");
                String cell = jArrObject.getString("$t");
                Log.d(TAG, "JSON result: " + cell);
            } // End Loop
        } catch (JSONException e) {
            Log.e("JSONException", "JSON Error: " + e.toString());
        } // catch (JSONException e)
    }
}