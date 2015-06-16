package sss1415.di.uniba.it.avi2016chatapp;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.gc.materialdesign.views.Button;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MainActivity extends Activity {
    private Button btnJoin;
    private EditText name1;
    private EditText surname1;
    private SharedPreferences memberId;

    // Creating JSON Parser object
    JSONParser jParser = new JSONParser();

    static HashMap<String, String> membershipList;

    // url to get all memberships list
    private static String url_membership = "http://10.0.2.2/chatApp_connect/login.php";

    // JSON Node names
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_MEMBERSHIPS = "memberships";
    private static final String TAG_MID = "codice";

    // products JSONArray
    JSONArray memberships = null;
    JSONParser jsonParser = new JSONParser();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        membershipList = new HashMap<>();
        btnJoin = (Button)findViewById(R.id.btnJoin);
        name1 = (EditText) findViewById(R.id.name);
        surname1 = (EditText)findViewById(R.id.surname);
        memberId = getSharedPreferences(TAG_MID, MODE_PRIVATE);
        btnJoin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String name = name1.getText().toString().trim();
                String surname = surname1.getText().toString().trim();
                if (name.length() > 0 && surname.length() > 0) {

                    // Loading products in Background Thread
                    new LoadMembership().execute();

                } else {
                    Toast.makeText(getApplicationContext(),
                            "Please enter again your data", Toast.LENGTH_LONG).show();
                }

            }
        });

    }

    class LoadMembership extends AsyncTask<String, String, String> {
        String name = name1.getText().toString().trim();
        String surname = surname1.getText().toString().trim();
        /**
         * getting All products from url
         * */
        protected String doInBackground(String... args) {
            // Building Parameters
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("name", name));
            params.add(new BasicNameValuePair("surname", surname));
            // getting JSON Object
            // Note that create product url accepts POST method
            JSONObject json = jsonParser.makeHttpRequest(url_membership, "POST", params);

            // check log cat fro response
                Log.d("Create Response", json.toString());
            // check for success tag

            try {
                int success = json.getInt(TAG_SUCCESS);

                    if (success == 1) {
                        memberships = json.getJSONArray(TAG_MEMBERSHIPS);
                        JSONObject c = memberships.getJSONObject(0);

                        // Storing each json item in variable
                        String codice = c.getString(TAG_MID);
                        System.out.println("id: " + codice);
                        // successfully login
                        //sharedPrefences
                        SharedPreferences.Editor preferencesEditor_id = memberId.edit();
                        preferencesEditor_id.putString(TAG_MID, codice);
                        preferencesEditor_id.apply();

                        Intent i = new Intent(getApplicationContext(), Home.class);
                        startActivity(i);
                        // closing this screen
                        finish();
                    }else {
                        runOnUiThread(new Runnable() {
                            public void run() {

                                Toast.makeText(MainActivity.this, "Please enter again your data", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }


            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;

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
