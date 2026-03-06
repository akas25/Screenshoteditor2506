package com.osamendi.screenshoteditor;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;

import org.json.JSONObject;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class UpdateChecker {

    public static void check(Context context) {

        new Thread(() -> {

            try {

                OkHttpClient client = new OkHttpClient();

                Request request = new Request.Builder()
                        .url("https://api.github.com/repos/akas25/Screenshoteditor2506/releases/latest")
                        .build();

                Response response = client.newCall(request).execute();

                if (!response.isSuccessful()) return;

                String body = response.body().string();

                JSONObject json = new JSONObject(body);

                String latestVersion = json.getString("tag_name");

                String currentVersion = "1.1";

                if (!latestVersion.equals(currentVersion)) {

                    new Handler(Looper.getMainLooper()).post(() -> {

                        new AlertDialog.Builder(context)
                                .setTitle("Update Available")
                                .setMessage("New version available: " + latestVersion)
                                .setCancelable(false)
                                .setPositiveButton("Download", (d, w) -> {

                                    Intent intent = new Intent(
                                            Intent.ACTION_VIEW,
                                            Uri.parse("https://github.com/akas25/Screenshoteditor2506/releases/latest")
                                    );

                                    context.startActivity(intent);

                                })
                                .setNegativeButton("Later", null)
                                .show();

                    });

                }

            } catch (Exception e) {

                e.printStackTrace();

            }

        }).start();
    }
}
