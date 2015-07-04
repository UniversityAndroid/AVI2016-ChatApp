package sss1415.di.uniba.it.avi2016chatapp;

/**
 * Created by katia on 27/05/2015.
 */

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.gc.materialdesign.views.Button;

import org.apache.http.NameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by hp1 on 21-01-2015.
 * Questa classe permette la visualizzazione  dei gruppi tematici.
 * L'utente una volta loggato, può inviare messaggi ad altri partecipanti iscritti ad un gruppo
 * tematico, semplicemente cliccando sul nome del gruppo.
 */
public class Tab2 extends ListFragment {
    private Button addGroup;

    // Creating JSON Parser object
    JSONParser jParser = new JSONParser();

    ArrayList<HashMap<String, String>> groupsList;

    // url to get all groups list
    private static String url_all_groups = "http://androidchatapp.altervista.org/chatApp_connect/get_all_groups.php";

    // JSON Node names
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_GROUPS = "groups";
    private static final String TAG_GID = "codiceGruppo";
    private static final String TAG_NAME = "nome";

    // products JSONArray
    JSONArray groups = null;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.tab2, container, false);

        // Hashmap for ListView
        groupsList = new ArrayList<HashMap<String, String>>();

        // Loading all groups in Background Thread
        new LoadAllGroups().execute();

        addGroup = (Button) v.findViewById(R.id.buttonFloat);
        //button that allow to create new group
        addGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent openNewGroup = new Intent(getActivity(), NewGroup.class);
                startActivity(openNewGroup);

            }
        });

        return v;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        // Get listview
        ListView lv = getListView();

        // on seleting single group
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                // getting values from selected ListItem
                String gId = ((TextView) view.findViewById(R.id.mid)).getText()
                        .toString();

                // Starting new intent
                Intent in = new Intent(view.getContext(), GroupChat.class);
                // sending pid to next activity
                in.putExtra(TAG_GID, gId);

                // starting new activity
                startActivity(in);
            }
        });

    }

    /**
     * Background Async Task to Load all groups by making HTTP Request
     */
    class LoadAllGroups extends AsyncTask<String, String, String> {

        /**
         * Before starting background thread Show Progress Dialog
         *
         @Override protected void onPreExecute() {
         super.onPreExecute();
         pDialog = new ProgressDialog(getActivity());
         pDialog.setMessage("Loading groups. Please wait...");
         pDialog.setIndeterminate(false);
         pDialog.setCancelable(false);
         pDialog.show();
         }*/

        /**
         * getting All groups from url
         */
        protected String doInBackground(String... args) {
            // Building Parameters
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            // getting JSON string from URL
            JSONObject json = jParser.makeHttpRequest(url_all_groups, "GET", params);

            // Check your log cat for JSON reponse
            Log.d("All Groups: ", json.toString());

            try {
                // Checking for SUCCESS TAG
                int success = json.getInt(TAG_SUCCESS);

                if (success == 1) {
                    // groups found
                    // Getting Array of groups
                    groups = json.getJSONArray(TAG_GROUPS);

                    // looping through All groups
                    for (int i = 0; i < groups.length(); i++) {
                        JSONObject c = groups.getJSONObject(i);

                        // Storing each json item in variable
                        String id = c.getString(TAG_GID);
                        String name = c.getString(TAG_NAME);

                        // creating new HashMap
                        HashMap<String, String> map = new HashMap<String, String>();

                        // adding each child node to HashMap key => value
                        map.put(TAG_GID, id);
                        map.put(TAG_NAME, name);

                        // adding HashList to ArrayList
                        groupsList.add(map);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;
        }

        /**
         * After completing background task Dismiss the progress dialog
         * *
         */
        protected void onPostExecute(String file_url) {
            // dismiss the dialog after getting all products
            // pDialog.dismiss();

            // updating UI from Background Thread
            getActivity().runOnUiThread(new Runnable() {
                public void run() {
                    /**
                     * Updating parsed JSON data into ListView
                     * */
                    ListAdapter adapter = new SimpleAdapter(
                            getActivity(), groupsList,
                            R.layout.list_item, new String[]{TAG_GID,
                            TAG_NAME, ""},
                            new int[]{R.id.mid, R.id.name, R.id.surname});
                    // updating listview
                    setListAdapter(adapter);
                }
            });

        }

    }
}