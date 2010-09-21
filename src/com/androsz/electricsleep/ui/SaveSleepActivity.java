package com.androsz.electricsleep.ui;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;

public class SaveSleepActivity extends ReviewSleepActivity {

	@Override
	// @SuppressWarnings("unchecked")
	protected void onCreate(Bundle savedInstanceState) {
		// final Uri uri = getIntent().getData();
		final AlertDialog.Builder dialog = new AlertDialog.Builder(this)
				.setMessage("Do you wish to save this sleep history?")
				.setCancelable(false).setPositiveButton("Yes",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
							}
						}).setNegativeButton("No",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {

								dialog.cancel();
								return;
							}
						});
		dialog.show();
		//super.onCreate(savedInstanceState);
	}
}
