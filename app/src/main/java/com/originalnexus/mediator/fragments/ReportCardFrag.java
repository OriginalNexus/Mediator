package com.originalnexus.mediator.fragments;

import android.graphics.Canvas;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import android.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.originalnexus.mediator.others.DataManager;
import com.originalnexus.mediator.activities.MainActivity;
import com.originalnexus.mediator.dialogs.NameDialog;
import com.originalnexus.mediator.R;
import com.originalnexus.mediator.adapters.SubjectAdapter;
import com.originalnexus.mediator.models.Subject;


public class ReportCardFrag extends Fragment implements NameDialog.NameDialogListener {

	private SubjectAdapter mSubjectAdapter;

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.frag_report_card, container, false);
	}

	@Override
	public void onActivityCreated(@Nullable final Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		if (getView() == null) return;

		getActivity().setTitle(R.string.frag_report_card_title);
		((MainActivity) getActivity()).setCheckedNavigationItem(R.id.menu_drawer_report_card);

		final CoordinatorLayout root = getView().findViewById(R.id.frag_report_card_container);
		final FloatingActionButton fab = getView().findViewById(R.id.frag_report_card_fab);
		final RecyclerView recyclerView = getView().findViewById(R.id.frag_report_card_subjects);

		fab.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				NameDialog dialog = NameDialog.newInstance("");
				dialog.setTargetFragment(ReportCardFrag.this, 0);
				dialog.show(getFragmentManager(), null);
			}
		});

		recyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), RecyclerView.VERTICAL, false));

		DataManager.loadSubjects(getActivity());
		mSubjectAdapter = new SubjectAdapter(DataManager.Subjects);
		mSubjectAdapter.setSubjectClickListener(new SubjectAdapter.SubjectClickListener() {
			@Override
			public void onClick(SubjectAdapter.SubjectViewHolder subjectViewHolder) {
				((MainActivity) getActivity()).openSubject(subjectViewHolder.getAdapterPosition());
			}
		});

		recyclerView.setAdapter(mSubjectAdapter);

		ItemTouchHelper ith = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN, ItemTouchHelper.RIGHT) {
			@Override
			public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
				mSubjectAdapter.swapSubjects(viewHolder.getAdapterPosition(), target.getAdapterPosition());
				DataManager.saveSubjects(getActivity());
				return true;
			}

			@Override
			public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
				mSubjectAdapter.removeSubject(viewHolder.getAdapterPosition());
				DataManager.saveSubjects(getActivity());
				Snackbar snack = Snackbar.make(root,
						String.format(getResources().getString(R.string.frag_report_card_snack_delete), ((SubjectAdapter.SubjectViewHolder) viewHolder).getSubjectName()),
						Snackbar.LENGTH_LONG);
				snack.setAction(R.string.frag_report_card_snack_undo, new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						mSubjectAdapter.undoRemove();
						DataManager.saveSubjects(getActivity());
					}
				});

				snack.show();
			}

			@Override
			public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
				super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
				// Custom look & feel
				if (isCurrentlyActive && actionState == ItemTouchHelper.ACTION_STATE_DRAG) {
					((SubjectAdapter.SubjectViewHolder) viewHolder).setRaised(true);
				}
				else if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
					((SubjectAdapter.SubjectViewHolder) viewHolder).setSwipeOpacity(dX);
				}

			}

			@Override
			public void clearView(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
				super.clearView(recyclerView, viewHolder);
				((SubjectAdapter.SubjectViewHolder) viewHolder).setRaised(false);
			}

		});

		ith.attachToRecyclerView(recyclerView);

		recyclerView.addItemDecoration(new DividerItemDecoration(recyclerView.getContext(), DividerItemDecoration.VERTICAL));
	}

	@Override
	public void onNameDialogConfirm(String input) {
		Subject s = new Subject(input);
		mSubjectAdapter.addSubject(s);
		DataManager.saveSubjects(getActivity());
	}

}
