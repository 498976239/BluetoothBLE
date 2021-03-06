package com.ss.bluetoothble.greendao.gen;

import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;

import org.greenrobot.greendao.AbstractDao;
import org.greenrobot.greendao.Property;
import org.greenrobot.greendao.internal.DaoConfig;
import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.database.DatabaseStatement;

import com.ss.www.bluetoothble.entity.Bean;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.
/** 
 * DAO for table "BEAN".
*/
public class BeanDao extends AbstractDao<Bean, Long> {

    public static final String TABLENAME = "BEAN";

    /**
     * Properties of entity Bean.<br/>
     * Can be used for QueryBuilder and for referencing column names.
    */
    public static class Properties {
        public final static Property Id = new Property(0, Long.class, "id", true, "_id");
        public final static Property Name = new Property(1, String.class, "name", false, "NAME");
        public final static Property Data1 = new Property(2, float.class, "data1", false, "DATA1");
        public final static Property Data2 = new Property(3, float.class, "data2", false, "DATA2");
        public final static Property Data3 = new Property(4, float.class, "data3", false, "DATA3");
        public final static Property TimeDetail = new Property(5, String.class, "timeDetail", false, "TIME_DETAIL");
        public final static Property Now = new Property(6, java.util.Date.class, "now", false, "NOW");
    };


    public BeanDao(DaoConfig config) {
        super(config);
    }
    
    public BeanDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
    }

    /** Creates the underlying database table. */
    public static void createTable(Database db, boolean ifNotExists) {
        String constraint = ifNotExists? "IF NOT EXISTS ": "";
        db.execSQL("CREATE TABLE " + constraint + "\"BEAN\" (" + //
                "\"_id\" INTEGER PRIMARY KEY AUTOINCREMENT ," + // 0: id
                "\"NAME\" TEXT," + // 1: name
                "\"DATA1\" REAL NOT NULL ," + // 2: data1
                "\"DATA2\" REAL NOT NULL ," + // 3: data2
                "\"DATA3\" REAL NOT NULL ," + // 4: data3
                "\"TIME_DETAIL\" TEXT," + // 5: timeDetail
                "\"NOW\" INTEGER);"); // 6: now
    }

    /** Drops the underlying database table. */
    public static void dropTable(Database db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "\"BEAN\"";
        db.execSQL(sql);
    }

    @Override
    protected final void bindValues(DatabaseStatement stmt, Bean entity) {
        stmt.clearBindings();
 
        Long id = entity.getId();
        if (id != null) {
            stmt.bindLong(1, id);
        }
 
        String name = entity.getName();
        if (name != null) {
            stmt.bindString(2, name);
        }
        stmt.bindDouble(3, entity.getData1());
        stmt.bindDouble(4, entity.getData2());
        stmt.bindDouble(5, entity.getData3());
 
        String timeDetail = entity.getTimeDetail();
        if (timeDetail != null) {
            stmt.bindString(6, timeDetail);
        }
 
        java.util.Date now = entity.getNow();
        if (now != null) {
            stmt.bindLong(7, now.getTime());
        }
    }

    @Override
    protected final void bindValues(SQLiteStatement stmt, Bean entity) {
        stmt.clearBindings();
 
        Long id = entity.getId();
        if (id != null) {
            stmt.bindLong(1, id);
        }
 
        String name = entity.getName();
        if (name != null) {
            stmt.bindString(2, name);
        }
        stmt.bindDouble(3, entity.getData1());
        stmt.bindDouble(4, entity.getData2());
        stmt.bindDouble(5, entity.getData3());
 
        String timeDetail = entity.getTimeDetail();
        if (timeDetail != null) {
            stmt.bindString(6, timeDetail);
        }
 
        java.util.Date now = entity.getNow();
        if (now != null) {
            stmt.bindLong(7, now.getTime());
        }
    }

    @Override
    public Long readKey(Cursor cursor, int offset) {
        return cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0);
    }    

    @Override
    public Bean readEntity(Cursor cursor, int offset) {
        Bean entity = new Bean( //
            cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0), // id
            cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1), // name
            cursor.getFloat(offset + 2), // data1
            cursor.getFloat(offset + 3), // data2
            cursor.getFloat(offset + 4), // data3
            cursor.isNull(offset + 5) ? null : cursor.getString(offset + 5), // timeDetail
            cursor.isNull(offset + 6) ? null : new java.util.Date(cursor.getLong(offset + 6)) // now
        );
        return entity;
    }
     
    @Override
    public void readEntity(Cursor cursor, Bean entity, int offset) {
        entity.setId(cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0));
        entity.setName(cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1));
        entity.setData1(cursor.getFloat(offset + 2));
        entity.setData2(cursor.getFloat(offset + 3));
        entity.setData3(cursor.getFloat(offset + 4));
        entity.setTimeDetail(cursor.isNull(offset + 5) ? null : cursor.getString(offset + 5));
        entity.setNow(cursor.isNull(offset + 6) ? null : new java.util.Date(cursor.getLong(offset + 6)));
     }
    
    @Override
    protected final Long updateKeyAfterInsert(Bean entity, long rowId) {
        entity.setId(rowId);
        return rowId;
    }
    
    @Override
    public Long getKey(Bean entity) {
        if(entity != null) {
            return entity.getId();
        } else {
            return null;
        }
    }

    @Override
    protected final boolean isEntityUpdateable() {
        return true;
    }
    
}
