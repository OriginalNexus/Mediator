package com.originalnexus.mediator.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import androidx.annotation.NonNull;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.ScrollView;
import android.widget.TextView;

import com.originalnexus.mediator.R;

/**
 * Help Dialog
 */
public class HelpDialog extends DialogFragment {
	@NonNull
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle(R.string.dialog_help_title)
				.setPositiveButton(R.string.dialog_help_button, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
		TextView message = new TextView(getActivity());
		message.setText(R.string.dialog_help_content);
		message.setTextSize(14);

		ScrollView rootView = new ScrollView(getActivity());
		rootView.addView(message);
		int px = (int) getResources().getDimension(R.dimen.dialog_padding);
		rootView.setPadding(px, px, px, px);
		builder.setView(rootView);
		return builder.create();
	}

}
