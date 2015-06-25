package org.example.mediator;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.ScrollView;
import android.widget.TextView;

/**
 * Help Dialog
 */
public class HelpDialog extends DialogFragment {
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
		ScrollView rootView = new ScrollView(getActivity());
		rootView.addView(message);
		int px = (int) (16 * getResources().getDisplayMetrics().density + 0.5);
		rootView.setPadding(px,px,px,px);
		builder.setView(rootView);
		return builder.create();
	}

}
