/*
 * Copyright (C) 2014 Petrolr LLC, a Colorado limited liability company
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/* 
 * Written by the Petrolr team in 2014. Based on the Android SDK Bluetooth Chat Example... matthew.helm@gmail.com
 */


package com.petrolr.petrolr_obdterminal;

import android.annotation.SuppressLint;
import android.os.Environment;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;

class LogWriter {

	static void write_info(final String logmsg) {
		
		// ++++ Fire off a thread to write info to file
		
		Thread w_thread = new Thread() {
			public void run() {
				File myFilesDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Android/data/Petrolr/files");
				myFilesDir.mkdirs();
					    
				String dataline = (timestamp() + ", " + logmsg + "\n");
				File myfile = new File(myFilesDir + "/" + "Term_Log" + sDate() + ".txt");
				if(myfile.exists()){
					try {
						FileWriter write = new FileWriter(myfile, true);
						write.append(dataline);
						//read_ct++;
						write.close();		
					}catch (IOException e){
						//do something with exception
					}
				}else{ //make a new file since we apparently need one
					try {
						FileWriter write = new FileWriter(myfile, true);
						//	write.append(header);
						write.append(dataline);
						//read_ct++;
						write.close();		
					}catch (IOException e){
						//do something with exception
					}
	
				}
			}
		};
		w_thread.start();
	}
	
	private static long timestamp(){
		return System.currentTimeMillis();
	}
	
	@SuppressLint("SimpleDateFormat")
	private static String sDate(){
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
		java.util.Date date= new java.util.Date();
		return sdf.format(date.getTime());
	}
	
}
