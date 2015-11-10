package pun.projects.buttonhero;

import android.animation.FloatEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    ArrayList<ImageView> blues = new ArrayList<>();
    ArrayList<ImageView> reds = new ArrayList<>();
    ArrayList<ImageView> greens = new ArrayList<>();
    ArrayList<ImageView> yellows = new ArrayList<>();

    Integer score = 0;
    Integer lives = 10;

    private Random rand = new Random();

    private static SoundPool soundPool;
    private static HashMap soundPoolMap;

    private SQLiteDatabase db = null;

    Integer RED = 0;
    Integer BLUE = 1;
    Integer GREEN = 2;
    Integer YELLOW = 3;
    Integer ERROR = 4;
    Integer SPEED = 3500;
    Integer INTERVAL = 500;
    Integer MIN_INTERVAL = 500;

    String name = "Player";

    Float HITZONE_START = 760f;
    Float HITZONE_END = 940f;
    Float SCREEN_END = 990f;

    Float[] startX = new Float[4];
    Integer difficultyCounter = 0;
    Boolean isPlaying = true;

    RelativeLayout screen = null;

    Button redButton = null;
    Button greenButton = null;
    Button blueButton = null;
    Button yellowButton = null;

    TextView scoreView = null;
    TextView livesView = null;

    private Handler handler = new Handler();
    private Handler gameOverHandler = new Handler();
    private Handler screenHandler = new Handler();
    private Runnable buttonRunnable = new Runnable(){
        public void run() {
            Integer color = rand.nextInt(4);
            addNewButton(color, SPEED);
            if (INTERVAL > MIN_INTERVAL) difficultyCounter++;
            if (difficultyCounter == 10)
            {
                difficultyCounter = 0;
                INTERVAL -= 150;
            }
            handler.postDelayed(buttonRunnable, INTERVAL);

        }
    };
    private Runnable gameOverRunnable = new Runnable() {
        public void run() {
            checkGameOver();
            gameOverHandler.postDelayed(gameOverRunnable, 200);
        }
    };
    private Runnable screenRunnable = new Runnable(){
        public void run(){
         livesView.setText("Lives: " + lives.toString());
         checkOutOfBoundsButtons();
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = openOrCreateDatabase("ScoreDB", Context.MODE_PRIVATE, null);
        db.execSQL("CREATE TABLE IF NOT EXISTS score(_id INTEGER PRIMARY KEY AUTOINCREMENT, name VARCHAR, score INTEGER);");

        scoreView = (TextView) findViewById(R.id.scoreView);
        livesView = (TextView) findViewById(R.id.livesView);
        redButton = (Button) findViewById(R.id.redButton);
        greenButton = (Button) findViewById(R.id.greenButton);
        blueButton = (Button) findViewById(R.id.blueButton);
        yellowButton = (Button) findViewById(R.id.yellowButton);

        screen = (RelativeLayout) findViewById(R.id.frame);

        rand.setSeed(Calendar.getInstance().get(Calendar.SECOND));
        startX[RED] = 120f;
        startX[BLUE] = 220f;
        startX[GREEN] = 320f;
        startX[YELLOW] = 420f;

        soundPool = new SoundPool(4, AudioManager.STREAM_MUSIC, 100);
        soundPoolMap = new HashMap(4);
        soundPoolMap.put(RED, soundPool.load(this, R.raw.red, 1));
        soundPoolMap.put(GREEN, soundPool.load(this, R.raw.c, 2));
        soundPoolMap.put(BLUE, soundPool.load(this, R.raw.b, 3));
        soundPoolMap.put(YELLOW, soundPool.load(this, R.raw.d, 4));
        soundPoolMap.put(ERROR, soundPool.load(this, R.raw.error, 5));


        redButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkPressedButton(RED, reds);
                scoreView.setText("Score: " + score.toString());
                livesView.setText("Lives: " + lives.toString());

            }
        });

        greenButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkPressedButton(GREEN, greens);
                scoreView.setText("Score: " + score.toString());
                livesView.setText("Lives: " + lives.toString());

            }
        });

        blueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkPressedButton(BLUE, blues);
                scoreView.setText("Score: " + score.toString());
                livesView.setText("Lives: " + lives.toString());

            }
        });

        yellowButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkPressedButton(YELLOW, yellows);
                scoreView.setText("Score: " + score.toString());
                livesView.setText("Lives: " + lives.toString());

            }
        });

        handler.post(buttonRunnable);
        screenHandler.post(screenRunnable);
        gameOverHandler.post(gameOverRunnable);



    }


    public void checkGameOver()
    {
        if (lives <= 0 && isPlaying)
        {
            livesView.setText("Lives: 0");
            handler.removeCallbacks(buttonRunnable);
            screenHandler.removeCallbacks(screenRunnable);
            gameOverHandler.removeCallbacks(gameOverRunnable);
            isPlaying = false;
            if (isHighscore(score))
            {
                db.execSQL("INSERT INTO score VALUES(NULL, '" + name + "', " + score.toString() + ");");
            }
            finish();
        }

    }

    public Boolean isHighscore(Integer value)
    {
        List<Integer> list = new ArrayList<Integer>();
        Cursor c = db.rawQuery("SELECT * FROM score", null);
        if(c.moveToFirst())
        {
            list.add(Integer.parseInt(c.getString(2)));
        }
        else return true;

        while (c.moveToNext())
        {
            list.add(Integer.parseInt(c.getString(2)));
        }
        Collections.sort(list);
        int i;
        if (list.size() >= 10) i = list.size() - 1 - 9;
        else return true;
        if (value > list.get(i)) return true;
        else return false;

    }

    public void checkOutOfBoundsButtons()
    {
        for (ImageView view : reds)
        {
            Float y = view.getY();
            if (y > SCREEN_END)
            {
                reds.remove(view);
                screen.removeView(view);
                screenHandler.postDelayed(screenRunnable, 200);
                lives--;
                return;
            }
        }
        for (ImageView view : greens)
        {
            Float y = view.getY();
            if (y > SCREEN_END)
            {
                greens.remove(view);
                screen.removeView(view);
                screenHandler.postDelayed(screenRunnable, 200);
                lives--;
                return;
            }
        }
        for (ImageView view : blues)
        {
            Float y = view.getY();
            if (y > SCREEN_END)
            {
                blues.remove(view);
                screen.removeView(view);
                screenHandler.postDelayed(screenRunnable, 200);
                lives--;
                return;
            }
        }
        for (ImageView view : yellows)
        {
            Float y = view.getY();
            if (y > SCREEN_END)
            {
                yellows.remove(view);
                screen.removeView(view);
                screenHandler.postDelayed(screenRunnable, 200);
                lives--;
                return;
            }
        }
        screenHandler.postDelayed(screenRunnable, 200);
    }

    public void checkPressedButton(Integer color, ArrayList<ImageView> list)
    {
        if (!isPlaying) return;

        for (ImageView view : list)
        {
            Float y = view.getY();
            if (y > HITZONE_START && y < HITZONE_END)
            {
                list.remove(view);
                view.setVisibility(ImageView.INVISIBLE);
                screen.removeView(view);
                score += 10;
                soundPool.play((Integer) soundPoolMap.get(color), 1, 1, 1, 0, 1f);
                return;
            }
        }
        soundPool.play((Integer) soundPoolMap.get(ERROR), 1, 1, 1, 0, 1f);
        lives--;

    }


    public void addNewButton(Integer color, Integer speed)
    {
        ImageView but = new ImageView(this);
        if (color == RED) {
            but.setImageDrawable(getResources().getDrawable(R.drawable.red_button));
            reds.add(but);
        }
        else if (color == BLUE)
        {
            but.setImageDrawable(getResources().getDrawable(R.drawable.blue_button));
            blues.add(but);
        }
        else if (color == GREEN)
        {
            but.setImageDrawable(getResources().getDrawable(R.drawable.green_button));
            greens.add(but);
        }
        else
        {
            but.setImageDrawable(getResources().getDrawable(R.drawable.yellow_button));
            yellows.add(but);
        }
        but.setX(startX[color]);
        but.setY(60f);
        screen.addView(but);
        startAnimation(but, speed);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
        }

        return super.onOptionsItemSelected(item);
    }

    public void startAnimation(final ImageView view, int time) {

        ValueAnimator anim = ValueAnimator.ofObject(new FloatEvaluator(), 0f,
                1000f);

        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                Float y = (Float) animation.getAnimatedValue();
                view.setY(y);

            }
        });


        anim.setInterpolator(new LinearInterpolator());
        anim.setDuration(time);
        anim.start();
    }
}
