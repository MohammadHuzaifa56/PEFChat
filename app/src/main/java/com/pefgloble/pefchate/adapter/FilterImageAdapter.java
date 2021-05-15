package com.pefgloble.pefchate.adapter;

import android.animation.LayoutTransition;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;


import com.pefgloble.pefchate.JsonClasses.otherClasses.ThumbnailFilter;
import com.pefgloble.pefchate.R;
import com.pefgloble.pefchate.helpers.AppHelper;

import java.util.List;

import androidx.recyclerview.widget.RecyclerView;

public class FilterImageAdapter extends RecyclerView.Adapter<FilterImageAdapter.ViewHolder> {

    private final FilterImageAdapterListener mListener;
    private List<ThumbnailFilter> imageFilters;
    private int lastCheckedPostion = 0;

    public interface FilterImageAdapterListener {
        void ThumbnailFilter(ThumbnailFilter filter);
    }

    public FilterImageAdapter(List<ThumbnailFilter> list, FilterImageAdapterListener listener) {
        imageFilters = list;
        this.mListener = listener;
    }

    public void setData(List<ThumbnailFilter> stickersList) {
        this.imageFilters = stickersList;
        notifyDataSetChanged();
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View v = inflater.inflate(R.layout.row_filter_layout, parent, false);
        // set the view's size, margins, paddings and layout parameters
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        final ThumbnailFilter thumbnailItem = imageFilters.get(position);
        Log.i("filter", thumbnailItem.filterName);
        if (thumbnailItem.image != null) {
            holder.filterIV.setImageBitmap(thumbnailItem.filter.processFilter(thumbnailItem.image));
        }

        FrameLayout.LayoutParams layoutParams = null;
        if (position == lastCheckedPostion) {
            holder.checkbox.setVisibility(View.VISIBLE);
            layoutParams = new FrameLayout.LayoutParams(AppHelper.dpToPx(holder.checkbox.getContext(), 70), AppHelper.dpToPx(holder.checkbox.getContext(), 110));

        } else {
            holder.checkbox.setVisibility(View.GONE);
            layoutParams = new FrameLayout.LayoutParams(AppHelper.dpToPx(holder.checkbox.getContext(), 64), AppHelper.dpToPx(holder.checkbox.getContext(), 100));
        }


        holder.filterIV.setLayoutParams(layoutParams);

        holder.filterTv.setText(thumbnailItem.filterName);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.ThumbnailFilter(thumbnailItem);
                int lastPosition = lastCheckedPostion;
                holder.checkbox.setVisibility(View.VISIBLE);

                FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(AppHelper.dpToPx(holder.checkbox.getContext(), 70), AppHelper.dpToPx(holder.checkbox.getContext(), 110));
                holder.filterIV.setLayoutParams(layoutParams);


                lastCheckedPostion = holder.getAdapterPosition();
                notifyItemChanged(lastPosition);
            }
        });
    }

    @Override
    public int getItemCount() {
        return imageFilters.size();
    }

    public void scaleView(View v, float startScale, float endScale, int duration) {
        Animation anim = new ScaleAnimation(
                startScale, endScale, // Start and end values for the X axis scaling
                startScale, endScale, // Start and end values for the Y axis scaling
                Animation.RELATIVE_TO_SELF, 0.5f, // Pivot point of X scaling
                Animation.RELATIVE_TO_SELF, 1f); // Pivot point of Y scaling
        anim.setFillAfter(true); // Needed to keep the result of the animation
        anim.setDuration(duration);
        v.startAnimation(anim);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        ImageView filterIV;
        ImageView checkbox;
        TextView filterTv;
        FrameLayout container;

        public ViewHolder(View v) {
            super(v);
            filterIV = v.findViewById(R.id.filter_iv);
            filterTv = v.findViewById(R.id.filter_name);
            checkbox = v.findViewById(R.id.check_box);
            container = v.findViewById(R.id.container);
            container.getLayoutTransition().enableTransitionType(LayoutTransition.CHANGING);
        }
    }

    public void add(int position, ThumbnailFilter item) {
        imageFilters.add(position, item);
        notifyItemInserted(position);
    }

    public void remove(int position) {
        imageFilters.remove(position);
        notifyItemRemoved(position);
    }
}