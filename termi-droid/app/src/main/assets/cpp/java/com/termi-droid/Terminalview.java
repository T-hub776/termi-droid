package com.termi-droid;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.ScrollView;
import android.widget.TextView;

public class TerminalView extends ScrollView {
    private final TextView textView;
    private TerminalBridge bridge;

    public TerminalView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setBackgroundColor(Color.BLACK);
        setFillViewport(true);

        // Configure the terminal view rendering grid properties
        textView = new TextView(context);
        textView.setTextSize(14);
        textView.setTextColor(Color.GREEN);
        textView.setTypeface(Typeface.MONOSPACE);
        textView.setPadding(16, 16, 16, 16);

        addView(textView);
    }

    /**
     * Links the view component to the background communication bridge layer.
     */
    public void attachBridge(TerminalBridge bridge) {
        this.bridge = bridge;
    }

    /**
     * Appends raw output characters directly onto the screen layout stream.
     */
    public void appendOutput(String text) {
        textView.append(text);
        this.post(() -> fullScroll(ScrollView.FOCUS_DOWN));
    }

    /**
     * Dynamically monitors view framework changes to trigger resize signals.
     */
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (bridge != null && w > 0 && h > 0) {
            // Measure precise character metrics to calculate total matrix rows and columns
            float charWidth = textView.getPaint().measureText("M");
            float charHeight = textView.getPaint().getFontMetrics().bottom - textView.getPaint().getFontMetrics().top;
            
            int cols = (int) (w / charWidth);
            int rows = (int) (h / charHeight);
            
            // Dispatch sizing arrays to the underlying Linux process
            bridge.resizeTerminal(rows, cols, w, h);
        }
    }
                              }

