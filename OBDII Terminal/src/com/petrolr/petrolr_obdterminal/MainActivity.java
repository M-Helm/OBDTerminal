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
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.ActionBar;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;





public class MainActivity extends Activity {
	private static final String TAG = "OBDII Terminal";
	private ListView mConversationView;
	
	// Name of the connected device
	private String mConnectedDeviceName = null;
	// Array adapter for the conversation thread
	private ArrayAdapter<String> mConversationArrayAdapter;
	// String buffer for outgoing messages
	private StringBuffer mOutStringBuffer;
	// Local Bluetooth adapter
	static BluetoothAdapter mBluetoothAdapter = null;
	// Member object for the chat services
	private BluetoothChatService mChatService = null;
	  
	  
	  
	// Intent request codes
	private static final int REQUEST_CONNECT_DEVICE_SECURE = 1;
	private static final int REQUEST_CONNECT_DEVICE_INSECURE = 2;
	private static final int REQUEST_ENABLE_BT = 3;
	
	// Key names received from the BluetoothChatService Handler
	public static final String DEVICE_NAME = "device_name";
	public static final String TOAST = "toast";
	  
	  
	// Message types sent from the BluetoothChatService Handler
	public static final int MESSAGE_STATE_CHANGE = 1;
	public static final int MESSAGE_READ = 2;
	public static final int MESSAGE_WRITE = 3;
	public static final int MESSAGE_DEVICE_NAME = 4;
	public static final int MESSAGE_TOAST = 5;
	
    EditText command_line;
    Button send_command;
    TextView msgWindow;
    String command_txt;

	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.terminal_frag);
		msgWindow = (TextView) findViewById(R.id.msgWindow);


		
		final ActionBar actionBar = getActionBar();
	  //  actionBar.setDisplayOptions(ActionBar.NAVIGATION_MODE_TABS);
	    actionBar.setDisplayShowTitleEnabled(true);
	    
	  // Get local Bluetooth adapter
	  mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
	  Log.d(TAG, "Adapter: " + mBluetoothAdapter);
	  
	  // If the adapter is null, then Bluetooth is not supported
	  if (mBluetoothAdapter == null) {
	      Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
	      finish();
	      return;
	  }

	    
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle presses on the action bar items
		Intent serverIntent = null;
		
	    switch (item.getItemId()) {
	    
	    case R.id.secure_connect_scan:
	        // Launch the DeviceListActivity to see devices and do scan
	        serverIntent = new Intent(this, DeviceListActivity.class);
	        startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_SECURE);
	        return true;
	        
	        
	    default:
            return super.onOptionsItemSelected(item);
	    }
	    
	}
	
	@Override
	public void onStart() {
	    super.onStart();
	    
    	command_line = (EditText) findViewById(R.id.command_line);
    	command_line.setInputType(InputType.TYPE_CLASS_TEXT);
    	command_line.setSingleLine();
    	addListenerOnButton();   

	    // If BT is not on, request that it be enabled.
	    // setupChat() will then be called during onActivityResult
	    if (!mBluetoothAdapter.isEnabled()) {
	        Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
	        startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
	    // Otherwise, setup the chat session
	    } else {
	        if (mChatService == null) setupChat();
	    }
	}

    private final void setStatus(int resId) {
        final ActionBar actionBar = getActionBar();
        actionBar.setSubtitle(resId);
    }

    private final void setStatus(CharSequence subTitle) {
        final ActionBar actionBar = getActionBar();
        actionBar.setSubtitle(subTitle);
    }	
	
	
	
	  private void setupChat() {
	        Log.d(TAG, "setupChat()");

	        // Initialize the array adapter for the conversation thread
	        mConversationArrayAdapter = new ArrayAdapter<String>(this, R.layout.message);
	        mConversationView = (ListView) findViewById(R.id.in);
	        mConversationView.setAdapter(mConversationArrayAdapter);


	        // Initialize the BluetoothChatService to perform bluetooth connections
	        mChatService = new BluetoothChatService(this, mHandler);

	        // Initialize the buffer for outgoing messages
	        mOutStringBuffer = new StringBuffer("");
	   
	       
	    }	
	  
	    public void sendMessage(String message) {
	        // Check that we're actually connected before trying anything
	        if (mChatService.getState() != BluetoothChatService.STATE_CONNECTED) {
	            Toast.makeText(this, "Not Connected", Toast.LENGTH_SHORT).show();
	            return;
	        }

	        // Check that there's actually something to send
	        if (message.length() > 0) {
	            // Get the message bytes and tell the BluetoothChatService to write
	            byte[] send = message.getBytes();
	            mChatService.write(send);

	            // Reset out string buffer to zero and clear the edit text field
	            mOutStringBuffer.setLength(0);
	            //mOutEditText.setText(mOutStringBuffer);
	        }
	    }
	  
	    public void onActivityResult(int requestCode, int resultCode, Intent data) {
	        
	        switch (requestCode) {
	        case REQUEST_CONNECT_DEVICE_SECURE:
	            // When DeviceListActivity returns with a device to connect
	            if (resultCode == Activity.RESULT_OK) {
	                connectDevice(data, true);
	            }
	            break;
	        case REQUEST_CONNECT_DEVICE_INSECURE:
	            // When DeviceListActivity returns with a device to connect
	            if (resultCode == Activity.RESULT_OK) {
	                connectDevice(data, false);
	            }
	            break;
	        case REQUEST_ENABLE_BT:
	            // When the request to enable Bluetooth returns
	            if (resultCode == Activity.RESULT_OK) {
	                // Bluetooth is now enabled, so set up a chat session
	                setupChat();
	            } else {
	                // User did not enable Bluetooth or an error occurred
	                Log.d(TAG, "BT not enabled");
	                Toast.makeText(this, "BT NOT ENABLED", Toast.LENGTH_SHORT).show();
	                finish();
	            }
	        }
	    }  
	  
	    private void connectDevice(Intent data, boolean secure) {
	        // Get the device MAC address
	        String address = data.getExtras()
	            .getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
	        // Get the BluetoothDevice object
	        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
	        // Attempt to connect to the device
	        mChatService.connect(device, secure);
	    }
	  
	  
	    private final Handler mHandler = new Handler() {
    		

	        @Override
	        public void handleMessage(Message msg) {
	        	
	        	String dataRecieved;
	        	
	        	
	            switch (msg.what) {
	            case MESSAGE_STATE_CHANGE:
	               
	                switch (msg.arg1) {
	                case BluetoothChatService.STATE_CONNECTED:
	                    setStatus(getString(R.string.title_connected_to, mConnectedDeviceName));
	                    mConversationArrayAdapter.clear();
	                    break;
	                case BluetoothChatService.STATE_CONNECTING:
	                    setStatus(R.string.title_connecting);
	                    break;
	                case BluetoothChatService.STATE_LISTEN:
	                case BluetoothChatService.STATE_NONE:
	                    setStatus(R.string.title_not_connected);
	                    break;
	                }
	                break;
	            case MESSAGE_WRITE:
	                break;
	            case MESSAGE_READ:
	            	//StringBuilder res = new StringBuilder();
	                byte[] readBuf = (byte[]) msg.obj;

	                // construct a string from the valid bytes in the buffer               
	                String readMessage = new String(readBuf, 0, msg.arg1);
	                dataRecieved = readMessage;
	                dataRecieved = dataRecieved.trim(); 
	        		msgWindow.append("\n" + "Response: " + dataRecieved + " ");

	                break;
	            case MESSAGE_DEVICE_NAME:
	                // save the connected device's name
	                mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
	                Toast.makeText(getApplicationContext(), "Connected to "
	                               + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
	                break;
	            case MESSAGE_TOAST:
	                Toast.makeText(getApplicationContext(), msg.getData().getString(TOAST),
	                               Toast.LENGTH_SHORT).show();
	                break;
	            }
	        }

			
	    };		  
	  


public void addListenerOnButton() {
		send_command = (Button) findViewById(R.id.send_command);
		send_command.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				command_txt = command_line.getText().toString();
		    	msgWindow.append("\n" + "Command: " + command_txt);
		    	command_line.setText("");
		    	sendMessage(command_txt + "\r");
			}
		});
	}
}
