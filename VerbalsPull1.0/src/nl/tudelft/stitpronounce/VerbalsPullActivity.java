/**
 * VerbalsPull1.0
 * 
 * LocationTag.java 11 okt. 2012
 */

package nl.tudelft.stitpronounce;


import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.widget.Toast;


/**
 * This is the first activity of the Verbals Pull Application. This activity
 * introduces the application and character
 * 
 * @author a.singh
 * @version 11 okt. 2012
 * 
 */

public class VerbalsPullActivity extends Activity {

    private static final int TTS_ACTIVITY_REQUEST_CODE = 0;
    MediaPlayer birdSound;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash);
        // To play the bird sound in the background
        birdSound = MediaPlayer.create(this, R.raw.magpiesound1);
        birdSound.start();
        String dialog = getResources().getString(R.string.dia1);
        Intent intent = new Intent(this, Text2Speech.class);
        intent.putExtra("key", dialog);
        startActivityForResult(intent, TTS_ACTIVITY_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == TTS_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK) {
            super.onActivityResult(requestCode, resultCode, data);
            Intent intent = new Intent(this, LocationTag.class);
            startActivity(intent);
        } else {
            Toast.makeText(this, "An error in using the phone's text-to-speech",
                           Toast.LENGTH_LONG).show();
        }
    }
}