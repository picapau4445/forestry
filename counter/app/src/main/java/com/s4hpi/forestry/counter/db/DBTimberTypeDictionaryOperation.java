package com.s4hpi.forestry.counter.db;

import java.io.IOException;
import java.util.HashMap;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.s4hpi.forestry.counter.R;

public class DBTimberTypeDictionaryOperation {

    private SQLiteDatabase m_db;
    private static final String TABLE_NAME = "timber_type_dictionary";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_SOURCE = "source";
    private static final String COLUMN_DESTINATION = "destination";
    private static final String TABLE_CREATE =
            "CREATE TABLE IF NOT EXISTS timber_type_dictionary "
                    + "("
                    + "id integer primary key autoincrement,"
                    + "source text,"
                    + "destination text"
                    + ")";

    public DBTimberTypeDictionaryOperation(Context context)
    {
        DBOpenHelper helper = DBOpenHelper.getInstance(
                context, context.getString(R.string.db_timber_type_dictionary) );
        if( helper != null ) {
            m_db = helper.getReadableDatabase();
        } else {
            m_db = null;
        }
    }

    public static void createDBTimberTypeDictionary(Context context) throws IOException {
        DBOpenHelper helper = DBOpenHelper.getInstance(
                context, context.getString(R.string.db_timber_type_dictionary) );
        if( helper != null ) {
            helper.createEmptyDataBase(context);
        }
    }

    public void close() {
        m_db.close();
    }

    public void insert( String src, String dest)
    {
        ContentValues val = new ContentValues();

        val.put(COLUMN_SOURCE, src );
        val.put(COLUMN_DESTINATION, dest );
        m_db.insert(TABLE_NAME, null, val );
    }

    public void update( int id, String src, String dest)
    {
        ContentValues val = new ContentValues();

        val.put(COLUMN_SOURCE, src );
        val.put(COLUMN_DESTINATION, dest );
        m_db.update(TABLE_NAME, val, COLUMN_ID + "=?", new String[] { Integer.toString( id ) });
    }
    
    public HashMap<String, String> load()
    {
        boolean res;
        Cursor c;
        HashMap<String, String> hash = new HashMap<>();

        if( m_db == null ) {
            return null;
        }

        c = m_db.query(TABLE_NAME,
                        new String[] {COLUMN_ID, COLUMN_SOURCE, COLUMN_DESTINATION},
                        null, null, null, null, null );
        res = c.moveToFirst();

        while(res)
        {
            hash.put(c.getString(1), c.getString(2));
            res = c.moveToNext();
        }

        c.close();

        return hash;
    }
}