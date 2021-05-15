package com.pefgloble.pefchate.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.pefgloble.pefchate.JsonClasses.messags.MessageModel;
import com.pefgloble.pefchate.R;
import com.pefgloble.pefchate.adapter.LinksAdapter;
import com.pefgloble.pefchate.helpers.AppHelper;
import com.pefgloble.pefchate.presenter.ProfilePresenter;

import java.util.List;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Abderrahim El imame on 1/25/17.
 *
 * @Email : abderrahim.elimame@gmail.com
 * @Author : https://twitter.com/Ben__Cherif
 * @Skype : ben-_-cherif
 */

public class LinksFragment extends Fragment {

    private View mView;
    private LinksAdapter linksAdapter;


    @BindView(R.id.linksList)
    RecyclerView linksList;


    private ProfilePresenter mProfilePresenter;

    public static LinksFragment newInstance(String tag) {
        LinksFragment linksFragment = new LinksFragment();
        Bundle args = new Bundle();
        args.putString("tag", tag);
        linksFragment.setArguments(args);
        return linksFragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_links, container, false);
        ButterKnife.bind(this, mView);
        initializerView();

        mProfilePresenter = new ProfilePresenter(this);
        mProfilePresenter.onCreate();
        return mView;
    }


    public void initializerView() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        linearLayoutManager.setOrientation(RecyclerView.VERTICAL);
        linksList.setLayoutManager(linearLayoutManager);
        linksAdapter = new LinksAdapter(getActivity());
        linksList.setAdapter(linksAdapter);
    }

    public void ShowMedia(List<MessageModel> messagesModel) {
        AppHelper.LogCat("messagesModel " + messagesModel.size());
        if (messagesModel.size() != 0) {
            linksAdapter.setMessages(messagesModel);
        }

    }

    public void onErrorLoading(Throwable throwable) {
        AppHelper.LogCat("MediaFragment throwable " + throwable.getMessage());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mProfilePresenter.onDestroy();
    }

}
