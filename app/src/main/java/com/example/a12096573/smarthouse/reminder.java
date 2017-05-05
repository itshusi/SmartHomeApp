package com.example.a12096573.smarthouse;

import java.io.Serializable;

/**
 * Created by user on 3/28/2017.
 */
@SuppressWarnings("serial")
public class reminder implements Serializable {
    String id;
    String todo;
    String date;
    String isDone;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTodo() {
        return todo;
    }

    public void setTodo(String todo) {
        this.todo = todo;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getIsDone() {
        return isDone;
    }

    public void setIsDone(String isDone) {
        this.isDone = isDone;
    }
}
