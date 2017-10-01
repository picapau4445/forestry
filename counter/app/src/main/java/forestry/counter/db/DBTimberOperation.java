package forestry.counter.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.SparseArray;

import java.sql.Date;

import forestry.counter.dto.Timber;

public class DBTimberOperation {

    private SQLiteDatabase m_db;
    private static final String TBL_NAME = "timber";

    public DBTimberOperation(Context context)
    {
        DBOpenHelper helper = DBOpenHelper.getInstance( context );
        if( helper != null )
            m_db = helper.getWritableDatabase();
        else
            m_db = null;
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
        val.put("send_status", 0 );
        val.put("reg_date", data.getRegDateString());
        val.put("send_date", "");
        return m_db.insert( TBL_NAME, null, val );
    }

    public void updateSendStatus( int id, int sendStatus, Date sendDate)
    {
        ContentValues val = new ContentValues();

        val.put("send_status", sendStatus );
        val.put("send_date", sendDate.toString() );
        m_db.update( TBL_NAME, val, "id=?", new String[] { Integer.toString( id ) });
    }

    public int delete( int id)
    {
        if(id == -1) {
            return m_db.delete(TBL_NAME, null, null);
        } else {
            return m_db.delete(TBL_NAME, "id=?", new String[]{Integer.toString(id)});
        }
    }

    public SparseArray<Timber> load(int user, int pref, int city)
    {
        boolean res;
        Cursor c;
        SparseArray<Timber> array = new SparseArray<>();
        
        if( m_db == null ) {
            return null;
        }

        c = m_db.query( TBL_NAME,
                        new String[] {"user", "pref", "city", "forest_group", "small_group",
                                "lat", "lon", "kind", "height", "dia", "volume", "send_status",
                                "reg_date", "send_date"},
                        "user = ? and pref = ? and city = ?",
                        new String[] {String.valueOf(user), String.valueOf(pref), String.valueOf(city)},
                        null, null, null );
        res = c.moveToFirst();

        while(res)
        {
            int colIndex=0;
            Timber data = new Timber();
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