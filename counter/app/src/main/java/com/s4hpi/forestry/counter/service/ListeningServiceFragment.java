package com.s4hpi.forestry.counter.service;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import com.s4hpi.forestry.counter.R;

public class ListeningServiceFragment extends Fragment {
    public View view;
    private TextView textResult;
    private TextView textRecord;
    private ImageView imageAudio;
    private ListeningService listeningService;

    Handler guiThreadHandler;

    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss", Locale.JAPANESE);

    public ListeningServiceFragment() {
        guiThreadHandler = new  Handler();
    }

    /*
     * 画面呼び出し
     */
    public View loadView(Context context, ListeningService service) {
        LayoutInflater inflater = LayoutInflater.from(context);
        view =  inflater.inflate(R.layout.fragment_listening, null);
        textResult = (TextView)view.findViewById((R.id.textResult));
        textRecord = (TextView)view.findViewById((R.id.textRecord));
        imageAudio = (ImageView)view.findViewById(R.id.imageAudio);
        Button buttonStop = (Button)view.findViewById(R.id.button_stop);

        buttonStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 音声入力を停止し、サービスを終了する
                listeningService.stopListening();
                listeningService.stopForeground(true);
                listeningService.openActivityWindow();
            }
        });

        listeningService = service;
        listeningService.startListening();

        return view;
    }

    public void setTextResult(String result) {
        textResult.setText(result);
    }

    public void setTextRecord(String result) {
        // 結果エリアに表示
        StringBuilder sbResult = new StringBuilder();
        sbResult.insert(0, textRecord.getText().toString());
        sbResult.append("\n");
        sbResult.append( sdf.format(new Date()));
        sbResult.append(" ");
        sbResult.append(result);
        textRecord.setText(sbResult.toString());
    }

    public void setImageAudio() {
        imageAudio.setImageResource(android.R.drawable.ic_btn_speak_now);
    }

    public void setImageWait() {
        imageAudio.setImageResource(android.R.drawable.presence_audio_online);
        imageAudio.setColorFilter(Color.parseColor("#000000"), PorterDuff.Mode.SRC_IN);
    }

    public void setImageAudioActive() {
        imageAudio.setColorFilter(Color.parseColor("#000000"), PorterDuff.Mode.SRC_IN);
    }

    public void setImageAudioWait() {
        imageAudio.setColorFilter(Color.parseColor("#cccccc"), PorterDuff.Mode.SRC_IN);
    }
}
