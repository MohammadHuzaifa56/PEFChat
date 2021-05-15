package com.pefgloble.pefchate.adapter;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.pefgloble.pefchate.JsonClasses.status.StatusModel;
import com.pefgloble.pefchate.R;
import com.pefgloble.pefchate.helpers.AppHelper;
import com.pefgloble.pefchate.helpers.UtilsString;
import com.pefgloble.pefchate.presenter.StatusPresenter;
import com.pefgloble.pefchate.staus.StatusActivity;
import com.pefgloble.pefchate.staus.StatusDelete;
import com.vanniktech.emoji.EmojiTextView;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;


/**
 * Created by Abderrahim El imame on 28/04/2016.
 * Email : abderrahim.elimame@gmail.com
 */
public class StatusAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Activity mActivity;
    private List<StatusModel> mStatusModel;
    private StatusPresenter statusPresenter;
    private String oldStatusId;

    public void setStatus(List<StatusModel> statusModelList) {
        this.mStatusModel = statusModelList;
        notifyDataSetChanged();
    }


    public StatusAdapter(@NonNull StatusActivity mActivity) {
        this.mActivity = mActivity;
        statusPresenter = new StatusPresenter(mActivity);
    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_status, parent, false);
        return new StatusViewHolder(itemView);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        StatusViewHolder statusViewHolder = (StatusViewHolder) holder;
        StatusModel statusModel = mStatusModel.get(position);
        try {


            if (statusModel.getBody() != null) {

                statusViewHolder.setStatus(statusModel.getBody());
            }

            if (statusModel.isCurrent()) {
                statusViewHolder.setStatusColorCurrent();
                oldStatusId = statusModel.get_id();
            } else {
                statusViewHolder.setStatusColor();
            }


        } catch (Exception e) {
            AppHelper.LogCat("" + e.getMessage());
        }
        statusViewHolder.setOnLongClickListener(v -> {
            Intent mIntent = new Intent(mActivity, StatusDelete.class);
            mIntent.putExtra("statusID", statusModel.get_id());
            mActivity.startActivity(mIntent);
            return true;
        });
        statusViewHolder.setOnClickListener(v -> statusPresenter.UpdateCurrentStatus(statusModel.getBody(), oldStatusId, statusModel.get_id()));

    }

    private void removeStatusItem(int position) {
        if (position != 0) {
            try {
                mStatusModel.remove(position);
                notifyItemRemoved(position);
            } catch (Exception e) {
                AppHelper.LogCat(e);
            }
        }
    }

    public void DeleteStatusItem(String StatusID) {
        try {
            int arraySize = mStatusModel.size();
            for (int i = 0; i < arraySize; i++) {
                StatusModel model = mStatusModel.get(i);
                if (model.isValid()) {
                    if (StatusID.equals(model.get_id())) {
                        removeStatusItem(i);
                        break;
                    }
                }
            }
        } catch (Exception e) {
            AppHelper.LogCat(e);
        }
    }

    @Override
    public int getItemCount() {
        if (mStatusModel != null)
            return mStatusModel.size();
        else
            return 0;
    }

    class StatusViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.status)
        TextView status;

        StatusViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            status.setSelected(true);

        }


        void setStatus(String Status) {
            String finalStatus = UtilsString.unescapeJava(Status);
            status.setText(finalStatus);
        }

        void setStatusColorCurrent() {
            status.setTextColor(mActivity.getResources().getColor(R.color.colorAccent));
        }

        void setStatusColor() {
            status.setTextColor(mActivity.getResources().getColor(R.color.colorBlack));
        }

        void setOnClickListener(View.OnClickListener listener) {
            itemView.setOnClickListener(listener);

        }

        void setOnLongClickListener(View.OnLongClickListener listener) {
            itemView.setOnLongClickListener(listener);

        }
    }


}
