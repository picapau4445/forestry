package com.s4hpi.forestry.counter;

import android.Manifest;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.s4hpi.forestry.counter.service.ListeningService;

public class CounterActivity extends AppCompatActivity {

    private static final int ACTION_RECORD_AUDIO_PERMISSION_REQUEST_CODE = 87;
    private static final int ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE = 88;
    private ListeningService listeningService;
    private ServiceConnection serviceConnection;

    private View decorView;
    private EditText editForestGroup;
    private EditText editSmallGroup;
    private EditText editHeight;

    private int forestGroup = 0;
    private int smallGroup = 0;
    private int height = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_counter);

        decorView = getWindow().getDecorView();

        this.serviceConnection = getServiceConnection((Activity)this);

        editForestGroup = new EditText(this);
        editForestGroup = (EditText)findViewById(R.id.editForestGroup);

        editSmallGroup = new EditText(this);
        editSmallGroup = (EditText)findViewById(R.id.editSmallGroup);

        editHeight = new EditText(this);
        editHeight = (EditText)findViewById(R.id.editHeight);

        Button buttonStart = (Button)findViewById(R.id.buttonStart);
        if(buttonStart != null ) {
            buttonStart.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(validateInput()) {
                        if(checkPermission()) {
                            startListeningService();
                        }
                    }
                }
            });
        }

        Intent intent = getIntent();
        int forestGroup = intent.getIntExtra("forestGroup", 0);
        int smallGroup = intent.getIntExtra("smallGroup", 0);
        int height = intent.getIntExtra("height", 0);

        if (forestGroup != 0) {
            editForestGroup.setText(String.valueOf(forestGroup));
        }
        if (smallGroup != 0) {
            editSmallGroup.setText(String.valueOf(smallGroup));
        }
        if (height != 0) {
            editHeight.setText(String.valueOf(height));
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (resultCode) {
            case ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE:
                if(checkPermission()) {
                    startListeningService();
                }
            default:
                // nop
        }
    }

    @Override
    protected void onResume() {
        // 再度ServiceからActivityへ戻ってきたことを考慮する
        if (listeningService != null) {
            listeningService.closeWindow();
            listeningService.stopListening();
            stopService(new Intent(this, ListeningService.class));
            listeningService = null;
        }
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode== KeyEvent.KEYCODE_BACK){
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }
        return super.onKeyDown(keyCode, event);
    }

    public boolean validateInput() {
        String strForestGroup = editForestGroup.getText().toString();
        String strSmallGroup = editSmallGroup.getText().toString();
        String strHeight = editHeight.getText().toString();
        int intForestGroup;
        int intSmallGroup;
        int intHeight;

        if(TextUtils.isEmpty(strForestGroup) ) {
            Toast.makeText(getApplicationContext(),
                    "林班が未入力です", Toast.LENGTH_SHORT).show();
            return false;
        }

        if(TextUtils.isEmpty(strSmallGroup) ) {
            Toast.makeText(getApplicationContext(),
                    "小班が未入力です", Toast.LENGTH_SHORT).show();
            return false;
        }

        if(TextUtils.isEmpty(strHeight)) {
            Toast.makeText(getApplicationContext(),
                    "樹高が未入力です", Toast.LENGTH_SHORT).show();
            return false;
        }

        intForestGroup = Integer.valueOf(strForestGroup);
        intSmallGroup = Integer.valueOf(strSmallGroup);
        intHeight = Integer.valueOf(strHeight);

        // TODO:取得した林班小班の組み合わせが存在するかチェックが必要(マスタ化
        forestGroup = intForestGroup;
        smallGroup = intSmallGroup;
        height = intHeight;

        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {

        if (requestCode == ACTION_RECORD_AUDIO_PERMISSION_REQUEST_CODE) {
            if (grantResults.length != 1 ||
                    grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.RECORD_AUDIO)) {
                    new AlertDialog.Builder(this)
                            .setTitle("パーミッション取得エラー")
                            .setMessage("再試行する場合は、再度ボタンを押してください")
                            .setPositiveButton(android.R.string.ok,
                                    new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                }
                            })
                            .create()
                            .show();
                } else {
                    new AlertDialog.Builder(this)
                            .setTitle("パーミッション取得エラー")
                            .setMessage("今後は許可しないが選択されました。" +
                                    "アプリ設定＞権限をチェックしてください")
                            .setPositiveButton(android.R.string.ok,
                                    new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    openApplicationDetailSettings();
                                }
                            })
                            .create()
                            .show();
                }
            } else {
                if(checkPermission()) {
                    startListeningService();
                }
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void openApplicationDetailSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getPackageName(), null);
        intent.setData(uri);
        startActivity(intent);
    }

    private void openManageOverlayPermissionSettings() {
        if (Build.VERSION.SDK_INT >= 23 && !Settings.canDrawOverlays(this)) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            this.startActivityForResult(intent, ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE);
        }
    }

    private boolean checkPermission() {
        // 音声入力の許可チェック
        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.RECORD_AUDIO},
                    ACTION_RECORD_AUDIO_PERMISSION_REQUEST_CODE);
            return false;
        }

        // オーバーレイ表示の許可チェック
        if (Build.VERSION.SDK_INT >= 23 && !Settings.canDrawOverlays(this)) {
            new AlertDialog.Builder(this)
                    .setTitle("他のアプリの上への表示許可")
                    .setMessage("このアプリで音声入力を行う際は、他のアプリの上への表示許可が必要です")
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            openManageOverlayPermissionSettings();
                        }
                    })
                    .create()
                    .show();

            return false;
        }

        return true;
    }

    private void startListeningService() {
        InputMethodManager inputMethodMgr =
                (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
        inputMethodMgr.hideSoftInputFromWindow(
                decorView.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

        Intent intent = new Intent(CounterActivity.this, ListeningService.class);
        Point point = new Point();
        this.getWindowManager().getDefaultDisplay().getSize(point);
        SharedPreferences preferences = getSharedPreferences("service_arg", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("forestGroup", forestGroup);
        editor.putInt("smallGroup", smallGroup);
        editor.putInt("height", height);
        editor.putInt("display_x", point.x);
        editor.putInt("display_y", point.y);
        editor.apply();

        // Lollipop 以前用の処理
        startService(intent);
        // Binder によるサービス用ウィンドウとの接続
        bindService(intent, serviceConnection, Context.BIND_ABOVE_CLIENT);
    }

    private ServiceConnection getServiceConnection(final Activity activity) {
        return new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                listeningService = ((ListeningService.ListeningServiceBinder) iBinder).getService();
                listeningService.openWindow();
                unbindService(serviceConnection);
                activity.moveTaskToBack(true);
            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {
            }
        };
    }
}
