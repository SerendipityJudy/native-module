package com.android.reader;
  
import java.io.*;
import java.util.*;

import android.app.*;
import android.content.*;
import android.os.*;
import android.view.*;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.view.View.OnCreateContextMenuListener;
import android.view.View.OnLongClickListener;
import android.widget.*;

import com.android.reader.filebrowser.*;
import com.android.reader.model.*;
import com.android.reader.util.*;

/**
 * txt文本阅读界面类
 * 
 * @Title: LoveReaderActivity.java
 * @Package com.android.reader
 */
public class LoveReaderActivity extends Activity {

	private static Boolean isExit = false;
	private static Boolean hasTask = false;
	private Context myContext;
    private ShelfAdapter mAdapter;
    private ListView shelf_list;
    int[ ] size = null;
	DbHelper db; 
	List<BookInfo> books;
	int realTotalRow;
	int bookNumber;
	final String[] font = new String[] {"20","24","26","30","32","36","40","46","50","56","60","66","70"};
    
    @ Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.shelf);
        db = new DbHelper(this);
        myContext = this;
        init ();
        
        books = db.getAllBookInfo();
        bookNumber = books.size();
        int count = books.size();
        int totalRow = count/3; 
        if (count%3 > 0 ){
        	totalRow = count/3 + 1;
        }
        realTotalRow = totalRow;
        if(totalRow<4){
        	totalRow = 4;
        }
        size = new int[totalRow];
        mAdapter = new ShelfAdapter();
        shelf_list.setAdapter(mAdapter);
    }

    
    private void init () {
        shelf_list = (ListView)findViewById(R.id.shelf_list);
    }

    public class ShelfAdapter extends BaseAdapter {

        public ShelfAdapter(){
        	
        }

        @ Override
        public int getCount(){
            if (size.length > 3) {
                return size.length;
            } else {
                return 3;
            }
        }

        @ Override
        public Object getItem (int position) {
            return size[position];
        }

        @ Override
        public long getItemId(int position) {
            return position;
        }
        
        @ Override
        public View getView (int position , View convertView , ViewGroup parent) {
        	LayoutInflater layout_inflater = (LayoutInflater) LoveReaderActivity.this.getSystemService ( Context.LAYOUT_INFLATER_SERVICE );
            View layout = layout_inflater.inflate (R.layout.shelf_list_item , null);
            if(position < realTotalRow){
            	int buttonNum = (position+1) * 3;
            	if(bookNumber <= 3){
            		buttonNum = bookNumber;
            	}
                for (int i = 0; i < buttonNum; i++) {
                	if(i == 0){
                		BookInfo book = books.get(position*3);
                		String buttonName = book.bookname;
                		if(buttonName.indexOf(".") > 0){
                			buttonName = buttonName.substring(0,buttonName.indexOf("."));
                		}
                		Button button = (Button) layout.findViewById(R.id.button_1);
                		button.setVisibility(View.VISIBLE);
                		button.setText(buttonName);
                		button.setId(book.id);
                		button.setOnClickListener(new ButtonOnClick(String.valueOf(book.id)));
                		button.setOnCreateContextMenuListener(listener);
                	}else if(i == 1){
                		BookInfo book = books.get(position*3+1);
                		String buttonName = book.bookname;
                		if(buttonName.indexOf(".") > 0){
                			buttonName = buttonName.substring(0,buttonName.indexOf("."));
                		}
                		Button button = (Button) layout.findViewById(R.id.button_2);
                		button.setVisibility(View.VISIBLE);
                		button.setText(buttonName);
                		button.setId(book.id);
                		button.setOnClickListener(new ButtonOnClick(String.valueOf(book.id)));
                		button.setOnCreateContextMenuListener(listener);
                	}else if(i == 2){
                		BookInfo book = books.get(position*3+2);
                		String buttonName = book.bookname;
                		if(buttonName.indexOf(".") > 0){
                			buttonName = buttonName.substring(0,buttonName.indexOf("."));
                		}
                		Button button = (Button)layout.findViewById(R.id.button_3);
                		button.setVisibility(View.VISIBLE);
                		button.setText(buttonName);
                		button.setId(book.id);
                		button.setOnClickListener (new ButtonOnClick(String.valueOf(book.id)));
                		button.setOnCreateContextMenuListener(listener);
                	}
    			}
                bookNumber -= 3;
            }
            return layout;
        }
    };

	OnCreateContextMenuListener listener = new OnCreateContextMenuListener() {
		@Override
		public void onCreateContextMenu(ContextMenu menu, View v,
				ContextMenuInfo menuInfo) {
			menu.add(0, 1, v.getId(), "删除本书");
		}
	};
	
    @Override
    public boolean onContextItemSelected(final MenuItem item) {
    	switch (item.getItemId()) {
		case 0:
			BookInfo book = db.getBookInfo(item.getOrder());
			new AlertDialog.Builder(this.getBaseContext()).setTitle("详细信息").
				setMessage("路径:" + "/sdcard/lovereader/"+book.bookname).setPositiveButton("确定", null).show(); 
			break;
		case 1:
			Dialog dialog = new AlertDialog.Builder(LoveReaderActivity.this).setTitle(
				"提示").setMessage(
				"确认要删除吗？").setPositiveButton(
				"确定",
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int whichButton) {
						BookInfo book = db.getBookInfo(item.getOrder());
						File dest = new File("/sdcard/lovereader/"+book.bookname);
						db.delete(item.getOrder());
						if (dest.exists()) {
							dest.delete();
							Toast.makeText(myContext, "删除成功", Toast.LENGTH_SHORT).show(); 
						}else{
							Toast.makeText(myContext, "磁盘文件删除失败", Toast.LENGTH_SHORT).show(); 
						}
						refreshShelf();
					}
				}).setNegativeButton("取消",
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.cancel();
					}
				}).create();
			dialog.show();
			break;
		default:
			break;
		}
    	return true;
    }
    
    @Override
	public void onActivityResult(int requestCode, int resultCode, Intent data){
    	if(requestCode == 222){
    		String isImport = data.getStringExtra("isImport");
    		if("1".equals(isImport)){
    			refreshShelf();
    		}
    	}
    } 
    
    public void refreshShelf(){
        books = db.getAllBookInfo();
        bookNumber = books.size();
        int count = books.size();
        int totalRow = count/3; 
        if (count%3 > 0 ){
        	totalRow = count/3 + 1;
        }
        realTotalRow = totalRow;
        if(totalRow<4){
        	totalRow = 4;
        }
        size = new int[totalRow];
		mAdapter = new ShelfAdapter();
	    shelf_list.setAdapter(mAdapter);
    }
    
	class ButtonOnClick implements OnClickListener {
		
		private final String id;
		
		public ButtonOnClick(String id) {
			this.id = id;
		}

		@Override
		public void onClick(View v) {
			if (this.id != null) {
				System.out.println("------------------->" + id);
				Intent intent = new Intent();
	    	    intent.setClass(LoveReaderActivity.this, BookActivity.class);
	    	    intent.putExtra("bookid", String.valueOf(this.id));
	    	    startActivity(intent);
			}
		}
	}
    
//    public class ButtonOnClick implements OnClickListener {
//
//    	@Override
//        public void onClick (View v) {
//        	Intent intent = new Intent();
//    	    intent.setClass(LoveReaderActivity.this, BookActivity.class);
//    	    intent.putExtra("bookid", String.valueOf(v.getId()));
//    	    startActivity(intent);
//        }
//    }
    
    public class ButtonOnLongClick implements OnLongClickListener {
    	
		@Override
		public boolean onLongClick(View v) {
			return true;
		}
    }
    
	Timer tExit = new Timer();
    TimerTask task = new TimerTask() {
        @Override
        public void run() {
            isExit = false;
            hasTask = true;
        }
    };
    
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
	  if (keyCode == KeyEvent.KEYCODE_BACK) {
		  if(isExit == false ) {
			  isExit = true;
			  Toast.makeText(this, "再按一次后退键退出应用程序",
					  Toast.LENGTH_SHORT).show(); 
			  if(!hasTask) {
				  tExit.schedule(task, 2000);
			  }
		  } else {
			  finish();
			  System.exit(0);
		  }
	  }
	  return false;
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
		return true;
	}
	
	@Override
    protected void onDestroy() {
    	super.onDestroy();
    }
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int ID = item.getItemId();
		switch (ID) { 
		case R.id.mainexit:
			creatIsExit();
			break;
		case R.id.addbook:
			Intent i = new Intent();
			i.setClass(LoveReaderActivity.this, Main.class);
			startActivityForResult(i, 222 );
			break;
		default:
			break;
		}
		return true;
	}
	
	private void creatIsExit() {
		Dialog dialog = new AlertDialog.Builder(LoveReaderActivity.this).setTitle(
			"提示").setMessage(
			"是否要确认Reader？").setPositiveButton(
			"",
			new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int whichButton) {
					LoveReaderActivity.this.finish();
					android.os.Process.killProcess(android.os.Process.myPid());
					System.exit(0);
				}
			}).setNegativeButton("取消",
			new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.cancel();
				}
			}).create();
		dialog.show();
	}
}