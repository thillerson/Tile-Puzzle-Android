package cz.destil.sliderpuzzle.ui;

import cz.destil.sliderpuzzle.R;
import android.app.Activity;
import android.os.Bundle;

/**
 * 
 * Main activity where the game is played.
 * 
 * @author David Vavra
 * 
 */
public class SliderPuzzleActivity extends Activity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	}

	// TODO: preserve state when rotated
}