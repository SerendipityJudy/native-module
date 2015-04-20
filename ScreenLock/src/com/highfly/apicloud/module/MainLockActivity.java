package com.highfly.apicloud.module;

import java.util.List;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.highfly.apicloud.module.LockPatternView.Cell;
import com.highfly.apicloud.module.LockPatternView.DisplayMode;
import com.highfly.apicloud.module.LockPatternView.OnPatternListener;
import com.uzmap.pkg.uzcore.UZResourcesIDFinder;

public class MainLockActivity extends Activity {

	public int flag = Constants.Flag_Check_Lock;

	private LockPatternView lockPatternView;

	private LockPatternUtils lockPatternUtils;
	private LinearLayout layout_root;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		int layoutId = UZResourcesIDFinder.getResLayoutID("mo_screenlock_main_activity");
		setContentView(layoutId);
		int layout_root_id = UZResourcesIDFinder.getResIdID("layout_root");
		layout_root = (LinearLayout) findViewById(layout_root_id);
		String color = "#334455";
		int alpha = 255;
		int red = 0;
		int green = 0;
		int blue = 0;

		if (getIntent().getExtras() != null) {
			flag = getIntent().getExtras().getInt("flag");
			if (getIntent().getExtras().getString("color") != null) {
				color = getIntent().getExtras().getString("color");
			}
		}
		if (color != null && color.length() > 0) {
			color = color.replace("#", "");
			if (color.length() == 3) {
				red = Integer.parseInt(color.substring(0, 1) + color.substring(0, 1), 16);
				green = Integer.parseInt(color.substring(1, 2) + color.substring(1, 2), 16);
				blue = Integer.parseInt(color.substring(2, 3) + color.substring(2, 3), 16);
			} else if (color.length() == 6) {
				red = Integer.parseInt(color.substring(0, 2), 16);
				green = Integer.parseInt(color.substring(2, 4), 16);
				blue = Integer.parseInt(color.substring(4, 6), 16);
			} else if (color.length() == 8) {
				alpha = Integer.parseInt(color.substring(0, 2), 16);
				red = Integer.parseInt(color.substring(2, 4), 16);
				green = Integer.parseInt(color.substring(4, 6), 16);
				blue = Integer.parseInt(color.substring(6, 8), 16);
			}
			layout_root.setBackgroundColor(Color.argb(alpha, red, green, blue));
		}
		int lpv_lock_id = UZResourcesIDFinder.getResIdID("lpv_lock");
		lockPatternView = (LockPatternView) findViewById(lpv_lock_id);
		lockPatternUtils = new LockPatternUtils(this);
		lockPatternView.setOnPatternListener(new OnPatternListener() {

			public void onPatternStart() {

			}

			public void onPatternDetected(List<Cell> pattern) {
				if (flag == Constants.Flag_Check_Lock) {
					int result = lockPatternUtils.checkPattern(pattern);
					if (result != Constants.RESULT_OK) {
						if (result == Constants.RESULT_WRONG) {
							lockPatternView.setDisplayMode(DisplayMode.Wrong);
							Toast.makeText(MainLockActivity.this, "密码错误", Toast.LENGTH_LONG).show();
						} else if (result == Constants.RESULT_INVALID) {
							lockPatternView.clearPattern();
							Toast.makeText(MainLockActivity.this, "未设置密码", Toast.LENGTH_LONG).show();
						}

					} else {
						lockPatternView.clearPattern();
						// Toast.makeText(MainLockActivity.this, "密码正确",
						// Toast.LENGTH_LONG).show();
						setResult(Constants.RESULT_OK);
						finish();
					}
				} else if (flag == Constants.Flag_set_Lock) {
					lockPatternUtils.saveLockPattern(pattern);
					Toast.makeText(MainLockActivity.this, "密码设置成功", Toast.LENGTH_LONG).show();
					lockPatternView.clearPattern();
					setResult(Constants.RESULT_OK);
					finish();
				}

			}

			public void onPatternCleared() {

			}

			public void onPatternCellAdded(List<Cell> pattern) {

			}
		});
	}

}
