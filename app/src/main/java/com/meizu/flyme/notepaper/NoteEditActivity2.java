package com.meizu.flyme.notepaper;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.Toolbar;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListPopupWindow;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.leixiao.materialdrawertest.R;
import com.meizu.flyme.notepaper.database.NotePaper;
import com.meizu.flyme.notepaper.utils.Constants;
import com.meizu.flyme.notepaper.utils.HanziToPinyin;
import com.meizu.flyme.notepaper.utils.NoteUtil;
import com.meizu.flyme.notepaper.widget.CheckImageView;
import com.meizu.flyme.notepaper.widget.DeleteImageView;
import com.meizu.flyme.notepaper.widget.DragShadowBuilderMz;
import com.meizu.flyme.notepaper.widget.EditDragView;
import com.meizu.flyme.notepaper.widget.EditTextCloud;
import com.meizu.flyme.notepaper.widget.NoteEditText;
import com.meizu.flyme.notepaper.widget.RecordLinearLayout;
import com.meizu.flyme.notepaper.widget.RecordingLayout;
import com.meizu.flyme.notepaper.widget.RichFrameLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.UUID;

/**
 * Created by LeiXiao on 2016/3/23.
 */
public class NoteEditActivity2 extends RecordActivityBase{

    private static final String TAG = "NoteEditActivity";

    private static final int CHANGE_CONTENT = 16;
    private static final int CHANGE_DESKTOP = 4;
    private static final int CHANGE_ENCRYPT = 8;
    private static final int CHANGE_FONT_COLOR = 4096;
    private static final int CHANGE_FONT_SIZE = 65536;
    private static final int CHANGE_PAPER = 256;
    private static final int CHANGE_TAG = 1048576;
    private static final int CHANGE_TITLE = 1;
    private static final int CHANGE_TOP = 2;
    public static final int MAX_WORDS = 20000;
    public static final int MSG_SHARE_START = 0;
    private static final int REQUEST_CODE_EXPORT_TO_PIC = 1;
    private static final int REQUEST_CODE_EXPORT_TO_TEXT = 2;
    private static final int REQUEST_CODE_LOGIN = 4;
    private static final int REQUEST_CODE_PICK = 0;
    private static final int REQUEST_CODE_SHARING = 3;
    private static final int REQUEST_CODE_VERIFY = 5;
    private static final int IMAGE_WIDTH = 800;
    private static String sLastExportPicDirPath = null;
    private static String sLastExportTextDirPath = null;
    private static String sLastInsertDirPath = null;
    final int MAX_LIST_COUNT = 100;
    final int MSG_EXPORT_FINISH = REQUEST_CODE_EXPORT_TO_TEXT;
    final int MSG_SHARE_FINISH = REQUEST_CODE_EXPORT_TO_PIC;
    final long SPACE_LIMIT = 2097152;
    final int STATE_PAUSE_BACKPRESS = REQUEST_CODE_EXPORT_TO_PIC;
    final int STATE_PAUSE_DELETE = REQUEST_CODE_SHARING;
    final int STATE_PAUSE_INSERT = REQUEST_CODE_EXPORT_TO_TEXT;
    final int STATE_PAUSE_SWITCH_TO_BACK = REQUEST_CODE_LOGIN;

    private Button mButtonSave = null;//保存按钮????
    int mChanged = 0;
    //CheckImageView 的监听事件
    View.OnClickListener mCheckClickListener = new View.OnClickListener() {
        public void onClick(View v) {
        }
    };

    private int mCount = 0;
    //存储NoteItem的列表，文字，图片，和声音
    private ArrayList<NoteItem> mDataList = new ArrayList();
    //删除 点击 的监听 deleteImageView调用？？？
    View.OnClickListener mDeleteClickListener = new View.OnClickListener() {
        public void onClick(View v) {
        }
    };

    //删除文件列表
    private HashSet<String> mDeleteFilesList;
    //拖动列？？？？
    View mDragLine;
    //键盘监听
    public View.OnKeyListener mEditKeyPreListener = new View.OnKeyListener() {
        public boolean onKey(View v, int keyCode, KeyEvent event) {
            return false;
        }
    };

    //正在编辑页面显示的笔记
    NoteData mEditNote;
    //所有笔记元素的父容器
    LinearLayout mEditParent;
    //第一张图片的路径？？名称？？
    String mFirstImg;
    //第一个录音的路径？？名称？？
    String mFirstRecord;
    //第一个TextView
    TextView mFirstTextView;
    //桌面显示的标签？？
    private boolean mFloatFlag = false;
    //当前光标焦点？？？
    private int mFocusId = -2;
    //当前得到焦点的NoteEditText
    NoteEditText mFocusNoteEditText;
    //灰色值
    int mGreyColor;
    //分类切换是否打开，如果打开，可以为笔记选择分组
    private boolean mGroupSwitch = true;
    //多线程
    private Handler mHandler = new Handler();
    //？？？
    Object mIMEListener;
    ImageView mImageColorView;
    //初始化是否完成？？？
    boolean mInitOK = false;
    //不知道？？
    boolean mIsCapture;
    //软键盘是否打开
    boolean mIsSoftInuptShow = false;
    //启动时间
    long mLaunchTime;

    private MenuItem mMenuDelete;//删除
    private MenuItem mMenuDesktop;//显示到桌面
    private MenuItem mMenuExport;//导出
    private MenuItem mMenuExportPic;//导出为图片
    private MenuItem mMenuFloat;//浮动？
    private MenuItem mMenuList;//？？
    private MenuItem mMenuMore;//更多？
    private MenuItem mMenuPaper;//背景纸
    private MenuItem mMenuPhoto;//照片
    private MenuItem mMenuRecord;//录音
    private MenuItem mMenuShare;//分享
    private MenuItem mMenuTop;//置顶
    //新标签？？
    private boolean mNewFlag = false;
    //什么监听？？
    private EditTextCloud.OnKeyPreImeListener mOnKeyPreImeListener = new EditTextCloud.OnKeyPreImeListener() {
        public boolean onKeyPreIme(View view, int keyCode, KeyEvent event) {
            return false;
        }
    };
    //暂停状态？？
    int mPauseState = REQUEST_CODE_LOGIN;
    ListPopupWindow mPopup;

    //位置？？什么位置？？
    int mPosition;
    //进度窗口
    ProgressDialog mProgressDialog;

    //录音水平和竖直间隔
    int mRecordHorizontalMargin;
    int mRecordVerticalMargin;
    //录音布局
    RecordingLayout mRecordingLayoutView;
    //重做图片
    ImageView mRedoView;
    //请求码
    private int mRequestCode = -1;

    //广播接收器
    private BroadcastReceiver mScreenOffAndHomeReceiver = null;
    //滚动器，最外层控件
    ScrollView mScrollView;
    //选择开始处
    private int mSelectStart = -1;
    //分享时传递的Intent
    Intent mShareIntent;
    //如键盘是否显示的标识
    boolean mSoftInputShown = false;
    //tag编辑器，实际是文本编辑框
    EditText mTagEditor;
    //Tag列表
    ArrayList<TagInfo> mTagList = new ArrayList();
    //Tag下拉菜单
    Spinner mTagSpinner;
    //尾巴textVeiw，用来显示时间
    TextView mTailView;
    //文本颜色
    int mTextColor;
    //文本监听器
    TextWatcher mTextWatch = new TextWatcher() {
        public void afterTextChanged(Editable editable) {
        }

        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }
    };

    //时间改变广播接收器
    private BroadcastReceiver mTimeChangedReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {

        }
    };
    LinearLayout mTitleFontBack;
    //toolbar工具栏
    LinearLayout mTitleToolBar;
    //标题栏
    EditTextCloud mTitleView;
    //类型，
    int mType;
    //界面更新Handler
    Handler mUiHandler = new Handler() {
        public void handleMessage(Message msg) {
        }
    };
    //？？？
    private boolean mUndoRedo = false;
    //重做视图
    ImageView mUndoView;
    //笔记的图片元素
    RichFrameLayout mViewImageItem;
    private boolean mWidgetJump;
    //宽度？？什么的宽度
    int mWidth;
    //电话状态监听器
    private PhoneStateListener phoneStateListener = new PhoneStateListener() {
        public void onCallStateChanged(int state, String incomingNumber) {
        }
    };
    //电话管理器
    private TelephonyManager telephonyManager;

    private Toolbar edit_toolbar;


    //Tag信息类
    class TagInfo {
        long id;
        String name;

        public TagInfo(long tid, String tname) {
            this.id = tid;
            this.name = tname;
        }

        public String toString() {
            return this.name;
        }
    }


    //入口
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);//加载布局
        if (!this.mGroupSwitch) {
            //是个spinner，用于选择标签
            findViewById(R.id.tag).setVisibility(View.GONE);
        }

        //加载tool_bar
        edit_toolbar = (Toolbar) findViewById(R.id.edit_toolbar);
        setSupportActionBar(edit_toolbar);
        //得到最外层mScrollView
        this.mScrollView = (ScrollView) findViewById(R.id.scroll_view);
//        还不清楚作用
//        ScrollViewUtils.setDelayTopOverScrollEnabled(this.mScrollView, true);
        //设置文字颜色
        this.mTextColor = getResources().getColor(R.color.common_font_color);
        //设置灰色
        this.mGreyColor = getResources().getColor(R.color.common_grey_color);
        //水平边距
        this.mRecordHorizontalMargin = getResources().getDimensionPixelSize(R.dimen.edit_recording_horizontal_margin);
        //数值边距
        this.mRecordVerticalMargin = getResources().getDimensionPixelOffset(R.dimen.edit_recording_bottom_margin);
        //得到屏幕像素信息，如240*320 分辨率等
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        //设置图片，或录音控件宽度，屏幕宽度-200px
        this.mWidth = dm.widthPixels - 200;
        //判断是否从浮动窗口打开？？

        //关键的地方，初始化界面的各种组件
        initContentView();

        //设置背景颜色为mEditNote.mPaper
/*        if (this.mEditNote != null) {
            getWindow().setBackgroundDrawable(new ColorDrawable(NoteUtil.getBackgroundColor(this.mEditNote.mPaper)));
            }*/

        //设置Actionbar和下弹出式菜单的颜色，现在不关心
//        setActionBarOverLayColor();

        //电话监听什么的？？
//        this.telephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
//        this.telephonyManager.listen(this.phoneStateListener, 32);
        //注册receiver
//        registerReceiver(this.mTimeChangedReceiver, new IntentFilter("android.intent.action.TIME_SET"));
    }



    public void initContentView() {
        Intent intent = getIntent();
        //得到传入的位置
        this.mPosition = intent.getIntExtra("pos", -1);
        //得到传入的id，笔记的id
        long id = intent.getLongExtra("id", -1);
        //得到笔记的唯一路径，ContentProvider需要
        Uri noteUri = ContentUris.withAppendedId(NotePaper.Notes.CONTENT_URI, id);
        //得到传入的mtype，没有则默认-1
        this.mType = intent.getIntExtra(Constants.JSON_KEY_TYPE, -1);
        //得到传入的mWidgetJump，不知道什么用处
        this.mWidgetJump = intent.getBooleanExtra("widgetJumpFlag", false);
        //得到传入的mFocusId，光标位置？？
        this.mFocusId = intent.getIntExtra("focus", -2);
        //得到传入的mSelectStart
        this.mSelectStart = intent.getIntExtra("select", -1);
        //得到传入的mNewFlag，新建笔记标签吗？？
        this.mNewFlag = intent.getBooleanExtra("creating", false);
        //得到是否是widgetJumpView
        boolean widgetJumpView = intent.getBooleanExtra("widgetJumpView", false);

        //发送一些什么鬼？？ 好像没有什么用处
        if (this.mWidgetJump || widgetJumpView) {
            switch (this.mType) {
                case NoteUtil.EDIT_TYPE_UPDATE /*-5*/:
//                    MobEventUtil.onSendMobEvent(this, "entry_to_notes", "preview");
                    break;
                case NoteUtil.EDIT_TYPE_CAMERA /*-4*/:
//                    MobEventUtil.onSendMobEvent(this, "entry_to_notes", "picture");
                    break;
                case NoteUtil.EDIT_TYPE_RECORD /*-3*/:
//                    MobEventUtil.onSendMobEvent(this, "entry_to_notes", "voice");
                    break;
                case NoteUtil.EDIT_TYPE_LIST /*-2*/:
//                    MobEventUtil.onSendMobEvent(this, "entry_to_notes", "list");
                    break;
                case DragShadowBuilderMz.STATE_IDLE /*-1*/:
//                    MobEventUtil.onSendMobEvent(this, "widget_to_normal_new", "null");
                    break;
            }
        }

        Log.d(TAG, "type : " + this.mType);
        //得到标签tag
        long tag = intent.getLongExtra("tag", -1);
        //如果传入的标签<=-1，新建笔记？？？
        if (id <= -1) {
            if (this.mEditNote == null) {
                //mmEditNote为空，新建笔记，并为其生成和设置唯一标识符mUUId
                this.mEditNote = new NoteData();//新建笔记，什么都没有设置
                this.mEditNote.mUUId = generateUUId();//设置uuid
            }
            //设置位置mPosition，-1表示什么？？
            this.mPosition = -1;
            //设置mEditNote.mId ，mid 表示什么？？
            this.mEditNote.mId = -1;
            //设置tag，即分类
            this.mEditNote.mTag = tag;
            //判断加密，tag=-2 表示加密文件夹
            if (TagData.FUN_ENCRYPT && tag == -2) {
                this.mEditNote.mEncrypt = true;
                this.mChanged |= CHANGE_ENCRYPT;//表示改变的方式？？
            }
            //设置置顶时间为0
            this.mEditNote.mTopTime = 0;
            //设置创建时间
            this.mEditNote.mCreateTime = System.currentTimeMillis();
            //新建笔记设为true
            this.mNewFlag = true;
        } else {
            //不是新建笔记的情况
            //从数据库读出笔记
            Cursor cursor = getContentResolver().query(noteUri, NoteData.NOTES_PROJECTION, null, null, NotePaper.Notes.DEFAULT_SORT_ORDER);
            if (cursor == null || cursor.getCount() <= 0) {
                if (cursor != null) {
                    cursor.close();
                }
//                ((NoteApplication) getApplication()).notifyWidgetUpdate();
                Toast.makeText(this, R.string.note_not_exist, Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
            //cursor光标移到开头
            cursor.moveToFirst();
            //解析cursor内容
            this.mEditNote = NoteData.getItem(cursor);
            cursor.close();
        }

        if (this.mEditNote == null) {
            this.mEditNote = new NoteData();
            Log.d(toString(), "mEditNote is null ");
        }

        //得到contentParent，其中包括mEditParent
        LinearLayout contentParent = (LinearLayout) this.mScrollView.findViewById(R.id.parent);
        //得到mEditParent
        this.mEditParent = (LinearLayout) contentParent.findViewById(R.id.edit_parent);
        //设置mFirstImg
        this.mFirstImg = this.mEditNote.mFirstImg;
        //设置mFirstRecord
        this.mFirstRecord = this.mEditNote.mFirstRecord;
        //得到mDragLine，什么用处？？
        this.mDragLine = this.mScrollView.findViewById(R.id.drag_line);
        //得到lastTimeView
        LinearLayout lastTimeView = (LinearLayout) contentParent.findViewById(R.id.last_parent);


        //重点来了
        initEditLayout();


//        初始化titlebar有许多问题
//        initTitle();



        //初始化完成
        this.mInitOK = true;
        //添加mScrollView的监听，点的时候进入onFocusToEdit();编辑模式？？
        //先不管监听
/*        this.mScrollView.findViewById(R.id.empty).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Point pt = new Point();
                ReflectUtils.getLastTouchPoint(v, pt);
                Rect r = new Rect();
                mEditParent.getGlobalVisibleRect(r);
                if (pt.y >= r.bottom) {
                    onFocusToEdit();
                }
            }
        });*/
        //点击进入编辑？？先不管监听
/*        lastTimeView.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                onFocusToEdit();
            }
        });*/

        View view = this.mScrollView.findViewById(R.id.frame_parent);
        //mType 表示 打开编辑页面的类型？？ 当类型为-4时直接onInsertImage()
        //-3时，直接onRecord();
        //先不管 插入图片和录音的类型
/*        switch (this.mType) {
            case NoteUtil.EDIT_TYPE_CAMERA *//*-4*//*:
                if (checkSdcardOK() && this.mType == -4) {
                    this.mHandler.postDelayed(new Runnable() {
                        public void run() {
                            onInsertImage();
                        }
                    }, 500);
                    break;
                }
            case NoteUtil.EDIT_TYPE_RECORD *//*-3*//*:
                if (checkSdcardOK()) {
                    onRecord();
                    break;
                }
                break;
        }*/

        //设置tagName,来自数据库，
        String tagName = getString(R.string.all_tag);//所有笔记分类？？
        if (this.mEditNote.mTag > 0) {
            Uri uri = ContentUris.withAppendedId(NotePaper.NoteCategory.CONTENT_URI, this.mEditNote.mTag);
            ContentResolver contentResolver = getContentResolver();
            String[] strArr = new String[1];
            strArr[0] = NoteUtil.JSON_FILE_NAME;
            Cursor c = contentResolver.query(uri, strArr, NotePaper.NoteCategory.DELETE + "<> 1", null, NotePaper.NoteCategory.DEFAULT_SORT_ORDER);
            if (c != null) {
                int cName = c.getColumnIndex(NoteUtil.JSON_FILE_NAME);
                if (c.moveToNext()) {
                    tagName = c.getString(cName);
                }
                c.close();
            }
        }


        //得到最后修改时间TextView
        this.mTailView = (TextView) lastTimeView.findViewById(R.id.last_modify);
        //得到mTagSpinner
        this.mTagSpinner = (Spinner) lastTimeView.findViewById(R.id.tag);
        //在新线程中为mTagSpinner设置数据和监听,现在不关心，
        runOnUiThread(new Runnable() {
                          public void run() {

                          }
                      }
                );

        //生成创建和修改时间字符串
        String createTime = getResources().getString(R.string.create_time) + HanziToPinyin.Token.SEPARATOR + NoteUtil.getDate(this, this.mEditNote.mCreateTime);
        String modifyTime = getResources().getString(R.string.last_modified) + HanziToPinyin.Token.SEPARATOR + NoteUtil.getDate(this, this.mEditNote.mModifyTime);

        //类型有：浮动，更新，拍照，录音，
        // 以不同的模式打开编辑界面有不同的设置
        //比如，以更新的方式打开，不用立即弹出键盘，
        //已新建的方式即Normal 或 新建列表的方式打开则需要，在最后一个文字元素
        //上获取焦点，并打开键盘
        switch (this.mType) {
            case NoteUtil.EDIT_TYPE_FLOAT /*-6*/:
                if (this.mNewFlag) {
                    this.mTailView.setText(createTime);
                } else {
                    this.mTailView.setText(modifyTime);
                }
                getWindow().setSoftInputMode(21);
                this.mSoftInputShown = true;
                //进入编辑？？先不管
            //    onFocusToEdit();
                break;
            case NoteUtil.EDIT_TYPE_UPDATE /*-5*/:
                this.mTailView.setText(modifyTime);
                getWindow().setSoftInputMode(18);
                //下面不懂，参数还有其他的
                this.mScrollView.setDescendantFocusability(ViewGroup.FOCUS_AFTER_DESCENDANTS);
                break;
            case NoteUtil.EDIT_TYPE_CAMERA /*-4*/:
            case NoteUtil.EDIT_TYPE_RECORD /*-3*/:
                this.mTailView.setText(createTime);
                getWindow().setSoftInputMode(18);
                int childCount = this.mEditParent.getChildCount();
                if (childCount > 0) {
                    //得到最后一个笔记元素
                    View last = this.mEditParent.getChildAt(childCount - 1);
                    //如果最后一个笔记元素是文本元素，则请求焦点
                    if (NoteUtil.JSON_TEXT.equals(last.getTag())) {
                        ((NoteEditText) last.findViewById(R.id.text)).requestFocus();
                    }
                }
                this.mSoftInputShown = true;
                break;
            case NoteUtil.EDIT_TYPE_LIST /*-2*/:
            case NoteUtil.EDIT_TYPE_NORMAL /*-1*/:
                this.mTailView.setText(createTime);
                getWindow().setSoftInputMode(21);
                this.mSoftInputShown = true;
                //进入编辑？？先不管
//                onFocusToEdit();
                break;
        }
//        设置第一个提示，先不管
//        setFirstHint();
    }


    String generateUUId() {
        return UUID.randomUUID().toString();
    }

    void initEditLayout() {
        boolean addNew = false;
        int index = 0;
        JSONObject o;
        NoteItem nt;
        NoteItemText nt2;
        //清除数据
        this.mDataList.clear();
        //置为空
        this.mFocusNoteEditText = null;
        //数量为0
        int size = 0;
        //json数组置为空
        JSONArray ja;
        //mNoteData是String类型，如果有数据就加到界面中
        if (this.mEditNote.mNoteData != null) {
            //从mNoteData恢复出json数组
            JSONArray ja2 = null;
            try {
                ja2 = new JSONArray(this.mEditNote.mNoteData);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            //设置size为数组的元素个数
            size = ja2.length();
            ja = ja2;
            addNew = true;
            //将所有从json数组中解析出来的对象，加到编辑界面中，重要
            while (index < size) {
                o = (JSONObject) ja.opt(index);
                nt = NoteData.getNoteItem(o);
                addNew = false;
                //加入数据列表
                this.mDataList.add(nt);
                //根据mStata判断
                switch (nt.mState) {
                    case 0 /*0*/:
                    case 1 /*1*/:
                    case 2 /*2*/:
                        addTextItem((NoteItemText) nt);
                        break;
                    case 3 /*3*/:
                        addImageItem((NoteItemImage) nt);
                        break;
                    case 4 /*4*/:
                        addRecordItem((NoteItemRecord) nt);
                        break;
                    default:
                        break;
                }
            }
        }

        //如果是新加的，则添加一个NoteItemText
        if (addNew) {
            nt2 = new NoteItemText();
            nt2.mState = 0;
            if (this.mType == -2) {
                nt2.mState = 1;
            }
            addTextItem(nt2);
        }
    }



    //设置titleView和titleToolBar
    public void initTitle() {

//        this.mTitleToolBar = (LinearLayout) custom.findViewById(R.id.toolBar);

        //设置mTitleView
//        this.mTitleView = (EditTextCloud) custom.findViewById(R.id.title);
        //为mTitleView设置标题
//        this.mTitleView.setText(this.mEditNote.mTitle);

//        this.mTitleView.clearFocus();
    }




    void addTextItem(NoteItemText nt) {
        //为父布局mEditParent添加一个子布局
        getLayoutInflater().inflate(R.layout.edit_textlist_item, this.mEditParent);
        //得到刚才加载的子布局
        View item = this.mEditParent.getChildAt(this.mEditParent.getChildCount() - 1);
        //得到子布局中的控件
        EditDragView drag = (EditDragView) item.findViewById(R.id.drag);
        CheckImageView check = (CheckImageView) item.findViewById(R.id.check);
        NoteEditText edit = (NoteEditText) item.findViewById(R.id.text);
        //设置文字
        edit.setText(nt.mText);
        //设置字体大小
        edit.setTextSize((float) (this.mEditNote.mTextSize > 0 ? this.mEditNote.mTextSize : NoteData.DEFAULT_FONT_SIZE));

        DeleteImageView deleteView = (DeleteImageView) item.findViewById(R.id.delete);

        switch (nt.mState) {
            case REQUEST_CODE_PICK /*0*/:
                drag.setImageType(nt.mState);
                check.setImageType(nt.mState);
                deleteView.setVisibility(View.VISIBLE);
                return;
            default:
                return;
        }
    }


    //在界面上加一个图片---重点分析
    void addImageItem(NoteItemImage nt) {
        //为父布局mEditParent添加一个子布局
        getLayoutInflater().inflate(R.layout.edit_image, this.mEditParent);
        //获得刚才添加的子布局
        RichFrameLayout imageParent = (RichFrameLayout) this.mEditParent.getChildAt(this.mEditParent.getChildCount() - 1);
        //为子布局设置尺寸
        imageParent.setSize(nt.mWidth, nt.mHeight);
        //为子布局涉资uuid和资源
        imageParent.setUUIDandName(this.mEditNote.mUUId, nt.mFileName);
    }


    //在界面上加一个录音---重点分析
    void addRecordItem(NoteItemRecord nt) {
        //为父布局mEditParent添加一个子布局
        getLayoutInflater().inflate(R.layout.edit_record_item, this.mEditParent);
        //获得刚才添加的子布局
        RichFrameLayout parent = (RichFrameLayout) this.mEditParent.getChildAt(this.mEditParent.getChildCount() - 1);
        //为子布局涉资uuid和资源
        parent.setUUIDandName(this.mEditNote.mUUId, nt.mFileName);
        //录音
        ((RecordLinearLayout) parent.findViewById(R.id.recordLayout)).setRecordPlayManager(this);
    }



    //创建OptionsMenu时
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        if (menu != null) {
            menu.clear();
        }
        if (this.mSoftInputShown) {
            //当软键盘打开时，加载编辑时的菜单
            getMenuInflater().inflate(R.menu.edit, menu);
            this.mMenuPhoto = menu.findItem(R.id.photo);
            this.mMenuRecord = menu.findItem(R.id.record);
            this.mMenuList = menu.findItem(R.id.list);
            this.mMenuDelete = menu.findItem(R.id.menu_delete);
            this.mMenuMore = menu.findItem(R.id.more);
        } else {
            //当软键盘关闭时，加载浏览时菜单，一些View设置为null
            getMenuInflater().inflate(R.menu.edit_browse, menu);
            this.mMenuPhoto = null;
            this.mMenuRecord = null;
            this.mMenuList = null;
            this.mMenuDelete = menu.findItem(R.id.menu_delete_browse);
            this.mMenuMore = null;
        }

        this.mMenuPaper = menu.findItem(R.id.menu_change_paper);//纸张
        this.mMenuShare = menu.findItem(R.id.menu_share);//分享
        this.mMenuExport = menu.findItem(R.id.menu_export);//导出
        this.mMenuExportPic = menu.findItem(R.id.menu_export_pic);//导出为图片
        this.mMenuTop = menu.findItem(R.id.menu_top);//置顶
        this.mMenuFloat = menu.findItem(R.id.menu_float);//浮动？？
        this.mMenuDesktop = menu.findItem(R.id.menu_desktop);//桌面
        if (!(this.mEditNote == null || this.mEditNote.mTopTime == 0)) {
            this.mMenuTop.setChecked(true);
        }
        if (this.mEditNote != null && this.mEditNote.mDesktop > 0) {
            this.mMenuDesktop.setChecked(true);
        }
        if (this.mEditNote != null && this.mEditNote.mEncrypt) {
            this.mMenuDesktop.setVisible(false);
        }
        return super.onCreateOptionsMenu(menu);
    }

}
