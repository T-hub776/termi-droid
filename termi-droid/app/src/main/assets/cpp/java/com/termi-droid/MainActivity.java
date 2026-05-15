package com.termidroid;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;

public class MainActivity extends Activity {
    private TerminalBridge bridge;
    private TerminalView terminalView;
    private boolean isControlPressed = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Root container layout
        LinearLayout rootLayout = new LinearLayout(this);
        rootLayout.setOrientation(LinearLayout.VERTICAL);
        rootLayout.setBackgroundColor(Color.BLACK);

        // 1. Initialize Terminal Output View
        terminalView = new TerminalView(this, null);
        LinearLayout.LayoutParams viewParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1.0f);
        rootLayout.addView(terminalView, viewParams);

        // 2. Initialize Terminal Shortcut Toolbar (Ctrl, Alt, Tab, Esc)
        HorizontalScrollView toolbarScroll = new HorizontalScrollView(this);
        LinearLayout toolbar = new LinearLayout(this);
        toolbar.setOrientation(LinearLayout.HORIZONTAL);
        toolbar.setGravity(Gravity.CENTER_VERTICAL);
        
        // Add Control Button
        final Button ctrlBtn = createToolbarButton("CTRL");
        ctrlBtn.setOnClickListener(v -> {
            isControlPressed = !isControlPressed;
            ctrlBtn.setBackgroundColor(isControlPressed ? Color.DKGRAY : Color.LTGRAY);
        });
        toolbar.addView(ctrlBtn);

        // Add Tab Button
        Button tabBtn = createToolbarButton("TAB");
        tabBtn.setOnClickListener(v -> bridge.write("\t"));
        toolbar.addView(tabBtn);

        // Add Escape Button
        Button escBtn = createToolbarButton("ESC");
        escBtn.setOnClickListener(v -> bridge.write("\u001B"));
        toolbar.addView(escBtn);

        toolbarScroll.addView(toolbar);
        rootLayout.addView(toolbarScroll);

        // 3. Initialize Live Input Buffer (Hidden or inline EditText acting as keyboard hook)
        EditText inputField = new EditText(this);
        inputField.setHint("Tap here to type...");
        inputField.setHintTextColor(Color.GRAY);
        inputField.setTextColor(Color.WHITE);
        
        // Listen to every character change in real-time
        inputField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (count > before) {
                    // Character added
                    CharSequence newChars = s.subSequence(start + before, start + count);
                    for (int i = 0; i < newChars.length(); i++) {
                        handleCharacterInput(newChars.charAt(i));
                    }
                    // Reset field to prevent text overflow tracking issues
                    inputField.removeTextChangedListener(this);
                    inputField.setText("");
                    inputField.addTextChangedListener(this);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        rootLayout.addView(inputField);
        setContentView(rootLayout);

        // 4. Initialize and Bind Native Shell Engine
        bridge = new TerminalBridge();
        bridge.startReading(terminalView);
    }

    /**
     * Intercepts characters and evaluates Ctrl/Alt modification properties
     */
    private void handleCharacterInput(char c) {
        if (isControlPressed) {
            if (c >= 'a' && c <= 'z') {
                int controlCode = c - 'a' + 1;
                bridge.write(String.valueOf((char) controlCode));
            } else if (c >= 'A' && c <= 'Z') {
                int controlCode = c - 'A' + 1;
                bridge.write(String.valueOf((char) controlCode));
            }
            isControlPressed = false;
            // Reset CTRL button visual state on UI thread
            runOnUiThread(() -> {
                // Finding control by index or maintaining global references works best
            });
        } else {
            bridge.write(String.valueOf(c));
        }
    }

    /**
     * Helper layout constructor for toolbar macros
     */
    private Button createToolbarButton(String text) {
        Button btn = new Button(this);
        btn.setText(text);
        btn.setTextSize(12);
        btn.setTextColor(Color.BLACK);
        btn.setBackgroundColor(Color.LTGRAY);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(8, 4, 8, 4);
        btn.setLayoutParams(params);
        return btn;
    }
            }
                                    
