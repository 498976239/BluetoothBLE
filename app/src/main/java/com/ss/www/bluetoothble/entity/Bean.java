package com.ss.www.bluetoothble.entity;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;

import java.io.Serializable;
import java.util.Date;
import org.greenrobot.greendao.annotation.Generated;

/**
 * Created by SS on 17-6-22.
 */
@Entity
public class Bean implements Serializable{
    @Id(autoincrement = true)
    private Long id;
    private String name;
    private float data1;
    private float data2;
    private float data3;
    private String timeDetail;
    private Date now;
    public Date getNow() {
        return this.now;
    }
    public void setNow(Date now) {
        this.now = now;
    }
    public String getTimeDetail() {
        return this.timeDetail;
    }
    public void setTimeDetail(String timeDetail) {
        this.timeDetail = timeDetail;
    }
    public float getData3() {
        return this.data3;
    }
    public void setData3(float data3) {
        this.data3 = data3;
    }
    public float getData2() {
        return this.data2;
    }
    public void setData2(float data2) {
        this.data2 = data2;
    }
    public float getData1() {
        return this.data1;
    }
    public void setData1(float data1) {
        this.data1 = data1;
    }
    public String getName() {
        return this.name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public Long getId() {
        return this.id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    @Generated(hash = 1856498431)
    public Bean(Long id, String name, float data1, float data2, float data3,
            String timeDetail, Date now) {
        this.id = id;
        this.name = name;
        this.data1 = data1;
        this.data2 = data2;
        this.data3 = data3;
        this.timeDetail = timeDetail;
        this.now = now;
    }
    @Generated(hash = 80546095)
    public Bean() {
    }

    @Override
    public String toString() {
        return "Bean{" +
                "name='" + name + '\'' +
                ", data1=" + data1 +
                ", data2=" + data2 +
                ", data3=" + data3 +
                '}';
    }
}
