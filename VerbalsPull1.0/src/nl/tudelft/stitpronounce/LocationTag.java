/**
 * VerbalsPull1.0
 * 
 * LocationTag.java 11 okt. 2012
 */
package nl.tudelft.stitpronounce;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import java.util.ArrayList;


/**
 * This activity interacts with user to get a location tag and for this calls
 * text-to-speech and speech recognition activities.
 * 
 * @author a.singh
 * @version 11 okt. 2012
 * 
 */

public class LocationTag extends Activity {

    private static final int SPEECH_ACTIVITY_REQUEST_CODE = 1;
    private static final int TTS_ACTIVITY_REQUEST_CODE = 0;

    private String dialog;
    private Button locationButton;
    private ListView wordsList;

    /**
     * Start speech recognition when a user clicks the button 'Tell Me A
     * Location'
     */
    public void startSpeech(View view) {
        Intent intent = new Intent(this, SpeechRecognition.class);
        startActivityForResult(intent, SPEECH_ACTIVITY_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        switch (requestCode) {
        case TTS_ACTIVITY_REQUEST_CODE:
            if (resultCode == RESULT_OK) {
                locationButton.setEnabled(true);
            }
            break;

        case SPEECH_ACTIVITY_REQUEST_CODE:
            if (resultCode == RESULT_OK) {
                Bundle extras = intent.getExtras();
                ArrayList<String> matchesReturn = extras.getStringArrayList("speechResultKey");
                wordsList.setAdapter(new ArrayAdapter<String>(
                                                              this,
                                                              android.R.layout.simple_list_item_1,
                                                              matchesReturn));
                startConfirmation(matchesReturn.get(0).trim());
            }
            break;

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.location);

        wordsList = (ListView) findViewById(R.id.list);
        locationButton = (Button) findViewById(R.id.locationButton);
        locationButton.setEnabled(false);

        dialog = getResources().getString(R.string.dia3);
        Intent intent = new Intent(this, Text2Speech.class);
        intent.putExtra("key", dialog);
        startActivityForResult(intent, TTS_ACTIVITY_REQUEST_CODE);
    }

    /** Start Implicit Confirmation Dialog */
    private void startConfirmation(final String result) {
        String dialogConfirm = getResources().getString(R.string.dia4).concat(result);
        Intent intent = new Intent(this, Text2Speech.class);
        intent.putExtra("key", dialogConfirm);
        startActivityForResult(intent, TTS_ACTIVITY_REQUEST_CODE);

        AlertDialog.Builder alertbox = new AlertDialog.Builder(this);
        alertbox.setMessage("Shall I fly to the past?");

        alertbox.setNeutralButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Bundle extras = new Bundle();
                extras.putString("key", result);
                Intent intent = new Intent("nl.tudelft.stitpronounce.FLICKRLOAD");
                intent.putExtras(extras);
                startActivity(intent);
            }
        });

        alertbox.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String dialogCancel = getResources().getString(R.string.dia5);
                Intent intent = new Intent("nl.tudelft.stitpronounce.TTS");
                intent.putExtra("key", dialogCancel);
                startActivityForResult(intent, TTS_ACTIVITY_REQUEST_CODE);
            }
        });
        alertbox.show();
    }
}
