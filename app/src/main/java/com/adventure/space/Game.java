package com.adventure.space;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Random;

public class Game extends AppCompatActivity {

    private static final String SAVE_MONEY = "saving_money";
    ImageView i1, i2, i3, i4, i5, i6, i7, i8, i9, i10, i11, i12, i13, i14, i15;
    ImageButton btn5, btn10, btn_back;
    TextView tikets;

    SharedPreferences sp;

    public boolean wasRunning;
    public int money, tiketsToPlay;
    public int miliseconds;
    public int image1, image2, image3, image4, image5, image6, image7, image8, image9, image10, image11, image12, image13, image14, image15;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        i1 = (ImageView)findViewById(R.id.index1);
        i2 = (ImageView)findViewById(R.id.index2);
        i3 = (ImageView)findViewById(R.id.index3);
        i4 = (ImageView)findViewById(R.id.index4);
        i5 = (ImageView)findViewById(R.id.index5);
        i6 = (ImageView)findViewById(R.id.index6);
        i7 = (ImageView)findViewById(R.id.index7);
        i8 = (ImageView)findViewById(R.id.index8);
        i9 = (ImageView)findViewById(R.id.index9);
        i10 = (ImageView)findViewById(R.id.index10);
        i11 = (ImageView)findViewById(R.id.index11);
        i12 = (ImageView)findViewById(R.id.index12);
        i13 = (ImageView)findViewById(R.id.index13);
        i14 = (ImageView)findViewById(R.id.index14);
        i15 = (ImageView)findViewById(R.id.index15);

        btn5 = (ImageButton)findViewById(R.id.play5);
        btn10 = (ImageButton)findViewById(R.id.play10);
        btn_back = (ImageButton)findViewById(R.id.btn_back);

        tikets = (TextView)findViewById(R.id.tikets);
        loadMoney();
        tikets.setText("Tikets: " + Integer.toString(money));

        btn5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!wasRunning){
                    wasRunning = true;
                    money-=5;
                    tiketsToPlay = 5;
                    tikets.setText("Tikets: " + Integer.toString(money));
                }
            }
        });

        btn10.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!wasRunning){
                    wasRunning = true;
                    money-=15;
                    tiketsToPlay = 15;
                    tikets.setText("Tikets: " + Integer.toString(money));
                }
            }
        });

        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!wasRunning){
                    onBackPressed();
                }
            }
        });

        startTimer();
    }

    public void startTimer(){
        Handler timer = new Handler();
        timer.post(new Runnable() {
            @Override
            public void run() {
                if(wasRunning){
                    miliseconds+=1;
                    gameRun();
                }
                timer.postDelayed(this, 10);
            }
        });
    }

    public void gameRun(){
        Random rand = new Random();
        if(miliseconds <= 40){
            image1 = 1+Math.abs(rand.nextInt()%4);
            spin(i1, image1);
            image2 = 1+Math.abs(rand.nextInt()%4);
            spin(i2, image2);
            image3 = 1+Math.abs(rand.nextInt()%4);
            spin(i3, image3);
        }
        if(miliseconds <= 50){
            image4 = 1+Math.abs(rand.nextInt()%4);
            spin(i4, image4);
            image5 = 1+Math.abs(rand.nextInt()%4);
            spin(i5, image5);
            image6 = 1+Math.abs(rand.nextInt()%4);
            spin(i6, image6);
        }
        if(miliseconds <= 60){
            image7 = 1+Math.abs(rand.nextInt()%4);
            spin(i7, image7);
            image8 = 1+Math.abs(rand.nextInt()%4);
            spin(i8, image8);
            image9 = 1+Math.abs(rand.nextInt()%4);
            spin(i9, image9);
        }
        if(miliseconds <= 70){
            image10 = 1+Math.abs(rand.nextInt()%4);
            spin(i10, image10);
            image11 = 1+Math.abs(rand.nextInt()%4);
            spin(i11, image11);
            image12 = 1+Math.abs(rand.nextInt()%4);
            spin(i12, image12);
        }
        if(miliseconds <= 80){
            image13 = 1+Math.abs(rand.nextInt()%4);
            spin(i13, image13);
            image14 = 1+Math.abs(rand.nextInt()%4);
            spin(i14, image14);
            image15 = 1+Math.abs(rand.nextInt()%4);
            spin(i15, image15);
        }
        if(miliseconds > 80){
            wasRunning = false;
            miliseconds = 0;
            checkLines();
        }
    }

    public void spin(ImageView view, int imageIndex){
        if(imageIndex == 1){
            view.setImageDrawable(getResources().getDrawable(R.drawable.coin));
        }
        if(imageIndex == 2){
            view.setImageDrawable(getResources().getDrawable(R.drawable.bar));
        }
        if(imageIndex == 3){
            view.setImageDrawable(getResources().getDrawable(R.drawable.kolokol));
        }
        if(imageIndex == 4){
            view.setImageDrawable(getResources().getDrawable(R.drawable.seven));
        }
        if(imageIndex == 5){
            view.setImageDrawable(getResources().getDrawable(R.drawable.podkova));
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

    public void checkLines(){
        if(image2 == image5 || image1 == image5 || image3 == image5){
            money+=tiketsToPlay*2;
        }
        if((image2 == image5 && image5 == image8) || (image1 == image5 && image5 == image7) || (image3 == image5 && image5 == image9)){
            money+=tiketsToPlay*3;
        }
        if((image2 == image5 && image5 == image8 && image8 == image11) || (image2 == image5 && image5 == image8 && image8 == image12) || (image2 == image5 && image5 == image8 && image8 == image10) || (image1 == image5 && image5 == image8 && image8 == image11) || (image3 == image5 && image5 == image8 && image8 == image11)){
            money+=tiketsToPlay*5;
        }
        if((image2 == image5 && image5 == image8 && image8 == image11 && image11 == image14) || (image2 == image5 && image5 == image8 && image8 == image11 && image11 == image13) || (image2 == image5 && image5 == image8 && image8 == image11 && image11 == image15) || (image1 == image5 && image5 == image9 && image9 == image11 && image11 == image13) || (image3 == image5 && image5 == image7 && image7 == image11 && image11 == image15) || (image1 == image5 && image5 == image8 && image8 == image11 && image11 == image14) || (image3 == image5 && image5 == image8 && image8 == image11 && image11 == image14)){
            money+=tiketsToPlay*10;
        }
        tikets.setText("Tikets: " + Integer.toString(money));
    }

    @Override
    protected void onDestroy() {
        saveMoney();
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        finish();
        super.onBackPressed();
    }
}