package com.example.leixiao.notepaper.data.dao;


import com.example.leixiao.notepaper.data.Note;

import java.util.List;

/**
 * 数据访问接口
 */
public interface NoteDAO {
    /**
     * 获取全部Note
     *
     * @return
     */

    List<Note> fetchAll();

    /**
     *
     */
    void insert(Note note);

    /**
     *
     */
    void update(Note note);

    /**
     *
     */
    void delete(Note note);
}