/*
 * Copyright (c) 2014-2021, Digi International Inc. <support@digi.com>
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

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.digi.android.cloudconnector.BinaryDataPoint;
import com.digi.android.cloudconnector.CloudConnectorManager;
import com.digi.android.cloudconnector.DataPoint;
import com.digi.android.cloudconnector.DataStream;

import java.util.ArrayList;

class DataPointDialog {

	// Constants.
	private final static String DATA_POINT_PREFIX = "CLOUD_CONNECTOR_TEST";
	private final static String DATA_POINT_INTEGER = DATA_POINT_PREFIX + "/INTEGER";
	private final static String DATA_POINT_LONG = DATA_POINT_PREFIX + "/LONG";
	private final static String DATA_POINT_FLOAT = DATA_POINT_PREFIX + "/FLOAT";
	private final static String DATA_POINT_DOUBLE = DATA_POINT_PREFIX + "/DOUBLE";
	private final static String DATA_POINT_STRING = DATA_POINT_PREFIX + "/STRING";
	private final static String DATA_POINT_BYTE_ARRAY = DATA_POINT_PREFIX + "/BINARY";
	private final static String DATA_POINT_BINARY = DATA_POINT_PREFIX + "/BINARY_RAW";

	private final static int DATA_POINT_TYPE_INT = 0;
	private final static int DATA_POINT_TYPE_LONG = 1;
	private final static int DATA_POINT_TYPE_FLOAT = 2;
	private final static int DATA_POINT_TYPE_DOUBLE = 3;
	private final static int DATA_POINT_TYPE_STRING = 4;
	private final static int DATA_POINT_TYPE_BINARY_64 = 5;
	private final static int DATA_POINT_TYPE_BINARY_RAW = 6;

	// Variables.
	private final Context context;

	private View dataPointDialogView;

	private AlertDialog dataPointDialog;

	private final CloudConnectorManager connectorManager;

	private TextView statusText;
	private TextView dataPointsNumberLabelText;

	private EditText valueText;

	private NumberPicker numberDataPoints;

	private Spinner dataPointTypeSpinner;

	/**
	 * Class constructor. Instantiates a new {@code DataPointDialog} using the given parameters.
	 *
	 * @param context The Android application context.
	 * @param connectorManager The Cloud Connector manager.
	 */
	DataPointDialog(Context context, CloudConnectorManager connectorManager) {
		this.context = context;
		this.connectorManager = connectorManager;

		// Setup the layout.
		setupLayout();
	}

	/**
	 * Displays the data point dialog.
	 */
	void show() {
		createDialog();

		dataPointDialog.show();

		dataPointDialog.getButton(AlertDialog.BUTTON_POSITIVE).setFocusable(true);
		dataPointDialog.getButton(AlertDialog.BUTTON_POSITIVE).requestFocus();
	}

	/**
	 * Configures the layout of the data point dialog.
	 */
	private void setupLayout() {
		// Create the layout.
		LayoutInflater layoutInflater = LayoutInflater.from(context);

		dataPointDialogView = layoutInflater.inflate(R.layout.data_point_dialog, null);

		// Initialize dialog controls.
		initializeControls();
	}

	/**
	 * Initializes the dialog controls.
	 */
	private void initializeControls() {
		// Get the status text.
		statusText = dataPointDialogView.findViewById(R.id.status_text);
		// Get the data points type spinner.
		dataPointTypeSpinner = dataPointDialogView.findViewById(R.id.data_point_type_spinner);
		dataPointTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
				handleDataPointTypeChanged();
			}

			@Override
			public void onNothingSelected(AdapterView<?> parentView) {
			}
		});
		ArrayAdapter<String> dataPointTypesAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, context.getResources().getStringArray(R.array.data_point_types));
		dataPointTypesAdapter.setDropDownViewResource(android.R.layout.select_dialog_singlechoice);
		dataPointTypeSpinner.setAdapter(dataPointTypesAdapter);
		dataPointTypeSpinner.setSelection(0);

		// Get the value text.
		valueText = dataPointDialogView.findViewById(R.id.value_text);
		valueText.addTextChangedListener(textWatcher);
		// Get the number picker controls.
		dataPointsNumberLabelText = dataPointDialogView.findViewById(R.id.data_points_number_label);
		numberDataPoints = dataPointDialogView.findViewById(R.id.data_points_number);
		numberDataPoints.setValue(1);
		numberDataPoints.getValueEditText().addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {

			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				validateDialog();
			}

			@Override
			public void afterTextChanged(Editable s) {
				validateDialog();
			}
		});
	}

	/**
	 * Creates the alert dialog that will be displayed.
	 */
	private void createDialog() {
		// Setup the dialog window.
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
		alertDialogBuilder.setView(dataPointDialogView);
		alertDialogBuilder.setTitle(R.string.title_data_point);
		alertDialogBuilder.setCancelable(false);
		alertDialogBuilder.setPositiveButton(R.string.button_send, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int id) {
				sendDataPoint();
				synchronized (DataPointDialog.this) {
					DataPointDialog.this.notify();
				}
			}
		});
		alertDialogBuilder.setNegativeButton(R.string.button_close, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int id) {
				dialog.cancel();
				synchronized (DataPointDialog.this) {
					DataPointDialog.this.notify();
				}
			}
		});
		// Create the dialog.
		dataPointDialog = alertDialogBuilder.create();
	}

	/**
	 * Handles what happens when the data point type spinner selection changes.
	 */
	private void handleDataPointTypeChanged() {
		dataPointsNumberLabelText.setVisibility(View.VISIBLE);
		numberDataPoints.setVisibility(View.VISIBLE);
		validateDialog();
	}

	/**
	 * Validates the dialog setting the corresponding configuration error.
	 */
	private void validateDialog() {
		String errorMessage = null;

		String value = valueText.getText().toString();
		if (value.trim().length() == 0) {
			errorMessage = "Value cannot be empty.";
		} else {
			try {
				switch (dataPointTypeSpinner.getSelectedItemPosition()) {
					case DATA_POINT_TYPE_INT:
						Integer.valueOf(valueText.getText().toString());
						break;
					case DATA_POINT_TYPE_LONG:
						Long.valueOf(valueText.getText().toString());
						break;
					case DATA_POINT_TYPE_FLOAT:
						Float.valueOf(valueText.getText().toString());
						break;
					case DATA_POINT_TYPE_DOUBLE:
						Double.valueOf(valueText.getText().toString());
						break;
					case DATA_POINT_TYPE_STRING:
					case DATA_POINT_TYPE_BINARY_64:
						break;
					case DATA_POINT_TYPE_BINARY_RAW:
					default:
						dataPointsNumberLabelText.setVisibility(View.GONE);
						numberDataPoints.setVisibility(View.GONE);
				}
			} catch (Exception e) {
				errorMessage = "Value is not valid for the specified type.";
			}
		}

		if (errorMessage == null) {
			if (numberDataPoints.getValue() < 1)
				errorMessage = "At least one data point must be sent to Remote Manager.";
			else if (numberDataPoints.getValue() > CloudConnectorManager.MAXIMUM_DATA_POINTS)
				errorMessage = "Only " + CloudConnectorManager.MAXIMUM_DATA_POINTS + " data points can be sent at once.";
		}

		if (errorMessage != null) {
			statusText.setError(errorMessage);
			statusText.setText(errorMessage);
			statusText.setTextColor(context.getResources().getColor(R.color.red));
			dataPointDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
		} else {
			statusText.setError(null);
			statusText.setText(context.getResources().getString(R.string.description_data_point));
			statusText.setTextColor(context.getResources().getColor(R.color.black));
			dataPointDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
		}
	}

	/**
	 * Sends the selected data point to Remote Manager.
	 */
	private void sendDataPoint() {
		DataStream dataStream;
		DataPoint dataPoint = null;
		BinaryDataPoint binaryDataPoint = null;
		switch (dataPointTypeSpinner.getSelectedItemPosition()) {
			case DATA_POINT_TYPE_INT:
				dataStream = new DataStream(DATA_POINT_INTEGER);
				dataPoint = new DataPoint(Integer.parseInt(valueText.getText().toString()), dataStream);
				break;
			case DATA_POINT_TYPE_LONG:
				dataStream = new DataStream(DATA_POINT_LONG);
				dataPoint = new DataPoint(Long.parseLong(valueText.getText().toString()), dataStream);
				break;
			case DATA_POINT_TYPE_FLOAT:
				dataStream = new DataStream(DATA_POINT_FLOAT);
				dataPoint = new DataPoint(Float.parseFloat(valueText.getText().toString()), dataStream);
				break;
			case DATA_POINT_TYPE_DOUBLE:
				dataStream = new DataStream(DATA_POINT_DOUBLE);
				dataPoint = new DataPoint(Double.parseDouble(valueText.getText().toString()), dataStream);
				break;
			case DATA_POINT_TYPE_STRING:
				dataStream = new DataStream(DATA_POINT_STRING);
				dataPoint = new DataPoint(valueText.getText().toString(), dataStream);
				break;
			case DATA_POINT_TYPE_BINARY_64:
				dataStream = new DataStream(DATA_POINT_BYTE_ARRAY);
				dataPoint = new DataPoint(valueText.getText().toString().getBytes(), dataStream);
				break;
			case DATA_POINT_TYPE_BINARY_RAW:
			default:
				dataStream = new DataStream(DATA_POINT_BINARY);
				binaryDataPoint = new BinaryDataPoint(valueText.getText().toString().getBytes(), dataStream);
		}
		if (dataPoint != null) {
			int numberOfDataPoints = numberDataPoints.getValue();
			ArrayList<DataPoint> dataPoints = new ArrayList<>();
			for (int i = 0; i < numberOfDataPoints; i++)
				dataPoints.add(dataPoint);
			connectorManager.sendDataPoints(dataPoints);
		} else
			connectorManager.sendBinaryDataPoint(binaryDataPoint);
	}

	private final TextWatcher textWatcher = new TextWatcher() {
		@Override
		public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

		@Override
		public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

		@Override
		public void afterTextChanged(Editable editable) {
			validateDialog();
		}
	};
}