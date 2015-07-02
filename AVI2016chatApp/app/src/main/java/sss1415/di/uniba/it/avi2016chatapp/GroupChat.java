package sss1415.di.uniba.it.avi2016chatapp;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Switch;
import android.widget.Toast;

import com.gc.materialdesign.views.Button;
import com.gc.materialdesign.views.CheckBox;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class GroupChat extends ListActivity {

    // url to create new group
    private static String url_send_message_group = "http://androidchatapp.altervista.org/chatApp_connect/send_message_group.php";
    private static String url_read_message_group = "http://androidchatapp.altervista.org/chatApp_connect/read_message_group.php";
    private static String url_subscrition = "http://androidchatapp.altervista.org/chatApp_connect/send_subscrition.php";
    private static String url_isSubscribe = "http://androidchatapp.altervista.org/chatApp_connect/isSubscribe.php";
    private static String url_remove_subscribe = "http://androidchatapp.altervista.org/chatApp_connect/remove_subscribe.php";

    ArrayList<HashMap<String, String>> groupMessageList;

    // JSON Node names
    private static final String TAG_SUCCESS = "success";
    JSONParser jsonParser = new JSONParser();

    private Button btnSend;
    private EditText inputMsg;
    private com.gc.materialdesign.views.Switch switch1;
    private Switch switch2;
    ListView listv;
    //per l'invio del messaggio
    SharedPreferences memberId;
    private static final String TAG_MID = "codice";
    private static final String TAG_GID = "codiceGruppo";
    //per la lettura e visualizzazione
    private static final String TAG_MESSAGE1 = "testoMessaggio";
    private static final String TAG_MESSAGE ="message";
    private static final String TAG_MITTENTE ="codiceMittente";
    private static final String TAG_GRUPPO ="idGruppo";
    private static final String TAG_NOME ="nome";
    JSONArray groupMessages = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);
        btnSend = (Button) findViewById(R.id.btnSend);
        inputMsg = (EditText) findViewById(R.id.inputMsg);
        //switch1 = (com.gc.materialdesign.views.Switch)findViewById(R.id.switch1);
        switch2 = (Switch)findViewById(R.id.switch1);


        memberId =getSharedPreferences(TAG_MID, MODE_PRIVATE);
        //deve ricevere anche l'id del destinatario(se conversazione) o id del gruppo(se è gruppo)
        //per conversazione
        groupMessageList = new ArrayList<HashMap<String, String>>();
        new isSubscribe().execute();

        switch2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (switch2.isChecked()) {
                    btnSend.setVisibility(View.VISIBLE);
                    inputMsg.setVisibility(View.VISIBLE);
                    new LoadMessages().execute();
                    new Subscribe().execute();

                } else if (!switch2.isChecked()) {
                    btnSend.setVisibility(View.INVISIBLE);
                    inputMsg.setVisibility(View.INVISIBLE);
                    new RemoveSubscribe().execute();
                }
            }
        });

        btnSend.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                if(inputMsg.getText().toString().length()>0){
                    // Hashmap for ListView


                    new SendMessage().execute();
                    groupMessageList = new ArrayList<HashMap<String, String>>();
                    new LoadMessages().execute();
                    // Clearing the input filed once message was sent
                    inputMsg.setText("");
                }

            }
        });
    }

    /**
     * Background Async Task to Create new product
     * */
    class SendMessage extends AsyncTask<String, String, String> {

        String idGruppo = getIntent().getExtras().getString(TAG_GID);

        /**
         * Creating group
         */
        protected String doInBackground(String... args) {
            Map<String, ?> entry_codice = memberId.getAll();
            final String[] codice = new String[entry_codice.size()];
            int i = 0;

            for (Map.Entry<String, ?> entryeach : entry_codice.entrySet()) {
                codice[i] = (String) entryeach.getValue();
                i++;
            }
            String messageText = inputMsg.getText().toString();
            String idMittente = codice[0];

            // Building Parameters
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("idMittente", idMittente));
            params.add(new BasicNameValuePair("messageText", messageText));
            params.add(new BasicNameValuePair("idGruppo", idGruppo));

            // getting JSON Object
            // Note that create product url accepts POST method
            JSONObject json = jsonParser.makeHttpRequest(url_send_message_group,
                    "POST", params);

            // check log cat fro response
            Log.d("Create Response", json.toString());

            // check for success tag
            try {
                int success = json.getInt(TAG_SUCCESS);

                if (success == 1) {
                    //null
                } else {
                    // failed to create product
                    runOnUiThread(new Runnable() {
                        public void run() {

                            Toast.makeText(GroupChat.this, "Message not send!", Toast.LENGTH_SHORT).show();
                        }
                    });
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;
        }
    }

    class LoadMessages extends AsyncTask<String, String, String> {

        String idGruppo = getIntent().getExtras().getString(TAG_GID);
        /**
         * Creating group
         * */
        protected String doInBackground(String... args) {

            // Building Parameters
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("idGruppo", idGruppo));
            //lettura dei messaggi
            JSONObject json = jsonParser.makeHttpRequest(url_read_message_group, "POST", params);

            // check log cat fro response
            Log.d("Message detail: ", json.toString());
            // check for success tag
            try {
                // Checking for SUCCESS TAG
                int success1 = json.getInt(TAG_SUCCESS);

                if (success1 == 1) {
                    // products found
                    // Getting Array of Products
                    groupMessages = json.getJSONArray(TAG_MESSAGE);

                    // looping through All Products
                    for (int j = 0; j < groupMessages.length(); j++) {
                        JSONObject c = groupMessages.getJSONObject(j);

                        // Storing each json item in variable
                        String mittente = c.getString(TAG_MITTENTE);
                        String messaggio = c.getString(TAG_MESSAGE1);
                        String gruppo = c.getString(TAG_GRUPPO);
                        String nome = c.getString(TAG_NOME);

                        // creating new HashMap
                        HashMap<String, String> map = new HashMap<String, String>();

                        // adding each child node to HashMap key => value
                        map.put(TAG_MITTENTE, mittente);
                        map.put(TAG_MESSAGE1, messaggio);
                        map.put(TAG_GRUPPO, gruppo);
                        map.put(TAG_NOME, nome);

                        groupMessageList.add(map);
                    }
                }else {
                    // failed to create product
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;
        }

        /**
         * After completing background task Dismiss the progress dialog
         * **/
        protected void onPostExecute(String file_url) {
            // updating UI from Background Thread
            runOnUiThread(new Runnable() {
                public void run() {
                    // String mittenteMessaggio = messageList.get(idMittente);
                        ListAdapter adapter = new SimpleAdapter(
                                GroupChat.this, groupMessageList,
                                R.layout.list_item_message_right, new String[]{TAG_NOME,
                                TAG_MESSAGE1},
                                new int[]{R.id.lblMsgFrom, R.id.txtMsg});
                        // updating listview
                        setListAdapter(adapter);
                }
            });

        }
    }

    class Subscribe extends AsyncTask<String, String, String> {

        /**
         * Creating group
         */
        protected String doInBackground(String... args) {
            Map<String, ?> entry_codice = memberId.getAll();
            final String[] codice = new String[entry_codice.size()];
            int i = 0;

            for(Map.Entry<String, ?> entryeach : entry_codice.entrySet()) {
                codice[i] = (String) entryeach.getValue();
                i++;
            }
            String idMittente = codice[0];
            String idGruppo = getIntent().getExtras().getString(TAG_GID);

            // Building Parameters
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("idGruppo", idGruppo));
            params.add(new BasicNameValuePair("idPartecipante", idMittente));
            // getting JSON Object
            // Note that create product url accepts POST method
            JSONObject json = jsonParser.makeHttpRequest(url_subscrition,
                    "POST", params);

            // check log cat fro response
            Log.d("Subscribe Response", json.toString());

            // check for success tag
            try {
                int success = json.getInt(TAG_SUCCESS);

                if (success == 1) {

                } else {
                    // failed to create product
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;
        }
    }

    class isSubscribe extends AsyncTask<String, String, String> {
        /**
         * getting All products from url
         * */
        protected String doInBackground(String... args) {
            Map<String, ?> entry_codice = memberId.getAll();
            final String[] codice = new String[entry_codice.size()];
            int i = 0;

            for(Map.Entry<String, ?> entryeach : entry_codice.entrySet()) {
                codice[i] = (String) entryeach.getValue();
                i++;
            }
            String idMittente = codice[0];
            String idGruppo = getIntent().getExtras().getString(TAG_GID);
            // Building Parameters
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("idGruppo", idGruppo));
            params.add(new BasicNameValuePair("idPartecipante", idMittente));
            // getting JSON Object
            // Note that create product url accepts POST method
            JSONObject json = jsonParser.makeHttpRequest(url_isSubscribe, "POST", params);

            // check log cat fro response
            Log.d("IsSubscribe Response", json.toString());
            // check for success tag

            try {
                int success = json.getInt(TAG_SUCCESS);

                if (success == 1) {
                    runOnUiThread(new Runnable() {
                        public void run() {
                        switch2.setChecked(true);
                        btnSend.setVisibility(View.VISIBLE);
                        inputMsg.setVisibility(View.VISIBLE);
                        new LoadMessages().execute();
                        }
                    });
                }else {
                    runOnUiThread(new Runnable() {
                        public void run() {
                            switch2.setChecked(false);
                            btnSend.setVisibility(View.INVISIBLE);
                            inputMsg.setVisibility(View.INVISIBLE);
                        }
                    });
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;

        }

    }

    class RemoveSubscribe extends AsyncTask<String, String, String> {
        /**
         * getting All products from url
         * */
        protected String doInBackground(String... args) {
            Map<String, ?> entry_codice = memberId.getAll();
            final String[] codice = new String[entry_codice.size()];
            int i = 0;

            for(Map.Entry<String, ?> entryeach : entry_codice.entrySet()) {
                codice[i] = (String) entryeach.getValue();
                i++;
            }
            String idMittente = codice[0];
            String idGruppo = getIntent().getExtras().getString(TAG_GID);
            // Building Parameters
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("idGruppo", idGruppo));
            params.add(new BasicNameValuePair("idPartecipante", idMittente));
            // getting JSON Object
            // Note that create product url accepts POST method
            JSONObject json = jsonParser.makeHttpRequest(url_remove_subscribe, "POST", params);

            // check log cat fro response
            Log.d("RemoveSub Response", json.toString());
            // check for success tag

            try {
                int success = json.getInt(TAG_SUCCESS);

                if (success == 1) {
                    //

                }else {
                    //
                }
                finish();

            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;

        }

    }

    @Override
    protected void onPause() {

        super.onPause();
        finish();
    }

}
