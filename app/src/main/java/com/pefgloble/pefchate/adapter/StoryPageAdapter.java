package com.pefgloble.pefchate.adapter;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class StoryPageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }



/*
    private List<CreateStoryModel> mStories;
    private final int IMAGE_STORY = 0;
    private final int VIDEO_STORY = 1;
    private StoryProgressView storyProgressView;
    private StoriesViewer storyFragment;

    public StoryPageAdapter(List<CreateStoryModel> mStories, StoryProgressView storyProgressView, StoriesViewer storyFragment) {
        this.mStories = mStories;
        this.storyProgressView = storyProgressView;
        this.storyFragment = storyFragment;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater=LayoutInflater.from(parent.getContext());
        if (viewType==0)
            return new ImageViewHolder(inflater.inflate(R.layout.row_story_details_image,parent,false));
        else
            return new VideoViewHolder(inflater.inflate(R.layout.row_story_details_video,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        switch (holder.getItemViewType()){
            case 0:
                configureImageViewHolder((ImageViewHolder) holder,position);
                break;
            case 1:
                return;
        }
    }
   private CreateStoryModel getItem(int position){
        return mStories.get(position);
   }

    @Override
    public int getItemViewType(int position) {
        CreateStoryModel createStoryModel=getItem(position);
        if (createStoryModel.getType().equals("image"))
            return 0;
        else
            return 1;
    }

    @Override
    public int getItemCount() {
        return mStories.size();
    }

    public class ImageViewHolder extends RecyclerView.ViewHolder {


        @BindView(R.id.story_image)
        AppCompatImageView story_image;

        @BindView(R.id.story_image_progress_bar)
        ProgressBar story_image_progress_bar;


        @BindView(R.id.btn_center)
        LinearLayout btn_center;

        @BindView(R.id.btn_reverse)
        LinearLayout btn_reverse;

        @BindView(R.id.btn_skip)
        LinearLayout btn_skip;

        @BindView(R.id.story_body)
        TextView story_body;

        @BindView(R.id.story_body_layout)
        LinearLayout story_body_layout;


        @BindView(R.id.open_seen_list_layout)
        LinearLayout open_seen_list_layout;


        @BindView(R.id.seen_counter)
        AppCompatTextView seen_counter;

        @BindView(R.id.open_reply)
        LinearLayout open_reply;

        private long pressTime = 0L;
        private long limit = 500L;
        boolean paused = false;
        boolean isExpand = false;

        @SuppressLint("ClickableViewAccessibility")
        public ImageViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            story_image_progress_bar.getIndeterminateDrawable().setColorFilter(AppHelper.getColor(itemView.getContext(), R.color.colorWhite), PorterDuff.Mode.SRC_IN);

            btn_center.setOnClickListener(v -> storyProgressView.skip());
            btn_reverse.setOnClickListener(v -> storyProgressView.reverse());
            btn_skip.setOnClickListener(v -> storyProgressView.skip());
    */
/*        story_body.setOnClickListener(v -> {
                CreateStoryModel storyModel = getItem(getAdapterPosition());
                if (storyModel.getBody().length() < 150) return;
                if (!isExpand) {
                    isExpand = true;

                    SpannyBen bio = new SpannyBen(storyModel.getBody());
                    bio.append(storyFragment.getString(R.string.see_less));
                    story_body.setText(bio);
                } else {
                    isExpand = false;
                    SpannyBen bio = new SpannyBen(storyModel.getBody().substring(0, 149) + "... ");
                    bio.append(storyFragment.getString(R.string.see_more));
                    story_body.setText(bio);
                }
            });*//*

            btn_center.setOnLongClickListener(v -> {
                AppHelper.LogCat("onLongClick image ");
                if (!paused) {
                    storyProgressView.pause();
                    hideMenuNavigation();
                    paused = true;
                }
                return false;
            });


            open_reply.setOnClickListener(v -> {
              */
/*  CreateStoryModel storyModel = mStories.get(getAdapterPosition());
                storyProgressView.pause();
                Intent mIntent = new Intent(storyFragment, StoriesReplyActivity.class);
                mIntent.putExtra("recipientId", storyModel.getId());
                mIntent.putExtra("storyId", storyModel.get_id());
                storyFragment.startActivityForResult(mIntent, AppConstants.PICK_REPLY_STORY);
                storyFragment.getActivity().overridePendingTransition(R.anim.appear, R.anim.disappear);*//*



            });


            open_seen_list_layout.setOnClickListener(v -> {
            */
/*    CreateStoryModel storyModel = mStories.get(getAdapterPosition());
                storyProgressView.pause();

                Intent mIntent = new Intent(storyFragment, StoriesSeenListActivity.class);

                mIntent.putExtra("storyId", storyModel.getStoryId());
                storyFragment.startActivityForResult(mIntent, AppConstants.PICK_REPLY_STORY);
                storyFragment.overridePendingTransition(R.anim.appear, R.anim.disappear);*//*



            });
            btn_center.setOnTouchListener(onTouchListener);
            btn_reverse.setOnTouchListener(onTouchListener);
            btn_skip.setOnTouchListener(onTouchListener);


        }

        public TextView getStory_body() {
            return story_body;
        }

        public ProgressBar getStory_image_progress_bar() {
            return story_image_progress_bar;
        }

        public AppCompatImageView getImageStatus() {
            return story_image;
        }

        private View.OnTouchListener onTouchListener = new View.OnTouchListener() {


            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        pressTime = System.currentTimeMillis();
                        */
/*storyProgressView.pause();
                        hideMenuNavigation();*//*

                        return false;
                    case MotionEvent.ACTION_UP:
                        long now = System.currentTimeMillis();
                        if (paused) {
                            storyProgressView.resume();
                            showMenuNavigation();
                            paused = false;
                        }
                        return limit < now - pressTime;
                }
                return false;
            }
        };


        private void showMenuNavigation() {
            CreateStoryModel storyModel = getItem(getAdapterPosition());
            final int flags;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
                flags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
            } else {
                flags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_FULLSCREEN;
            }

            storyFragment.getWindow().getDecorView().setSystemUiVisibility(flags);
            EventBus.getDefault().post("showTopPanel");
            StoriesViewer.fadeInAnimation(story_body_layout, 100);

            if (!storyModel.getUserId().equals(PreferenceManager.getInstance().getID(storyFragment)))
                StoriesViewer.fadeInAnimation(open_reply, 100);
            else StoriesViewer.fadeInAnimation(open_seen_list_layout, 100);
        }

        private void hideMenuNavigation() {
            CreateStoryModel storyModel = getItem(getAdapterPosition());
            final int flags;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
                flags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
            } else {
                flags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN;
            }

            storyFragment.getWindow().getDecorView().setSystemUiVisibility(flags);
            storyFragment.getWindow().setFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED, WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);

            final View decorView = storyFragment.getWindow().getDecorView();
            decorView.setOnSystemUiVisibilityChangeListener(visibility -> {

                if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
                    decorView.setSystemUiVisibility(flags);
                }
            });
            EventBus.getDefault().post("hideTopPanel");
            StoriesViewer.fadeOutAnimation(story_body_layout, 100);


            if (!storyModel.getUserId().equals(PreferenceManager.getInstance().getID(storyFragment))) {
                StoriesViewer.fadeOutAnimation(open_reply, 100);
            } else {
                StoriesViewer.fadeOutAnimation(open_seen_list_layout, 100);
            }
        }

    }

    public class VideoViewHolder extends RecyclerView.ViewHolder implements ToroPlayer {


        ExoPlayerViewHelper helper;
        Uri mediaUri;


        @BindView(R.id.player)
        PlayerView playerView;

        @BindView(R.id.story_video_thumb)
        AppCompatImageView story_video_thumb;

        @BindView(R.id.story_video_progress_bar)
        ProgressBar story_video_progress_bar;


        @BindView(R.id.btn_center)
        LinearLayout btn_center;

        @BindView(R.id.btn_reverse)
        LinearLayout btn_reverse;

        @BindView(R.id.btn_skip)
        LinearLayout btn_skip;

        @BindView(R.id.story_body)
        EmojiTextView story_body;

        @BindView(R.id.story_body_layout)
        LinearLayout story_body_layout;


        @BindView(R.id.open_reply)
        LinearLayout open_reply;

        @BindView(R.id.seen_counter)
        AppCompatTextView seen_counter;

        @BindView(R.id.open_seen_list_layout)
        LinearLayout open_seen_list_layout;


        private long pressTime = 0L;
        private long limit = 500L;
        boolean paused = false;
        boolean isExpand = false;

        @SuppressLint("ClickableViewAccessibility")
        public VideoViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            story_video_progress_bar.getIndeterminateDrawable().setColorFilter(AppHelper.getColor(itemView.getContext(), R.color.colorWhite), PorterDuff.Mode.SRC_IN);


            btn_center.setOnClickListener(v -> storyProgressView.skip());
            btn_reverse.setOnClickListener(v -> storyProgressView.reverse());
            btn_skip.setOnClickListener(v -> storyProgressView.skip());

          */
/*  story_body.setOnClickListener(v -> {
                CreateStoryModel storyModel = getItem(getAdapterPosition());
                if (storyModel.getBody().length() < 150) return;
                if (!isExpand) {
                    isExpand = true;
                    SpannyBen bio = new SpannyBen(storyModel.getBody());
                    bio.append(storyFragment.getString(R.string.see_less));
                    story_body.setText(bio);
                } else {
                    isExpand = false;
                    SpannyBen bio = new SpannyBen(storyModel.getBody().substring(0, 149) + "... ");
                    bio.append(storyFragment.getString(R.string.see_more));
                    story_body.setText(bio);
                }
            });*//*

            btn_center.setOnLongClickListener(v -> {
                AppHelper.LogCat("onLongClick ");

                if (!paused) {

                    if (story_video_progress_bar.getVisibility() == View.GONE) {
                        storyProgressView.pause();
                        pause();
                        hideMenuNavigation();
                        paused = true;
                    }
                }
                return false;
            });

            open_reply.setOnClickListener(v -> {
                CreateStoryModel storyModel = mStories.get(getAdapterPosition());
                storyProgressView.pause();
                pause();

                */
/*Intent mIntent = new Intent(storyFragment, StoriesReplyActivity.class);
                mIntent.putExtra("recipientId", storyModel.getUserId());
                mIntent.putExtra("storyId", storyModel.getStoryId());
                storyFragment.startActivityForResult(mIntent, AppConstants.PICK_REPLY_STORY);
                storyFragment.overridePendingTransition(R.anim.appear, R.anim.disappear);*//*


            });
            open_seen_list_layout.setOnClickListener(v -> {
                CreateStoryModel storyModel = mStories.get(getAdapterPosition());
                storyProgressView.pause();
                pause();
                Intent mIntent = new Intent(storyFragment, StoriesSeenListActivity.class);
                mIntent.putExtra("storyId", storyModel.getStoryId());
                mIntent.putExtra("userId",storyModel.getUserId());
                storyFragment.startActivityForResult(mIntent, AppConstants.PICK_REPLY_STORY);
                storyFragment.overridePendingTransition(R.anim.appear, R.anim.disappear);

            });
            btn_center.setOnTouchListener(onTouchListener);
            btn_reverse.setOnTouchListener(onTouchListener);
            btn_skip.setOnTouchListener(onTouchListener);
            storyFragment.setStoryStateListener(new StoryProgressView.StoryStateListener() {


                @Override
                public void onPause() {
                    CreateStoryModel storyModel = getItem(getAdapterPosition());
                    if (!storyModel.getType().equals("video")) return;
                    if (!paused) {
                        storyProgressView.pause();
                        if (isPlaying())
                            pause();
                        paused = true;
                    }
                }

                @Override
                public void onResume() {
                    CreateStoryModel storyModel = getItem(getAdapterPosition());
                    if (!storyModel.getType().equals("video")) return;


                    if (paused) {
                        storyProgressView.resume();
                        if (!isPlaying())
                            play();
                        paused = false;
                    }
                }
            });
        }

        public EmojiTextView getStory_body() {
            return story_body;
        }

        private View.OnTouchListener onTouchListener = new View.OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View v, MotionEvent event) {


                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        pressTime = System.currentTimeMillis();

                        if (!paused) {
                            if (story_video_progress_bar.getVisibility() == View.GONE) {
                                storyProgressView.pause();
                                pause();
                                hideMenuNavigation();
                                paused = true;
                            }
                        }

                        return false;
                    case MotionEvent.ACTION_UP:
                        long now = System.currentTimeMillis();
                        if (paused) {
                            if (story_video_progress_bar.getVisibility() == View.GONE) {
                                storyProgressView.resume();
                                play();
                                showMenuNavigation();
                                paused = false;
                            }
                        }
                        return limit < now - pressTime;
                }
                return false;
            }
        };


        private void showMenuNavigation() {
            CreateStoryModel storyModel = getItem(getAdapterPosition());
            final int flags;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
                flags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
            } else {
                flags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_FULLSCREEN;
            }

            storyFragment.getWindow().getDecorView().setSystemUiVisibility(flags);
            EventBus.getDefault().post("showTopPanel");
            StoriesViewer.fadeInAnimation(story_body_layout, 100);


            if (!storyModel.getUserId().equals(PreferenceManager.getInstance().getID(storyFragment))){
                open_reply.setVisibility(View.VISIBLE);
                StoriesViewer.fadeInAnimation(open_reply, 100);
            }
            else{
                open_seen_list_layout.setVisibility(View.VISIBLE);
                StoriesViewer.fadeInAnimation(open_seen_list_layout, 100);
            }
        }

        private void hideMenuNavigation() {
            CreateStoryModel storyModel = getItem(getAdapterPosition());
            final int flags;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
                flags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
            } else {
                flags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN;
            }

            storyFragment.getWindow().getDecorView().setSystemUiVisibility(flags);
            storyFragment.getWindow().setFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED, WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);

            final View decorView = storyFragment.getWindow().getDecorView();
            decorView.setOnSystemUiVisibilityChangeListener(visibility -> {

                if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
                    decorView.setSystemUiVisibility(flags);
                }
            });
            EventBus.getDefault().post("hideTopPanel");
            StoriesViewer.fadeOutAnimation(story_body_layout, 100);


            if (!storyModel.getUserId().equals(PreferenceManager.getInstance().getID(storyFragment))) {
                StoriesViewer.fadeOutAnimation(open_reply, 100);

            } else {
                StoriesViewer.fadeOutAnimation(open_seen_list_layout, 100);
            }
        }

        @NonNull
        @Override
        public View getPlayerView() {
            return playerView;
        }

        @NonNull
        @Override
        public PlaybackInfo getCurrentPlaybackInfo() {
            return helper != null ? helper.getLatestPlaybackInfo() : new PlaybackInfo();
        }

        @Override
        public void initialize(@NonNull Container container, @Nullable PlaybackInfo playbackInfo) {
            if (helper == null) {
                helper = new ExoPlayerViewHelper(this, mediaUri);
            }
            helper.initialize(container, playbackInfo);

            helper.addEventListener(new Playable.DefaultEventListener() {

                @Override
                public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                    super.onPlayerStateChanged(playWhenReady, playbackState);
                    //AppHelper.LogCat("onPlayerStateChanged " + playbackState);
                    if (playbackState == Player.STATE_READY && playWhenReady) {
                        story_video_thumb.animate().alpha(0.f).setDuration(250).setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {

                                if (paused) {
                                    storyProgressView.resume();
                                    paused = false;
                                }
                                story_video_progress_bar.setVisibility(View.GONE);
                            }
                        }).start();
                    } else if (playbackState == Player.STATE_BUFFERING) {
                        story_video_thumb.animate().alpha(1.f).setDuration(250).setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {

                                if (!paused) {
                                    storyProgressView.pause();
                                    paused = true;
                                }
                                story_video_progress_bar.setVisibility(View.VISIBLE);
                            }
                        }).start();
                    }
                }
            });
        }

        @Override
        public void play() {
            if (helper != null) helper.play();
        }

        @Override
        public void pause() {
            if (helper != null) helper.pause();
        }


        @Override
        public boolean isPlaying() {
            return helper != null && helper.isPlaying();
        }


        @Override
        public void release() {
            if (helper != null) {
                helper.release();
                helper = null;
            }
        }


        @Override
        public boolean wantsToPlay() {
            return ToroUtil.visibleAreaOffset(this, itemView.getParent()) >= 0.85;
        }


        @Override
        public int getPlayerOrder() {
            return getAdapterPosition();
        }

        @Override
        public String toString() {
            return "ExoPlayer{" + hashCode() + " " + getAdapterPosition() + "}";
        }

        public void setVideoStatus(Uri mediaUri) {
            this.mediaUri = mediaUri;
        }


        public void setStory_video_thumb(String thumb) {

            long interval = 5000 * 1000;
            RequestOptions options = new RequestOptions().frame(interval).transform(new BlurTransformation(AppConstants.BLUR_RADIUS));

            RequestBuilder<Bitmap> thumbnailRequest = Glide.with(WhatsCloneApplication.getInstance()).asBitmap()
                    .load(thumb).apply(options).override(10, 10);

            if (thumb.startsWith("/storage")) {

                Glide.with(WhatsCloneApplication.getInstance())
                        .asBitmap()
                        .load(thumb)
                        .dontAnimate()
                        .thumbnail(thumbnailRequest)
                        .centerCrop()
                        .override(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
                        .apply(options)
                        .into(story_video_thumb);
            } else {

                Glide.with(WhatsCloneApplication.getInstance())
                        .asBitmap()
                        .load(GlideUrlHeaders.getUrlWithHeaders(EndPoints.MESSAGE_VIDEO_URL + thumb))
                        .dontAnimate()
                        .thumbnail(thumbnailRequest)
                        .centerCrop()
                        .override(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
                        .apply(options)
                        .into(story_video_thumb);
            }
        }

    }
    private void configureImageViewHolder(ImageViewHolder holder, int position) {
        CreateStoryModel storyModel = getItem(position);
//        storyProgressView.pause();
        holder.getStory_image_progress_bar().setVisibility(View.VISIBLE);

        RequestBuilder<Drawable> thumbnailRequest = Glide.with(storyFragment)
                .load(storyModel.getUrl()).override(10, 10);

        DrawableImageViewTarget target = new DrawableImageViewTarget(holder.getImageStatus()) {


            @Override
            public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                super.onResourceReady(resource, transition);
                holder.getImageStatus().setImageDrawable(resource);
//                storyProgressView.resume();
                holder.getStory_image_progress_bar().setVisibility(View.GONE);
            }


            @Override
            public void onLoadFailed(@Nullable Drawable errorDrawable) {
                super.onLoadFailed(errorDrawable);
                holder.getImageStatus().setImageDrawable(errorDrawable);
                holder.getStory_image_progress_bar().setVisibility(View.GONE);
            }

            @Override
            public void onLoadStarted(@Nullable Drawable placeholder) {
                super.onLoadStarted(placeholder);
                holder.getImageStatus().setImageDrawable(placeholder);
                holder.getStory_image_progress_bar().setVisibility(View.GONE);
            }
        };


        if (storyModel.getUrl().startsWith("/storage")) {

            Glide.with(storyFragment)
                    .load(storyModel.getUrl())
                    .signature(new ObjectKey(storyModel.getUrl()))
                    .thumbnail(thumbnailRequest)
                    .override(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
                    .into(target);
        } else {

            Glide.with(storyFragment)
                    .load(GlideUrlHeaders.getUrlWithHeaders(storyModel.getUrl(),storyFragment))
                    .signature(new ObjectKey(storyModel.getUrl()))
                    .thumbnail(thumbnailRequest)
                    .override(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
                    .into(target);

        }


*/
/*        if (storyModel.getBody() != null) {

            holder.story_body_layout.setVisibility(View.VISIBLE);
            if (!storyModel.getBody().equals("null")) {
                holder.getStory_body().setVisibility(View.VISIBLE);
                if (storyModel.getBody().length() > 150) {
                    SpannyBen bio = new SpannyBen(storyModel.getBody().substring(0, 149) + "... ");
                    bio.append(storyFragment.getString(R.string.see_more));
                    holder.getStory_body().setText(bio);
                } else {
                    holder.getStory_body().setText(storyModel.getBody());

                }
            } else {
                holder.getStory_body().setVisibility(View.GONE);
            }


        } else {
            holder.getStory_body().setVisibility(View.GONE);
            holder.story_body_layout.setVisibility(View.GONE);
        }*//*



        if (storyModel.getUserId().equals(PreferenceManager.getInstance().getID(storyFragment))) {
            holder.open_reply.setVisibility(View.GONE);
            holder.open_seen_list_layout.setVisibility(View.VISIBLE);
        //    holder.seen_counter.setText(String.format(Locale.getDefault(),"%d", storyModel.size());
        } else {

            holder.open_reply.setVisibility(View.VISIBLE);
            holder.open_seen_list_layout.setVisibility(View.GONE);
        }

//        holder.getSeen_counter().setText(storyModel.getSeenList().size());
    }
*/

}
