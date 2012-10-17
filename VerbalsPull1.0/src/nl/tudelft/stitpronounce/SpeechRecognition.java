/**
 * VerbalsPull1.0
 * 
 * SpeechRecognition.java 11 okt. 2012
 */
package nl.tudelft.stitpronounce;


import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.speech.RecognizerIntent;

import java.util.ArrayList;
import java.util.List;


/**
 * This activity handles speech recognition
 * 
 * @author a.singh
 * @version 11 okt. 2012
 * 
 */

public class SpeechRecognition extends Activity {

    private static final int REQUEST_CODE = 123;

    /**
     * Handle the results from the voice recognition activity.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
            // Populate the wordsList with the String values the recognition
            // engine thought it heard
            ArrayList<String> matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            // Push the speech results to the calling activity
            Intent intent = new Intent();
            Bundle extras = new Bundle();
            extras.putStringArrayList("speechResultKey", matches);
            intent.putExtras(extras);
            setResult(RESULT_OK, intent);
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.speech);

        PackageManager pm = getPackageManager();
        List<ResolveInfo> activities = pm.queryIntentActivities(new Intent(
                                                                           RecognizerIntent.ACTION_RECOGNIZE_SPEECH),
                                                                0);
        startVoiceRecognitionActivity();
    }

    /**
     * Fire an intent to start the voice recognition activity.
     */
    private void startVoiceRecognitionActivity() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                        RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Voice recognition Demo...");
        startActivityForResult(intent, REQUEST_CODE);
    }

}
