package com.example.leixiao.adapter;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.leixiao.adapter.viewholder.MyViewHolder;
import com.example.leixiao.data.Note;
import com.example.leixiao.materialdrawertest.R;

import java.text.DateFormat;
import java.util.ArrayList;

/**
 * Created by LeiXiao on 2016/3/17.
 */
public class MainRecyclerViewAdapter extends RecyclerView.Adapter<MyViewHolder> {

    private static final DateFormat DATETIME_FORMAT = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM);

    Context context;
    ArrayList<Note> notes;

    public MainRecyclerViewAdapter(Context context,ArrayList<Note> notes){

        this.context=context;
        this.notes=notes;

    }

    public interface OnItemClickLitener
    {
        void onItemClick(View view, int position);//实现接口的方法中实现了对要操作的数据的引用
        void onItemLongClick(View view , int position);
    }

    private OnItemClickLitener mOnItemClickLitener;

    //在MainActivity中调用这个方法，传入一个自己写的OnItemClickLitener接口的实现对象
    //保存到mOnItemClickLitener中
    public void setOnItemClickLitener(OnItemClickLitener mOnItemClickLitener)
    {
        this.mOnItemClickLitener = mOnItemClickLitener;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        MyViewHolder holder = new MyViewHolder(LayoutInflater.from(context).inflate(R.layout.notes_row, parent, false));
    return holder;
}

    //根据position加载数据，并设置传入的ViewHolder中各种从item布局加载的View的属性
    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        Note note = notes.get(position);
        holder.getNoteTitleText().setText(note.getTitle());
        holder.getNoteContentText().setText(note.getContent().length() > 15 ? note.getContent().substring(0, 5).concat("...") : note.getContent());
        holder.getNoteIdText().setText(String.valueOf(note.getId()));
        holder.getNoteDateText().setText(DATETIME_FORMAT.format(note.getUpdatedAt()));
        if (position==3) {
            holder.getCardView().setCardBackgroundColor(Color.GREEN);
        }

        // 在加载holder时，如果设置了回调，则为每个itemView设置点击事件，和长按事件
        if (mOnItemClickLitener != null) {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = holder.getLayoutPosition();
                    //
                    mOnItemClickLitener.onItemClick(holder.itemView, pos);
                }
            });

            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    int pos = holder.getLayoutPosition();
                    mOnItemClickLitener.onItemLongClick(holder.itemView, pos);
                    return false;
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return notes.size();
    }


}