package com.termidroid;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;
import android.view.KeyEvent;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;

public class MainActivity extends Activity {
    private TextView terminalDisplay;
    private FileOutputStream outputStream;
    private FileInputStream inputStream;
    private int ptyFd;

    static {
        System.loadLibrary("termidroid");
    }

    public native int createSubprocess(String cmd, String[] envp, int[] processIdArray);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        terminalDisplay = new TextView(this);
        terminalDisplay.setTextSize(14);
        terminalDisplay.setTypeface(android.graphics.Typeface.MONOSPACE);
        terminalDisplay.setText("--- Termi-Droid Console Initialization ---\n$ ");
        setContentView(terminalDisplay);

        // Spin up the pseudo-terminal stream process boundary
        ptyFd = createSubprocess("/system/bin/sh", null, new int[1]);
        
        if (ptyFd >= 0) {
            outputStream = new FileOutputStream(new java.io.FileDescriptor());
            inputStream = new FileInputStream(new java.io.FileDescriptor());
            setDescriptorFd(outputStream.getFD(), ptyFd);
            setDescriptorFd(inputStream.getFD(), ptyFd);
            startBackgroundReadThread();
        }
    }

    private void startBackgroundReadThread() {
        new Thread(() -> {
            byte[] buffer = new byte[4096];
            int bytesRead;
            try {
                while ((bytesRead = inputStream.read(buffer)) > 0) {
                    final String text = new String(buffer, 0, bytesRead);
                    runOnUiThread(() -> terminalDisplay.append(text));
                }
            } catch (Exception ignored) {}
        }).start();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        char unicodeChar = (char) event.getUnicodeChar();
        if (outputStream != null && unicodeChar != 0) {
            try {
                outputStream.write(unicodeChar);
                outputStream.flush();
                return true;
            } catch (Exception ignored) {}
        }
        return super.onKeyDown(keyCode, event);
    }

    private void setDescriptorFd(FileDescriptor descriptor, int fd) {
        try {
            java.lang.reflect.Field field = FileDescriptor.class.getDeclaredField("descriptor");
            field.setAccessible(true);
            field.setInt(descriptor, fd);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
  }
      
