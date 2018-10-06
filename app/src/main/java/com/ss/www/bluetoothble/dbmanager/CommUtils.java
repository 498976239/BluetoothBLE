package com.ss.www.bluetoothble.dbmanager;

import android.content.Context;

import com.ss.bluetoothble.greendao.gen.BeanDao;
import com.ss.www.bluetoothble.entity.Bean;

import org.greenrobot.greendao.query.QueryBuilder;

import java.util.Date;
import java.util.List;

/**
 * Created by SS on 17-6-22.
 */
public class CommUtils {
    private DaoManager manager;
    private BeanDao mBeanDao;
    private Context mContext;
    public CommUtils(Context mContext){
        this.mContext = mContext;
        manager = DaoManager.getInstance(mContext);
    }
    public BeanDao getBeanDao(){
        if(mBeanDao == null){
            mBeanDao = manager.getDaoSession().getBeanDao();
        }
        return mBeanDao;
    }

    /**批量添加数据
     * @param list
     */
    public void insertMultBean(final List<Bean> list){
        if(mBeanDao == null){
            mBeanDao = getBeanDao();
        }
        mBeanDao.insertOrReplaceInTx(list,false);
    }
    public void delectBean(Bean b){
        if (mBeanDao == null){
            mBeanDao = getBeanDao();
        }
        mBeanDao.delete(b);
    }

    /**
     * 删除所有数据
     */
    public void delectAllBean(){
        if(mBeanDao == null){
            mBeanDao = getBeanDao();
        }
        mBeanDao.deleteAll();
    }

    /**查询所有数据
     * @return
     */
    public List<Bean> allLoad(){
        BeanDao userBeanDao = manager.getDaoSession().getBeanDao();
        List<Bean> userBeen = userBeanDao.loadAll();
        return userBeen;
    }

    /**按时间和str一起查询
     * @param str
     * @param d1
     * @param d2
     * @return
     */
    public  List<Bean> queryCondition(String str, Date d1, Date d2){
        QueryBuilder<Bean> beanQueryBuilder = manager.getDaoSession().queryBuilder(Bean.class);
        List<Bean> list = beanQueryBuilder.where(BeanDao.Properties.Name.eq(str),BeanDao.Properties.Now.between(d1,d2)).list();
        return list;
    }
    public List<Bean> queryCondition(Date d1,Date d2){
        QueryBuilder<Bean> beanQueryBuilder = manager.getDaoSession().queryBuilder(Bean.class);
        List<Bean> list = beanQueryBuilder.where(BeanDao.Properties.Now.between(d1, d2)).list();
        return list;
    }
}
