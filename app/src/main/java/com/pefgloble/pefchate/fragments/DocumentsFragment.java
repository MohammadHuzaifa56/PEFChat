package com.pefgloble.pefchate.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import com.pefgloble.pefchate.JsonClasses.messags.MessageModel;
import com.pefgloble.pefchate.R;
import com.pefgloble.pefchate.adapter.DocumentsAdapter;
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

public class DocumentsFragment extends Fragment {

    private View mView;
    private DocumentsAdapter documentsAdapter;
    private ProfilePresenter mProfilePresenter;

    @BindView(R.id.documentsList)
    RecyclerView documentsList;

    public static DocumentsFragment newInstance(String tag) {
        DocumentsFragment documentsFragment = new DocumentsFragment();
        Bundle args = new Bundle();
        args.putString("tag", tag);
        documentsFragment.setArguments(args);
        return documentsFragment;
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_documents, container, false);
        ButterKnife.bind(this, mView);
        initializerView();
        mProfilePresenter = new ProfilePresenter(this);
        mProfilePresenter.onCreate();

        return mView;
    }


    public void initializerView() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        linearLayoutManager.setOrientation(RecyclerView.VERTICAL);
        documentsList.setLayoutManager(linearLayoutManager);
        documentsAdapter = new DocumentsAdapter(getActivity());
        documentsList.setAdapter(documentsAdapter);
    }

    public void ShowMedia(List<MessageModel> messagesModel) {
        if (messagesModel.size() != 0) {
            documentsAdapter.setMessages(messagesModel);
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
