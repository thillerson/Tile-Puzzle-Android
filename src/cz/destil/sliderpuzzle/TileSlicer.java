package cz.destil.sliderpuzzle;

import java.util.ArrayList;
import java.util.Random;

import android.graphics.Bitmap;

/**
 * 
 * Slices original bitmap into tiles and adds border. Provides randomized access
 * to tiles.
 * 
 * Based on
 * https://github.com/thillerson/Android-Slider-Puzzle/blob/master/src/com
 * /tackmobile/TileServer.java
 * 
 * @author David Vavra
 */
public class TileSlicer {

	private static final String TAG = "TileSlicer";
	private Bitmap original;
	private int tileSize;
	private ArrayList<Bitmap> slices;
	private Random random;

	/**
	 * Initializes TileSlicer.
	 * 
	 * @param original
	 *            Bitmap which should be sliced
	 * @param gridSize
	 *            Grid size, for example 4 for 4x4 grid
	 */
	public TileSlicer(Bitmap original, int gridSize) {
		super();
		this.original = original;
		this.tileSize = original.getWidth() / gridSize;
		random = new Random();
		slices = new ArrayList<Bitmap>();
		sliceOriginal();
	}

	/**
	 * Slices original bitmap and adds border to slices.
	 */
	private void sliceOriginal() {
		int x, y;
		Bitmap bitmap;
		for (int rowI = 0; rowI < 4; rowI++) {
			for (int colI = 0; colI < 4; colI++) {
				x = rowI * tileSize;
				y = colI * tileSize;
				bitmap = Bitmap.createBitmap(original, x, y, tileSize, tileSize);
				slices.add(bitmap);
			}
		}
		// remove original bitmap from memory
		original = null;
	}

	/**
	 * Serves random slice and frees it from memory
	 * 
	 * @return Bitmap of random slice
	 */
	public Bitmap getRandomSlice() {
		if (slices.size() > 0) {
			int randomIndex = random.nextInt(slices.size());
			Bitmap drawable = slices.remove(randomIndex);
			return drawable;
		}
		return null;
	}

}
