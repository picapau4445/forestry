package com.s4hpi.forestry.counter.service;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.media.AudioManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.WindowManager;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import com.s4hpi.forestry.counter.CounterActivity;
import com.s4hpi.forestry.counter.R;
import com.s4hpi.forestry.counter.db.DBTimberOperation;
import com.s4hpi.forestry.counter.dto.Timber;
import com.s4hpi.forestry.counter.util.NetworkUtil;
import com.s4hpi.forestry.counter.util.SpeechUtil;

public class ListeningService extends Service implements TextToSpeech.OnInitListener {

    private WindowManager windowManager;
    private ListeningServiceFragment listeningServiceFragment;
    private final  IBinder binder = new ListeningServiceBinder();

    private SpeechRecognizer speechRecognizer;
    private TextToSpeech textToSpeech;
    private SpeechUtil speechUtil;
    private DBTimberOperation dbTimber;

    private AudioManager mAudioManager;
    private int mStreamVolume = 0;

    private int forestGroup = 0;
    private int smallGroup = 0;
    private int height = 0;
    private int display_x = 0;
    private int display_y = 0;

    private Handler guiThreadHandler;

    //TODO:サービスの状況でHomeキーを押されても音声入力が停止しないようにする
    //TODO:タスクkillの後だとサービスからActivityに戻れないので戻れるようにする
    //TODO:音量0の処理は実験段階、不評ならやめる

    @Override
    public void onCreate() {
        textToSpeech = new TextToSpeech(this, this);
        speechUtil = new SpeechUtil(getApplicationContext());
        dbTimber = new DBTimberOperation(getApplicationContext());
        guiThreadHandler = new Handler();
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        mStreamVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public class ListeningServiceBinder extends Binder {
        public ListeningService getService() {
            return ListeningService.this;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        SharedPreferences preferences = getSharedPreferences("service_arg", MODE_PRIVATE);
        this.forestGroup = preferences.getInt("forestGroup", 0);
        this.smallGroup = preferences.getInt("smallGroup", 0);
        this.height = preferences.getInt("height", 0);
        this.display_x = preferences.getInt("display_x", 0);
        this.display_y = preferences.getInt("display_y", 0);

        //Intent notificationIntent = new Intent(this, ListeningService.class);
        //PendingIntent pendingIntent = PendingIntent.getService(this, 0, notificationIntent, 0);
        // サービスを永続化するために、通知を作成する
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext());
        //builder.setContentIntent(pendingIntent);
        builder.setTicker("音声入力サービス開始中...");
        builder.setContentTitle(getString(R.string.app_name));
        builder.setContentText(getString(R.string.service_listening_active));
        builder.setSmallIcon(R.mipmap.ic_launcher);
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(R.string.service_listening_active, builder.build());
        // サービス永続化
        startForeground(R.string.service_listening_active, builder.build());

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, mStreamVolume, 0);

        super.onDestroy();

        if(textToSpeech != null) {
            if (textToSpeech.isSpeaking()) {
                textToSpeech.stop();
            }
            textToSpeech.shutdown();
            textToSpeech = null;
        }
    }

    // TextToSpeechのOverride
    @Override
    public void onInit(int status) {

        if (status == TextToSpeech.SUCCESS) {
            Locale locale = Locale.JAPANESE;

            if (textToSpeech.isLanguageAvailable(locale) >= TextToSpeech.LANG_AVAILABLE) {
                textToSpeech.setLanguage(locale);
            } else {
                Log.d("", "音声合成が使えません");
            }
        } else {
            Log.d("", "音声合成が使えません");
        }
    }

    /*
     * 自ウィンドウの生成
     */
    public void openWindow() {
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_SYSTEM_ALERT, // 画面にタッチできるように SYSTEM_ALERT レイヤーに表示
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | // 下の画面を操作できるようにする
                        WindowManager.LayoutParams.FLAG_FULLSCREEN |
                        WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                PixelFormat.TRANSLUCENT
        );
        params.gravity = Gravity.CENTER;
        params.width = (int)(this.display_x * 0.9);
        params.height = (int)(this.display_y * 0.9);

        listeningServiceFragment = new ListeningServiceFragment();
        windowManager = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);
        windowManager.addView(listeningServiceFragment.loadView(getApplicationContext(), this), params);
    }

    /*
     * 自ウィンドウを閉じる
     */
    public void closeWindow() {
        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, mStreamVolume, 0);
        if (listeningServiceFragment != null && listeningServiceFragment.view != null) {
            windowManager.removeView(listeningServiceFragment.view);
            listeningServiceFragment.view = null;
        }
    }

    /*
     * Activityのウィンドウを開く
     */
    public void openActivityWindow() {
        closeWindow();
        //新規タスクでCounterActivityを起動する
        Intent intent = new Intent(getApplicationContext(), CounterActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("forestGroup", forestGroup);
        intent.putExtra("smallGroup", smallGroup);
        intent.putExtra("height", height);
        startActivity(intent);
    }

    protected void startListening() {
        try {
            if (speechRecognizer != null) {
                speechRecognizer.cancel();
                speechRecognizer.destroy();
                speechRecognizer = null;
            }

            mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0);

            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);

            if (!SpeechRecognizer.isRecognitionAvailable(getApplicationContext())) {
                displayResult("音声認識が使えません");
                stopListening();
                return;
            }

            speechRecognizer.setRecognitionListener(new ListeningService.SpeechRecognizerListener());

            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH);// Web検索モデル
            // TODO:オフラインモードの音声認識の精度は0に等しくなるので、辞書から引き当ては必須
            // 強制的にオフラインモードで使用させる
            //intent.putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true);

            speechRecognizer.startListening(intent);
        } catch (Exception ex) {
            displayResult("音声認識が開始できませんでした");
            stopListening();
        }
    }

    public void stopListening() {
        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, mStreamVolume, 0);
        if (speechRecognizer != null) {
            speechRecognizer.cancel();
            speechRecognizer.destroy();
            speechRecognizer = null;
        }
    }

    public void restartListeningService(int wait) {
        // 音声出力の間は待機、タイムアウトは5秒
        waitSpeaking(wait, 5000);
        startListening();
    }

    /**
     * 音声認識(SpeechRecognizer)用のリスナークラス
     */
    private class SpeechRecognizerListener implements RecognitionListener {

        @Override
        public void onBeginningOfSpeech() {
            Log.d("SRL", "onBeginningOfSpeech");
            setImageAudio();
        }

        @Override
        public void onBufferReceived(byte[] buffer) {
            Log.d("SRL", "onBufferReceived");
        }

        @Override
        public void onEndOfSpeech() {
            Log.d("SRL", "onEndOfSpeech");
            //setImageWait();
        }

        @Override
        public void onError(int error) {
            Log.d("SRL", "onError");
            setImageAudio();
            String reason = "";
            switch (error) {
                case SpeechRecognizer.ERROR_AUDIO:
                    reason = "ERROR_AUDIO";
                    break;
                case SpeechRecognizer.ERROR_CLIENT:
                    reason = "ERROR_CLIENT";
                    break;
                case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                    reason = "ERROR_INSUFFICIENT_PERMISSIONS";
                    break;
                case SpeechRecognizer.ERROR_NETWORK:
                    reason = "ERROR_NETWORK";
                    /* ネットワーク接続をチェックする処理をここに入れる */
                    break;
                case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                    reason = "ERROR_NETWORK_TIMEOUT";
                    break;
                case SpeechRecognizer.ERROR_NO_MATCH:
                    //reason = "ERROR_NO_MATCH";
                    break;
                case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                    reason = "ERROR_RECOGNIZER_BUSY";
                    break;
                case SpeechRecognizer.ERROR_SERVER:
                    reason = "ERROR_SERVER";
                    /* ネットワーク接続をチェックをする処理をここに入れる */
                    break;
                case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                    //reason = "ERROR_SPEECH_TIMEOUT";
                    break;
            }

            if(reason.length() > 0) {
                displayResult(reason);
            }

            mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, mStreamVolume, 0);

            restartListeningService(0);
        }

        @Override
        public void onEvent(int eventType, Bundle params) {
            Log.d("SRL", "onEvent");
        }

        @Override
        public void onPartialResults(Bundle partialResults) {
            Log.d("SRL", "onPartialResults");
        }

        @Override
        public void onReadyForSpeech(Bundle params) {
            Log.d("SRL", "onReadyForSpeech");
            //displayResult("話して下さい");
        }

        @Override
        public void onResults(Bundle results) {
            String convertString;
            final Timber data;
            mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, mStreamVolume, 0);

            ArrayList results_array =
                    results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);

            if (results_array == null || results_array.size() == 0) {
                setImageAudio();
                restartListeningService(0);
                return;
            }

            String resultsString = results_array.get(0).toString();

            if (resultsString.length() == 0) {
                setImageAudio();
                restartListeningService(0);
                return;
            }

            // TODO:音声テキストデータの保存
            //su.saveRealResult(resultsString);

            // TODO:音声テキストデータの変換
            data = speechUtil.convertResultToData(resultsString);

            // TODO:音声入力結果判定は現在甘め
            if(data.getKind() == null && data.getDia() == 0) {
                // nop
            }
            else if(data.getKind() != null && data.getDia() == 0) {
                speechText("「" + resultsString + "」からは、強硬直径が認識できませんでした。", true);
            }
            else if(data.getKind() == null && data.getDia() != 0 &&
                    speechUtil.getPreSpeechTimberType() == null) {
                speechText("「" + resultsString + "」からは、樹種が認識できませんでした。", true);
            }
            else {
                // 変換後テキストデータの音声出力
                convertString = data.toString();
                speechText(convertString, false);

                // TODO:池田町固定
                data.setRegDate((new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.JAPANESE))
                        .format(new Date()));
                data.setUser(1);
                data.setPref(14);
                data.setCity(14);
                data.setForestGroup(forestGroup);
                data.setSmallGroup(smallGroup);
                data.setHeight(height);
                data.setSend(0); // 0:未送信

                // 立木カウントデータのローカル保存
                final long rowId = dbTimber.insert(data);

                // 変換後テキストデータの画面表示
                displayResult(convertString);
                displayRecord(convertString);

                // 立木データ送信
                if(NetworkUtil.isConnected(getApplicationContext())) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                if (doPost(data).equals("1")) {
                                    dbTimber.updateSendStatus(rowId, 1, new Date()); // 1:送信済
                                }
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        }
                    }).start();
                }
            }

            setImageAudio();
            restartListeningService(3000);
        }

        @Override
        public void onRmsChanged(float rmsdB) {
            //Log.d("SRL", "onRmsChanged:" + String.valueOf(rmsdB));
            if(rmsdB > 5) {
                setImageAudioActive();
            }
            else {
                setImageAudioWait();
            }
        }
    }

    public void speechText(CharSequence text, boolean isDisplayToast) {
        if (textToSpeech == null) {
            textToSpeech = new TextToSpeech(getApplication(), new TextToSpeech.OnInitListener() {
                @Override
                public void onInit(int status) {
                    if (status == TextToSpeech.SUCCESS) {
                        Locale locale = Locale.JAPANESE;

                        if (textToSpeech.isLanguageAvailable(locale) >= TextToSpeech.LANG_AVAILABLE) {
                            textToSpeech.setLanguage(locale);
                        } else {
                            displayResult("音声合成が使えません");
                        }
                    } else {
                        displayResult("音声合成が使えません");
                    }
                }
            });
        }
        if (text.length() > 0) {
            if (textToSpeech.isSpeaking()) {
                textToSpeech.stop();
            }

            if (isDisplayToast) {
                Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
            }

            textToSpeech.speak((String) text, android.speech.tts.TextToSpeech.QUEUE_FLUSH, null);
        }
    }


    private void setImageWait() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                guiThreadHandler.post(new Runnable(){
                    @Override
                    public void run() {
                        listeningServiceFragment.setImageWait();
                    }
                });
            }
        }).start();
    }

    private void setImageAudio() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                guiThreadHandler.post(new Runnable(){
                    @Override
                    public void run() {
                        listeningServiceFragment.setImageAudio();
                    }
                });
            }
        }).start();
    }

    private void setImageAudioActive() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                guiThreadHandler.post(new Runnable(){
                    @Override
                    public void run() {
                        listeningServiceFragment.setImageAudioActive();
                    }
                });
            }
        }).start();
    }

    private void setImageAudioWait() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                guiThreadHandler.post(new Runnable(){
                    @Override
                    public void run() {
                        listeningServiceFragment.setImageAudioWait();
                    }
                });
            }
        }).start();
    }

    public void displayResult(final String result) {
        // 結果エリアに表示
        new Thread(new Runnable() {
            @Override
            public void run() {
                listeningServiceFragment.guiThreadHandler.post(new Runnable(){
                    @Override
                    public void run() {
                        listeningServiceFragment.setTextResult(result);
                    }
                });
            }
        }).start();
    }

    public void displayRecord(final String result) {
        // 結果エリアに表示
        new Thread(new Runnable() {
            @Override
            public void run() {
                listeningServiceFragment.guiThreadHandler.post(new Runnable(){
                    @Override
                    public void run() {
                        listeningServiceFragment.setTextRecord(result);
                    }
                });
            }
        }).start();
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
                + "\"height\":" + data.getHeight() + ", "
                + "\"dia\":" + data.getDia() + ", "
                + "\"volume\":" + 0
                + "}";

        try {

            HttpURLConnection con;
            URL url = new URL(getApplicationContext().getString(R.string.url));
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

    /**
     * TextToSpeechが音声出力中は待機する
     * ただし音声出力が終了していたとしても引数waitに指定した秒数は待機する
     * 待機時間超過によるプログラム停止を防ぐため、引数timeoutで指定した
     * 停止時間が過ぎた場合は待機を終了する。
     * @param wait 最低限待機する秒数
     * @param timeout 待機時間のタイムアウト秒数
     */
    public void waitSpeaking(int wait, int timeout) {
        if (textToSpeech != null) {
            final int sleepMilliSec;
            sleepMilliSec = 500;
            int elapsed = 0;

            while(elapsed < timeout) {

                if(!textToSpeech.isSpeaking() && elapsed >= wait) {
                    break;
                }

                elapsed = elapsed + sleepMilliSec;

                try {
                    Thread.sleep(sleepMilliSec);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
