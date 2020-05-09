package com.s4hpi.forestry.counter.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.SparseArray;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import com.s4hpi.forestry.counter.R;
import com.s4hpi.forestry.counter.dto.Timber;

public class DBTimberOperation {

    private SQLiteDatabase m_db;
    private static final String TABLE_NAME = "timber";
    private static final String TABLE_CREATE =
            "CREATE TABLE IF NOT EXISTS timber "
                    + "("
                    + "id integer primary key autoincrement,"
                    + "user integer,"
                    + "pref integer,"
                    + "city integer,"
                    + "forest_group integer,"
                    + "small_group integer,"
                    + "lat text,"
                    + "lon text,"
                    + "kind text,"
                    + "height integer,"
                    + "dia integer,"
                    + "volume integer,"
                    + "send_status integer,"
                    + "reg_date text,"
                    + "send_date text"
                    + ")";

    public DBTimberOperation(Context context)
    {
        DBOpenHelper helper = DBOpenHelper.getInstance(
                context, context.getString(R.string.db_timber) );
        if( helper != null ) {
            m_db = helper.getWritableDatabase();
            m_db.execSQL(TABLE_CREATE);
        } else {
            m_db = null;
        }
    }

    public void close() {
        m_db.close();
    }

    public long insert(Timber data)
    {
        ContentValues val = new ContentValues();

        val.put("user", data.getUser() );
        val.put("city", data.getCity());
        val.put("pref", data.getPref() );
        val.put("forest_group", data.getForestGroup() );
        val.put("small_group", data.getSmallGroup() );
        val.put("lat", data.getLatString());
        val.put("lon", data.getLatString());
        val.put("kind", data.getKind() );
        val.put("height", data.getHeight() );
        val.put("dia", data.getDia() );
        val.put("volume", data.getVolume() );
        val.put("send_status", data.getSend() );
        val.put("reg_date", data.getRegDateString());
        val.put("send_date", data.getSendDateString());
        return m_db.insert(TABLE_NAME, null, val );
    }

    public void updateSendStatus( long rowId, int sendStatus, Date sendDate)
    {
        ContentValues val = new ContentValues();

        val.put("send_status", sendStatus );
        val.put("send_date", (new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.JAPANESE))
                .format(new Date()) );
        m_db.update(TABLE_NAME, val, "rowid=?", new String[]{Long.toString(rowId)});
    }

    public int delete( int id)
    {
        if(id == -1) {
            return m_db.delete(TABLE_NAME, null, null);
        } else {
            return m_db.delete(TABLE_NAME, "id=?", new String[]{Integer.toString(id)});
        }
    }

    public SparseArray<Timber> getTimberDataOrderByRegDate(int user, int pref, int city)
    {
        String selection = "user = ? and pref = ? and city = ?";
        String[] selectionArgs = new String[] {
                String.valueOf(user),
                String.valueOf(pref),
                String.valueOf(city)};
        String orderBy = "reg_date DESC";

        return load(selection, selectionArgs, orderBy);
    }

    public SparseArray<Timber> getTimberDataNotSend(int user, int pref, int city)
    {
        String selection = "user = ? and pref = ? and city = ? and send_status <> 1";
        String[] selectionArgs = new String[] {
                String.valueOf(user),
                String.valueOf(pref),
                String.valueOf(city)};

        return load(selection, selectionArgs, null);
    }

    public SparseArray<Timber> load(String selection, String[] selectionArgs, String orderBy)
    {
        boolean res;
        Cursor c;
        SparseArray<Timber> array = new SparseArray<>();
        
        if( m_db == null ) {
            return null;
        }

        c = m_db.query(TABLE_NAME,
                        new String[] {"rowid", "user", "pref", "city", "forest_group", "small_group",
                                "lat", "lon", "kind", "height", "dia", "volume", "send_status",
                                "reg_date", "send_date"},
                selection,
                selectionArgs,
                null, null, orderBy );
        res = c.moveToFirst();

        while(res)
        {
            int colIndex=0;
            Timber data = new Timber();
            data.setRowId(c.getLong(colIndex));colIndex++;
            data.setUser(c.getInt(colIndex));colIndex++;
            data.setPref(c.getInt(colIndex));colIndex++;
            data.setCity(c.getInt(colIndex));colIndex++;
            data.setForestGroup(c.getInt(colIndex));colIndex++;
            data.setSmallGroup(c.getInt(colIndex));colIndex++;
            data.setLat(c.getString(colIndex));colIndex++;
            data.setLon(c.getString(colIndex));colIndex++;
            data.setKind(c.getString(colIndex));colIndex++;
            data.setHeight(c.getInt(colIndex));colIndex++;
            data.setDia(c.getInt(colIndex));colIndex++;
            data.setVolume(c.getInt(colIndex));colIndex++;
            data.setSend(c.getInt(colIndex));colIndex++;
            data.setRegDate(c.getString(colIndex));colIndex++;
            data.setSendDate(c.getString(colIndex));

            array.append(c.getPosition(), data);
            res = c.moveToNext();
        }

        c.close();

        return array;
    }
}