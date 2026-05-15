package com.termidroid.app;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;

public class TerminalActivity extends AppCompatActivity {

    private TextView terminalOutput;
    private EditText commandInput;
    private ScrollView terminalScroll;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        terminalOutput = findViewById(R.id.terminal_output);
        commandInput = findViewById(R.id.command_input);
        terminalScroll = findViewById(R.id.terminal_scroll);

        // Bootstrap verification block
        File systemPrefix = new File("/data/data/com.termidroid.app/files/usr");
        File homeDirectory = new File("/data/data/com.termidroid.app/files/home");
        if (!systemPrefix.exists()) {
            systemPrefix.mkdirs();
            homeDirectory.mkdirs();
            extractBootstrapFiles();
        }

        // Intercept user tapping "Enter" on their soft keyboards
        commandInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEND || 
                    (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN)) {
                    
                    String cmd = commandInput.getText().toString().trim();
                    if (!cmd.isEmpty()) {
                        executeUserCommand(cmd);
                        commandInput.setText("");
                    }
                    return true;
                }
                return false;
            }
        });
    }

    private void executeUserCommand(final String cmd) {
        terminalOutput.append("\n$ " + cmd + "\n");
        
        // Push process pipeline execution onto an isolated thread to prevent UI freezing
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Process process = TerminalExecutor.startTerminalSession(cmd);
                    BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                    BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
                    
                    String line;
                    final StringBuilder outputCollector = new StringBuilder();
                    
                    // Consume stdout output channels
                    while ((line = reader.readLine()) != null) {
                        outputCollector.append(line).append("\n");
                    }
                    // Consume stderr channels if failures arise
                    while ((line = errorReader.readLine()) != null) {
                        outputCollector.append(line).append("\n");
                    }

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            terminalOutput.append(outputCollector.toString());
                            // Automatically snap terminal focus to the bottom entry line
                            terminalScroll.post(new Runnable() {
                                @Override
                                public void run() {
                                    terminalScroll.fullScroll(ScrollView.FOCUS_DOWN);
                                }
                            });
                        }
                    });

                } catch (Exception e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            terminalOutput.append("[Engine Error]: Execution failed.\n");
                        }
                    });
                }
            }
        }).start();
    }

    private void extractBootstrapFiles() {
        try {
            InputStream is = getAssets().open("bootstrap-aarch64.tar.xz");
            File out = new File("/data/data/com.termidroid.app/files/usr/bootstrap-aarch64.tar.xz");
            try (FileOutputStream fos = new FileOutputStream(out)) {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = is.read(buffer)) != -1) {
                    fos.write(buffer, 0, bytesRead);
                }
            }
            terminalOutput.append("[System]: Core system binaries staged successfully.\n");
        } catch (Exception e) {
            terminalOutput.append("[System Error]: Failed to unpack internal core assets.\n");
        }
    }
    }
            
