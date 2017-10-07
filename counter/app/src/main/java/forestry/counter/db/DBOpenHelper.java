package forestry.counter.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBOpenHelper extends SQLiteOpenHelper {

    private static final int DB_VERSION = 1;
    private int m_writableDatabaseCount = 0;
    private static DBOpenHelper m_instance = null;

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

    
}
