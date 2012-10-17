/**
 * VerbalsPull1.0
 * 
 * LocationTag.java 11 okt. 2012
 */

package nl.tudelft.stitpronounce;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.speech.tts.TextToSpeech.OnUtteranceCompletedListener;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Locale;


/**
 * This activity handles dialog delivery using Text-to-Speech engine
 * 
 * @author a.singh
 * @version 11 okt. 2012
 * 
 */

public class Text2Speech extends Activity implements OnInitListener {

    private static final String END_OF_SPEECH = "END";
    private static final int MY_DATA_CHECK_CODE = 0;
    private String text = "Dialog not recieved";
    private TextToSpeech tts;

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            speakText();
        } else if (status == TextToSpeech.ERROR) {
            Toast.makeText(Text2Speech.this,
                           "Error occurred while initializing Text-To-Speech engine",
                           Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == MY_DATA_CHECK_CODE) {
            if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
                tts = new TextToSpeech(this, this); // success, create the TTS
                                                    // instance
            } else {
                // missing data, install it
                Intent installIntent = new Intent();
                installIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                startActivity(installIntent);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.text2speech);

        // To receive dialog text from the calling activity's intent
        Bundle bundle = this.getIntent().getExtras();
        if (bundle != null) {
            text = bundle.getString("key"); // assign dialog text
            Intent checkIntent = new Intent();
            checkIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
            startActivityForResult(checkIntent, MY_DATA_CHECK_CODE);
        }
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        tts.shutdown();
    }

    /** Open phone's gallery when user clicks the button 'Select a video' */
    private void speakText() {
        if (text != null && text.length() > 0) {
            HashMap<String, String> myHash = new HashMap<String, String>();
            myHash.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, END_OF_SPEECH);
            tts.setOnUtteranceCompletedListener(new OnUtteranceCompletedListener() {
                @Override
                public void onUtteranceCompleted(String utteranceId) {
                    if (0 == utteranceId.compareToIgnoreCase(END_OF_SPEECH)) {
                        Intent intent = new Intent();
                        setResult(RESULT_OK, intent);
                        finish();
                    }
                }
            });
            tts.setLanguage(Locale.UK);
            tts.setSpeechRate((float) .9);
            tts.setPitch(1);
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, myHash);
        }
    }
}
