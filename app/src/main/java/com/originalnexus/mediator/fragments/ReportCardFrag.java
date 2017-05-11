package com.originalnexus.mediator.fragments;

import android.graphics.Canvas;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
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

		final CoordinatorLayout root = (CoordinatorLayout) getView().findViewById(R.id.frag_report_card_container);
		final FloatingActionButton fab = (FloatingActionButton) getView().findViewById(R.id.frag_report_card_fab);
		final RecyclerView recyclerView = (RecyclerView) getView().findViewById(R.id.frag_report_card_subjects);

		fab.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				NameDialog dialog = NameDialog.newInstance("");
				dialog.setTargetFragment(ReportCardFrag.this, 0);
				dialog.show(getFragmentManager(), null);
			}
		});

		// Workaround for https://code.google.com/p/android/issues/detail?id=221387
		fab.post(new Runnable() {
			@Override
			public void run() {
				fab.requestLayout();
			}
		});

		recyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));

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
			public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
				mSubjectAdapter.swapSubjects(viewHolder.getAdapterPosition(), target.getAdapterPosition());
				DataManager.saveSubjects(getActivity());
				return true;
			}

			@Override
			public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
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
			public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
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
			public void clearView(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
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
