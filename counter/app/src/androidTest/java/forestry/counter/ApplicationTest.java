package forestry.counter;

import android.app.Application;
import android.content.Context;
import android.test.ApplicationTestCase;
import android.test.RenamingDelegatingContext;
import android.util.Log;
import android.util.SparseArray;

import forestry.counter.db.DBTimberOperation;
import forestry.counter.dto.Timber;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */

public class ApplicationTest extends ApplicationTestCase<Application> {

    DBTimberOperation db;

    public ApplicationTest() {
        super(Application.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        db = new DBTimberOperation(mContext);
        db.delete(-1);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();

    }

    public void testDBTimberInsert01() {
        Timber data = new Timber();
        data.setKind("カラマツ");
        data.setDia(22);
        long actual = 0;
        try {
            actual = db.insert(data);
            //assertEquals(1, actual);
        } finally {
            if( actual > 0 ) {
                db.delete((int)actual);
            }
        }
    }

    public void testDBTimberLoad01() {
        SparseArray<Timber> array;
        Timber data = new Timber();
        data.setUser(1);
        data.setPref(14);
        data.setCity(14);
        data.setKind("カラマツ");
        data.setDia(22);
        db.insert(data);
        db.insert(data);

        array = db.load(1,14,14);
        assertEquals(2, array.size());

        array = db.load(2,14,14);
        assertEquals(0, array.size());

        array = db.load(1,13,14);
        assertEquals(0, array.size());

        array = db.load(1,14,13);
        assertEquals(0, array.size());

    }
}
