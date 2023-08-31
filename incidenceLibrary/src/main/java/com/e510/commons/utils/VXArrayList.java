package com.e510.commons.utils;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Set;

public class VXArrayList extends ArrayList implements Serializable, Parcelable
{
    private static final long serialVersionUID = 83654539025336656L;

    private LinkedHashMap<String, Object> hashmap;

    public VXArrayList()
    {
        this.hashmap = new LinkedHashMap<>();
    }

    @Override
    public void clear()
    {
        super.clear();
        this.hashmap.clear();
    }

    public void addItem(String key, Object o)
    {
        if (this.hashmap.get(key) != null)
        {
            removeItem(key);
        }

        if (this.contains(o))
        {
            int itemIndex = this.indexOf(o);
            this.set(itemIndex, o);
            this.hashmap.put(key, o);
        }
        else if (this.add(o))
        {
            this.hashmap.put(key, o);
        }
    }

    public void addAll(VXArrayList c) {
        for (String key: c.getKeys()) {
            Object v = c.get(key);
            addItem(key, v);
        }
    }

    public void replaceItem(String oldKey, Object o, String newKey)
    {
        throw new RuntimeException("Method replaceItem isn't usable");
    }

    public void removeItem(String key)
    {
        Object o = get(key);

        if (o != null)
        {
            removeItem(key, o);
        }
    }

    public void removeItem(String key, Object o)
    {
        if (this.remove(o))
        {
            this.hashmap.remove(key);
        }
    }

    public Set<String> getKeys() {
        return this.hashmap.keySet();
    }

    public Object get(String key)
    {
        return this.hashmap.get(key);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeSerializable(this.hashmap);
    }

    protected VXArrayList(Parcel in) {
        this.hashmap = (LinkedHashMap<String, Object>) in.readSerializable();
    }

    public static final Creator<VXArrayList> CREATOR = new Creator<VXArrayList>() {
        @Override
        public VXArrayList createFromParcel(Parcel source) {
            return new VXArrayList(source);
        }

        @Override
        public VXArrayList[] newArray(int size) {
            return new VXArrayList[size];
        }
    };

    public void printKeys(String title)
    {
        LogUtil.logE("VXArrayList", title + " -> " + getKeys().toString());
    }
}
