package com.example.leixiao.data.dao;


import com.example.leixiao.data.Note;

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