package com.originalnexus.mediator;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;

public class NameDialog extends DialogFragment {

	private final static String STATE_STRING = "string1";
	private final static String STATE_REQ_CODE = "req_code";

	private String mString = "";
	private int reqCode;
	private NameDialogListener mCallback;

	public interface NameDialogListener {
		void onNameDialogConfirm(int requestCode, String input);
	}


	public static NameDialog newInstance(int requestCode, String initialString) {
		NameDialog f = new NameDialog();
		f.mString = initialString;
		f.reqCode = requestCode;
		return f;
	}

	@NonNull
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {

		// Restore state
		if (savedInstanceState != null) {
			mString = savedInstanceState.getString(STATE_STRING);
			reqCode = savedInstanceState.getInt(STATE_REQ_CODE);
		}

		// Create edit text
		final EditText editT = new EditText(getActivity());
		editT.setText(mString);
		editT.setSelection(mString.length());
		editT.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {

			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {

			}

			@Override
			public void afterTextChanged(Editable s) {
				mString = s.toString();

				// Make sure input is not blank
				if (mString.equals("") || mString.replaceAll("\\s", "").equals("")) {
					((AlertDialog) getDialog()).getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
				}
				else {
					((AlertDialog) getDialog()).getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
				}
			}
		});

		// Create root view
		FrameLayout rootView = new FrameLayout(getActivity());
		int px = (int) (16 * getResources().getDisplayMetrics().density + 0.5);
		rootView.setPadding(px, px, px, px);
		rootView.addView(editT);

		// Create the builder
		AlertDialog.Builder b = new AlertDialog.Builder(getActivity());
		b
			.setView(rootView)
			.setTitle(R.string.name_dialog_title)
			.setPositiveButton(R.string.name_dialog_ok, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					mCallback.onNameDialogConfirm(reqCode, mString);
					dialog.dismiss();
				}
			})
			.setNegativeButton(R.string.name_dialog_cancel, new DialogInterface.OnClickListener() {
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
				// Make sure input is not blank
				if (mString.equals("") || mString.replaceAll("\\s", "").equals("")) {
					((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
				}

				// Open keyboard
				InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.showSoftInput(editT, InputMethodManager.SHOW_IMPLICIT);
			}
		});

		return dialog;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putString(STATE_STRING, mString);
		outState.putInt(STATE_REQ_CODE, reqCode);
		super.onSaveInstanceState(outState);
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			mCallback = (NameDialogListener) activity;
		}
		catch (ClassCastException e) {
			throw new ClassCastException(activity.toString() + " must implement NameDialogListener.");
		}
	}
}
