package com.originalnexus.mediator.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import androidx.annotation.NonNull;
import android.app.DialogFragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;

import com.originalnexus.mediator.R;

public class NameDialog extends DialogFragment {

	public interface NameDialogListener {
		void onNameDialogConfirm(String input);
	}

	private final static String STATE_TEXT = "text";
	private String mText = "";


	public static NameDialog newInstance(String initialString) {
		NameDialog f = new NameDialog();
		f.mText = initialString;
		return f;
	}

	@NonNull
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {

		// Restore state
		if (savedInstanceState != null) {
			mText = savedInstanceState.getString(STATE_TEXT);
		}

		// Create edit mText
		final EditText editT = new EditText(getActivity());
		editT.setText(mText);
		editT.setSelection(mText.length());
		editT.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {}

			@Override
			public void afterTextChanged(Editable s) {
				mText = s.toString();
				updatePositiveButton();
			}
		});

		// Create root view
		FrameLayout rootView = new FrameLayout(getActivity());
		int px = (int) getResources().getDimension(R.dimen.dialog_padding);
		rootView.setPadding(px, px, px, px);
		rootView.addView(editT);

		// Create the builder
		AlertDialog.Builder b = new AlertDialog.Builder(getActivity());
		b
				.setView(rootView)
				.setTitle(R.string.dialog_name_title)
				.setPositiveButton(R.string.dialog_name_ok, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						try {
							((NameDialogListener) getTargetFragment()).onNameDialogConfirm(mText);
						}
						catch (ClassCastException ignored) {}

						dialog.dismiss();
					}
				})
				.setNegativeButton(R.string.dialog_name_cancel, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.cancel();
				}
			});

		// Create the dialog
		AlertDialog dialog = b.create();
		dialog.setOnShowListener(new DialogInterface.OnShowListener() {
			@Override
			public void onShow(DialogInterface dialog) {
				updatePositiveButton();

				// Focus edit mText
				editT.requestFocus();

				// Open keyboard
				InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
				if (imm != null) imm.showSoftInput(editT, InputMethodManager.SHOW_IMPLICIT);
			}
		});

		return dialog;
	}

	private void updatePositiveButton() {
		// If input is blank disable positive button
		if (mText.equals("") || mText.replaceAll("\\s", "").equals("")) {
			((AlertDialog) getDialog()).getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
		}
		else {
			((AlertDialog) getDialog()).getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putString(STATE_TEXT, mText);
		super.onSaveInstanceState(outState);
	}
}
