package com.originalnexus.mediator;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.ScrollView;
import android.widget.TextView;

/**
 * Help Dialog
 */
public class HelpDialog extends DialogFragment {
	@NonNull
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle(R.string.help_dialog_title)
				.setPositiveButton(R.string.help_dialog_button, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
		TextView message = new TextView(getActivity());
		message.setText(R.string.help_dialog_content);
		message.setTextSize(14);
		// On android level < 11 background of dialog is always black so we make the text white
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
			message.setTextColor(getResources().getColor(R.color.text_dark));
		}

		ScrollView rootView = new ScrollView(getActivity());
		rootView.addView(message);
		int px = (int) (16 * getResources().getDisplayMetrics().density + 0.5);
		rootView.setPadding(px,px,px,px);
		builder.setView(rootView);
		return builder.create();
	}

}
