package bob.eve.label;

import android.graphics.Bitmap;
import android.graphics.Matrix;

/**
 * Created by Bob on 17/12/29.
 */

public class BitmapElement {
	private Bitmap bitmap;
	private Matrix matrix = new Matrix();

	public Bitmap getBitmap() {
		return bitmap;
	}

	public void setBitmap(Bitmap bitmap) {
		this.bitmap = bitmap;
	}

	public Matrix getMatrix() {
		if (this.matrix == null) {
			this.matrix = new Matrix();
		}
		return matrix;
	}

	public void setMatrix(Matrix matrix) {
		this.matrix = matrix;
	}

	public void release() {
		if (bitmap != null) {
			bitmap.recycle();
			bitmap = null;
		}

		if (matrix != null) {
			matrix.reset();
			matrix = null;
		}
	}
}
