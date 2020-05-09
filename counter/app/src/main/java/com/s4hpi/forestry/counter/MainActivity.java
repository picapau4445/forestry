package com.s4hpi.forestry.counter;

import android.content.DialogInterface;
import android.app.ActivityManager;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import java.io.IOException;
import java.util.Locale;

import com.s4hpi.forestry.counter.db.DBTimberOperation;
import com.s4hpi.forestry.counter.db.DBTimberTypeDictionaryOperation;
import com.s4hpi.forestry.counter.dto.Timber;
import com.s4hpi.forestry.counter.util.NetworkUtil;
import com.s4hpi.forestry.counter.service.ListeningService;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnRecyclerListener   {

    private final int requestCode = 1;

    private DBTimberOperation dbTimber;

    private View decorView;
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private SparseArray<Timber> mDataset;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // マスタ関連のDB
        try {
            DBTimberTypeDictionaryOperation.createDBTimberTypeDictionary(this);
        } catch (IOException e) {
            e.printStackTrace();
            //TODO:ダイアログ表示して異常終了
        }

        decorView = getWindow().getDecorView();
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        if(fab != null ) {
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // カウンター用Activity起動のintent作成
                    Intent intent = new Intent(getApplicationContext(), CounterActivity.class);
                    startActivityForResult(intent, requestCode);
                }
            });
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);

        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(mRecyclerView.getContext()));
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        dbTimber = new DBTimberOperation(getApplicationContext());
        // TODO:池田町固定
        mDataset = dbTimber.getTimberDataOrderByRegDate(1, 14, 14);

        mAdapter = new RecyclerAdapter(this, mDataset, this);
        mRecyclerView.setAdapter(mAdapter);

        stopListeningServiceIfRunning();

    }

    private void stopListeningServiceIfRunning() {
        ActivityManager am = (ActivityManager)this.getSystemService(ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo serviceInfo :
                am.getRunningServices(Integer.MAX_VALUE)) {
            // クラス名を比較
            if (serviceInfo.service.getClassName().equals(ListeningService.class.getName())) {
                stopService(new Intent(MainActivity.this, ListeningService.class));
                break;
            }
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            finish();
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        // データ送信
        if (id == R.id.action_send_offline_data) {
            final MenuItem menuItem = item;
            String message;

            // 回線接続判定
            if(!NetworkUtil.isConnected(getApplicationContext())) {
                message = "オフラインです。\n電波状況の良い場所で再度実行してください。";
                Snackbar.make(mRecyclerView, message, Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                return true;
            }

            message = "データ送信を開始します";
            item.setIcon(android.R.drawable.stat_sys_upload);
            Snackbar.make(mRecyclerView, message, Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();

            //非同期送信の開始
            AsyncPost asyncPost = new AsyncPost(1, 14, 14, getApplicationContext(),
                    new AsyncCallback() {
                public void onPreExecute() {
                }
                public void onProgressUpdate(int progress) {
                }
                public void onPostExecute(String result) {
                    String message;
                    menuItem.setIcon(android.R.drawable.stat_sys_upload_done);

                    if(result.equals("error")) {
                        message = "データ送信に失敗しました。";
                    } else {
                        message = "データ送信を完了しました";
                    }
                    Snackbar.make(mRecyclerView, message, Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();

                    mDataset = dbTimber.getTimberDataOrderByRegDate(1, 14, 14);
                    mAdapter.notifyDataSetChanged();
                }
                public void onCancelled() {
                }
            });
            asyncPost.execute();

            return true;
        }

        //noinspection SimplifiableIfStatement
        //TODO:アプリ設定は必要になれば追加する
        //if (id == R.id.action_settings) {
        //    return true;
        //}

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        // このアプリについて
        if (id == R.id.nav_about) {
            displayAbout();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void displayAbout() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.dialog_about, null);

        int versionCode = 0;
        String versionName = "";
        PackageManager packageManager = this.getPackageManager();
        try {
            PackageInfo packageInfo = packageManager.getPackageInfo(this.getPackageName(),
                    PackageManager.GET_ACTIVITIES);
            versionCode = packageInfo.versionCode;
            versionName = packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        TextView textVersion = (TextView)dialogView.findViewById(R.id.text_version);
        textVersion.setText(String.format(Locale.JAPANESE, "バージョン %s ( %d )",
                versionName, versionCode));

        new AlertDialog.Builder(this)
                .setView(dialogView)
                .setTitle(getString(R.string.nav_about))
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog, final int which) {

                    }
                })
                .create()
                .show();
    }

    @Override
    public void onRecyclerClicked(View v, int position) {
      //TODO:クリック時イベントは今後検討;
    }


}
