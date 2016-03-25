package com.example.leixiao.materialdrawertest;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;

import com.example.leixiao.data.Note;
import com.getbase.floatingactionbutton.FloatingActionButton;

import java.text.DateFormat;

/**
 *
 */
public class ViewNoteActivity extends AppCompatActivity {

    private static final int EDIT_NOTE_RESULT_CODE = 8;
    private static final String EXTRA_NOTE = "EXTRA_NOTE";
    private static final String EXTRA_UPDATED_NOTE = "EXTRA_UPDATED_NOTE";
    private static final DateFormat DATETIME_FORMAT = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM);

    private ScrollView scrollView;
    private FloatingActionButton editNoteButton;
    private TextView noteTitleText;
    private TextView noteContentText;
    private TextView noteCreatedAtDateText;
    private TextView noteUpdatedAtDateText;

    private Toolbar toolbar;

    private Note note;

    public static Intent buildIntent(Context context, Note note) {
        Intent intent = new Intent(context, ViewNoteActivity.class);
        intent.putExtra(EXTRA_NOTE, note);
        return intent;
    }

    public static Note getExtraUpdatedNote(Intent intent) {
        return (Note) intent.getExtras().get(EXTRA_UPDATED_NOTE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_note);
        scrollView = (ScrollView) findViewById(R.id.scroll_view);
        editNoteButton = (FloatingActionButton) findViewById(R.id.edit_note_button);

        noteTitleText = (TextView) findViewById(R.id.note_title);
        noteContentText = (TextView) findViewById(R.id.note_content);
        noteCreatedAtDateText = (TextView) findViewById(R.id.note_created_at_date);
        noteUpdatedAtDateText = (TextView) findViewById(R.id.note_updated_at_date);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("查看");
        setSupportActionBar(toolbar);

        editNoteButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                startActivityForResult(EditNoteActivity.buildIntent(ViewNoteActivity.this, note), EDIT_NOTE_RESULT_CODE);
            }
        });
        note = (Note) getIntent().getSerializableExtra(EXTRA_NOTE);
        noteTitleText.setText(note.getTitle());
        noteContentText.setText(note.getContent());
        noteCreatedAtDateText.setText(DATETIME_FORMAT.format(note.getCreatedAt()));
        noteUpdatedAtDateText.setText(DATETIME_FORMAT.format(note.getUpdatedAt()));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed(); // Cerrar esta actividad
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == EDIT_NOTE_RESULT_CODE) {
            if (resultCode == RESULT_OK) {
                Intent resultIntent = new Intent();
                Note note = EditNoteActivity.getExtraNote(data);
                resultIntent.putExtra(EXTRA_UPDATED_NOTE, note);
                setResult(RESULT_OK, resultIntent);
                finish();
            } else if (resultCode == RESULT_CANCELED) onBackPressed();
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onBackPressed() {
        // No se edito la nota
        setResult(RESULT_CANCELED, new Intent());
        finish();
    }
}