package com.pefgloble.pefchate.ui;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import com.pefgloble.pefchate.R;
import com.vanniktech.emoji.listeners.OnSoftKeyboardCloseListener;
import com.vanniktech.emoji.listeners.OnSoftKeyboardOpenListener;

import androidx.annotation.NonNull;

public class InputGeneralPanel extends LinearLayout implements OnSoftKeyboardCloseListener, OnSoftKeyboardOpenListener {

    private static final String TAG = InputGeneralPanel.class.getSimpleName();


    // private QuoteView quoteView;
    private EmojiToggleButton emojiToggle;
    private EditText composeText;


    public InputGeneralPanel(Context context) {
        super(context);
    }

    public InputGeneralPanel(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public InputGeneralPanel(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void onFinishInflate() {
        super.onFinishInflate();
        this.emojiToggle = findViewById(R.id.emoji_toggle);
        this.composeText=findViewById(R.id.embedded_text_editor);


        emojiToggle.setVisibility(View.VISIBLE);

    }

    public void setListener(final @NonNull Listener listener) {
        emojiToggle.setOnClickListener(v -> listener.onEmojiToggle());
    }


    public void setEnabled(boolean enabled) {
        composeText.setEnabled(enabled);
        emojiToggle.setEnabled(enabled);
    }


    @Override
    public void onKeyboardClose() {

    }

    @Override
    public void onKeyboardOpen(int keyBoardHeight) {
        emojiToggle.setToEmoji();
    }


    public void setToIme() {
        emojiToggle.setToIme();
    }

    public void setToEmoji() {
        emojiToggle.setToEmoji();
    }


    public interface Listener {

        void onEmojiToggle();
    }

}
