package com.sany.yiyuan_radio.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.SeekBar;

import java.util.Locale;

/**
 * vlad805 (c) 2018
 * Custom SeekBar
 */
@SuppressLint("AppCompatCustomView")
public class FrequencySeekView extends SeekBar {
	private Paint mTrait;
	private Paint mFrequency0;
	private Paint mFrequency5;
	private Paint mCurrentLine;
	private Paint mStationLine;

	private int mValueMin = 0;
	private int mValueMax = 100;
	private int mSpacing = 10;

	public FrequencySeekView(final Context context) {
		super(context);
		init();
	}

	public FrequencySeekView(final Context context, final AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	private void init() {
		final float dpi = getResources().getDisplayMetrics().density;

		mTrait = new Paint();
		mTrait.setColor(Color.GRAY);
		mTrait.setStrokeWidth(dpi);
		mTrait.setStyle(Paint.Style.STROKE);

		mFrequency0 = new Paint();
		mFrequency0.setColor(Color.WHITE);
		mFrequency0.setTextSize(sp2px(22));
		mFrequency0.setTextAlign(Paint.Align.CENTER);
		mFrequency0.setAntiAlias(true);

		mFrequency5 = new Paint();
		mFrequency5.setColor(Color.LTGRAY);
		mFrequency5.setTextSize(sp2px(12));
		mFrequency5.setTextAlign(Paint.Align.CENTER);
		mFrequency5.setColor(Color.LTGRAY);
		mFrequency5.setAntiAlias(true);

		mCurrentLine = new Paint();
		mCurrentLine.setColor(Color.RED);
		mCurrentLine.setStrokeWidth(dpi * 2f);
		mCurrentLine.setStyle(Paint.Style.STROKE);

		mStationLine = new Paint();
		mStationLine.setColor(Color.YELLOW);
		mStationLine.setStrokeWidth(dpi);
		mStationLine.setStyle(Paint.Style.STROKE);

		invalidate();
	}

	private void setMaxValue(final int maxValue) {
		mValueMax = maxValue;
	}

	private void setMinValue(final int minValue) {
		mValueMin = minValue;
	}

	public void setSpacing(final int spacing) {
		mSpacing = spacing;
	}

	/**
	 * Change edges
	 * @param minValue Lower frequency in kHz
	 * @param maxValue Higher frequency in kHz
	 * @param spacing Spacing in kHz
	 */
	public void setMinMaxValue(final int minValue, final int maxValue, final int spacing) {
		setMinValue(minValue);
		setMaxValue(maxValue);
		setSpacing(spacing);
		setMax((mValueMax - mValueMin) / mSpacing);
	}

	private int sp2px(final float spValue) {
		final float fontScale = getResources().getDisplayMetrics().scaledDensity;
		return (int) (spValue * fontScale + 0.5f);
	}

	/**
	 * Returns current frequency
	 * @param progress Value by SeekBar
	 * @return Frequency in kHz
	 */
	public int fixProgress(final int progress) {
		return progress * mSpacing + mValueMin;
	}

	/**
	 * Change current frequency
	 * @param kHz Frequency in kHz
	 */
	@Override
	public synchronized void setProgress(final int kHz) {
		if (mSpacing != 0) {
			super.setProgress((kHz - mValueMin) / mSpacing);
		}
	}

	/**
	 * Returns current frequency in kHz
	 * @return Frequency in kHz
	 */
	@Override
	public synchronized int getProgress() {
		return fixProgress(super.getProgress());
	}

	@Override
	protected void onDraw(final Canvas canvas) {
		final int paddingTop = 0;

		// Width of view
		final int viewWidth = getWidth() - getPaddingRight() - getPaddingLeft();

		// Padding from left side
		final int deltaX = getPaddingLeft();

		// Computed width of interval between dashes of frequencies
		final float viewInterval = viewWidth / ((mValueMax - mValueMin) * 1f / mSpacing);

		// y-coordinate, end of short stick relative to the top
		final int baseYShort = paddingTop + 25;

		// y-coordinate, end of long dash relative to the top
		final int baseYLong = paddingTop + 60;

		// y-coordinate, position of label .0 relative to the top
		final int baseYText0 = baseYLong + 5 + (int) mFrequency0.getTextSize();

		// y-coordinate, position of label .5 relative to the top
		final int baseYText5 = baseYLong + 5 + (int) mFrequency5.getTextSize();

		// Selected kHz
		final int kHzCurrent = getProgress();

		// Draw horizontal line
		canvas.drawLine(deltaX, paddingTop, getWidth() - getPaddingRight(), paddingTop, mTrait);

		// For each 100 kHz ...
		for (int i = 0; i <= (mValueMax - mValueMin) / mSpacing; ++i) {

			// kHz frequency for current iteration
			final int kHz = mValueMin + i * mSpacing;

			// X coordinate for current iteration
			final float x = deltaX + viewInterval * i;

			// The presence of the current frequency in the list of stations
			final boolean hasStation = false; // mStations != null && mStations.contains(kHz);

			// Choose color for dash in depend of presence of the current frequency in the list of stations
			@SuppressWarnings("ConstantConditions")
			final Paint colorTrait = hasStation ? mStationLine : mTrait;

			final boolean isRoundMHz = kHz % 1000 == 0;

			// Label
			final String MHz = String.format(Locale.ENGLISH, isRoundMHz ? "%.0f" : "%.1f", kHz / 1000f);

			// If .0 MHz or .5 MHz
			if (kHz % 500 == 0) {
				// Draw long dash
				canvas.drawLine(x, paddingTop, x, baseYLong, colorTrait);

				canvas.drawText(MHz, x,
					isRoundMHz ? baseYText0 : baseYText5,
					isRoundMHz ? mFrequency0 : mFrequency5
				);
			} else {
				// Others dashes are short
				canvas.drawLine(x, paddingTop, x, baseYShort, colorTrait);
			}

			// If selected frequency equals current iterate, draw red line
			if (kHzCurrent == kHz) {
				canvas.drawLine(x, 0, x, getHeight(), mCurrentLine);
			}
		}

		super.onDraw(canvas);
	}
/*
	/**
	 * List of stations, that will be draw
	 * /
	private Set<Integer> mStations;


	/**
	 * Update list of stations for draw lines on seek bar
	 * @param stations List of frequencies
	 * /
	public void notifyStationList(List<Integer> stations) {
		mStations = new HashSet<>(stations);
		invalidate();
	}
 */
}
