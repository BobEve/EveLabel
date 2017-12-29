package bob.eve.label;

import android.graphics.Bitmap;
import java.io.FileNotFoundException;

/**
 * Created by Bob on 17/12/29.
 */

public interface ILabelView {
	// 设置Label背景图
	void setBackgroundBitmap(Bitmap bitmap);

	// 设置Label
	void addLabel(Bitmap bitmap);

	// 获取和成后的bitmap将其存储与filePath
	void createResultBitmapWithFile(String filePath) throws FileNotFoundException;

	// 获取和成后的bitmap
	Bitmap createResultBitmap();
}
