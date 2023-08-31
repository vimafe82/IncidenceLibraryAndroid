package es.incidence.core.utils;

import android.os.Parcel;
import android.text.ParcelableSpan;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.style.MetricAffectingSpan;
import android.text.style.SuperscriptSpan;

import androidx.annotation.NonNull;

public class CustomSuperscriptSpan extends SuperscriptSpan  {
    public CustomSuperscriptSpan() {
    }

    @Override
    public void updateDrawState(@NonNull TextPaint textPaint) {
        textPaint.baselineShift += (int) (textPaint.ascent());
    }

    @Override
    public void updateMeasureState(@NonNull TextPaint textPaint) {
        textPaint.baselineShift += (int) (textPaint.ascent() / 2);
    }
}
