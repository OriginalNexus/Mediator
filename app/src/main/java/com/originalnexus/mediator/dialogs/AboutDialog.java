package com.originalnexus.mediator.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;

import com.originalnexus.mediator.R;

/**
 * About Dialog
 */
public class AboutDialog extends DialogFragment {
	@NonNull
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

		// Set version in text
		String msg = getString(R.string.dialog_about_content);
		try {
			msg = String.format(msg, getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0).versionName);
		}
		catch (PackageManager.NameNotFoundException e) {
			e.printStackTrace();
		}

		// Create view
		TextView mView = new TextView(getActivity());
		mView.setMovementMethod(LinkMovementMethod.getInstance());
		int px = (int) getResources().getDimension(R.dimen.dialog_padding);
		mView.setPadding(px, px, px, px);
		mView.setText(fromHtml(msg));

		builder.setTitle(R.string.dialog_about_title)
				.setView(mView)
				.setPositiveButton(R.string.dialog_about_button, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});

		return builder.create();
	}

	private Spanned fromHtml(String s) {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
			//noinspection deprecation
			return Html.fromHtml(s);
		}
		else {
			return Html.fromHtml(s, Html.FROM_HTML_MODE_LEGACY);
		}
	}

}
