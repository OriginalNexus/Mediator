package com.originalnexus.mediator;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;

/**
 * About Dialog
 */
public class AboutDialog extends DialogFragment {
	@NonNull
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

		// Set version in text
		String msg = getString(R.string.about_dialog_content);
		try {
			msg = msg.replace("{version}", getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0).versionName);
		}
		catch (PackageManager.NameNotFoundException e) {
			e.printStackTrace();
		}

		// Create view
		TextView mView = new TextView(getActivity());
		mView.setMovementMethod(LinkMovementMethod.getInstance());
		int px = (int) (16 * getResources().getDisplayMetrics().density + 0.5);
		mView.setPadding(px,px,px,px);
		mView.setText(Html.fromHtml(msg));
		// On android level < 11 background of dialog is always black so we make the text white
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
			mView.setTextColor(getResources().getColor(R.color.text_dark));
		}

		builder.setTitle(R.string.about_dialog_title)
				.setView(mView)
				.setPositiveButton(R.string.about_dialog_button, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
		return builder.create();
	}

}
