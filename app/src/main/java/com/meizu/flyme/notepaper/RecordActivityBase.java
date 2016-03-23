package com.meizu.flyme.notepaper;

import android.app.Activity;
import android.media.AudioManager;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.os.Bundle;

import com.meizu.flyme.notepaper.utils.NoteUtil;
import com.meizu.flyme.notepaper.widget.DragShadowBuilderMz;
import com.meizu.flyme.notepaper.widget.RecordLinearLayout;
import com.meizu.flyme.notepaper.widget.RecordLinearLayout.RecordPlayManager;

public class RecordActivityBase extends Activity implements RecordPlayManager {
    public static final int NORMAL = 0;
    public static final int PLAYING = 1;
    public static final int PLAYPAUSE = 2;
    private OnAudioFocusChangeListener mAudioFocusListener = new OnAudioFocusChangeListener() {
        public void onAudioFocusChange(int focusChange) {
            switch (focusChange) {
                case NoteUtil.EDIT_TYPE_RECORD /*-3*/:
                case NoteUtil.EDIT_TYPE_LIST /*-2*/:
                    if (RecordActivityBase.PLAYING == RecordActivityBase.this.mPlayingState) {
                        RecordActivityBase.this.mPausedByTransientLossOfFocus = true;
                        if (RecordActivityBase.this.mRecordView != null) {
                            RecordActivityBase.this.mRecordView.pausePlay();
                            RecordActivityBase.this.setPlayState(RecordActivityBase.PLAYPAUSE);
                            return;
                        }
                        return;
                    }
                    return;
                case DragShadowBuilderMz.STATE_IDLE /*-1*/:
                    if (RecordActivityBase.PLAYING == RecordActivityBase.this.mPlayingState && RecordActivityBase.this.mRecordView != null) {
                        RecordActivityBase.this.mRecordView.pausePlay();
                        return;
                    }
                    return;
                case RecordActivityBase.PLAYING /*1*/:
                    if (RecordActivityBase.PLAYPAUSE == RecordActivityBase.this.mPlayingState && RecordActivityBase.this.mPausedByTransientLossOfFocus) {
                        RecordActivityBase.this.mPausedByTransientLossOfFocus = false;
                        RecordActivityBase.this.startPlay(RecordActivityBase.this.mRecordView);
                        return;
                    }
                    return;
                default:
                    return;
            }
        }
    };
    private AudioManager mAudioManager;
    private boolean mPausedByTransientLossOfFocus = false;
    public int mPlayingState;
    private RecordLinearLayout mRecordView;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mAudioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
    }

    protected void onDestroy() {
        if (this.mRecordView != null) {
            stopPlay(this.mRecordView);
            this.mRecordView = null;
        }
        super.onDestroy();
    }

    public void requestAudioFocus() {
        this.mAudioManager.requestAudioFocus(this.mAudioFocusListener, 3, PLAYING);
    }

    public void abandonAudioFocus() {
        this.mAudioManager.abandonAudioFocus(this.mAudioFocusListener);
    }

    public void setPlayState(int playState) {
        this.mPlayingState = playState;
    }

    public void startPlay(RecordLinearLayout rl) {
        if (rl != null) {
            if (!(rl == this.mRecordView || this.mRecordView == null)) {
                this.mRecordView.pausePlay();
            }
            this.mRecordView = rl;
            this.mRecordView.startPlay();
            setPlayState(PLAYING);
            requestAudioFocus();
        }
    }

    public void pausePlay(RecordLinearLayout rl) {
        if (rl != null) {
            rl.pausePlay();
            if (rl == this.mRecordView) {
                setPlayState(PLAYPAUSE);
                abandonAudioFocus();
            }
        }
    }

    public void stopPlay(RecordLinearLayout rl) {
        if (rl != null) {
            rl.stopPlay();
            if (rl == this.mRecordView) {
                this.mRecordView = null;
                setPlayState(NORMAL);
                abandonAudioFocus();
            }
        }
    }
}
