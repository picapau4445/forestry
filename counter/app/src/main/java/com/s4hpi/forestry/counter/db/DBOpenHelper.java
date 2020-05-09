package com.s4hpi.forestry.counter.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class DBOpenHelper extends SQLiteOpenHelper {

    private static final int DB_VERSION = 1;
    private int m_writableDatabaseCount = 0;
    private static DBOpenHelper m_instance = null;
    private final File m_dataBasePath;
    private final String m_dbName;

    synchronized static
    public DBOpenHelper getInstance( Context context, String dbName )
    {
        if ( m_instance == null )
        {
            m_instance = new DBOpenHelper( context.getApplicationContext(), dbName );
        }
        
        return m_instance;
    }

    public DBOpenHelper( Context context, String dbName )
    {
        super( context, dbName, null, DB_VERSION );
        m_dataBasePath = context.getDatabasePath(dbName);
        m_dbName = dbName;
    }

    @Override
    synchronized public SQLiteDatabase getWritableDatabase()
    {
        SQLiteDatabase db = super.getWritableDatabase();
        if ( db != null )
        {
            ++m_writableDatabaseCount;
        }

        return db;
    }

    synchronized public void closeWritableDatabase( SQLiteDatabase database )
    {
        if ( m_writableDatabaseCount > 0 && database != null )
        {
            --m_writableDatabaseCount;
            if ( m_writableDatabaseCount == 0 )
            {
                database.close();
            }
        }
    }
    
    @Override
    public void onCreate(SQLiteDatabase db) {
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    public void createEmptyDataBase(Context context) throws IOException {
        if (!isExistDB()) {
            getReadableDatabase();

            try {
                copyDBFromAsset(context);

                String dataBasePath = m_dataBasePath.getAbsolutePath();
                SQLiteDatabase checkDb = null;
                try {
                    checkDb = SQLiteDatabase.openDatabase(
                            dataBasePath, null, SQLiteDatabase.OPEN_READWRITE);
                } catch (SQLiteException e) {
                    e.printStackTrace();
                }

                if (checkDb != null) {
                    checkDb.setVersion(DB_VERSION);
                    checkDb.close();
                }

            } catch (IOException e) {
                throw new Error("Error copying database");
            }
        }
    }

    /**
     * 端末内にDBが存在するか確認
     *
     * @return 存在している場合 {@code true}
     */
    private boolean isExistDB() {
        String dbPath = m_dataBasePath.getAbsolutePath();

        SQLiteDatabase checkDb = null;
        try {
            checkDb = SQLiteDatabase.openDatabase(dbPath, null, SQLiteDatabase.OPEN_READONLY);
        } catch (SQLiteException e) {
            // データベースはまだ存在していない
        }

        if (checkDb == null) {
            // データベースはまだ存在していない
            return false;
        }

        if (checkDb.getVersion() == DB_VERSION) {
            checkDb.close();
            return true;
        }

        File f = new File(dbPath);
        f.delete();
        return false;
    }

    /**
     * assetのDBファイルをデータベースパスにコピーする
     */
    private void copyDBFromAsset(Context context) throws IOException{

        // asset 内のデータベースファイルにアクセス
        InputStream mInput = context.getAssets().open(m_dbName);

        // デフォルトのデータベースパスに作成した空のDB
        OutputStream mOutput = new FileOutputStream(m_dataBasePath);

        // コピー
        byte[] buffer = new byte[1024];
        int size;
        while ((size = mInput.read(buffer)) > 0) {
            mOutput.write(buffer, 0, size);
        }

        // Close the streams
        mOutput.flush();
        mOutput.close();
        mInput.close();
    }

}
