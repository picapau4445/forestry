package com.s4hpi.forestry.counter;

public interface AsyncCallback {

    void onPreExecute();
    void onPostExecute(String result);
    void onProgressUpdate(int progress);
    void onCancelled();

}
