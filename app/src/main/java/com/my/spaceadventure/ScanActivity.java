package com.my.spaceadventure;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

public class ScanActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String SAVE_MONEY = "saving_money";
    private ImageButton scanBtn;
    private int money;
    private SharedPreferences sp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);

        loadMoney();

        scanBtn = (ImageButton)findViewById(R.id.scan_button);

        scanBtn.setOnClickListener(this);
    }

    @Override
    public void onBackPressed() {
        finish();
        super.onBackPressed();
    }

    public void scanQRCode(View v) {
        IntentIntegrator integrator = new IntentIntegrator(ScanActivity.this);
        integrator.initiateScan(IntentIntegrator.QR_CODE_TYPES);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        IntentResult result =
                IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
        if (result != null) {
            String contents = result.getContents();
            if (contents != null) {
                if(money < 100){
                    money+=Integer.parseInt(contents)%1000000;
                    showDialog(R.string.zachislenie, "You earned " + Integer.parseInt(contents)%1000000 + " tickets!");
                    saveMoney();
                }
                if(money > 100){
                    showDialog(R.string.allert, "You cant add tickets if them count more than 100!!!");
                }
            } else {
                showDialog(R.string.result_failed,
                        getString(R.string.result_failed_why));
            }
        }
    }

    private void showDialog(int title, CharSequence message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton(R.string.ok_button, null);
        builder.show();
    }

    @Override
    public void onClick(View v) {
        if(v.getId()==R.id.scan_button){
            IntentIntegrator scanIntegrator = new IntentIntegrator(this);
            scanIntegrator.initiateScan();
        }
    }

    public void saveMoney(){
        sp = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt(SAVE_MONEY, money);
        editor.commit();
    }

    public void loadMoney(){
        sp = getPreferences(MODE_PRIVATE);
        money = sp.getInt(SAVE_MONEY, 10000);
    }
}