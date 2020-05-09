package com.s4hpi.forestry.counter.util;

import android.content.Context;
import android.text.TextUtils;

import java.util.HashMap;

import com.s4hpi.forestry.counter.db.DBTimberTypeDictionaryOperation;
import com.s4hpi.forestry.counter.dto.Timber;


public class SpeechUtil {

    private HashMap<String, String> timberTypeHash;
    private String preSpeechTimberType = null;

    public SpeechUtil(Context context) {
        // 辞書データをDBから取得してキャッシュする
        DBTimberTypeDictionaryOperation db = new DBTimberTypeDictionaryOperation(context);
        timberTypeHash = db.load();
    }

    public void saveRealResult(String resultsString) {
        // TODO:音声入力された生のテキストデータを保管し、送信する仕組みが欲しい
    }

    public int[] convertResultToGroup(String resultsString) {
        int[] group = {174, 13};
        return group;
    }

    public Timber convertResultToData(String resultsString) {

        Timber data = new Timber();

        // TODO:すでにカウントした記録をキャンセルする機能の実装
        // TODO:メモ機能の実装
        // TODO:樹高を音声で変更する機能

        // 音声入力された生のテキストデータから文字列と数字に分割する
        String strType = resultsString.replaceAll("[0-9]", "");
        String strDiameter = resultsString.replaceAll("[^0-9]", "");

        if(strDiameter != null && !TextUtils.isEmpty(strDiameter)) {
            try {
                int dia = Integer.parseInt(strDiameter);
                data.setDia(dia);
            } catch (NumberFormatException e) {
                data.setDia(0);
            }
        }
        else {
            data.setDia(0);
        }

        // TODO:樹種の判定
        data.setKind(strType);

        // 樹種の音声入力が省略されている場合、前回の樹種があればセットする
        if(strType.equals("")) {
            if(this.preSpeechTimberType != null) {
                data.setKind(this.preSpeechTimberType);
            }
            else {
                data.setKind(null);
            }
        }
        else {
            // 辞書データとの照合
            if (timberTypeHash.containsKey(strType)) {
                data.setKind(timberTypeHash.get(strType));
            }
            //else {
            //    return null;
            //}
        }

        // 樹種を記憶し、次回の音声入力での省略時に使用する
        preSpeechTimberType = data.getKind();

        return data;
    }

    public String getPreSpeechTimberType() {
        return preSpeechTimberType;
    }
}
