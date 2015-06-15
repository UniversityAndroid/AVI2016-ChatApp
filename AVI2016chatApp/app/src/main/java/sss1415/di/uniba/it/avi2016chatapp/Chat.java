package sss1415.di.uniba.it.avi2016chatapp;




import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import com.gc.materialdesign.views.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;



public class Chat extends ActionBarActivity {


    private Button btnSend;
    private EditText inputMsg;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        btnSend = (Button) findViewById(R.id.btnSend);
        inputMsg = (EditText) findViewById(R.id.inputMsg);

        btnSend.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // Sending message to web socket server
                //sendMessageToServer(utils.getSendMessageJSON(inputMsg.getText()
                //     .toString()));

                // Clearing the input filed once message was sent
                //inputMsg.setText("");
            }
        });

    }
}
