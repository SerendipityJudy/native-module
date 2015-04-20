
package com.android.reader;

import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.text.*;
import java.util.*;

import android.graphics.*;
import android.graphics.Paint.Align;

public class BookPageFactory {

	private File book_file = null;
	private MappedByteBuffer m_mbBuf = null;
	private int m_mbBufLen = 0;
	private int m_mbBufBegin = 50; //50
	private int m_mbBufEnd = 0;
	private String m_strCharsetName = "UTF-8";
	private Bitmap m_book_bg = null;
	private final int mWidth;
	private final int mHeight;

	private Vector<String> m_lines = new Vector<String>();

	private int m_fontSize = 40;
	private final int m_textColor = Color.BLACK;
	private final int m_backColor = 0xffff9e85; 
	private final int marginWidth = 15; 
	private final int marginHeight = 20;
	private final int youmiHeight = 0;

	private int mLineCount;
	private final float mVisibleHeight;
	private final float mVisibleWidth;
	private boolean m_isfirstPage, m_islastPage;
	private final int b_FontSize = 16;
	private final int spaceSize = 20;
	private int curProgress = 0;
	private String fileName = "";

	private final Paint mPaint;
	private final Paint bPaint;
	private final Paint spactPaint;
	private final Paint titlePaint;

	public BookPageFactory(int w, int h) {
		mWidth = w;
		mHeight = h;
		mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mPaint.setTextAlign(Align.LEFT);
		mPaint.setTextSize(m_fontSize);
		mPaint.setColor(m_textColor);
	
		mVisibleWidth = mWidth - marginWidth * 2;
		mVisibleHeight = mHeight - marginHeight * 2 - youmiHeight;
		int totalSize = m_fontSize+spaceSize;
		mLineCount = (int) ((mVisibleHeight)/ totalSize);
		
		bPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		bPaint.setTextAlign(Align.LEFT);
		bPaint.setTextSize(b_FontSize);
		bPaint.setColor(m_textColor);
		
		spactPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		spactPaint.setTextAlign(Align.LEFT);
		spactPaint.setTextSize(spaceSize);
		spactPaint.setColor(m_textColor);
		
		titlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		titlePaint.setTextAlign(Align.LEFT);
		titlePaint.setTextSize(30);
		titlePaint.setColor(m_textColor);
		
	}

	@SuppressWarnings("resource")
	public void openbook(String strFilePath) {
		try {
			InputStream inputStream = new FileInputStream(strFilePath);  
	        byte[] head = new byte[3];  
	        inputStream.read(head);    
	        String code = "";  
	            code = "gb2312";  
	        if (head[0] == -1 && head[1] == -2 )  
	            code = "UTF-16";  
	        if (head[0] == -2 && head[1] == -1 )  
	            code = "Unicode";  
	        if(head[0]==-17 && head[1]==-69 && head[2] ==-65)  
	            code = "UTF-8";  
	        this.m_strCharsetName = code;
			book_file = new File(strFilePath);
			long lLen = book_file.length();
			m_mbBufLen = (int) lLen;
			m_mbBuf = new RandomAccessFile(book_file, "r").getChannel().map(FileChannel.MapMode.READ_ONLY, 0, lLen);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	protected byte[] readParagraphBack(int nFromPos) {
		int nEnd = nFromPos;
		int i;
		byte b0, b1;
		if (m_strCharsetName.equals("UTF-16LE")) {
			i = nEnd - 2;
			while (i > 0) {
				b0 = m_mbBuf.get(i);
				b1 = m_mbBuf.get(i + 1);
				if (b0 == 0x0a && b1 == 0x00 && i != nEnd - 2) {
					i += 2;
					break;
				}
				i--;
			}

		} else if (m_strCharsetName.equals("UTF-16BE")) {
			i = nEnd - 2;
			while (i > 0) {
				b0 = m_mbBuf.get(i);
				b1 = m_mbBuf.get(i + 1);
				if (b0 == 0x00 && b1 == 0x0a && i != nEnd - 2) {
					i += 2;
					break;
				}
				i--;
			}
		} else {
			i = nEnd - 1;
			while (i > 0) {
				b0 = m_mbBuf.get(i);
				if (b0 == 0x0a && i != nEnd - 1) {
					i++;
					break;
				}
				i--;
			}
		}
		if (i < 0)
			i = 0;
		int nParaSize = nEnd - i;
		int j;
		byte[] buf = new byte[nParaSize];
		for (j = 0; j < nParaSize; j++) {
			buf[j] = m_mbBuf.get(i + j);
		}
		return buf;
	}

	protected byte[] readParagraphForward(int nFromPos) {
		int nStart = nFromPos;
		int i = nStart;
		byte b0, b1;
		
		if (m_strCharsetName.equals("UTF-16LE")) {
			while (i < m_mbBufLen - 1) {
				b0 = m_mbBuf.get(i++);
				b1 = m_mbBuf.get(i++);
				if (b0 == 0x0a && b1 == 0x00) {
					break;
				}
			}
		} else if (m_strCharsetName.equals("UTF-16BE")) {
			while (i < m_mbBufLen - 1) {
				b0 = m_mbBuf.get(i++);
				b1 = m_mbBuf.get(i++);
				if (b0 == 0x00 && b1 == 0x0a) {
					break;
				}
			}
		} else {
			while (i < m_mbBufLen) {
				b0 = m_mbBuf.get(i++);
				if (b0 == 0x0a) {
					break;
				}
			}
		}
		int nParaSize = i - nStart;
		byte[] buf = new byte[nParaSize];
		for (i = 0; i < nParaSize; i++) {
			buf[i] = m_mbBuf.get(nFromPos + i);
		}
		return buf;
	}

	protected Vector<String> pageDown() {
		String strParagraph = "";
		Vector<String> lines = new Vector<String>();
		while (lines.size() < mLineCount && m_mbBufEnd < m_mbBufLen) {
			byte[] paraBuf = readParagraphForward(m_mbBufEnd); 
			m_mbBufEnd += paraBuf.length;
			try {
				strParagraph = new String(paraBuf, m_strCharsetName);
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			String strReturn = "";
			if (strParagraph.indexOf("\r\n") != -1) {
				strReturn = "\r\n";
				strParagraph = strParagraph.replaceAll("\r\n", "");
			} else if (strParagraph.indexOf("\n") != -1) {
				strReturn = "\n";
				strParagraph = strParagraph.replaceAll("\n", "");
			}

			if (strParagraph.length() == 0) {
				lines.add(strParagraph);
			}
			while (strParagraph.length() > 0) {
				int nSize = mPaint.breakText(strParagraph, true, mVisibleWidth,
						null);
				lines.add(strParagraph.substring(0, nSize));
				strParagraph = strParagraph.substring(nSize);
				if (lines.size() >= mLineCount) {
					break;
				}
			}
			if (strParagraph.length() != 0) {
				try {
					m_mbBufEnd -= (strParagraph + strReturn)
							.getBytes(m_strCharsetName).length;
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
			}
		}
		return lines;
	}

	protected void pageUp() {
		if (m_mbBufBegin < 0)
			m_mbBufBegin = 0;
		Vector<String> lines = new Vector<String>();
		String strParagraph = "";
		while (lines.size() < mLineCount && m_mbBufBegin > 0) {
			Vector<String> paraLines = new Vector<String>();
			byte[] paraBuf = readParagraphBack(m_mbBufBegin);
			m_mbBufBegin -= paraBuf.length;
			try {
				strParagraph = new String(paraBuf, m_strCharsetName);
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			strParagraph = strParagraph.replaceAll("\r\n", "");
			strParagraph = strParagraph.replaceAll("\n", "");

			if (strParagraph.length() == 0) {
				//paraLines.add(strParagraph);
			}
			while (strParagraph.length() > 0) {
				int nSize = mPaint.breakText(strParagraph, true, mVisibleWidth,
						null);
				paraLines.add(strParagraph.substring(0, nSize));
				strParagraph = strParagraph.substring(nSize);
			}
			lines.addAll(0, paraLines);
		}
		while (lines.size() > mLineCount) {
			try {
				m_mbBufBegin += lines.get(0).getBytes(m_strCharsetName).length;
				lines.remove(0);
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
		m_mbBufEnd = m_mbBufBegin;
		return;
	}

	protected void prePage() throws IOException {
		if (m_mbBufBegin <= 0) {
			m_mbBufBegin = 0;
			m_isfirstPage = true;
			return;
		} else
			m_isfirstPage = false;
		m_lines.clear();
		pageUp();
		m_lines = pageDown();
	}

	public void nextPage() throws IOException {
		if (m_mbBufEnd >= m_mbBufLen) {
			m_islastPage = true;
			return;
		} else
			m_islastPage = false;
		m_lines.clear();
		m_mbBufBegin = m_mbBufEnd;
		m_lines = pageDown();
	}

	public void onDraw(Canvas c) {
		if (m_lines.size() == 0)
			m_lines = pageDown();
		if (m_lines.size() > 0) {
			if (m_book_bg == null)
				c.drawColor(m_backColor);
			else
				c.drawBitmap(m_book_bg, 0, 0, null);
			int y = marginHeight + youmiHeight;
			int i = 0;
			for (String strLine : m_lines) {
				y += m_fontSize;
				c.drawText(strLine, marginWidth, y, mPaint);
				y+=spaceSize;
				if(i!=m_lines.size()-1){
					c.drawText("", marginWidth, y, spactPaint);
				}
				i++;
			}
		}
		float fPercent = (float) (m_mbBufBegin * 1.0 / m_mbBufLen);
		DecimalFormat df = new DecimalFormat("#0.0");
		String strPercent = df.format(fPercent * 100) + "%";
		
		curProgress = (int)round1(fPercent * 100,0);
		int nPercentWidth = (int) bPaint.measureText("99.9%") + 1;
		c.drawText(strPercent, mWidth - nPercentWidth, mHeight-5, bPaint);
		
		SimpleDateFormat formatter = new SimpleDateFormat("HH:mm");      
		Date curDate = new Date(System.currentTimeMillis());//��ȡ��ǰʱ��      
		String str = formatter.format(curDate);  
		c.drawText(str, 5, mHeight-5, bPaint);
		int titleWidth = (int) bPaint.measureText("��"+fileName+"��") + 1;
		c.drawText("��"+fileName+"��", (mWidth-titleWidth)/2, mHeight-5, bPaint);
	}

	private static double round1(double v, int scale) {
		if (scale < 0)
		return v;
		String temp = "#####0.";
		for (int i = 0; i < scale; i++) {
		temp += "0";
		}
		return Double.valueOf(new java.text.DecimalFormat(temp).format(v));
		}

	public void setBgBitmap(Bitmap BG) {
		if (BG.getWidth() != mWidth || BG.getHeight() != mHeight)
			m_book_bg = Bitmap.createScaledBitmap(BG, mWidth, mHeight, true);
		else
			m_book_bg = BG;
	}
	 
	public boolean isfirstPage() {
		return m_isfirstPage;
	}

	public void setIslastPage(boolean islast){
		m_islastPage = islast;
	}
	public boolean islastPage() {
		return m_islastPage;
	} 
	public int getCurPostion() {
		return m_mbBufEnd;
	}
	
	public int getCurPostionBeg(){
		return m_mbBufBegin;
	}
	public void setBeginPos(int pos) {
		m_mbBufEnd = pos;
		m_mbBufBegin = pos;
	}
	
	public int getBufLen() {
		return m_mbBufLen;
	}
	
	public int getCurProgress(){
		return curProgress;
	}
	public String getOneLine() {
		return m_lines.toString().substring(0, 10);
	}
	
	public void changBackGround(int color) {
		mPaint.setColor(color);
	}
	
	public void setFontSize(int size) {
		m_fontSize = size;
		mPaint.setTextSize(size);
		int totalSize = m_fontSize+spaceSize;
		mLineCount = (int) (mVisibleHeight / totalSize);
	}
	
	public void setFileName(String fileName){
		fileName = fileName.substring(0,fileName.indexOf("."));
		this.fileName = fileName; 
	}
}
