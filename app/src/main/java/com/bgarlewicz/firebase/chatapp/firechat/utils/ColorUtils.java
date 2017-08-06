package com.bgarlewicz.firebase.chatapp.firechat.utils;

import android.content.Context;
import android.content.res.Resources;
import android.support.annotation.ColorInt;
import android.util.TypedValue;

public class ColorUtils {

    public static int getColorFromAttr(Context context, int colorAttrId) {
        TypedValue typedValue = new TypedValue();
        Resources.Theme theme = context.getTheme();
        theme.resolveAttribute(colorAttrId, typedValue, true);
        @ColorInt int color = typedValue.data;
        return color;
    }
}
