package com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Presentation;

import android.arch.lifecycle.Observer;
import android.support.annotation.Nullable;
import android.view.View;

public class ShowViewObserver implements Observer<Boolean> {
    private View view;

    public ShowViewObserver(View view) {
        this.view = view;
    }

    @Override
    public void onChanged(@Nullable Boolean show) {
        if (show == null) {
            show = false;
        }
        view.setVisibility(show ? View.VISIBLE : View.INVISIBLE);
    }
}
