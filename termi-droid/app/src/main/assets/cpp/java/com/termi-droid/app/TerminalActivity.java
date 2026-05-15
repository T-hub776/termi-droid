package com.termidroid.app;

import android.content.Context;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import java.io.*;

public class TerminalActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Target paths unique to termi-droid
        File systemPrefix = new File("/data/data/com.termidroid.app/files/usr");
        File homeDirectory = new File("/data/data/com.termidroid.app/files/home");

        // Verify if your environment setup has already been extracted
        if (!systemPrefix.exists()) {
            systemPrefix.mkdirs();
            homeDirectory.mkdirs();
            extractBootstrapFiles(this, "bootstrap-aarch64.tar.xz", systemPrefix);
        }
    }

    private void extractBootstrapFiles(Context context, String assetName, File destDir) {
        try (InputStream is = context.getAssets().open(assetName);
             FileOutputStream fos = new FileOutputStream(new File(destDir, "bootstrap.tar.xz"))) {
            
            byte[] buffer = new byte[4096];
            int length;
            while ((length = is.read(buffer)) > 0) {
                fos.write(buffer, 0, length);
            }
            
            // Execute a native extraction shell call to untar files instantly
            String targetCmd = "tar -xJf " + destDir.getAbsolutePath() + "/bootstrap.tar.xz -C " + destDir.getAbsolutePath();
            Runtime.getRuntime().exec(targetCmd).waitFor();
            
            // Delete the temporary installer archive file
            new File(destDir, "bootstrap.tar.xz").delete();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
        }
              
