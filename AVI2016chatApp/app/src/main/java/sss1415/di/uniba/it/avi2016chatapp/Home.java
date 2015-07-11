package sss1415.di.uniba.it.avi2016chatapp;

/**
 * Created by katia on 27/05/2015.
 */

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.afollestad.materialdialogs.AlertDialogWrapper;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * This java class contains two lists: memberships and groups.
 * It allow you to start a conversation with a membership or a group.
 */

public class Home extends ActionBarActivity {

    // Declaring Your View and Variables
    Toolbar toolbar;
    ViewPager pager;
    ViewPagerAdapter adapter;
    SlidingTabLayout tabs;
    CharSequence Titles[] = {"Memberships", "Groups"};
    int Numboftabs = 2;
    private static final String TAG_MID = "codice";
    private static final String TAG_REGID = "regid";
    SharedPreferences memberId;
    //per recuperare l'id di registrazione al gcm
    SharedPreferences registerID;
    //notifiche con intent
    private static final String BROADCAST = "com.google.android.c2dm.intent.RECEIVE";

    // JSON Node names
    private static final String TAG_SUCCESS = "success";
    JSONParser jsonParser = new JSONParser();
    NotificationReceiver broadCastReceiver = new NotificationReceiver();

    // url to remove GCM registration id
    private static String url_remove_register = "http://androidchatapp.altervista.org/chatApp_connect/unregister_GCM.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home);

        // Creating The Toolbar and setting it as the Toolbar for the activity

        toolbar = (Toolbar) findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);
        //controllo connessione
        if (isNetworkAvailable()) {

            // Creating The ViewPagerAdapter and Passing Fragment Manager, Titles fot the Tabs and Number Of Tabs.
            adapter = new ViewPagerAdapter(getSupportFragmentManager(), Titles, Numboftabs);

            // Assigning ViewPager View and setting the adapter
            pager = (ViewPager) findViewById(R.id.pager);
            pager.setAdapter(adapter);

            // Assiging the Sliding Tab Layout View
            tabs = (SlidingTabLayout) findViewById(R.id.tabs);
            tabs.setDistributeEvenly(true); // To make the Tabs Fixed set this true, This makes the tabs Space Evenly in Available width

            // Setting Custom Color for the Scroll bar indicator of the Tab View
            tabs.setCustomTabColorizer(new SlidingTabLayout.TabColorizer() {
                @Override
                public int getIndicatorColor(int position) {
                    return getResources().getColor(R.color.tabsScrollColor);
                }
            });

            // Setting the ViewPager For the SlidingTabsLayout
            tabs.setViewPager(pager);
        } else {
            Toast.makeText(getApplicationContext(),
                    "Please, enable your internet connection", Toast.LENGTH_LONG).show();
        }


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        memberId = getSharedPreferences(TAG_MID, MODE_PRIVATE);
        registerID = getSharedPreferences(TAG_REGID, MODE_PRIVATE);
        switch (id) {
            case R.id.MENU: {

                AlertDialogWrapper.Builder dialog = new AlertDialogWrapper.Builder(this);

                dialog.setTitle(R.string.title1);
                dialog.setMessage(R.string.message);
                dialog.setPositiveButton(R.string.OK, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        dialog.dismiss();
                         //non riceve più notifiche push all'arrivo dei messaggi
                        unregisterReceiver(broadCastReceiver);
                        //rimuovo i dati di login e registrazione
                        memberId.edit().remove(TAG_MID).commit();
                        registerID.edit().remove(TAG_REGID).commit();
                        Intent login = new Intent(Home.this, MainActivity.class);
                        startActivity(login);
                        finish();


                    }
                });
                dialog.setNegativeButton(R.string.Cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        dialog.dismiss();


                    }
                }).show();

            }

            break;

        }
        return false;
    }

    /*
  Rimuove il GCM register id, in mdo tale da non ricevere più le notifiche
   */
   /* class Unregister extends AsyncTask<String, String, String> {

        protected String doInBackground(String... args) {

            String regid = registerID.getString(TAG_REGID, null);

            // Building Parameters
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("regid", regid));
            // getting JSON Object
            // it accepts POST method
            JSONObject json = jsonParser.makeHttpRequest(url_remove_register, "POST", params);

            // check log cat fro response
            Log.d("Unregister Response", json.toString());
            // check for success tag

            try {
                int success = json.getInt(TAG_SUCCESS);

                if (success == 1) {
                    registerID.edit().remove(TAG_REGID).commit();

                } else {
                    //null
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;

        }

    }*/

    /*
    Connection state control
     */
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null;
    }
    @Override
    protected  void onStart(){
        super.onStart();
        IntentFilter filter = new IntentFilter(BROADCAST);
        registerReceiver(broadCastReceiver, filter);
    }

}