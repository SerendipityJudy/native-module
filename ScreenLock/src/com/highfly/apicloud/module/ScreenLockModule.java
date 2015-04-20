package com.highfly.apicloud.module;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;

import com.uzmap.pkg.uzcore.UZWebView;
import com.uzmap.pkg.uzcore.annotation.UzJavascriptMethod;
import com.uzmap.pkg.uzcore.uzmodule.UZModule;
import com.uzmap.pkg.uzcore.uzmodule.UZModuleContext;

public class ScreenLockModule extends UZModule {

	private UZModuleContext mJsCallback_check;
	private UZModuleContext mJsCallback_set;

	public ScreenLockModule(UZWebView webView) {
		super(webView);
	}

	@UzJavascriptMethod
	public void jsmethod_show(final UZModuleContext moduleContext) {
		mJsCallback_check = moduleContext;
		Intent i = new Intent(getContext(), MainLockActivity.class);
		i.putExtra("flag", Constants.Flag_Check_Lock);
		i.putExtra("color", moduleContext.optString("color"));
		startActivityForResult(i, Constants.Flag_Check_Lock);
	}

	@UzJavascriptMethod
	public void jsmethod_set(final UZModuleContext moduleContext) {
		mJsCallback_set = moduleContext;
		Intent i = new Intent(getContext(), MainLockActivity.class);
		i.putExtra("flag", Constants.Flag_set_Lock);
		i.putExtra("color", moduleContext.optString("color"));
		startActivityForResult(i, Constants.Flag_set_Lock);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
		case Constants.Flag_Check_Lock:
			if (mJsCallback_check != null) {
				JSONObject json = new JSONObject();
				try {
					json.put("status", resultCode);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				mJsCallback_check.success(json, false);
			}
			break;
		case Constants.Flag_set_Lock:
			if (mJsCallback_set != null) {
				JSONObject json = new JSONObject();
				try {
					json.put("status", resultCode);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				mJsCallback_set.success(json, false);
			}
			break;
		default:
			break;
		}
	}
}
