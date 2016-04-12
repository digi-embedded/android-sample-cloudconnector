/**
 * Copyright (c) 2014-2016, Digi International Inc. <support@digi.com>
 *
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 *
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
 * ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
 * OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */

package com.digi.android.sample.cloudconnector;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.digi.android.cloudconnector.CloudConnectorManager;
import com.digi.android.cloudconnector.CloudConnectorPreferencesManager;
import com.digi.android.cloudconnector.ICloudConnectorEventListener;
import com.digi.android.cloudconnector.IDeviceRequestListener;

public class CloudConnectorSampleActivity extends Activity {

    // Constants.
    private final static String DEVICE_REQUEST_TAG = "cloud_connector_sample";

    private final static String DATAPOINTS_SEND_SUCCESS = "Datapoints successfully sent";

    private final static boolean SHOW_RECONNECT = false;

    // Variables.
    private CloudConnectorManager connectorManager;

    private CloudConnectorPreferencesManager preferencesManager;

    private Switch connectSwitch;

    private TextView statusText;
    private TextView deviceIDText;
    private TextView reconnectTimeLabel;

    private EditText deviceNameText;
    private EditText deviceDescriptionText;
    private EditText contactText;
    private EditText vendorIDText;
    private EditText urlText;
    private EditText reconnectTimeText;

    private CheckBox autoStartCheckbox;
    private CheckBox reconnectCheckbox;
    private CheckBox secureConnectionCheckbox;
    private CheckBox compressCheckbox;

    private Button saveButton;
    private Button refreshButton;
    private Button datapointsButton;

    private ICloudConnectorEventListener eventListener;

    private IDeviceRequestListener deviceRequestListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_layout);
        // Initialize variables.
        connectorManager = new CloudConnectorManager(this);
        preferencesManager = connectorManager.getPreferencesManager();
        initializeEventListener();
        initializeDeviceRequestListener();
        // Initialize interface.
        initializeUIComponents();
    }

    @Override
    protected void onResume() {
        super.onResume();

        connectorManager.registerEventListener(eventListener);
        connectorManager.registerDeviceRequestListener(DEVICE_REQUEST_TAG, deviceRequestListener);

        updateInterface();
        connectSwitch.setOnCheckedChangeListener(connectCheckedListener);
    }

    @Override
    protected void onPause() {
        super.onPause();

        connectorManager.unregisterEventListener(eventListener);
        connectorManager.unregisterDeviceRequestListener(deviceRequestListener);
    }

    /**
     * Initializes the user interface components.
     */
    private void initializeUIComponents() {
        connectSwitch = (Switch)findViewById(R.id.connector_switch);

        statusText = (TextView)findViewById(R.id.connector_status);
        deviceIDText = (TextView)findViewById(R.id.device_id);
        reconnectTimeLabel = (TextView)findViewById(R.id.reconnect_time_label);

        deviceNameText = (EditText)findViewById(R.id.device_name);
        deviceDescriptionText = (EditText)findViewById(R.id.device_description);
        contactText = (EditText)findViewById(R.id.contact);
        vendorIDText = (EditText)findViewById(R.id.vendor_id);
        urlText = (EditText)findViewById(R.id.url);
        reconnectTimeText = (EditText)findViewById(R.id.reconnect_time);

        autoStartCheckbox = (CheckBox)findViewById(R.id.auto_connect);
        reconnectCheckbox = (CheckBox)findViewById(R.id.reconnect);
        reconnectCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                reconnectTimeText.setEnabled(isChecked);
                if (isChecked)
                    reconnectTimeLabel.setTextColor(getResources().getColor(R.color.black));
                else
                    reconnectTimeLabel.setTextColor(getResources().getColor(R.color.light_gray));
            }
        });
        secureConnectionCheckbox = (CheckBox)findViewById(R.id.secure_connection);
        compressCheckbox = (CheckBox)findViewById(R.id.compression);

        saveButton = (Button)findViewById(R.id.save);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleSavePressed();
            }
        });
        datapointsButton = (Button)findViewById(R.id.datapoints);
        datapointsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleDataPointsPressed();
            }
        });
        refreshButton = (Button)findViewById(R.id.refresh);
        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleRefreshPressed();
            }
        });

        if (!SHOW_RECONNECT) {
            reconnectTimeLabel.setVisibility(View.GONE);
            reconnectTimeText.setVisibility(View.GONE);
            reconnectCheckbox.setVisibility(View.GONE);
        }
    }

    /**
     * Updates the user interface by reading current connector
     * status and preferences.
     */
    private void updateInterface() {
        if (connectorManager.isConnected()) {
            connectSwitch.setEnabled(true);
            connectSwitch.setChecked(true);
            statusText.setText(getResources().getString(R.string.status_connected));
            statusText.setTextColor(getResources().getColor(R.color.green));
            datapointsButton.setEnabled(true);
        } else {
            connectSwitch.setEnabled(true);
            connectSwitch.setChecked(false);
            statusText.setText(getResources().getString(R.string.status_disconnected));
            statusText.setTextColor(getResources().getColor(R.color.red));
            datapointsButton.setEnabled(false);
        }
        deviceIDText.setText(connectorManager.getDeviceID());
        deviceNameText.setText(preferencesManager.getDeviceName());
        deviceDescriptionText.setText(preferencesManager.getDeviceDescription());
        contactText.setText(preferencesManager.getDeviceContactInformation());
        vendorIDText.setText(preferencesManager.getVendorID());
        urlText.setText(preferencesManager.getURL());
        reconnectTimeText.setText("" + preferencesManager.getReconnectTime());
        autoStartCheckbox.setChecked(preferencesManager.isAutoConnectEnabled());
        boolean reconnectEnabled = preferencesManager.isReconnectEnabled();
        reconnectCheckbox.setChecked(reconnectEnabled);
        reconnectTimeText.setEnabled(reconnectEnabled);
        if (reconnectEnabled)
            reconnectTimeLabel.setTextColor(getResources().getColor(R.color.black));
        else
            reconnectTimeLabel.setTextColor(getResources().getColor(R.color.light_gray));
        secureConnectionCheckbox.setChecked(preferencesManager.isSecureConnectionEnabled());
        compressCheckbox.setChecked(preferencesManager.isCompressionEnabled());
    }

    /**
     * Handles what happens when the connect switch is pressed.
     *
     * @param connect {@code true} to connect, {@code false} otherwise.
     */
    private void handleConnectPressed(boolean connect) {
        connectSwitch.setEnabled(false);
        if (connect) {
            statusText.setText(getResources().getString(R.string.status_connecting));
            statusText.setTextColor(getResources().getColor(R.color.light_gray));
            connectorManager.connect();
        } else {
            statusText.setText(getResources().getString(R.string.status_disconnecting));
            statusText.setTextColor(getResources().getColor(R.color.light_gray));
            connectorManager.disconnect();
        }
    }

    /**
     * Handles what happens when the refresh button is pressed.
     */
    private void handleRefreshPressed() {
        updateInterface();
    }

    /**
     * Handles what happens when the save button is pressed.
     */
    private void handleSavePressed() {
        try {
            preferencesManager.setDeviceName(deviceNameText.getText().toString());
            preferencesManager.setDeviceDescription(deviceDescriptionText.getText().toString());
            preferencesManager.setDeviceContactInformation(contactText.getText().toString());
            preferencesManager.setVendorID(vendorIDText.getText().toString());
            preferencesManager.setURL(urlText.getText().toString());
            preferencesManager.setAutoConnectEnabled(autoStartCheckbox.isChecked());
            preferencesManager.setReconnectEnabled(reconnectCheckbox.isChecked());
            if (reconnectCheckbox.isChecked())
                preferencesManager.setReconnectTime(Integer.parseInt(reconnectTimeText.getText().toString()));
            preferencesManager.setSecureConnectionEnabled(secureConnectionCheckbox.isChecked());
            preferencesManager.setCompressionEnabled(compressCheckbox.isChecked());
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error writing setting: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Handles what happens when the data points button is pressed.
     */
    private void handleDataPointsPressed() {
        DataPointDialog dataPointDialog = new DataPointDialog(this, connectorManager);
        dataPointDialog.show();
    }

    /**
     * Initializes the connector event listener.
     */
    private void initializeEventListener() {
        eventListener = new ICloudConnectorEventListener() {
            @Override
            public void connected() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        connectSwitch.setEnabled(true);
                        connectSwitch.setChecked(true);
                        statusText.setText(getResources().getString(R.string.status_connected));
                        statusText.setTextColor(getResources().getColor(R.color.green));
                        datapointsButton.setEnabled(true);
                    }
                });
            }

            @Override
            public void disconnected() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        connectSwitch.setEnabled(true);
                        connectSwitch.setChecked(false);
                        statusText.setText(getResources().getString(R.string.status_disconnected));
                        statusText.setTextColor(getResources().getColor(R.color.red));
                        datapointsButton.setEnabled(false);
                    }
                });
            }

            @Override
            public void connectionError(final String s) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        connectSwitch.setEnabled(true);
                        connectSwitch.setChecked(false);
                        statusText.setText(getResources().getString(R.string.status_disconnected));
                        statusText.setTextColor(getResources().getColor(R.color.red));
                        datapointsButton.setEnabled(false);
                        Toast.makeText(CloudConnectorSampleActivity.this, s, Toast.LENGTH_LONG).show();
                    }
                });
            }

            @Override
            public void sendDataPointsSuccess() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(CloudConnectorSampleActivity.this, DATAPOINTS_SEND_SUCCESS, Toast.LENGTH_LONG).show();
                    }
                });
            }

            @Override
            public void sendDataPointsError(final String errorMessage) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(CloudConnectorSampleActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                    }
                });
            }
        };
    }

    /**
     * Initializes the device request listener.
     */
    private void initializeDeviceRequestListener() {
        deviceRequestListener = new IDeviceRequestListener() {
            @Override
            public String handleDeviceRequest(String s, byte[] bytes) {
                Toast.makeText(CloudConnectorSampleActivity.this, "Device Request for target " + s + " - Data: " + new String(bytes), Toast.LENGTH_LONG).show();
                return "Success";
            }

            @Override
            public String handleDeviceRequest(String s, String s1) {
                Toast.makeText(CloudConnectorSampleActivity.this, "Device Request for target " + s + " - Data: " + s1, Toast.LENGTH_LONG).show();
                return "Success";
            }
        };
    }

    private CompoundButton.OnCheckedChangeListener connectCheckedListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            handleConnectPressed(isChecked);
        }
    };
}