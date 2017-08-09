package com.originalnexus.mediator.adapters;

import android.animation.ValueAnimator;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.widget.TextView;

import com.originalnexus.mediator.others.GradeCalc;
import com.originalnexus.mediator.R;
import com.originalnexus.mediator.models.Subject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EventListener;
import java.util.Locale;

public class SubjectAdapter extends RecyclerView.Adapter<SubjectAdapter.SubjectViewHolder> {

	public class SubjectViewHolder extends RecyclerView.ViewHolder {
		private final View mView;
		private final TextView mNameView;
		private final TextView mGradesView;
		private final TextView mThesisView;
		private final TextView mAverageView;
		private final TextView mThesisTextView;

		private final ValueAnimator mElevateAnim;
		private boolean mRaised = false;

		SubjectViewHolder(View v) {
			super(v);
			mView = v;
			mNameView = v.findViewById(R.id.item_subject_name);
			mGradesView = v.findViewById(R.id.item_subject_grades);
			mThesisView = v.findViewById(R.id.item_subject_thesis);
			mAverageView = v.findViewById(R.id.item_subject_average);
			mThesisTextView = v.findViewById(R.id.item_subject_thesis_label);
			mElevateAnim = ValueAnimator.ofFloat(0f, mView.getResources().getDimension(R.dimen.drag_drop_elevation)).setDuration(200);
			mElevateAnim.setInterpolator(new AccelerateInterpolator());
			mElevateAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
				@Override
				public void onAnimationUpdate(ValueAnimator animation) {
					ViewCompat.setTranslationZ(mView, (float) animation.getAnimatedValue());
				}
			});
		}

		public String getSubjectName() {
			return String.valueOf(mNameView.getText());
		}
		public void setRaised(boolean raised) {
			if (raised && !mRaised) mElevateAnim.start();
			else if (!raised && mRaised) mElevateAnim.reverse();
			mRaised = raised;
		}
		public void setSwipeOpacity(float dX) {
			mView.setAlpha(1f - Math.abs(dX) / mView.getWidth());
		}

	}

	public interface SubjectClickListener extends EventListener {
		void onClick(SubjectViewHolder subjectViewHolder);
	}

	private final ArrayList<Subject> mSubjects;
	private Subject mUndoSubject;
	private int mUndoPosition;
	private SubjectClickListener mSubjectClickListener;

	public SubjectAdapter(ArrayList<Subject> subjects) {
		mSubjects = subjects;
	}


	public void setSubjectClickListener(SubjectClickListener l) {
		mSubjectClickListener = l;
	}

	@Override
	public int getItemCount() {
		return mSubjects.size();
	}

	@Override
	public SubjectViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		final SubjectViewHolder svh = new SubjectViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_subject, parent, false));
		svh.mView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mSubjectClickListener != null) mSubjectClickListener.onClick(svh);
			}
		});
		return svh;
	}

	@Override
	public void onBindViewHolder(SubjectViewHolder subjectViewHolder, int i) {
		Subject s = mSubjects.get(i);
		subjectViewHolder.mNameView.setText(s.name);
		subjectViewHolder.mGradesView.setText((s.grades.size() > 0) ? GradeCalc.arrayListToString(s.grades) : "");

		if (s.thesis != 0) {
			subjectViewHolder.mThesisTextView.setVisibility(View.VISIBLE);
			subjectViewHolder.mThesisView.setText(String.valueOf(s.thesis));
		}
		else {
			subjectViewHolder.mThesisTextView.setVisibility(View.GONE);
			subjectViewHolder.mThesisView.setText("");
		}

		// Get average
		double average = GradeCalc.average(s.grades, s.thesis);
		subjectViewHolder.mAverageView.setText((average != 0) ? String.format(Locale.ENGLISH, "%.2f", average) : "");
	}


	public void addSubject(Subject sub) {
		mSubjects.add(sub);
		notifyItemInserted(getItemCount() - 1);
	}

	public void removeSubject(int index) {
		mUndoSubject = mSubjects.remove(index);
		mUndoPosition = index;
		notifyItemRemoved(index);
	}

	public void swapSubjects(int pos1, int pos2) {
		Collections.swap(mSubjects, pos1, pos2);
		notifyItemMoved(pos1, pos2);
	}

	public void undoRemove() {
		if (mUndoSubject != null) {
			mSubjects.add(mUndoPosition, mUndoSubject);
			notifyItemInserted(mUndoPosition);
			mUndoSubject = null;
		}
	}

}
