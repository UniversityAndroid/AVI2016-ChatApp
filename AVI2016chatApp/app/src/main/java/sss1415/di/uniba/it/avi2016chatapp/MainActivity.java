package sss1415.di.uniba.it.avi2016chatapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.gc.materialdesign.views.Button;


public class MainActivity extends Activity {
    private Button btnJoin;
    private EditText name;
    private EditText surname;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btnJoin = (Button)findViewById(R.id.btnJoin);
        name = (EditText) findViewById(R.id.name);
        surname = (EditText)findViewById(R.id.surname);
        btnJoin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (name.getText().toString().trim().length() > 0 && surname.getText().toString().trim().length() > 0) {

                    String name1 = name.getText().toString().trim();
                    String surname1 = surname.getText().toString().trim();
                    Toast.makeText(getApplicationContext(),
                            "Welcome!", Toast.LENGTH_LONG).show();

                    Intent apriTabs = new Intent(MainActivity.this, Home.class);
                    startActivity(apriTabs);

                } else {
                    Toast.makeText(getApplicationContext(),
                            "Please enter your data", Toast.LENGTH_LONG).show();
                }

            }
        });

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
