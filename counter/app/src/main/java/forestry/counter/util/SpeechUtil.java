package forestry.counter.util;

import android.content.Context;
import android.text.TextUtils;

import java.util.HashMap;

import forestry.counter.db.DBTimberTypeDictionaryOperation;
import forestry.counter.dto.Timber;


public class SpeechUtil {

    HashMap<String, String> timberTypeHash;

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

        // 音声入力された生のテキストデータから文字列と数字に分割する
        String strType = resultsString.replaceAll("[0-9]", "");
        String strDiameter = resultsString.replaceAll("[^0-9]", "");

        // TODO:音声入力されたテキストデータから専門用語のみ辞書データから引き当てる
        data.setKind(strType);
        if(timberTypeHash.containsKey(strType)) {
            data.setKind(timberTypeHash.get(strType));
        }
        //else {
        //    return null;
        //}

        if(strDiameter != null && !TextUtils.isEmpty(strDiameter)) {
            try {
                int dia = Integer.parseInt(strDiameter);
                data.setDia(dia);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        else {
            return null;
        }

        return data;
    }

}
