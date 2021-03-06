package com.example.leixiao.notepaper;


import com.example.leixiao.notepaper.materialdrawertest.R;

public class NoteItemText extends NoteItem {
    public String mSpan;
    public String mText;

    public static int getTypeIconInStagger(int type) {
        switch (type) {
            case 1 /*1*/:
                return R.drawable.ic_tab_check_small_off;
            case 2 /*2*/:
                return R.drawable.ic_tab_check_small_on;
            default:
                return 0;
        }
    }

    public static int getTypeIconInEdit(int type) {
        switch (type) {
            case 1 /*1*/:
                return R.drawable.ic_tab_check_off;
            case 2 /*2*/:
                return R.drawable.ic_tab_check_on;
            default:
                return 0;
        }
    }
}
