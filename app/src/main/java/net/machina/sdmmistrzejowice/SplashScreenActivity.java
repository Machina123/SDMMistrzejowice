package net.machina.sdmmistrzejowice;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.IntegerRes;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import net.machina.sdmmistrzejowice.common.Constants;

import java.io.File;
import java.io.InputStream;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okio.BufferedSink;
import okio.Okio;
import okio.Source;

public class SplashScreenActivity extends AppCompatActivity {
    public static final int PERMISSION_REQUEST_CODE = 127;
    public static final int SPLASH_DISMISS_TIME_MS = 2000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        if (!isConnected()) {
            AlertDialog.Builder dialog = new AlertDialog.Builder(SplashScreenActivity.this);
            dialog.setTitle(getString(R.string.label_dialog_warning))
                    .setMessage(R.string.alert_no_internet)
                    .setPositiveButton(R.string.label_go_to_settings, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            startActivity(new Intent(Settings.ACTION_SETTINGS));
                        }
                    })
                    .setNegativeButton(R.string.label_cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    })
                    .setCancelable(false)
                    .show();
        }

        checkGPS();

        if(isConnected() && isGPSActive()) {
            handlePoints();
            requestPermission();
        }

    }

    public boolean isConnected() {
        ConnectivityManager manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = manager.getActiveNetworkInfo();
        return info != null && info.isConnectedOrConnecting();
    }


    public void checkGPS() {
        LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        boolean isGPS = manager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (!isGPS) {
            AlertDialog.Builder builder = new AlertDialog.Builder(SplashScreenActivity.this);
            builder.setTitle(R.string.label_dialog_warning)
                    .setMessage(R.string.alert_no_gps)
                    .setCancelable(false)
                    .setPositiveButton(R.string.label_go_to_settings, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                        }
                    })
                    .setNegativeButton(R.string.label_not_now, null)
                    .show();
        }
    }

    public boolean isGPSActive() {
        LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        boolean isGPS = manager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        return isGPS;
    }

    public void requestPermission() {
        if (Build.VERSION.SDK_INT >= 23) {
            ActivityCompat.requestPermissions(SplashScreenActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_CODE);
        } else {
            new android.os.Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    startActivity(new Intent(SplashScreenActivity.this, MainActivity.class));
                }
            }, SPLASH_DISMISS_TIME_MS);
        }

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    new android.os.Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            startActivity(new Intent(SplashScreenActivity.this, MainActivity.class));
                            finish();
                        }
                    }, SPLASH_DISMISS_TIME_MS);
                } else {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(SplashScreenActivity.this);
                    builder .setTitle(R.string.label_dialog_warning)
                            .setMessage(R.string.alert_no_gps_permission)
                            .setPositiveButton(R.string.label_yes, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    requestPermission();
                                }
                            })
                            .setNegativeButton(R.string.label_not_now, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    AlertDialog.Builder builder1 = new AlertDialog.Builder(SplashScreenActivity.this);
                                    builder1.setMessage(R.string.alert_permissions_later)
                                            .setPositiveButton(R.string.label_ok, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    new android.os.Handler().postDelayed(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            startActivity(new Intent(SplashScreenActivity.this, MainActivity.class));
                                                            finish();
                                                        }
                                                    }, SPLASH_DISMISS_TIME_MS);
                                                }
                                            })
                                            .setCancelable(false)
                                            .show();
                                }
                            })
                            .setCancelable(false)
                            .show();
                }
                break;
            default:
                new android.os.Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        finish();
                    }
                }, 1000);
        }
    }

    public void handlePoints() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                OkHttpClient httpClient = new OkHttpClient();
                File cacheFile = new File(getCacheDir(), "points.csv");
                String localVersion = getSharedPreferences(getString(R.string.data_shared_prefs), Context.MODE_PRIVATE).getString("version", "0");
                String remoteVersion;
                if(isConnected()) {
                    try {
                        Request request = new Request.Builder().url(Constants.DATA_VERSION_URL).build();
                        Response response = httpClient.newCall(request).execute();
                        remoteVersion = response.body().string();
                        final String remoteFinal = "Remote: " + remoteVersion + " | Local: " + localVersion;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(SplashScreenActivity.this, remoteFinal, Toast.LENGTH_SHORT).show();
                            }
                        });
                        if(Integer.parseInt(remoteVersion) > Integer.parseInt(localVersion)){
                            copyRemotePoints(httpClient, cacheFile, remoteVersion);
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                } else {
                    copyPointsFromAssets(cacheFile);
                }
            }
        }).start();
    }

    public void copyPointsFromAssets(File output) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(SplashScreenActivity.this, "Copying points from assets", Toast.LENGTH_SHORT).show();
            }
        });
        if(!output.exists()) {
            try {
                output.createNewFile();
                BufferedSink sink = Okio.buffer(Okio.sink(output));
                Source assetInput = Okio.source(getAssets().open("points.csv"));
                sink.writeAll(assetInput);
                sink.close();
                assetInput.close();
            } catch(Exception ex) {
                ex.printStackTrace();
            }
        } else {
            try {
                output.delete();
                output.createNewFile();
                BufferedSink sink = Okio.buffer(Okio.sink(output));
                Source assetInput = Okio.source(getAssets().open("points.csv"));
                sink.writeAll(assetInput);
                sink.close();
                assetInput.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    public void copyRemotePoints(OkHttpClient httpClient, File output, String remoteVersion) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(SplashScreenActivity.this, "Copying points from remote location", Toast.LENGTH_SHORT).show();
            }
        });

        Request request = new Request.Builder().url(Constants.DATA_URL).build();
        Response response;
        try {
            response = httpClient.newCall(request).execute();

        } catch(Exception ex) {
            response = null;
            ex.printStackTrace();
        }

        if(!output.exists()) {
            try {
                output.createNewFile();
                BufferedSink sink = Okio.buffer(Okio.sink(output));
                sink.writeAll(response.body().source());
                sink.close();
            } catch(Exception ex) {
                ex.printStackTrace();
            }
        } else {
            try {
                output.delete();
                output.createNewFile();
                BufferedSink sink = Okio.buffer(Okio.sink(output));
                sink.writeAll(response.body().source());
                sink.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        if(response != null) response.close();
        getSharedPreferences(getString(R.string.data_shared_prefs), Context.MODE_PRIVATE).edit().putString("version", remoteVersion).commit();
    }
}
