package cz.destil.sliderpuzzle.ui;

import java.util.LinkedList;

import android.content.Intent;
import android.os.Bundle;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

import cz.destil.sliderpuzzle.R;

/**
 * 
 * Main activity where the game is played.
 * 
 * @author David Vavra
 * 
 */
public class MainActivity extends SherlockActivity {

	private GameBoardView gameBoard;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		gameBoard = (GameBoardView) findViewById(R.id.gameboard);
		// use preserved tile locations when orientation changed
		@SuppressWarnings({ "deprecation", "unchecked" })
		final LinkedList<Integer> tileOrder = (LinkedList<Integer>) getLastNonConfigurationInstance();
		if (tileOrder != null) {
			gameBoard.setTileOrder(tileOrder);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getSupportMenuInflater();
		inflater.inflate(R.menu.main_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.new_game:
			gameBoard.setTileOrder(null);
			gameBoard.fillTiles();
			return true;
		case R.id.about:
			startActivity(new Intent(this, AboutActivity.class));
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public Object onRetainNonConfigurationInstance() {
		// preserve state when rotated
		return gameBoard.getTileOrder();
	}
}