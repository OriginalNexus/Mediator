package com.originalnexus.mediator.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import com.originalnexus.mediator.fragments.ReportCardFrag;
import com.originalnexus.mediator.R;

/**
 * Simple yes/no delete dialog
 */
public class DeleteDialog extends DialogFragment {

	private final static String STATE_INDEX = "delete_index";

	private DeleteDialogListener mCallback;
	private int sIndex;


	public static DeleteDialog newInstance(int index) {
		DeleteDialog f = new DeleteDialog();
		f.sIndex = index;
		return f;
	}

	@NonNull
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		// Restore state
		if (savedInstanceState != null) {
			sIndex = savedInstanceState.getInt(STATE_INDEX);
		}

		AlertDialog.Builder b = new AlertDialog.Builder(getActivity());
		b
				.setMessage(String.format(getResources().getString(R.string.delete_dialog_text), ReportCardFrag.sAdapter.subjects.get(sIndex).name))
				.setPositiveButton(getResources().getText(R.string.answer_yes), new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						mCallback.onDeleteSubject(sIndex);
						dismiss();
					}
				})
				.setNegativeButton(getResources().getText(R.string.answer_no), new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dismiss();
					}
				});

		return b.create();
	}

	public interface DeleteDialogListener {
		/**
		 * Called when a subject needs to be deleted
		 * @param index the index of the deleted subject
		 */
		void onDeleteSubject(int index);
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			mCallback = (DeleteDialogListener) activity;
		}
		catch (ClassCastException e) {
			throw new ClassCastException(activity.toString() + " must implement DeleteDialogListener.");
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putInt(STATE_INDEX, sIndex);
		super.onSaveInstanceState(outState);
	}
}
