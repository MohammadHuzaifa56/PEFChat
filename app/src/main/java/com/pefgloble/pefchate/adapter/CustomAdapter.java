package com.pefgloble.pefchate.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.pefgloble.pefchate.JsonClasses.contacts.UsersModel;

import java.util.List;

public class CustomAdapter extends BaseAdapter {

    Context context;
    List<UsersModel> usersModels;
    public CustomAdapter(Context context, List<UsersModel> usersModels) {
        this.context = context;
        this.usersModels = usersModels;
    }
    @Override
    public int getCount() {
        return 0;
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        return null;
    }
}
