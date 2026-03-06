package com.osamendi.screenshoteditor;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

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

                String body = response.body().string();

                JSONObject json = new JSONObject(body);

                String latestVersion = json.getString("tag_name");

                if(!latestVersion.equals(BuildConfig.VERSION_NAME)) {

                    ((android.app.Activity)context).runOnUiThread(() -> {

                        new AlertDialog.Builder(context)
                                .setTitle("Update Available")
                                .setMessage("New version available")
                                .setPositiveButton("Download", (d,w)->{

                                    Intent i = new Intent(Intent.ACTION_VIEW,
                                            Uri.parse("https://github.com/akas25/Screenshoteditor2506/releases/latest"));

                                    context.startActivity(i);

                                })
                                .setNegativeButton("Later",null)
                                .show();

                    });

                }

            } catch (Exception e) {

                e.printStackTrace();

            }

        }).start();
    }
}
