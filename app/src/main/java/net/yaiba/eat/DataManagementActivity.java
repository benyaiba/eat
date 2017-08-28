package net.yaiba.eat;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Environment;
import android.util.Xml;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import net.yaiba.eat.db.EatDB;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlSerializer;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class DataManagementActivity extends Activity {
	private EatDB EatDB;
	private Cursor mCursor;
	private int RECORD_ID = 0;
	private int selectBakupFileIndex = 0;
	private String[] bakFileArray ;
	private String FILE_DIR_NAME = "eat";
	private String fileNameSuff = ".xml";

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {

		EatDB = new EatDB(this);
		super.onCreate(savedInstanceState);


		//判断文件名中是否包含20170216020803!!!!!.xml 这种文件，如果目录中包含这种文件，将在画面最下方以红色文字提示。
		List<String> bakupFileList = new ArrayList<String>();
		String keepPath = Environment.getExternalStorageDirectory().toString()  + "//" +FILE_DIR_NAME;
		File[] files = new File(keepPath).listFiles();

		setContentView(R.layout.data_management);

		// 注释掉区域暂未开启使用
//		Button bn_back = (Button)findViewById(R.id.back);
//		bn_back.setOnClickListener(new OnClickListener(){
//			   public void  onClick(View v)
//			   {
//					   Intent mainIntent = new Intent(DataManagementActivity.this,MainActivity.class);
//					   startActivity(mainIntent);
//					   overridePendingTransition(android.R.anim.slide_in_left,android.R.anim.slide_out_right);
//					   setResult(RESULT_OK, mainIntent);
//					   finish();
//			   }
//			  });



		
		
		Button bn_data_bakup = (Button)findViewById(R.id.data_bakup);
		bn_data_bakup.setOnClickListener(new OnClickListener(){
			   public void  onClick(View v)
			   {  
				   dataBakup();
				   
			   }  
			  });
		
		Button bn_data_recover = (Button)findViewById(R.id.data_recover);
		bn_data_recover.setOnClickListener(new OnClickListener(){
			   public void  onClick(View v)
			   
			   {  
				   
		   //弹出提示，确认后恢复
		   AlertDialog.Builder builder= new AlertDialog.Builder(DataManagementActivity.this);
		   builder.setIcon(android.R.drawable.ic_dialog_info);
		   builder.setTitle("选择要恢复的文件,恢复后原有记录将被清空");

		   List<String> bakupFileList = new ArrayList<String>();
		   String keepPath = Environment.getExternalStorageDirectory().toString()  + "//" +FILE_DIR_NAME;
		   File[] files = new File(keepPath).listFiles();

		   //判断文件夹不存在或文件夹中没有文件时
		   if(files != null){
			   //存在时

			   for (int i = 0; i < files.length; i++) {
				   File file = files[i];
				   if (checkIsXMLFile(file.getPath())) {
					   //判断文件名中是否不包含20170216020803!!!!!.xml 这种文件，这种文件是未加密的文件，禁止在列表中显示
					   if(file.getName().indexOf("!")==-1){
						   bakupFileList.add(file.getName());
					   }
				   }
			   }

			   if(bakupFileList.size()<=0){

				   builder.setMessage("没有找到可用来恢复的备份文件");
				   builder.setNegativeButton("取消", null);
				   builder.create().show();

			   } else {
				   Collections.sort(bakupFileList);
				   Collections.reverse(bakupFileList);

				   bakFileArray = bakupFileList.toArray(new String[bakupFileList.size()]);


				   builder.setIcon(android.R.drawable.ic_dialog_alert);
				   builder.setSingleChoiceItems(bakFileArray, 0, new DialogInterface.OnClickListener() {
					   public void onClick(DialogInterface dialog, int index) {
						   selectBakupFileIndex = index;
						   //Toast.makeText(DataManagementActivity.this, "selectBakupFileIndex:"+selectBakupFileIndex , Toast.LENGTH_SHORT).show();
					   }
				   });
				   builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
					   public void onClick(DialogInterface dialog, int whichButton) {
						   dataRecover(bakFileArray[selectBakupFileIndex]);
						   //Toast.makeText(DataManagementActivity.this, "selectBakupFileIndex:"+selectBakupFileIndex , Toast.LENGTH_SHORT).show();
					   }
				   });
				   builder.setNegativeButton("取消", null);
				   builder.create().show();

//					   builder.setMessage("程序将读取SD卡中的备份文件"+fileName+"，恢复后原有记录将被清空。确定要执行吗？");
//						builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
//		                    public void onClick(DialogInterface dialog, int whichButton) {
//		                    	dataRecover();
//		                    }
//		                });
//						builder.setNegativeButton("取消", null);
//						builder.create().show();
			   }
		   } else {
			   //不存在时
			   builder.setMessage("没有找到可用来恢复的备份文件");
			   builder.setNegativeButton("取消", null);
			   builder.create().show();
		   }
	   }
	  });
		
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		if(keyCode == KeyEvent.KEYCODE_BACK){
			Intent mainIntent = new Intent(DataManagementActivity.this,MainActivity.class);
			startActivity(mainIntent);
			overridePendingTransition(android.R.anim.slide_in_left,android.R.anim.slide_out_right);
			setResult(RESULT_OK, mainIntent);  
			finish(); 
		}
		return super.onKeyDown(keyCode, event);
	}
	
	
	@SuppressWarnings("resource")
	@SuppressLint("SimpleDateFormat")
	public void dataBakup(){
		//Toast.makeText(this, "他很懒，备份程序还没做好", Toast.LENGTH_SHORT).show();
		EatDB = new EatDB(this);
		mCursor = EatDB.getAll(null);

		//检查目录并确定生成目录结构
		boolean sdCardExist = Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);
		String baseDir = Environment.getExternalStorageDirectory().getAbsolutePath() + "//" + FILE_DIR_NAME;
		Date dt = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
		//String fileName = fileNamePre + sdf.format(dt)+ fileNameSuff;

		//showAboutDialog("准备","正在处理未加密数据");
		try {
			if (sdCardExist) {
				String fileName = sdf.format(dt) + fileNameSuff;
				File f = createFile(baseDir, fileName);

				FileWriter fileWriter = new FileWriter(f,false);
				BufferedWriter bf = new BufferedWriter(fileWriter);
				bf.write(writeToString(mCursor));
				bf.flush();
				showAboutDialog("完成","备份文件"+fileName+"已输出到SD卡。");
			}
		} catch (IOException e) {
			showAboutDialog("错误","处理数据时出错，文件未生成");
		}
	}


	public File createFile(String baseDir, String fileName) {
		File f = new File(baseDir + "//"+ fileName );
		if(!f.getParentFile().exists()){
			f.getParentFile().mkdirs();
		}
		if(!f.exists()){
			try {
				f.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return f;
	}

	public void dataRecover(String fileName){

		showAboutDialog("完成",dataRecoverFromXml("normal",fileName));

	}

    private void showAboutDialog(String title, String msg){
		AlertDialog.Builder builder= new AlertDialog.Builder(this);
		builder.setIcon(android.R.drawable.ic_dialog_info);
		builder.setTitle(title);
		builder.setMessage(msg);
		builder.setPositiveButton("确认", null);
		builder.create().show();
	}


 	public void setUpViews(){
		EatDB = new EatDB(this);
		mCursor = EatDB.getOne(RECORD_ID);

	}


 	private String writeToString(Cursor mCursor){
 	    XmlSerializer serializer = Xml.newSerializer();
 	    StringWriter writer = new StringWriter();
 	    try {
 	        serializer.setOutput(writer);
 	        serializer.startDocument("UTF-8", true);
 	        serializer.startTag("", "resources");
 	        serializer.attribute("", "count", String.valueOf(mCursor.getCount()));

 	       if(mCursor.moveToFirst()) {
		        while(!mCursor.isAfterLast()) {
					serializer.startTag("", "record");
					serializer.attribute("", "id", mCursor.getString(0));
					serializer.attribute("", "food_name", mCursor.getString(1));
					serializer.attribute("", "eat_time", mCursor.getString(2));
					serializer.attribute("", "eat_where", mCursor.getString(3));
					serializer.attribute("", "remark", mCursor.getString(4));
					serializer.attribute("", "create_time", mCursor.getString(5));
					serializer.endTag("", "record");

		            mCursor.moveToNext();
		         }
		      }
 	        serializer.endTag("", "resources");
 	        serializer.endDocument();
 	        return writer.toString();
 	    } catch (Exception e) {
 	        throw new RuntimeException(e);
 	    } finally {
 	    	mCursor.close();
 	    }
 	}


 	private String dataRecoverFromXml(String recoverType, String fileName)
	{
		EatDB = new EatDB(this);
		String returnString = "";
		try {

			boolean sdCardExist = Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);
			
			if (sdCardExist) {
				String baseDir = Environment.getExternalStorageDirectory().getAbsolutePath() + "//" + FILE_DIR_NAME;
				
				String fn = "";
				if(recoverType.equals("normal")){
					fn = fileName;
				}
				File f = new File(baseDir + "//"+ fn );
				
				if(f.exists()){
					if(recoverType.equals("normal")){
						EatDB.deleteAll();
					}
					
					 int counter = 0;   
		               StringBuilder sb = new StringBuilder("");
		               XmlPullParser xrp = Xml.newPullParser();
		               FileInputStream fin = new FileInputStream(f);
		               xrp.setInput(fin, "UTF-8");  
		                while (xrp.getEventType() != XmlPullParser.END_DOCUMENT) {
		                         if (xrp.getEventType() == XmlPullParser.START_TAG) {
		                              String tagName = xrp.getName();
		                              if(tagName.equals("record")){   
		                                  counter++;   
		                                  sb.append("第"+counter+"条信息："+"\n");
		                                  //sb.append(xrp.getAttributeValue(0)+"\n"); 
		                                  String food_name = xrp.getAttributeValue(1);
		                                  String eat_time = xrp.getAttributeValue(2);
		                                  String eat_where = xrp.getAttributeValue(3);
		                                  String remark = xrp.getAttributeValue(4);
										  String create_time = xrp.getAttributeValue(5);
		                                  if(!food_name.isEmpty() && !eat_time.isEmpty() && !create_time.isEmpty() ){
		                                	  if(remark.equals(null)){
		                                		  remark="";
		                                	  }
											  EatDB.insert(food_name, eat_time, eat_where, remark, create_time);
		                                  }
		                              }   
		                         } else if (xrp.getEventType() == XmlPullParser.END_TAG) {
		                         } else if (xrp.getEventType() == XmlPullParser.TEXT) {
		                         }    
		                         xrp.next();  
		                    }   
		                
		                if(recoverType.equals("normal")){
		                	returnString = counter +"条数据已恢复";
						}
		                
		                return returnString;
				}else{
					if(recoverType.equals("normal")){
						//showAboutDialog("错误","未检测到备份程序");
						returnString = "未检测到备份文件"+fileName;
					}
					return returnString;
				}
			}else{
				//showAboutDialog("错误","程序未检测到SD卡");
				returnString = "程序未检测到SD卡";
				return returnString;
			}
			
		} catch (Exception e) {
			return "真的出错了";
		} finally {
			EatDB.close();
 	    }
		
	}
 	
 	
 	@SuppressLint("DefaultLocale")
	private boolean checkIsXMLFile(String fName) {
 		 boolean isXMLFile = false;
 		 String fileEnd = fName.substring(fName.lastIndexOf(".") + 1,  fName.length()).toLowerCase();
 		 if (fileEnd.equals("xml")) {
 			isXMLFile = true;
 		 } else {
 			isXMLFile = false;
 		 }
 		 return isXMLFile;
 	} 
 	


}
