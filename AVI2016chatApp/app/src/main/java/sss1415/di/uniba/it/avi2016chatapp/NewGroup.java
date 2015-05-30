package sss1415.di.uniba.it.avi2016chatapp;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.gc.materialdesign.views.Button;

public class NewGroup extends ActionBarActivity {
    private android.support.v7.widget.Toolbar toolbar;
    private Button addGroup;
    private EditText object;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_group);

        toolbar = (android.support.v7.widget.Toolbar)findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);
        addGroup = (Button)findViewById(R.id.btnAddGroup);
        object = (EditText)findViewById(R.id.object);
        addGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (object.getText().toString().trim().length() > 0) {

                    String objectTeam = object.getText().toString().trim();
                    Toast.makeText(getApplicationContext(),
                            "Group added successfully", Toast.LENGTH_LONG).show();

                    Intent intent = new Intent(NewGroup.this,Home.class);
                    startActivity(intent);

                } else {
                    Toast.makeText(getApplicationContext(),
                            "Please enter the new group object", Toast.LENGTH_LONG).show();
                }
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_new_groups, menu);
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
