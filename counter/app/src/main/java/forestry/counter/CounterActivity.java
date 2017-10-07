package forestry.counter;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
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

import forestry.counter.db.DBTimberOperation;
import forestry.counter.dto.Timber;
import forestry.counter.util.SpeechUtil;

public class CounterActivity extends AppCompatActivity {

    private Context context;

    private SpeechRecognizer sr;
    private TextToSpeech tts;

    private SpeechUtil su;

    private EditText editForestGroup;
    private EditText editSmallGroup;
    private TextView textResult;

    private DBTimberOperation dbTimber;

    private int forestGroup = 0;
    private int smallGroup = 0;
    private String kind;
    private String dia;

    //TODO:TextToSpeechのインスタンス生成で重くなっている→別スレッドか
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_counter);

        su = new SpeechUtil(getApplicationContext());

        dbTimber = new DBTimberOperation(getApplicationContext());

        editForestGroup = new EditText(this);
        editForestGroup = (EditText)findViewById(R.id.editForestGroup);

        editSmallGroup = new EditText(this);
        editSmallGroup = (EditText)findViewById(R.id.editSmallGroup);

        textResult = new TextView(this);
        textResult = (TextView)findViewById((R.id.textResult));

        ImageButton buttonSpeechGroup = (ImageButton)findViewById(R.id.img_button_speech_group);
        if(buttonSpeechGroup != null ) {
            buttonSpeechGroup.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startListeningGroup();
                }
            });
        }

        Button buttonStart = (Button)findViewById(R.id.buttonStart);
        if(buttonStart != null ) {
            buttonStart.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(validateInput()) {
                        startListening();
                    }
                }
            });
        }

        Button buttonStop = (Button)findViewById(R.id.buttonStop);
        if(buttonStop != null ) {
            buttonStop.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    stopListening();
                    clearInput();
                }
            });
        }


        //Intent intent = getIntent();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopListening();

        if(tts != null) {
            if (tts.isSpeaking()) {
                tts.stop();
            }
            tts.shutdown();
            tts = null;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode== KeyEvent.KEYCODE_BACK){
            stopListening();

            if(tts != null) {
                if (tts.isSpeaking()) {
                    tts.stop();
                }
                tts.shutdown();
                tts = null;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    // TextToSpeechのOverride
    //@Override
    public void onInit(int status) {

        if (status == TextToSpeech.SUCCESS) {
            Locale locale = Locale.JAPANESE;

            if (tts.isLanguageAvailable(locale) >= TextToSpeech.LANG_AVAILABLE) {
                tts.setLanguage(locale);
            } else {
                Log.d("", "音声合成が使えません");
            }
        } else {
            Log.d("", "音声合成が使えません");
        }

        // 林班小班を入れてください
        //speechText("りんぱんとしょうはんを入力して、スタートしてください", false);
    }

    public boolean validateInput() {
        String strForestGroup = editForestGroup.getText().toString();
        String strSmallGroup = editSmallGroup.getText().toString();
        int intForestGroup;
        int intSmallGroup;

        if(sr != null) {
            Toast.makeText(getApplicationContext(),
                    "既に音声入力を開始しています", Toast.LENGTH_SHORT).show();
            return false;
        }

        if(TextUtils.isEmpty(strForestGroup) || TextUtils.isEmpty(strSmallGroup) ) {
            Toast.makeText(getApplicationContext(),
                    "林班または小班が未入力です", Toast.LENGTH_SHORT).show();
            return false;
        }

        intForestGroup = Integer.valueOf(strForestGroup);
        intSmallGroup = Integer.valueOf(strSmallGroup);

        // TODO:取得した林班小班の組み合わせが存在するかチェックが必要(マスタ化
        forestGroup = intForestGroup;
        smallGroup = intSmallGroup;

        return true;
    }

    public void clearInput() {
        forestGroup = 0;
        smallGroup = 0;
    }

    public void speechText(CharSequence text, boolean isDisplayToast) {
        if (tts == null) {
            tts = new TextToSpeech(getApplication(), new TextToSpeech.OnInitListener() {
                @Override
                public void onInit(int status) {
                    if (status == TextToSpeech.SUCCESS) {
                        Locale locale = Locale.JAPANESE;

                        if (tts.isLanguageAvailable(locale) >= TextToSpeech.LANG_AVAILABLE) {
                            tts.setLanguage(locale);
                        } else {
                            Log.d("", "音声合成が使えません");
                        }
                    } else {
                        Log.d("", "音声合成が使えません");
                    }

                    // 林班小班を入れてください
                    //speechText("りんぱんとしょうはんを入力して、スタートしてください", false);
                }
            });
        }
        if (text.length() > 0) {
            if (tts.isSpeaking()) {
                tts.stop();
            }

            if (isDisplayToast) {
                Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
            }

            tts.speak((String) text, android.speech.tts.TextToSpeech.QUEUE_FLUSH, null);
        }
    }

    // TODO:サービスとしてバックグラウンド動作するようにする
    protected void startListening() {
        try {
            if (sr != null) {
                sr.cancel();
                sr.destroy();
                sr = null;
            }

            sr = SpeechRecognizer.createSpeechRecognizer(this);
            if (!SpeechRecognizer.isRecognitionAvailable(getApplicationContext())) {
                Toast.makeText(getApplicationContext(), "音声認識が使えません",
                        Toast.LENGTH_LONG).show();
                finish();
            }
            sr.setRecognitionListener(new SpeechRecognizerListener());

            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            // Web検索モデル
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH);
            // TODO:オフラインモードの音声認識の精度は0に等しくなるので、辞書から引き当ては必須
            // 強制的にオフラインモードで使用させる
            //intent.putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true);

            sr.startListening(intent);
        } catch (Exception ex) {
            Toast.makeText(getApplicationContext(), "音声認識が開始できませんでした",
                    Toast.LENGTH_LONG).show();
            finish();
        }
    }

    // TODO:サービスとしてバックグラウンド動作するようにする
    protected void startListeningGroup() {
        try {
            if (sr != null) {
                sr.cancel();
                sr.destroy();
                sr = null;
            }

            sr = SpeechRecognizer.createSpeechRecognizer(this);
            if (!SpeechRecognizer.isRecognitionAvailable(getApplicationContext())) {
                Toast.makeText(getApplicationContext(), "音声認識が使えません",
                        Toast.LENGTH_LONG).show();
                finish();
            }
            sr.setRecognitionListener(new GroupRecognitionListener());

            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            // Web検索モデル
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH);
            // TODO:オフラインモードの音声認識の精度は0に等しくなるので、辞書から引き当ては必須
            // 強制的にオフラインモードで使用させる
            //intent.putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true);

            sr.startListening(intent);
        } catch (Exception ex) {
            Toast.makeText(getApplicationContext(), "音声認識が開始できませんでした",
                    Toast.LENGTH_LONG).show();
            finish();
        }
    }

    protected void stopListening() {
        if (sr != null) {
            sr.cancel();
            sr.destroy();
            sr = null;
            Toast.makeText(getApplicationContext(), "音声入力をストップしました",
                    Toast.LENGTH_SHORT).show();
        }
    }

    public void restartListeningService() {
        // 音声出力の間は待機、最低2秒は待機、タイムアウトは5秒
        waitSpeaking(3000, 5000);
        startListening();
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
        if (tts != null) {
            final int sleepMilliSec;
            sleepMilliSec = 500;
            int elapsed = 0;

            while(elapsed < timeout) {

                if(!tts.isSpeaking() && elapsed >= wait) {
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

    public void displayResult(String resultsString) {
        // 結果エリアに表示
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd hh:mm:ss", Locale.JAPANESE);
        StringBuilder sbResult = new StringBuilder();
        sbResult.insert(0, textResult.getText().toString());
        sbResult.append("\n");
        sbResult.append( sdf.format(new Date()));
        sbResult.append(" ");
        sbResult.append(resultsString);
        textResult.setText(sbResult.toString());
    }

    /**
     * 音声認識(SpeechRecognizer)用のリスナークラス
     */
    class SpeechRecognizerListener implements RecognitionListener {

        public void onBeginningOfSpeech() {
        }

        public void onBufferReceived(byte[] buffer) {
        }

        public void onEndOfSpeech() {
        }

        public void onError(int error) {
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
                    reason = "ERROR_SPEECH_TIMEOUT";
                    break;
            }

            if(reason.length() > 0) {
                Toast.makeText(getApplicationContext(), reason, Toast.LENGTH_SHORT).show();
            }

            restartListeningService();
        }

        public void onEvent(int eventType, Bundle params) {
        }

        public void onPartialResults(Bundle partialResults) {
            ArrayList results_array =
                    partialResults.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);

            if (results_array == null || results_array.size() == 0) {
                restartListeningService();
                return;
            }

            Log.i("SPEAK_PARTIAL", results_array.toString());
        }

        public void onReadyForSpeech(Bundle params) {
            Toast.makeText(getApplicationContext(), "話してください",
                    Toast.LENGTH_SHORT).show();
        }

        public void onResults(Bundle results) {
            String convertString;
            final Timber data;

            ArrayList results_array =
                    results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);

            if (results_array == null || results_array.size() == 0) {
                restartListeningService();
                return;
            }

            Log.i("SPEAK", results_array.toString());

            String resultsString = results_array.get(0).toString();

            if (resultsString.length() == 0) {
                restartListeningService();
                return;
            }

            // TODO:音声テキストデータの保存
            //su.saveRealResult(resultsString);

            // TODO:音声テキストデータの変換
            data = su.convertResultToData(resultsString);

            // 樹種と胸高直径が取得できた場合
            if(data != null) {
                // 変換後テキストデータの音声出力
                convertString = data.toString();
                speechText(convertString, false);

                // TODO:池田町固定
                data.setRegDate(new Date());
                data.setUser(1);
                data.setPref(14);
                data.setCity(14);
                data.setForestGroup(forestGroup);
                data.setSmallGroup(smallGroup);
                data.setSend(1);

                // 立木カウントデータのローカル保存
                final long rowId = dbTimber.insert(data);

                // 変換後テキストデータの画面表示
                displayResult(convertString);

                // 立木データ送信
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            if(doPost(data).equals("1")) {
                                dbTimber.updateSendStatus(rowId, 1, new Date());
                            }
                        } catch (Exception ex) {
                            System.out.println(ex);
                        }
                    }
                }).start();
            }
            else {
                // 正しく認識されなかったメッセージ
                //speechText("樹種と直径が正しく認識できませんでした。もう一度話してください", false);
            }

            restartListeningService();
        }

        public void onRmsChanged(float rmsdB) {
        }
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
     * 音声認識(SpeechRecognizer)用のリスナークラス
     */
    class GroupRecognitionListener implements RecognitionListener {

        public void onBeginningOfSpeech() {
        }

        public void onBufferReceived(byte[] buffer) {
        }

        public void onEndOfSpeech() {
        }

        public void onError(int error) {
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
                    reason = "ERROR_SPEECH_TIMEOUT";
                    break;
            }

            if(reason.length() > 0) {
                Toast.makeText(getApplicationContext(), reason, Toast.LENGTH_SHORT).show();
            }

        }

        public void onEvent(int eventType, Bundle params) {
        }

        public void onPartialResults(Bundle partialResults) {
        }

        public void onReadyForSpeech(Bundle params) {
            Toast.makeText(getApplicationContext(), "音声で「林班○○、小班○○」",
                    Toast.LENGTH_SHORT).show();
        }

        public void onResults(Bundle results) {
            String convertString;
            final int[] data;

            ArrayList results_array =
                    results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);

            if (results_array == null || results_array.size() == 0) {
                Toast.makeText(getApplicationContext(), "林班・小班を認識できませんでした",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            String resultsString = results_array.get(0).toString();

            if (resultsString.length() == 0) {
                Toast.makeText(getApplicationContext(), "林班・小班を認識できませんでした",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            // TODO:音声テキストデータの変換
            data = su.convertResultToGroup(resultsString);

            // 樹種と胸高直径が取得できた場合
            if(data.length == 2) {
                // テキストセットする
                editForestGroup.setText(String.valueOf(data[0]));
                editSmallGroup.setText(String.valueOf(data[1]));
            }
            else {
                // 正しく認識されなかったメッセージ
                //speechText("樹種と直径が正しく認識できませんでした。もう一度話してください", false);
            }

        }

        public void onRmsChanged(float rmsdB) {
        }
    }


}
