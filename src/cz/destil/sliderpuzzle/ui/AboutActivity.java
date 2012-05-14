package cz.destil.sliderpuzzle.ui;

import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockActivity;

import cz.destil.sliderpuzzle.R;

/**
 * 
 * Screen containing information about the app.
 * 
 * @author David Vavra
 * 
 */
public class AboutActivity extends SherlockActivity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_about);
		makeLinksClickable(new int[] { R.id.created_by, R.id.project1, R.id.project2, R.id.settle_up });
	}

	/**
	 * Makes links in the TextViews clickable. Links must be defined as HTML
	 * text.
	 * 
	 * @param resourceIds
	 *            Resource IDs of TextViews with links.
	 */
	private void makeLinksClickable(int[] resourceIds) {
		for (int resourceId : resourceIds) {
			if (findViewById(resourceId) instanceof TextView) {
				TextView textView = (TextView) findViewById(resourceId);
				if (textView != null) {
					textView.setText(Html.fromHtml(textView.getText().toString()));
					textView.setMovementMethod(LinkMovementMethod.getInstance());
				}
			}
		}
	}
}