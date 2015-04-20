package com.android.reader;

import java.io.*;
import java.util.*;

import android.annotation.*;
import android.app.*;
import android.content.*;
import android.database.*;
import android.graphics.*;
import android.os.*;
import android.view.*;
import android.view.View.OnTouchListener;
import android.widget.*;

import com.android.reader.model.*;
import com.android.reader.util.*;

/**
 * ��ܽ�����
 * 
 * @Title: BookActivity.java
 * @Package com.android.reader
 */
@SuppressLint("WrongCall")
public class BookActivity extends Activity {
	
	public final static int OPENMARK = 0;
	public final static int SAVEMARK = 1;
	public final static int TEXTSET = 2;
	
	private PageWidget mPageWidget;
	private Bitmap mCurPageBitmap, mNextPageBitmap;
	private Canvas mCurPageCanvas, mNextPageCanvas;
	private BookPageFactory pagefactory;
	private int whichSize=12;//��ǰ�������С
	private int txtProgress = 0;//��ǰ�Ķ��Ľ���
	private final String bookPath = "/sdcard/lovereader/";
	final String[] font = new String[] {"20","24","26","30","32","36","40","46","50","56","60","66","70"};
	int curPostion;
	DbHelper db; 
	Context mContext;
	Cursor mCursor;
	BookInfo book = null; 
	SetupInfo setup = null;

	@SuppressWarnings("deprecation")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		Display display = getWindowManager().getDefaultDisplay();
		int w = display.getWidth();
		int h = display.getHeight(); 
		System.out.println(w + "\t" + h);
		mCurPageBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
		mNextPageBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);

		mCurPageCanvas = new Canvas(mCurPageBitmap);
		mNextPageCanvas = new Canvas(mNextPageBitmap);
		pagefactory = new BookPageFactory(w, h); 
		pagefactory.setBgBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.bg));
		
		//ȡ�ô��ݵĲ���
		Intent intent = getIntent();
		String bookid = intent.getStringExtra("bookid");
		mContext = this;
		db = new DbHelper(mContext);
		try {
			book = db.getBookInfo(Integer.parseInt(bookid));
			setup = db.getSetupInfo();
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			if(book != null){
				pagefactory.setFileName(book.bookname);
				mPageWidget = new PageWidget(this, w, h);
				setContentView(mPageWidget);
				pagefactory.openbook(bookPath + book.bookname);
				
				if (book.bookmark > 0) { 
					whichSize = setup.fontsize;
					pagefactory.setFontSize(Integer.parseInt(font[setup.fontsize]));
					pagefactory.setBeginPos(Integer.valueOf(book.bookmark));
					try {
						pagefactory.prePage();
					} catch (IOException e) {
						e.printStackTrace();
					}
					pagefactory.onDraw(mNextPageCanvas);
					mPageWidget.setBitmaps(mNextPageBitmap, mNextPageBitmap);
					mPageWidget.postInvalidate();
					db.close(); 
				}else{
					pagefactory.onDraw(mCurPageCanvas);
					mPageWidget.setBitmaps(mCurPageBitmap, mCurPageBitmap);
				} 

				mPageWidget.setOnTouchListener(new OnTouchListener() {
					@Override
					public boolean onTouch(View v, MotionEvent e) {
						boolean ret = false;
						if (v == mPageWidget) {
							if (e.getAction() == MotionEvent.ACTION_DOWN) {
								mPageWidget.abortAnimation();
								mPageWidget.calcCornerXY(e.getX(), e.getY());

								pagefactory.onDraw(mCurPageCanvas);
								if (mPageWidget.DragToRight()) {
									try {
										pagefactory.prePage();
									} catch (IOException e1) {
										e1.printStackTrace();
									}
									if (pagefactory.isfirstPage()){
										Toast.makeText(mContext, "�Ѿ��ǵ�һҳ",Toast.LENGTH_SHORT).show(); 
										return false;
									}
									pagefactory.onDraw(mNextPageCanvas);
								} else {
									try {
										pagefactory.nextPage();
									} catch (IOException e1) {
										e1.printStackTrace();
									}
									if (pagefactory.islastPage()){
										Toast.makeText(mContext, "�Ѿ������һҳ",Toast.LENGTH_SHORT).show();
										return false;
									}
									pagefactory.onDraw(mNextPageCanvas);
								}
								mPageWidget.setBitmaps(mCurPageBitmap, mNextPageBitmap);
							}
							ret = mPageWidget.doTouchEvent(e);
							return ret;
						}
						return false;
					}
				});
			}else{
				Toast.makeText(mContext, "�����鲻���ڣ������Ѿ�ɾ��",Toast.LENGTH_SHORT).show(); 
				BookActivity.this.finish();
			}
		} catch (Exception e) {
			Toast.makeText(mContext, "�����鲻���ڣ������Ѿ�ɾ��",Toast.LENGTH_SHORT).show(); 
			BookActivity.this.finish();
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {// �����˵�
		super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {// �����˵�
		int ID = item.getItemId();
		switch (ID) { 
		case R.id.exitto:
			addBookMark();
			finish();
			break;
		case R.id.fontsize:
			new AlertDialog.Builder(this)
			.setTitle("��ѡ��")
			.setIcon(android.R.drawable.ic_dialog_info)                
			.setSingleChoiceItems(font, whichSize, 
			  new DialogInterface.OnClickListener() {
			     @Override
				public void onClick(DialogInterface dialog, int which) {
			    	 dialog.dismiss();
			    	 setFontSize(Integer.parseInt(font[which]));
			    	 whichSize = which;
			     }
			  }
			)
			.setNegativeButton("ȡ��", null)
			.show();
			break;
		case R.id.nowprogress:
			LayoutInflater inflater = getLayoutInflater();
			final View layout = inflater.inflate(R.layout.bar,
		    (ViewGroup) findViewById(R.id.seekbar));
			SeekBar seek = (SeekBar)layout.findViewById(R.id.seek);
			final TextView textView = (TextView)layout.findViewById(R.id.textprogress);
			txtProgress = pagefactory.getCurProgress();
			seek.setProgress(txtProgress);
			textView.setText(String.format(getString(R.string.progress), txtProgress+"%"));
			seek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
				@Override
				public void onStopTrackingTouch(SeekBar seekBar) {
					int progressBar = seekBar.getProgress();
					int m_mbBufLen = pagefactory.getBufLen();
					int pos = m_mbBufLen*progressBar/100;
					if(progressBar == 0){
						pos = 1;
					}
					pagefactory.setBeginPos(Integer.valueOf(pos));
					try {
						pagefactory.prePage();
					} catch (IOException e) {
						e.printStackTrace();
					}
					pagefactory.onDraw(mCurPageCanvas);
					mPageWidget.setBitmaps(mCurPageBitmap, mCurPageBitmap);
					mPageWidget.postInvalidate();
				}
				
				@Override
				public void onStartTrackingTouch(SeekBar seekBar) {
					
				}
				
				@Override
				public void onProgressChanged(SeekBar seekBar, int progress,
						boolean fromUser) {
					if(fromUser){
						textView.setText(String.format(getString(R.string.progress), progress+"%"));
					}
				}
			});
		   new AlertDialog.Builder(this).setTitle("��ת").setView(layout)
		     .setPositiveButton("ȷ��", 
	    		 new DialogInterface.OnClickListener() {
				     @Override
					public void onClick(DialogInterface dialog, int which) {
				    	 //Toast.makeText(mContext, "��ѡ�е���", Toast.LENGTH_SHORT).show();
				        dialog.dismiss();
				     }
				  }
	    		 ).show();
			break;
		default:
			break;
		}
		return true;
	}
	
	private void setFontSize(int size){
		pagefactory.setFontSize(size);
		int pos = pagefactory.getCurPostionBeg();
		pagefactory.setBeginPos(pos);
		try {
			pagefactory.nextPage();
		} catch (IOException e) {
			e.printStackTrace();
		}
		setContentView(mPageWidget);
		pagefactory.onDraw(mNextPageCanvas);
		mPageWidget.setBitmaps(mNextPageBitmap, mNextPageBitmap);
		mPageWidget.invalidate();
	}
	
	private void creatIsExit() {
		Dialog dialog = new AlertDialog.Builder(BookActivity.this).setTitle(
			"��ʾ").setMessage(
			"�Ƿ�ȷ���˳���ȷ���������").setPositiveButton(
			"ȷ��",
			new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int whichButton) {
					dialog.cancel();
					finish();
				}
			}).setNegativeButton("ȡ��",
			new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.cancel();
				}
			}).create();// ������ť
		dialog.show();
	}
	
	Timer tExit = new Timer();
    TimerTask task = new TimerTask() {
        @Override
        public void run() {
        }
    };
    
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		  if (keyCode == KeyEvent.KEYCODE_BACK) {
			  addBookMark();
			  this.finish();
		  }
		  return false;
	}
	  
	//�����ǩ
	public void addBookMark(){
		Message msg = new Message();
		msg.what = SAVEMARK;
		msg.arg1 = whichSize;
		curPostion = pagefactory.getCurPostion();
		msg.arg2 = curPostion;
		mhHandler.sendMessage(msg);
	} 
	
	Handler mhHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {

			case TEXTSET:
				pagefactory.changBackGround(msg.arg1);
				pagefactory.onDraw(mCurPageCanvas);
				mPageWidget.postInvalidate();
				break;

			case OPENMARK:
				try {
					mCursor = db.select();

				} catch (Exception e) {
					e.printStackTrace();
				}
				if (mCursor.getCount() > 0) {
					mCursor.moveToPosition(mCursor.getCount() - 1);
					String pos = mCursor.getString(2);
					String tmp = mCursor.getString(1);
					 
					pagefactory.setBeginPos(Integer.valueOf(pos));
					try {
						pagefactory.prePage();
					} catch (IOException e) {
						e.printStackTrace();
					}
					pagefactory.onDraw(mNextPageCanvas);
					mPageWidget.setBitmaps(mCurPageBitmap, mNextPageBitmap);
					mPageWidget.invalidate();
					db.close(); 
				}
				break;

			case SAVEMARK:
				try {
					db.update(book.id, book.bookname, String.valueOf(msg.arg2));
					db.updateSetup(setup.id,String.valueOf(msg.arg1), "0", "0");
				} catch (Exception e) {
					e.printStackTrace();
				}
				db.close();
				break;

			default:
				break;
			}
			super.handleMessage(msg);
		}
	};
}