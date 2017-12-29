package bob.eve.label;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Bob on 17/12/29.
 */

public class EveLabelView extends View implements ILabelView {
	public static final int MODE_NONE = 0;
	public static final int MODE_DRAG = 1;
	public static final int MODE_ZOOM = 2;
	public static final int LIMIT = 40;

	private int mode = MODE_NONE;

	private int moveIndex = 0;
	private int transformIndex = 0;
	private int deleteIndex = 0;

	private float cacheX = 0;
	private float cacheY = 0;
	private float cacheDestance;
	private float cacheRotation;

	private PointF midPointF = new PointF();
	private Matrix moveMatrix = new Matrix();
	private Matrix downMatrix = new Matrix();

	private RectF targetRectF = new RectF();

	private BitmapElement bitmapElementForBackground;
	private Paint paintForBitmap;
	private Paint paintForLabelFrame;

	private Bitmap deleteIcon;

	private List<BitmapElement> labels = new ArrayList<>();

	public EveLabelView(Context context) {
		this(context, null);
	}

	public EveLabelView(Context context, @Nullable AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public EveLabelView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init();
	}

	private void init() {
		bitmapElementForBackground = new BitmapElement();
		paintForBitmap = new Paint(Paint.ANTI_ALIAS_FLAG);
		paintForBitmap.setFilterBitmap(true);

		paintForLabelFrame = new Paint(Paint.ANTI_ALIAS_FLAG);
		paintForBitmap.setStyle(Paint.Style.STROKE);

		deleteIcon = BitmapFactory.decodeResource(getResources(), R.drawable.ic_delete);
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		super.onLayout(changed, left, top, right, bottom);
		if (changed) {
			targetRectF.set(left, top, right, bottom);
		}
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		if (bitmapElementForBackground != null && bitmapElementForBackground.getBitmap() != null) {
			canvas.drawBitmap(bitmapElementForBackground.getBitmap(),
												bitmapElementForBackground.getMatrix(), paintForBitmap);
		}

		for (BitmapElement label : labels) {
			final float[] points = mapPoints(label);
			canvas.drawLine(points[0], points[1], points[2], points[3], paintForLabelFrame);
			canvas.drawLine(points[2], points[3], points[6], points[7], paintForLabelFrame);
			canvas.drawLine(points[6], points[7], points[4], points[5], paintForLabelFrame);
			canvas.drawLine(points[4], points[5], points[0], points[1], paintForLabelFrame);

			canvas.drawCircle(points[2], points[3], deleteIcon.getWidth() / 2, paintForLabelFrame);
			canvas.drawBitmap(deleteIcon, points[2] - deleteIcon.getWidth() / 2,
												points[3] - deleteIcon.getHeight() / 2, paintForLabelFrame);

			canvas.drawBitmap(label.getBitmap(), label.getMatrix(), paintForBitmap);
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		switch (event.getActionMasked()) {
			case MotionEvent.ACTION_DOWN:
				cacheX = event.getX();
				cacheY = event.getY();
				// 判断是否在某一个label
				moveIndex = checkLabel(cacheX, cacheY);
				// 判断是否在某一个删除按钮
				deleteIndex = checkDelete(cacheX, cacheY);

				if (moveIndex != -1 && deleteIndex == -1) {
					// 设置模式移动
					mode = MODE_DRAG;
					// 设置触摸label matrix到downMatrix
					downMatrix.set(labels.get(moveIndex)
															 .getMatrix());
				}

				break;
			case MotionEvent.ACTION_POINTER_DOWN:
				moveIndex = checkLabel(event.getX(0), event.getY(0));
				transformIndex = checkLabel(event.getX(1), event.getY(1));
				if (transformIndex != -1 && moveIndex != -1 && deleteIndex == -1) {
					// 设置模式缩放
					mode = MODE_ZOOM;
				}
				// 缓存两个手指间距离
				cacheDestance = getDistance(event);
				// 缓存当前Label选择角度
				cacheRotation = getRotation(event);
				midPointF = midPoint(event);

				break;
			case MotionEvent.ACTION_MOVE:
				if (mode == MODE_DRAG) {
					// 如果单指移动
					moveMatrix.set(downMatrix);
					float dx = event.getX() - cacheX;
					float dy = event.getY() - cacheY;

					moveMatrix.postTranslate(dx, dy);
					if (moveIndex != -1) {
						// 设置当前移动Label的matrix
						labels.get(moveIndex)
									.setMatrix(moveMatrix);
					}

					invalidate();
				} else if (mode == MODE_ZOOM) {
					moveMatrix.set(downMatrix);
					float rotate = getRotation(event) - cacheRotation;
					float scale = getDistance(event) / cacheDestance;

					moveMatrix.postRotate(rotate, midPointF.x, midPointF.y);
					moveMatrix.postScale(scale, scale, midPointF.x, midPointF.y);
					if (moveIndex != -1) {
						labels.get(moveIndex)
									.setMatrix(moveMatrix);
					}
					invalidate();
				}
				break;
			case MotionEvent.ACTION_UP:
				if (deleteIndex != -1) {
					labels.remove(deleteIndex)
								.release();
					invalidate();
				}
				mode = MODE_NONE;

				break;
			case MotionEvent.ACTION_POINTER_UP:
				mode = MODE_NONE;
				break;
		}

		return true;
	}

	/**
	 * 判断是否在矩形区域内
	 */
	private boolean isInRect(BitmapElement element, float x, float y) {
		final float[] points = mapPoints(element);
		float x1 = points[0];
		float y1 = points[1];
		float x2 = points[2];
		float y2 = points[3];
		float x3 = points[4];
		float y3 = points[5];
		float x4 = points[6];
		float y4 = points[7];

		float edge = (float) Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2));
		return (2 + Math.sqrt(2)) * edge >= Math.sqrt(Math.pow(x - x1, 2) + Math.pow(y - y1, 2)) +
																				Math.sqrt(Math.pow(x - x2, 2) + Math.pow(y - y2, 2)) +
																				Math.sqrt(Math.pow(x - x3, 2) + Math.pow(y - y3, 2)) +
																				Math.sqrt(Math.pow(x - x4, 2) + Math.pow(y - y4, 2));
	}

	/**
	 * 判断是否在园内
	 */
	private boolean isInCircle(BitmapElement element, float x, float y) {
		final float[] points = mapPoints(element);
		return (int) Math.sqrt(Math.pow(x - points[2], 2) + Math.pow(y - points[3], 2)) < LIMIT;
	}

	@Override
	public void setBackgroundBitmap(Bitmap bitmap) {
		bitmapElementForBackground.setBitmap(bitmap);
		if (bitmapElementForBackground.getMatrix() == null) {
			bitmapElementForBackground.setMatrix(new Matrix());
		}

		invalidate();
	}

	@Override
	public void addLabel(Bitmap bitmap) {
		BitmapElement label = new BitmapElement();
		label.setBitmap(bitmap);
		if (label.getMatrix() == null) {
			label.setMatrix(new Matrix());
		}
		float transX = (getWidth() - label.getBitmap()
																			.getWidth()) / 2;
		float transY = (getHeight() - label.getBitmap()
																			 .getHeight()) / 2;
		label.getMatrix()
				 .postTranslate(transX, transY);

		labels.add(label);
		invalidate();
	}

	@Override
	public void createResultBitmapWithFile(String filePath) throws FileNotFoundException {
		Bitmap resultBitmap = createResultBitmap();
		File f = new File(filePath);
		resultBitmap.compress(Bitmap.CompressFormat.JPEG, 85, new FileOutputStream(f));
		resultBitmap.recycle();
	}

	@Override
	public Bitmap createResultBitmap() {
		Bitmap bitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888); // 背景图片
		Canvas canvas = new Canvas(bitmap); // 新建画布
		canvas.drawColor(Color.WHITE);

		if (bitmapElementForBackground.getBitmap() != null &&
				bitmapElementForBackground.getMatrix() != null) {
			canvas.drawBitmap(bitmapElementForBackground.getBitmap(),
												bitmapElementForBackground.getMatrix(), paintForBitmap);
		}

		for (BitmapElement element : labels) {
			canvas.drawBitmap(element.getBitmap(), element.getMatrix(), paintForBitmap);
		}

		canvas.save(Canvas.ALL_SAVE_FLAG); // 保存画布
		canvas.restore();
		return bitmap;
	}

	private int checkDelete(float x, float y) {
		for (int i = 0; i < labels.size(); i++) {
			if (isInCircle(labels.get(i), x, y)) {
				return i;
			}
		}
		return -1;
	}

	private int checkLabel(float x, float y) {
		for (int i = 0; i < labels.size(); i++) {
			if (isInRect(labels.get(i), x, y)) {
				return i;
			}
		}
		return -1;
	}

	private float[] mapPoints(BitmapElement element) {
		return mapPoints(element.getBitmap(), element.getMatrix());
	}

	private float[] mapPoints(Bitmap bitmap, Matrix matrix) {
		if (bitmap == null) {
			return null;
		}

		final float[] dst = new float[8];
		final float[] src = new float[] {
				0, 0, // left-top
				bitmap.getWidth(), 0, // right_top
				0, bitmap.getHeight(), // left-bottom
				bitmap.getWidth(), bitmap.getHeight() // right-bottom
		};

		matrix.mapPoints(dst, src);
		return dst;
	}

	// 触碰两点间距
	public float getDistance(MotionEvent event) {
		float x = event.getX(0) - event.getX(1);
		float y = event.getY(0) - event.getY(1);
		return (float) Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2));
	}

	// 取手势中心点
	public PointF midPoint(MotionEvent event) {
		PointF point = new PointF();
		float x = event.getX(0) + event.getX(1);
		float y = event.getY(0) + event.getY(1);
		point.set(x / 2, y / 2);

		return point;
	}

	// 取旋转角
	public float getRotation(MotionEvent event) {
		double x = event.getX(0) - event.getX(1);
		double y = event.getY(0) - event.getY(1);
		double radians = Math.atan2(y, x);
		return (float) Math.toDegrees(radians);
	}
}
