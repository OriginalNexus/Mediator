package com.originalnexus.mediator;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.melnykov.fab.FloatingActionButton;


public class ReportCardFrag extends Fragment {

	public static SubjectAdapter sAdapter = null;
	public static final int DIALOG_REQ_CODE = 2;

	public interface ItemClickListener {
		void onItemClick(int index);
	}

	private static ItemClickListener mCaller;

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.report_card, container, false);
	}

	@Override
	public void onActivityCreated(@Nullable final Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		if (getView() != null) {
			FloatingActionButton fab = (FloatingActionButton) getView().findViewById(R.id.fab);
			RecyclerView recyclerView = (RecyclerView) getView().findViewById(R.id.subjects_list);
			fab.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					NameDialog dialog = NameDialog.newInstance(DIALOG_REQ_CODE, "");
					dialog.show(getChildFragmentManager(), null);
				}
			});

			fab.attachToRecyclerView(recyclerView);
			recyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
			if (sAdapter == null) {
				sAdapter = new SubjectAdapter();
				sAdapter.loadSubjects();
				sAdapter.setItemClickListener(new SubjectAdapter.ItemClickListener() {
					@Override
					public void onClick(SubjectAdapter.SubjectViewHolder subjectViewHolder) {
						mCaller.onItemClick(subjectViewHolder.index);
					}
				});

				SubjectFrag.sAdapter = sAdapter;
			}

			sAdapter.saveSubjects();
			recyclerView.setAdapter(sAdapter);
		}

	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			mCaller = (ItemClickListener) activity;
		}
		catch (ClassCastException e) {
			throw new ClassCastException(activity.toString() + " must implement ItemClickListener.");
		}
	}
}
