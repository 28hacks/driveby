package com.github.a28hacks.driveby.usecase.settings;

import android.content.Context;
import android.graphics.Color;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


public class ColoredTitlePreference extends Preference {
    public ColoredTitlePreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public ColoredTitlePreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public ColoredTitlePreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ColoredTitlePreference(Context context) {
        super(context);
    }

    @Override
    protected View onCreateView(ViewGroup parent) {
        View view = super.onCreateView(parent);
        TextView titleView = (TextView) view.findViewById(android.R.id.title);
        titleView.setTextColor(Color.RED);
        return view;
    }
}
