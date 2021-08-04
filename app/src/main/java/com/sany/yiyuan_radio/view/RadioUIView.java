package com.sany.yiyuan_radio.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.sany.yiyuan_radio.BandUtils;
import com.sany.yiyuan_radio.C;
import com.sany.yiyuan_radio.R;
import com.sany.yiyuan_radio.Utils;
import com.sany.yiyuan_radio.controller.ControllerAgent;

import net.grandcentrix.tray.AppPreferences;

public class RadioUIView extends LinearLayout {
    private TextView mFrequencyView;
    private TextView mNameView;
    private HorizontalScrollView mSeekWrap;
    private FrequencySeekView mSeek;
    private ControllerAgent mRadioController;
    private BandUtils.BandLimit mBandLimits;
    private int mSpacing;
    private final AppPreferences mPreferences;

    /**
     * Current frequency
     */
    private int mkHz;

    public RadioUIView(final Context context) {
        super(context);

        mPreferences = new AppPreferences(getContext());

        init();
    }

    public RadioUIView(final Context context, final AttributeSet attrs) {
        super(context, attrs);

        mPreferences = new AppPreferences(getContext());

        init();
    }
    @Override
    protected void onWindowVisibilityChanged(int visibility) {
        super.onWindowVisibilityChanged(visibility);

        this.reloadPreferences();
    }

    private void init() {
        LayoutInflater.from(getContext()).inflate(R.layout.frequency_info, this, true);
        mFrequencyView = findViewById(R.id.frequency_mhz);
        mNameView = findViewById(R.id.name_mhz);
        mSeekWrap = findViewById(R.id.frequency_seek_wrap);
        mSeek = findViewById(R.id.frequency_seek);

        reloadPreferences();
        mkHz = mBandLimits.lower;
    }

    private void reloadPreferences() {
        final int regionPref = mPreferences.getInt(C.PrefKey.TUNER_REGION, C.PrefDefaultValue.TUNER_REGION);
        final int spacingPref = mPreferences.getInt(C.PrefKey.TUNER_SPACING, C.PrefDefaultValue.TUNER_SPACING);

        mBandLimits = BandUtils.getBandLimit(regionPref);
        mSpacing = BandUtils.getSpacing(spacingPref);

        mSeek.setMinMaxValue(mBandLimits.lower, mBandLimits.upper, mSpacing);
        mSeek.setOnSeekBarChangeListener(mOnSeekFrequencyChanged);

        final float seekWidthDp = (mBandLimits.upper - mBandLimits.lower) / 10.67f;
        final int seekWidthPx = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, seekWidthDp, getResources().getDisplayMetrics());

        final ViewGroup.LayoutParams lp = mSeek.getLayoutParams();
        lp.width = seekWidthPx;
        mSeek.setLayoutParams(lp);
    }

    private void onUserClickOnFrequency(final int kHz) {
        if (mkHz == kHz) {
            return;
        }

        mRadioController.setFrequency(kHz);

        setFrequency(kHz);
    }
    public final void setFrequency(int kHz) {
        mkHz = kHz;
        final int spacing = mPreferences.getInt(C.PrefKey.TUNER_SPACING, C.PrefDefaultValue.TUNER_SPACING);

        mFrequencyView.setText(Utils.getMHz(kHz, spacing == BandUtils.SPACING_50kHz ? 2 : 1));

        mSeek.setProgress(kHz);

        scrollSeekBar();
    }

    /**
     * 根据mkhz 查找中文名称
     * @param state
     */
    public final void setRadioState(final RadioState state) {

    }


    private final SeekBar.OnSeekBarChangeListener mOnSeekFrequencyChanged = new SeekBar.OnSeekBarChangeListener() {

        private int current;

        @Override
        public void onProgressChanged(final SeekBar seekBar, final int progress, final boolean fromUser) {
            if (!fromUser) {
                return;
            }

            int curr = seekBar.getProgress();

            /*
             * Android 5.1 (Sony Xperia L at least) progress contains value from
             * seekBar.getProgress(), that was already fixed
             */
            if (curr > mBandLimits.upper) {
                curr /= 1000;
            }

            current = curr;

            onUserClickOnFrequency(current);
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) { }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            onUserClickOnFrequency(current);
        }
    };

    private void scrollSeekBar() {
        final int deltaPadding = mSeek.getPaddingLeft() + mSeek.getPaddingRight();
        final int bandLength = (mBandLimits.upper - mBandLimits.lower) / mSpacing;
        final int ticksFromStart = (mkHz - mBandLimits.lower) / mSpacing;

        final int viewWidth = mSeek.getWidth() - deltaPadding;
        final float viewInterval = viewWidth * 1f / bandLength;

        final int halfScreen = mSeekWrap.getWidth() / 2;

        final int x = (int) (viewInterval * ticksFromStart - halfScreen + mSeek.getPaddingLeft());
        mSeekWrap.smoothScrollTo(x, 0);
    }
}
