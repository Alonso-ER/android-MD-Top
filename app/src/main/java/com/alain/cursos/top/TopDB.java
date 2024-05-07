package com.alain.cursos.top;

import com.raizlabs.android.dbflow.annotation.Database;

@Database(name = TopDB.NAME, version = TopDB.VERSION)
public class TopDB {

    static final String NAME = "TopDB";
    static final int VERSION = 1;
}
