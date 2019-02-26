package com.originalnexus.mediator.dialogs;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import androidx.annotation.NonNull;
import android.app.DialogFragment;
import android.app.AlertDialog;

import com.originalnexus.mediator.others.DataManager;
import com.originalnexus.mediator.R;

/**
 * Delete dialog
 */
public class DeleteDialog extends DialogFragment {

	public interface DeleteDialogListener {
		void onDeleteSubject(int index);
	}

	private final static String STATE_SUBJECT_INDEX = "subjectIndex";
	private int mSubjectIndex;

	public static DeleteDialog newInstance(int index) {
		DeleteDialog f = new DeleteDialog();
		f.mSubjectIndex = index;
		return f;
	}

	@NonNull
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		// Restore state
		if (savedInstanceState != null) {
			mSubjectIndex = savedInstanceState.getInt(STATE_SUBJECT_INDEX);
		}

		AlertDialog.Builder b = new AlertDialog.Builder(getActivity());
		b
				.setMessage(String.format(getResources().getString(R.string.dialog_delete_text), DataManager.Subjects.get(mSubjectIndex).name))
				.setPositiveButton(getResources().getText(R.string.yes), new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						try {
							((DeleteDialogListener) getTargetFragment()).onDeleteSubject(mSubjectIndex);
						}
						catch (ClassCastException ignored) {}

						dialog.dismiss();
					}
				})
				.setNegativeButton(getResources().getText(R.string.no), new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});

		return b.create();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putInt(STATE_SUBJECT_INDEX, mSubjectIndex);
		super.onSaveInstanceState(outState);
	}

}
