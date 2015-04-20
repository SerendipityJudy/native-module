package com.android.reader;

import android.content.*;

import com.android.reader.util.*;
import com.uzmap.pkg.uzcore.*;
import com.uzmap.pkg.uzcore.annotation.*;
import com.uzmap.pkg.uzcore.uzmodule.*;

/**
 * API Cloud 调用方法入口
 * 
 * @Title: APIModule.java
 * @Package com.android.reader
 */
public class APIModule extends UZModule {
	
	public APIModule(UZWebView webView) {
		super(webView);
	}
	
	/**
	 * 打开一本书 
	 */
	@UzJavascriptMethod
	public void jsmethod_openBook(UZModuleContext moduleContext){
		
		String path = moduleContext.optString("path");
		DbHelper db = new DbHelper(this.getContext());
		Intent intent = new Intent();
	    intent.setClass(this.getContext(), BookActivity.class);
	    intent.putExtra("bookid", db.doExist(path));
	    startActivity(intent);
		db.close();
	}
	
	/**
	 * 打开阅读书架
	 */
	@UzJavascriptMethod
	public void jsmethod_openBookShelf(UZModuleContext moduleContext){
		Intent intent = new Intent(getContext(), LoveReaderActivity.class);
		startActivity(intent);
	}
}
