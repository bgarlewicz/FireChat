package com.bgarlewicz.firebase.chatapp.firechat.views;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;

public class TitleView extends android.support.v7.widget.AppCompatTextView {


    public TitleView(Context context) {
        super(context);
        init();
    }

    public TitleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public TitleView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public void init() {
        Typeface tf = Typeface.createFromAsset(getContext().getAssets(), "fonts/bimini_normal.ttf");
        setTypeface(tf, Typeface.NORMAL);

    }
}
