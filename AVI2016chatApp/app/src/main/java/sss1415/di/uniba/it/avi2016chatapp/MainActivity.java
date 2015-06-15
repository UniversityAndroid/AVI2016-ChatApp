package sss1415.di.uniba.it.avi2016chatapp;

import android.app.Activity;
import android.app.ProgressDialog;
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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MainActivity extends Activity {
    private Button btnJoin;
    private EditText name;
    private EditText surname;
    private SharedPreferences memberId;

    // Creating JSON Parser object
    JSONParser jParser = new JSONParser();

    static HashMap<String, String> membershipsList;

    // url to get all memberships list
    private static String url_all_memberships = "http://10.0.2.2/chatApp_connect/get_all_memberships.php";

    // JSON Node names
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_MEMBERSHIPS = "memberships";
    private static final String TAG_MID = "codice";
    private static final String TAG_NAME = "nome";
    private static final String TAG_SURNAME = "cognome";
    // products JSONArray
    JSONArray memberships = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        membershipsList = new HashMap<>();
        btnJoin = (Button)findViewById(R.id.btnJoin);
        name = (EditText) findViewById(R.id.name);
        surname = (EditText)findViewById(R.id.surname);
        btnJoin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String name1 = name.getText().toString().trim();
                String surname1 = surname.getText().toString().trim();
                if (name1.length() > 0 && surname1.length() > 0) {

                    // Loading products in Background Thread
                    new LoadAllMemberships().execute();
                    //confronto per effettuare il login
                    //vedere come far restituire il membershipsList pieno!!!
                    /*boolean nameExists = membershipsList.containsValue(name1);
                    boolean surnameExists = membershipsList.containsValue(surname1);
                    if(nameExists && surnameExists){
                        Toast.makeText(getApplicationContext(),
                                "Welcome!", Toast.LENGTH_LONG).show();*/

                        Intent apriTabs = new Intent(MainActivity.this, Home.class);
                        startActivity(apriTabs);
                    /*
                    } else {
                        Toast.makeText(getApplicationContext(),
                                "Please enter again your data", Toast.LENGTH_LONG).show();
                    }*/

                } else {
                    Toast.makeText(getApplicationContext(),
                            "Please enter again your data", Toast.LENGTH_LONG).show();
                }

            }
        });

    }

    class LoadAllMemberships extends AsyncTask<String, String, String> {
        String name1 = name.getText().toString().trim();
        String surname1 = surname.getText().toString().trim();
        /**
         * getting All products from url
         * */
        protected String doInBackground(String... args) {
            // Building Parameters
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            // getting JSON string from URL
            JSONObject json = jParser.makeHttpRequest(url_all_memberships, "GET", params);

            // Check your log cat for JSON reponse
            Log.d("All Memberships: ", json.toString());

            try {
                // Checking for SUCCESS TAG
                int success = json.getInt(TAG_SUCCESS);

                if (success == 1) {
                    // products found
                    // Getting Array of Products
                    memberships = json.getJSONArray(TAG_MEMBERSHIPS);

                    // looping through All Products
                    for (int i = 0; i < memberships.length(); i++) {
                        JSONObject c = memberships.getJSONObject(i);

                        // Storing each json item in variable
                        String id = c.getString(TAG_MID);
                        String name = c.getString(TAG_NAME);
                        String surname = c.getString(TAG_SURNAME);

                        // creating new HashMap
                        HashMap<String, String> map = new HashMap<String, String>();

                        // adding each child node to HashMap key => value
                        map.put(TAG_MID, id);
                        map.put(TAG_NAME, name);
                        map.put(TAG_SURNAME, surname);
                        membershipsList.putAll(map);
                    }

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
