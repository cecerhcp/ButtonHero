package pun.projects.buttonhero;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.graphics.Color;
import android.database.sqlite.SQLiteDatabase;
import android.content.Context;
import android.database.Cursor;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

public class ScoreActivity extends AppCompatActivity {


    private SQLiteDatabase db = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_score);
        LinearLayout layout = (LinearLayout) findViewById(R.id.scoreLayout);

        TextView tv = new TextView(this);
        tv.setTextColor(Color.GREEN);
        tv.setPadding(250, 0, 0, 50);
        tv.setTextSize(22f);
        tv.setText("Highscores");
        layout.addView(tv);

        loadHighscores();

    }

    public void loadHighscores()
    {
        LinearLayout layout = (LinearLayout) findViewById(R.id.scoreLayout);
        db = openOrCreateDatabase("ScoreDB", Context.MODE_PRIVATE, null);
        List<Integer> list = new ArrayList<Integer>();
        Cursor c = db.rawQuery("SELECT * FROM score", null);
        if (c.moveToFirst())
        {
            list.add(Integer.parseInt(c.getString(2)));
        }
        while (c.moveToNext()) {
            list.add(Integer.parseInt(c.getString(2)));
        }
        Collections.sort(list);
        Integer limit;
        if (list.size() >= 10) limit = 9;
        else limit = list.size() - 1;
        Integer i = 0;
        while (i <= limit)
        {
            TextView tv = new TextView(this);
            tv.setTextColor(Color.WHITE);
            String rank = new Integer(i+1).toString();
            String score = list.get(list.size() - 1 - i).toString();
            tv.setPadding(250, 0, 0, 50);
            tv.setTextSize(20f);
            tv.setText("Rank " + rank + " - " + score);
            layout.addView(tv);
            i++;
        }


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
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
