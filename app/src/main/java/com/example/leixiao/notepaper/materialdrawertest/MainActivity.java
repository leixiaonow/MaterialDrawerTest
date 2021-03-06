package com.example.leixiao.notepaper.materialdrawertest;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.example.leixiao.notepaper.NoteEditActivity;
import com.example.leixiao.notepaper.adapter.MainRecyclerViewAdapter;
import com.example.leixiao.notepaper.data.Note;
import com.example.leixiao.notepaper.data.dao.NoteDAO;
import com.example.leixiao.notepaper.data.dao.impl.sqlite.NoteSQLiteDAO;
import com.example.leixiao.notepaper.data.source.sqlite.NotesDatabaseHelper;
import com.example.leixiao.notepaper.database.NotePaper;
import com.example.leixiao.notepaper.utils.Constants;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.Drawer;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private static final int NEW_NOTE_RESULT_CODE = 4;
    private static final int EDIT_NOTE_RESULT_CODE = 5;
    NoteDAO noteDAO;
    private Boolean isAddOpen = false;
    private Boolean isGridView = false;
    private Boolean isActionMode = false;
    private TextView emptyListTextView;
    private Toolbar toolbar;
    private RecyclerView recyclerView;
    private FloatingActionsMenu menuMultipleActions;
    private FloatingActionButton actionButton_JiShi;
    private FloatingActionButton actionButton_ZhaoPian;
    private ArrayList<Integer> selectedPositions;//被选中的位置
    private ArrayList<Note> notesData;//笔记数据
    private ActionMode.Callback actionModeCallback;//callback的声明
    private ActionMode actionMode;

    private MainRecyclerViewAdapter mAdapter;

    private AccountHeader accountHeader = null;
    private Drawer result = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        selectedPositions = new ArrayList<>();


        emptyListTextView = (TextView) findViewById(android.R.id.empty);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerview);
        menuMultipleActions = (FloatingActionsMenu) findViewById(R.id.multiple_actions);

        actionButton_JiShi = (FloatingActionButton) findViewById(R.id.action_b);
        actionButton_ZhaoPian = (FloatingActionButton) findViewById(R.id.action_a);
        actionButton_ZhaoPian.setSize(FloatingActionButton.SIZE_MINI);
        actionButton_JiShi.setSize(FloatingActionButton.SIZE_MINI);


        noteDAO = new NoteSQLiteDAO(new NotesDatabaseHelper(this));

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.mipmap.ic_drawer_home);
        //    toolbar.setLogo(R.mipmap.ic_launcher);
        toolbar.setTitle("记事本");
        toolbar.setSubtitle("视图");

        notesData = new ArrayList<>();
        for (Note note : noteDAO.fetchAll()) {
            notesData.add(note);
        }

        //不同的显示方式GridView和LinearLayout
        isGridView = false;
        if (isGridView) {
            recyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
        } else {
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
        }


        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mAdapter = new MainRecyclerViewAdapter(this, notesData));
        updateView();

        //添加监听事件，简直是太棒了
        mAdapter.setOnItemClickLitener(new MainRecyclerViewAdapter.OnItemClickLitener() {
            @Override
            public void onItemClick(View view, int position) {

                if (!isActionMode) {
                    startActivityForResult(ViewNoteActivity.buildIntent(MainActivity.this, notesData.get(position)), EDIT_NOTE_RESULT_CODE);
                } else {
                    if (selectedPositions.contains(position)) {
                        notesData.get(position).setSelected(false);
                        selectedPositions.remove((Object) position);
                        mAdapter.notifyDataSetChanged();
                        if (selectedPositions.size() == 0) {
                            actionMode.finish();
                            isActionMode = false;
                            return;
                        }
                        actionMode.setTitle(String.valueOf(selectedPositions.size()));

                    } else {
                        selectedPositions.add(position);
                        notesData.get(position).setSelected(true);
                        actionMode.setTitle(String.valueOf(selectedPositions.size()));
                        mAdapter.notifyDataSetChanged();
                    }
                }
            }

            @Override
            public void onItemLongClick(View view, int position) {

                if (!isActionMode) {
                    isActionMode = true;
                    notesData.get(position).setSelected(true);
                    mAdapter.notifyDataSetChanged();
                    selectedPositions.add(position);
                    //进入ActionMode
                    actionMode = startSupportActionMode(actionModeCallback);
                    actionMode.setTitle(String.valueOf(selectedPositions.size()));
                }
            }
        });

        actionButton_JiShi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                menuMultipleActions.collapse();
                startActivityForResult(EditNoteActivity.buildIntent(MainActivity.this), NEW_NOTE_RESULT_CODE);
            }
        });

        //切换Layout
        actionButton_ZhaoPian.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, NoteEditActivity.class);
                intent.setData(NotePaper.Notes.CONTENT_URI);
                intent.putExtra(Constants.JSON_KEY_TYPE, -1);
                intent.putExtra("id", -1);
                intent.putExtra("pos", -1);
                startActivity(intent);
            }
        });

        actionModeCallback = new ActionMode.Callback() {

            //开始ActionMode的时候，调用setListOnItemClickListenersWhenActionMode（）方法
            //添加ActionMode的时候的点击事件。同时加载ActionMode下的导航菜单
            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                //    setListOnItemClickListenersWhenActionMode();
                mode.getMenuInflater().inflate(R.menu.context_note, menu);
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            //当点击导航菜单时的方法
            @Override
            public boolean onActionItemClicked(final ActionMode mode, MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_delete:
                        if (!selectedPositions.isEmpty()) {
                            new AlertDialog.Builder(MainActivity.this)
                                    .setMessage(getString(R.string.delete_notes_alert, selectedPositions.size()))
                                    .setNegativeButton(android.R.string.no, null)
                                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            deleteNotes(selectedPositions);
                                            mode.finish();
                                            isActionMode = false;
                                        }
                                    })
                                    .show();
                        } else mode.finish();
                        return true;
                    default:
                        return false;
                }
            }

            //当退出ActionMode的时候调用的方法
            //设置不是ActionMode的时候的点击事件
            //重置SelectedListItems
            @Override
            public void onDestroyActionMode(ActionMode mode) {
                //   setListOnItemClickListenersWhenNoActionMode();
                resetSelectedListItems();
            }
        };
    }


    private void resetSelectedListItems() {
        for (Note note : notesData)
            note.setSelected(false);
        selectedPositions.clear();
        mAdapter.notifyDataSetChanged();
    }

    private void deleteNotes(ArrayList<Integer> selectedPositions) {
        ArrayList<Note> toRemoveList = new ArrayList<>(selectedPositions.size());
        for (int position : selectedPositions) {
            Note note = notesData.get(position);
            toRemoveList.add(note);
            noteDAO.delete(note);
        }

        for (Note noteToRemove : toRemoveList) notesData.remove(noteToRemove);
        updateView();
        mAdapter.notifyDataSetChanged();
    }


    private void updateNote(Intent data) {
        Note updatedNote = ViewNoteActivity.getExtraUpdatedNote(data);
        noteDAO.update(updatedNote);
        for (Note note : notesData) {

            if (note.getId().equals(updatedNote.getId())) {
                note.setTitle(updatedNote.getTitle());
                note.setContent(updatedNote.getContent());
                note.setUpdatedAt(updatedNote.getUpdatedAt());
            }
        }
        mAdapter.notifyDataSetChanged();
    }

    //开始运行时，若没有笔记就让recyclerView 消失显示一句话
    private void updateView() {
        if (notesData.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            emptyListTextView.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyListTextView.setVisibility(View.GONE);
        }
    }

    //创建选项菜单 OptionsMenu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_chang_view:
                if (isGridView) {
                    isGridView = false;
                    recyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));
                } else {
                    isGridView = true;
                    recyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
                }
                break;
            default:
        }

        return super.onOptionsItemSelected(item);
    }

    //视图返回时调用
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == NEW_NOTE_RESULT_CODE) {
            if (resultCode == RESULT_OK) addNote(data);
        }
        if (requestCode == EDIT_NOTE_RESULT_CODE) {
            if (resultCode == RESULT_OK) updateNote(data);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    private void addNote(Intent data) {
        Note note = EditNoteActivity.getExtraNote(data);
        noteDAO.insert(note);
        notesData.add(0, note);//插入首部
        updateView();
        recyclerView.scrollToPosition(0);
        mAdapter.notifyItemInserted(0);//通知首部插入数据
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        menuMultipleActions.collapse();
    }

}
