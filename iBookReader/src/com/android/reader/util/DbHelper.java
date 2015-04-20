package com.android.reader.util;

import java.io.*;
import java.util.*;
import java.util.regex.*;

import android.content.*;
import android.database.*;
import android.database.sqlite.*;

import com.android.reader.filebrowser.*;
import com.android.reader.model.*;

/**
 * 操作本地数据库
 * 
 * @Title: DbHelper.java
 * @Package com.android.reader.util
 */
public class DbHelper extends SQLiteOpenHelper {
	
	private final static String DATABASE_NAME = "love_db";
	private final static int DATABASE_VERSION = 1;
	private final static String TABLE_NAME = "book_mark";
	private final static String TABLE_SETUP = "book_setup";
	public final static String FIELD_ID = "_id";
	public final static String FIELD_FILENAME = "filename";
	public final static String FIELD_BOOKMARK = "bookmark";
	public final static String FONT_SIZE = "fontsize";
	public final static String ROW_SPACE = "rowspace";
	public final static String COLUMN_SPACE = "columnspace";

	public DbHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	/**
	 * 创建系统设置表及书籍表
	 */
	@Override
	public void onCreate(SQLiteDatabase db) {
		try {
			StringBuffer sqlCreateCountTb = new StringBuffer();
			sqlCreateCountTb.append("create table ").append(TABLE_NAME)
			.append("(_id integer primary key autoincrement,")		   
			.append(" filename text,")   
			.append(" bookmark text);");
			db.execSQL(sqlCreateCountTb.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			StringBuffer setupTb = new StringBuffer();
			setupTb.append("create table ").append(TABLE_SETUP)
			   .append("(_id integer primary key autoincrement,")	
			   .append(" fontsize text,")  
			   .append(" rowspace text,")  
			   .append(" columnspace text);");
			db.execSQL(setupTb.toString());
			String setup = "insert into " + TABLE_SETUP + "(fontsize,rowspace,columnspace) values('12','0','0')";
			db.execSQL(setup);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void onUpgrade(SQLiteDatabase db, int arg1, int arg2) {
		String sql = " DROP TABLE IF EXISTS " + TABLE_NAME;
		db.execSQL(sql);
		onCreate(db);
	}

	public Cursor select() {
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.query(TABLE_NAME, null, null, null, null, null, " _id desc");
		return cursor;
	}

	/**
	 * 根据id获得单本书信息
	 */
	public BookInfo getBookInfo(int id){
		BookInfo book = new BookInfo();
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = null;
		cursor = db.query(TABLE_NAME, null, "_id=" + id, null, null, null, null);
		cursor.moveToPosition(0);
		book.id = id;
		book.bookname = cursor.getString(1);
		book.bookmark = cursor.getInt(2);
		db.close();
		return book;
	}
	
	/**
	 * 获得系统设置信息
	 */
	public SetupInfo getSetupInfo(){
		SQLiteDatabase db = this.getReadableDatabase();
		SetupInfo setup = new SetupInfo();
		try {
			Cursor cursor = null;
			cursor = db.query(TABLE_SETUP, null, null, null, null, null, null);
			cursor.moveToPosition(0);
			setup.id = cursor.getInt(0); 
			setup.fontsize = cursor.getInt(1);
			setup.rowspace = cursor.getInt(2);
			setup.columnspace = cursor.getInt(3);
			db.close();
		} catch (Exception e) {
			StringBuffer setupTb = new StringBuffer();
			setupTb.append("create table ").append(TABLE_SETUP)
			   .append("(_id integer primary key autoincrement,")	
			   .append(" fontsize text,")  
			   .append(" rowspace text,")  
			   .append(" columnspace text);");
			db.execSQL(setupTb.toString());
			String setups = "insert into " + TABLE_SETUP + "(fontsize,rowspace,columnspace) values('12','0','0')";
			db.execSQL(setups);
			return getSetupInfo();
		}
		return setup;
	}
	
	/**
	 * 获得所有书籍信息
	 */
	public List<BookInfo> getAllBookInfo(){
		List<BookInfo> books = new ArrayList<BookInfo>();
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.query(TABLE_NAME, null, null, null, null, null, " _id desc");
		int count = cursor.getCount();
		for (int i = 0; i < count; i++) {
			cursor.moveToPosition(i);
			BookInfo book = new BookInfo();
			book.id = cursor.getInt(0);
			book.bookname = cursor.getString(1);
			book.bookmark = cursor.getInt(2);
			books.add(book);
		}
		return books;
	}
	
	public long insert(String Title) {
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues cv = new ContentValues();
		cv.put(FIELD_BOOKMARK, Title);
		long row = db.insert(TABLE_NAME, null, cv);
		return row;
	}
	
	public long insert(String filename, String bookmark) {
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues cv = new ContentValues();
		cv.put(FIELD_FILENAME, filename);
		cv.put(FIELD_BOOKMARK, bookmark);
		long row = db.insert(TABLE_NAME, null, cv);
		return row;
	}
	
	public void delete(int id) {
		SQLiteDatabase db = this.getWritableDatabase();
		String where = FIELD_ID + "=?";
		String[] whereValue = { Integer.toString(id) };
		db.delete(TABLE_NAME, where, whereValue);
	}

	public void update(int id, String filename, String bookmark) {
		SQLiteDatabase db = this.getWritableDatabase();
		String where = FIELD_ID + "=?";
		String[] whereValue = { Integer.toString(id) };
		ContentValues cv = new ContentValues();
		cv.put(FIELD_FILENAME, filename);
		cv.put(FIELD_BOOKMARK, bookmark);
		db.update(TABLE_NAME, cv, where, whereValue);
	}
	
	/**
	 * 更新系统设置表
	 */
	public void updateSetup(int id, String fontsize, String rowspace,String columnspace) {
		SQLiteDatabase db = this.getWritableDatabase();
		try {
			String where = FIELD_ID + "=?";
			String[] whereValue = { Integer.toString(id) };
			ContentValues cv = new ContentValues();
			cv.put(FONT_SIZE, fontsize);
			cv.put(ROW_SPACE, rowspace);
			cv.put(COLUMN_SPACE, columnspace);
			db.update(TABLE_SETUP, cv, where, whereValue);
		} catch (Exception e) {
			StringBuffer setupTb = new StringBuffer();
			setupTb.append("create table ").append(TABLE_SETUP)
			   .append("(_id integer primary key autoincrement,")	
			   .append(" fontsize text,")  
			   .append(" rowspace text,")  
			   .append(" columnspace text);");
			db.execSQL(setupTb.toString());
			String setups = "insert into " + TABLE_SETUP + "(fontsize,rowspace,columnspace) values('12','0','0')";
			db.execSQL(setups);
			updateSetup(id, fontsize, rowspace, columnspace);
		}
	}
	
	/**
	 * 检测书籍是否存在
	 */
	public String doExist(String path){
		SQLiteDatabase db = this.getWritableDatabase();
		File f = new File(path);
		try {
			Cursor cursor = db.query(TABLE_NAME, null, "filename='" + f.getName() + "'", null, null, null, null);
			if(cursor.getCount() == 0){
				String targetPath = "/sdcard/lovereader/";
				String src = f.getPath();
				String tar = targetPath + f.getName();
				try {
					FileUtil.copyFile(new File(src), new File(tar));
					renameFile(targetPath, f.getName(), f.getName());
				} catch (Exception e) {
					e.printStackTrace();
				}
				insert(f.getName(), "0");
				Cursor cursor1 = db.query(TABLE_NAME, null, "filename='" + f.getName() + "'", null, null, null, null);
				cursor1.moveToPosition(0);
				String result = String.valueOf(cursor1.getInt(0));
				cursor1.close();
				cursor.close();
				return result;
			}else{
				Cursor cursor0 = db.query(TABLE_NAME, null, "filename='" + f.getName() + "'", null, null, null, null);
				cursor0.moveToPosition(0);
				String result = String.valueOf(cursor0.getInt(0));
				cursor0.close();
				cursor.close();
				return result;
			}
		} catch (Exception e) {
			e.printStackTrace();
			String targetPath = "/sdcard/lovereader/";
			String src = f.getPath();
			String tar = targetPath + f.getName();
			try {
				FileUtil.copyFile(new File(src), new File(tar));
			} catch (Exception e1) {
				e1.printStackTrace();
			}
			insert(f.getName(), "0");
			Cursor cursor1 = db.query(TABLE_NAME, null, "filename=" + f.getName(), null, null, null, null);
			cursor1.moveToPosition(0);
			String result = String.valueOf(cursor1.getInt(0));
			cursor1.close();
			return result;
		}
	}
	
	/**
	 * 重命名文件
	 */
    public void renameFile(String path,String oldname,String newname){ 
        if(!oldname.equals(newname)){//新的文件名和以前文件名不同时,才有必要进行重命名 
            File oldfile=new File(path + oldname); 
            File newfile=new File(path + newname); 
            if(!oldfile.exists()){
                return;//重命名文件不存在
            }
            if(newfile.exists())//若在该目录下已经有一个文件和新文件名相同，则不允许重命名 
                System.out.println(newname+"已经存在！"); 
            else{ 
                oldfile.renameTo(newfile); 
            } 
        }else{
            System.out.println("新文件名和旧文件名相同...");
        }
    }
	
    /**
     * 过滤特殊字符
     */
	public static String stringFilter(String str) throws PatternSyntaxException {
		String regEx = "[`~!@#$%^&*()+=|{}':;',\\[\\]<>《》/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
		Pattern p = Pattern.compile(regEx);
		Matcher m = p.matcher(str);
		return m.replaceAll("").trim();
	}
}
