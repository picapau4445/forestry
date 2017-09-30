package forestry.counter.util;

import android.content.Context;
import android.text.TextUtils;

import java.util.HashMap;

import forestry.counter.db.DBTreeTypeOperation;
import forestry.counter.dto.Timber;


public class SpeechUtil {

    HashMap<String, String> treeTypeHash;

    public SpeechUtil(Context context) {
        // 辞書データをDBから取得してキャッシュする
        DBTreeTypeOperation db = new DBTreeTypeOperation(context);
        treeTypeHash = db.load();
    }

    public void saveRealResult(String resultsString) {
        // TODO:音声入力された生のテキストデータを保管し、送信する仕組みが欲しい
    }

    public Timber convertResultToData(String resultsString) {

        Timber data = new Timber();

        // TODO:すでにカウントした記録をキャンセルする機能の実装
        // TODO:メモ機能の実装

        // 音声入力された生のテキストデータから文字列と数字に分割する
        String strType = resultsString.replaceAll("[0-9]", "");
        String strDiameter = resultsString.replaceAll("[^0-9]", "");

        // TODO:音声入力されたテキストデータから専門用語のみ辞書データから引き当てる
        if(treeTypeHash.containsKey(strType)) {
            data.setKind(treeTypeHash.get(strType));
        }
        else {
            return null;
        }

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
