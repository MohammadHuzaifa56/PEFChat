package com.pefgloble.pefchate.util;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.pefgloble.pefchate.helpers.ListenableFuture;

import androidx.annotation.IdRes;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;

public class ViewUtil {
    public static void setBackground(final @NonNull View v, final @Nullable Drawable drawable) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            v.setBackground(drawable);
        } else {
            v.setBackgroundDrawable(drawable);
        }
    }

    public static void setY(final @NonNull View v, final int y) {
        ViewCompat.setY(v, y);
    }

    public static float getY(final @NonNull View v) {
        return ViewCompat.getY(v);
    }

    public static void setX(final @NonNull View v, final int x) {
        ViewCompat.setX(v, x);
    }

    public static float getX(final @NonNull View v) {
        if (Build.VERSION.SDK_INT >= 11) {
            return ViewCompat.getX(v);
        } else {
            return ((LinearLayout.LayoutParams) v.getLayoutParams()).leftMargin;
        }
    }

    public static void swapChildInPlace(ViewGroup parent, View toRemove, View toAdd, int defaultIndex) {
        int childIndex = parent.indexOfChild(toRemove);
        if (childIndex > -1) parent.removeView(toRemove);
        parent.addView(toAdd, childIndex > -1 ? childIndex : defaultIndex);
    }

    public static CharSequence ellipsize(@Nullable CharSequence text, @NonNull TextView view) {
        if (TextUtils.isEmpty(text) || view.getWidth() == 0 || view.getEllipsize() != TextUtils.TruncateAt.END) {
            return text;
        } else {
            return TextUtils.ellipsize(text,
                    view.getPaint(),
                    view.getWidth() - view.getPaddingRight() - view.getPaddingLeft(),
                    TextUtils.TruncateAt.END);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T extends View> T inflateStub(@NonNull View parent, @IdRes int stubId) {
        return (T) ((ViewStub) parent.findViewById(stubId)).inflate();
    }

    @SuppressWarnings("unchecked")
    public static <T extends View> T findById(@NonNull View parent, @IdRes int resId) {
        return (T) parent.findViewById(resId);
    }

    @SuppressWarnings("unchecked")
    public static <T extends View> T findById(@NonNull Activity parent, @IdRes int resId) {
        return (T) parent.findViewById(resId);
    }

    /*public static <T extends View> ITrustedWebActivityCallback.Stub<T> findStubById(@NonNull Activity parent, @IdRes int resId) {
        return new ITrustedWebActivityCallback.Stub<T>((ViewStub) parent.findViewById(resId));
    }

    public static <T extends View> Stub<T> findStubById(@NonNull View parent, @IdRes int resId) {
        return new Stub<T>((ViewStub) parent.findViewById(resId));
    }*/

    private static Animation getAlphaAnimation(float from, float to, int duration) {
        final Animation anim = new AlphaAnimation(from, to);
        anim.setInterpolator(new FastOutSlowInInterpolator());
        anim.setDuration(duration);
        return anim;
    }

    public static void fadeIn(final @NonNull View view, final int duration) {
        animateIn(view, getAlphaAnimation(0f, 1f, duration));
    }

    public static ListenableFuture<Boolean> fadeOut(final @NonNull View view, final int duration) {
        return fadeOut(view, duration, View.GONE);
    }

    public static com.pefgloble.pefchate.helpers.ListenableFuture<Boolean> fadeOut(@NonNull View view, int duration, int visibility) {
        return animateOut(view, getAlphaAnimation(1f, 0f, duration), visibility);
    }

    public static com.pefgloble.pefchate.helpers.ListenableFuture<Boolean> animateOut(final @NonNull View view, final @NonNull Animation animation, final int visibility) {
        final SettableFuture future = new SettableFuture();
        if (view.getVisibility() == visibility) {
            future.set(true);
        } else {
            view.clearAnimation();
            animation.reset();
            animation.setStartTime(0);
            animation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    view.setVisibility(visibility);
                    future.set(true);
                }
            });
            view.startAnimation(animation);
        }
        return future;
    }

    public static void animateIn(final @NonNull View view, final @NonNull Animation animation) {
        if (view.getVisibility() == View.VISIBLE) return;

        view.clearAnimation();
        animation.reset();
        animation.setStartTime(0);
        view.setVisibility(View.VISIBLE);
        view.startAnimation(animation);
    }

    @SuppressWarnings("unchecked")
    public static <T extends View> T inflate(@NonNull LayoutInflater inflater,
                                             @NonNull ViewGroup parent,
                                             @LayoutRes int layoutResId) {
        return (T) (inflater.inflate(layoutResId, parent, false));
    }


    public static int dpToPx(Context context, int dp) {
        return (int) ((dp * context.getResources().getDisplayMetrics().density) + 0.5);
    }
}
