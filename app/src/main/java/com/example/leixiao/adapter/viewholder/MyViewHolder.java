package com.example.leixiao.adapter.viewholder;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;
import com.example.leixiao.materialdrawertest.R;

/**
 * Created by LeiXiao on 2016/3/18.
 */
public class MyViewHolder extends RecyclerView.ViewHolder {

    private TextView noteIdText;
    private TextView noteTitleText;
    private TextView noteContentText;
    private TextView noteDateText;
    private View view;
    private CardView cardView;

    public MyViewHolder(View view) {
        super(view);
        noteIdText = (TextView) view.findViewById(R.id.note_id);
        noteTitleText = (TextView) view.findViewById(R.id.note_title);
        noteContentText = (TextView) view.findViewById(R.id.note_content);
        noteDateText = (TextView) view.findViewById(R.id.note_date);
        cardView=(CardView) view.findViewById(R.id.card_view);
    }

    public TextView getNoteIdText() {
        return noteIdText;
    }

    public TextView getNoteTitleText() {
        return noteTitleText;
    }

    public TextView getNoteContentText() {
        return noteContentText;
    }

    public TextView getNoteDateText() {
        return noteDateText;
    }

    public View getView() {
        return view;
    }

    public void setNoteIdText(TextView noteIdText) {
        this.noteIdText = noteIdText;
    }

    public void setNoteTitleText(TextView noteTitleText) {
        this.noteTitleText = noteTitleText;
    }

    public void setNoteContentText(TextView noteContentText) {
        this.noteContentText = noteContentText;
    }

    public void setNoteDateText(TextView noteDateText) {
        this.noteDateText = noteDateText;
    }

    public void setView(View view) {
        this.view = view;
    }

    public CardView getCardView() {
        return cardView;
    }

    public void setCardView(CardView cardView) {
        this.cardView = cardView;
    }
}
