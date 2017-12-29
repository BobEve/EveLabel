package bob.eve.label.sample;

import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import bob.eve.label.EveLabelView;
import java.io.FileNotFoundException;

public class MainActivity extends AppCompatActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		final EveLabelView eveLabelView = findViewById(R.id.labelView);
		Button btnImage = findViewById(R.id.btn_image);
		Button btnLabel = findViewById(R.id.btn_label);
		Button btnCreate = findViewById(R.id.btn_create);

		final BitmapFactory.Options o = new BitmapFactory.Options();
		o.outWidth = 1000;
		o.outHeight = 1000;
		btnImage.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				eveLabelView.setBackgroundBitmap(
						BitmapFactory.decodeResource(getResources(), R.drawable.label_background));
			}
		});

		btnLabel.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				eveLabelView.addLabel(BitmapFactory.decodeResource(getResources(), R.drawable.ic_label));
			}
		});

		btnCreate.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {

				eveLabelView.setBackgroundBitmap(eveLabelView.createResultBitmap());

				String file;
				try {
					file = getExternalCacheDir() + "/image.png";
					eveLabelView.createResultBitmapWithFile(file);
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}
			}
		});
	}
}
