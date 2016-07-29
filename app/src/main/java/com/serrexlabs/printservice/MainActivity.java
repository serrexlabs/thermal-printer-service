
package com.serrexlabs.printservice;

import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import com.serrexlabs.printservice.util.PrintBitmap;
import com.serrexlabs.printservice.util.PrintUtils;
import com.zj.btsdk.BluetoothService;

import java.io.File;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private static final String TAG_BT = "BTService";
    private AlertDialog.Builder builderSingle;
    BluetoothDevice con_dev;
    BluetoothService btService;
    ArrayAdapter<String> mPairedDevices;
    boolean isConnected = false;
    String filePath = null;
    Bitmap printData = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btService = new BluetoothService(this, mHandler);
        filePath=  getIntent().getStringExtra("FILE");
        if (filePath != null) {
            ImageView imageView = (ImageView) findViewById(R.id.imageView);
            PrintUtils pu = new PrintUtils(filePath);
            printData = pu.getBitmapImages();
            imageView.setImageBitmap(printData);
            new File(filePath).delete();
        }

        mPairedDevices = new ArrayAdapter<String>(MainActivity.this,
                android.R.layout.simple_list_item_1, android.R.id.text1);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);
        initDeviceListDialog();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isConnected) {
                    print();
                    return;
                }
                builderSingle.show();
            }
        });

        Set<BluetoothDevice> pairedDevices = btService.getPairedDev();

        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                mPairedDevices.add(device.getName() + "\n" + device.getAddress());

            }
        } else {
            mPairedDevices.add("No printers");
        }

    }

    private void initDeviceListDialog() {
        builderSingle = new AlertDialog.Builder(MainActivity.this);
        builderSingle.setIcon(android.R.drawable.ic_btn_speak_now);
        builderSingle.setTitle("Select Printer:-");

        builderSingle.setNegativeButton(
                "cancel",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        builderSingle.setAdapter(
                mPairedDevices,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String info = mPairedDevices.getItem(which);
                        String address = info.substring(info.length() - 17);
                        con_dev = btService.getDevByMac(address);
                        btService.connect(con_dev);
                    }
                });

    }

    private void print() {
        byte[] sendData = null;
        PrintBitmap pg = new PrintBitmap();
        pg.initCanvas(384);
        pg.initPaint();
        pg.drawImage(0, 0, printData);
        sendData = pg.printDraw();
        btService.write(sendData);
    }

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case BluetoothService.MESSAGE_STATE_CHANGE:
                    if (msg.arg1 == BluetoothService.STATE_CONNECTED) {
                            print();
                        isConnected = true;
                            break;
                    }
                    break;
                case BluetoothService.MESSAGE_UNABLE_CONNECT:
                    Log.d(TAG_BT, "Unable to connect device");
                    break;
            }
        }

    };


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (btService != null) {
            btService.cancelDiscovery();
        }
        btService = null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }
}
