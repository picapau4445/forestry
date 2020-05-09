package com.s4hpi.forestry.counter;

import android.content.Context;
import android.os.AsyncTask;
import android.util.SparseArray;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.Exception;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;

import com.s4hpi.forestry.counter.db.DBTimberOperation;
import com.s4hpi.forestry.counter.dto.Timber;

public class AsyncPost extends AsyncTask<String, Integer, String> {

    private Context context;
    private AsyncCallback _asyncCallback = null;
    private DBTimberOperation dbTimber;
    private int user;
    private int pref;
    private int city;

    public AsyncPost(int user, int pref, int city, Context context, AsyncCallback asyncCallback) {
        this.context = context;
        this._asyncCallback = asyncCallback;
        this.user = user;
        this.pref = pref;
        this.city = city;
        dbTimber = new DBTimberOperation(context);
    }

    protected String doInBackground(String... dummy) {

        SparseArray<Timber> data = dbTimber.getTimberDataNotSend(user, pref, city);
        if(data == null) {
            return "error";
        }

        for (int i = 0; i < data.size(); i++) {
            Timber item = data.valueAt(i);
            try {
                if(doPost(item).equals("1")) {
                    dbTimber.updateSendStatus(item.getRowId(), 1, new Date());
                }
            } catch (IOException e) {
                return "error";
            }
        }

        return "success";
    }

    protected void onPreExecute() {
        super.onPreExecute();
        this._asyncCallback.onPreExecute();
    }

    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
        this._asyncCallback.onProgressUpdate(values[0]);
    }

    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        this._asyncCallback.onPostExecute(result);
    }

    protected void onCancelled() {
        super.onCancelled();
        this._asyncCallback.onCancelled();
    }

    public String doPost(Timber data) throws IOException {
        final String json = "{"
                + "\"pref\":" + data.getPref() + ", "
                + "\"city\":" + data.getCity() + ", "
                + "\"rinpan\":" + data.getForestGroup() + ", "
                + "\"shohan\":" + data.getSmallGroup() + ", "
                + "\"lat\":" + 0 + ", "
                + "\"lon\":" + 0 + ", "
                + "\"kind\":\"" + data.getKind() + "\", "
                + "\"height\":" + 0 + ", "
                + "\"dia\":" + data.getDia() + ", "
                + "\"volume\":" + 0
                + "}";

        try {

            HttpURLConnection con;
            URL url = new URL(context.getString(R.string.url));
            con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            con.setInstanceFollowRedirects(false);
            con.setRequestProperty("Accept-Language", "jp");
            con.setDoOutput(true);
            con.setRequestProperty("Content-Type", "application/json; charset=utf-8");
            OutputStream os = con.getOutputStream();
            PrintStream ps = new PrintStream(os);
            ps.print(json);
            ps.close();

            String buffer;
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(con.getInputStream(), "UTF-8"));
            buffer = reader.readLine();
            System.out.println(buffer);

            con.disconnect();
            return buffer;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "";
    }


}

