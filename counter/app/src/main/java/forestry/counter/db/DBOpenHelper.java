package forestry.counter.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Debug;

public class DBOpenHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "forestry.counter.db";
    private static final String TABLE_OFFLINE_DATA_CREATE =
            "CREATE TABLE IF NOT EXISTS timber "
            + "("
            + "id integer primary key autoincrement,"
            + "user integer,"
            + "pref integer,"
            + "city integer,"
            + "forestGroup integer,"
            + "smallGroup integer,"
            + "lat real,"
            + "lon real,"
            + "kind text,"
            + "height integer,"
            + "dia integer,"
            + "volume integer"
            + ")";
    private static final String TABLE_TREE_TYPE_CREATE =
            "CREATE TABLE IF NOT EXISTS tree_type_dictionary "
            + "("
            + "id integer primary key autoincrement,"
            + "source text,"
            + "destination text"
            + ")";

    private static final int DB_VERSION = 1;
    private int m_writableDatabaseCount = 0;

    private static DBOpenHelper m_instance = null;

    synchronized static
    public DBOpenHelper getInstance( Context context )
    {
        if ( m_instance == null )
        {
            m_instance = new DBOpenHelper( context.getApplicationContext() );
        }
        
        return m_instance;
    }

    public DBOpenHelper( Context context )
    {
        super( context, DB_NAME, null, DB_VERSION );
    }

    @Override
    synchronized public SQLiteDatabase getWritableDatabase()
    {
        SQLiteDatabase db = super.getWritableDatabase();
        if ( db != null )
        {
            ++m_writableDatabaseCount;

            if(Debug.isDebuggerConnected()) {
                //db.execSQL("drop table if exists timber");
                //db.execSQL("drop table if exists tree_type_dictionary");
                //db.execSQL(TABLE_OFFLINE_DATA_CREATE);
                //db.execSQL(TABLE_TREE_TYPE_CREATE);
            }
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
        db.execSQL(TABLE_OFFLINE_DATA_CREATE);
        db.execSQL(TABLE_TREE_TYPE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO Auto-generated method stub
    }

    
}
