package com.deange.numberview;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.LinearLayout;

import com.deange.numberview.digits.Digit;
import com.deange.numberview.digits.Digits;

public class NumberViewGroup extends LinearLayout {

    private boolean mPerformNow;
    private int mMinShown;
    private int mNumber;
    private boolean mHide;

    private PaintProvider mPaintProvider;

    public NumberViewGroup(final Context context) {
        super(context);
        init();
    }

    public NumberViewGroup(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public NumberViewGroup(final Context context, final AttributeSet attrs, final int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public NumberViewGroup(
            final Context context,
            final AttributeSet attrs,
            final int defStyleAttr,
            final int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        setOrientation(HORIZONTAL);
        setGravity(Gravity.CENTER);
    }

    protected NumberView addNewChild() {
        final NumberView child = new NumberView(getContext());
        if (mPaintProvider != null) {
            mPaintProvider.mutate(child.getPaint(), getChildCount());
        }

        child.hideNow();
        addView(child, 0);

        return child;
    }

    private Digit resolveDigit(int number, int digit) {
        return (mHide) ? Digits.empty() : Digits.forInt((int) ((number / Math.pow(10, digit)) % 10));
    }

    private int getIntLength(final int number) {
        // Yeah it's ugly as sin but it works, and it works fast.
        final int n = (number == Integer.MIN_VALUE) ? Integer.MAX_VALUE : Math.abs(number);
        return n < 100000
                ? n < 100 ? n < 10 ? 1 : 2 : n < 1000 ? 3 : n < 10000 ? 4 : 5
                : n < 10000000 ? n < 1000000 ? 6 : 7 : n < 100000000 ? 8 : n < 1000000000 ? 9 : 10;
    }

    private int getRequiredChildCount() {
        return Math.max(mMinShown, mHide ? 0 : getIntLength(mNumber));
    }

    private void bindViews() {

        final int size = getRequiredChildCount();

        for (int i = 0; i < size; i++) {

            while (i >= getChildCount()) {
                addNewChild();
            }

            final NumberView child = getDigitAt(i);
            final Digit d = resolveDigit(mNumber, i);

            if (mPerformNow) {
                child.showNow(d);
            } else {
                child.show(d);
            }
        }

        for (int i = size; i < getChildCount(); i++) {
            // Unused children :'(
            final NumberView child = getDigitAt(i);
            if (mPerformNow) {
                child.hideNow();
            } else {
                child.hide();
            }
        }

        requestLayout();
        invalidate();
    }

    public NumberView getDigitAt(final int index) {
        // Reverse the indexing order of the children
        return (NumberView) getChildAt(getChildCount() - index - 1);
    }

    public NumberView[] getDigits() {
        // Returns views in order from LSB to MSB
        final NumberView[] views = new NumberView[getChildCount()];
        for (int i = 0; i < getChildCount(); i++) {
            views[i] = getDigitAt(i);
        }
        return views;
    }

    public void show(final int number) {
        mHide = false;
        mNumber = number;
        mPerformNow = false;
        bindViews();
    }

    public void showNow(final int number) {
        mHide = false;
        mNumber = number;
        mPerformNow = true;
        bindViews();
    }

    public void hide() {
        mHide = true;
        mPerformNow = false;
        bindViews();
    }

    public void hideNow() {
        mHide = true;
        mPerformNow = true;
        bindViews();
    }

    public void setMinimumNumbersShown(final int minimum) {
        mMinShown = minimum;

        while (getChildCount() < mMinShown) {
            addNewChild();
        }
    }

    public void setPaintProvider(final PaintProvider paintProvider) {
        mPaintProvider = paintProvider;
    }
}
