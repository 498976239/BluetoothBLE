package com.ss.www.bluetoothble.dbmanager;

import android.content.Context;

import com.ss.bluetoothble.greendao.gen.DaoMaster;
import com.ss.bluetoothble.greendao.gen.DaoSession;

/**
 * Created by SS on 17-6-22.
 */
public class DaoManager {
    private static final String DB_NAME = "bluetoothDate.db";//数据库名称
    private volatile static DaoManager manager;//单例设计模式
    private static DaoMaster.DevOpenHelper helper;
    private static DaoMaster daoMaster;
    private static DaoSession daoSession;
    private Context mContext;
    private DaoManager(Context mContext){
        this.mContext = mContext;
    }
    public static DaoManager getInstance(Context mContext){
        DaoManager instance = null;
        if(manager == null){
            synchronized (DaoManager.class){
                if(instance == null){
                    instance = new DaoManager(mContext);
                    manager = instance;
                }
            }
        }
        return manager;
    }
    public DaoMaster getDaoMaster(){
        if(daoMaster == null){
            helper = new DaoMaster.DevOpenHelper(mContext,DB_NAME);
            daoMaster = new DaoMaster(helper.getWritableDatabase());
        }
        return daoMaster;
    }
    public DaoSession getDaoSession(){
        if(daoSession == null){
            if(daoMaster == null){
                daoMaster = getDaoMaster();
            }
            daoSession = daoMaster.newSession();
        }
        return daoSession;
    }
    /**
     * 数据库使用之后需要关闭数据库
     */
    public void closeConnection(){
        closeHelper();
        closeDaoSession();
    }
    public void closeHelper(){
        if(helper != null){
            helper.close();
            helper = null;
        }
    }
    public void closeDaoSession(){
        if(daoSession != null){
            daoSession.clear();
            daoSession = null;
        }
    }
}
