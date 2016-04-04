/*      Copyright 2016 Marcello de Paula Ferreira Costa

        Licensed under the Apache License, Version 2.0 (the "License");
        you may not use this file except in compliance with the License.
        You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

        Unless required by applicable law or agreed to in writing, software
        distributed under the License is distributed on an "AS IS" BASIS,
        WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
        See the License for the specific language governing permissions and
        limitations under the License. */
package mchs.neverforget.util;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;

import mchs.neverforget.model.Book;

public class NeverForgetDatabaseAdapter {
    private NeverForgetDatabaseHelper helper;

    public ArrayList<Book> getBooks(){
        SQLiteDatabase db = helper.getReadableDatabase();
        String[] columnsToQuery = {
                NeverForgetDatabaseHelper.AUTHOR,
                NeverForgetDatabaseHelper.TITLE,
                NeverForgetDatabaseHelper.RETURN_DATE,
                NeverForgetDatabaseHelper.LIBRARY};

        Cursor cursor = db.query(
                NeverForgetDatabaseHelper.TABLE_NAME,
                columnsToQuery,
                null,
                null,
                null,
                null,
                null);

        ArrayList<Book> bookArrayList = new ArrayList<>(cursor.getCount());
        while(cursor.moveToNext()){
            bookArrayList.add(
                    new Book(cursor.getString(cursor.getColumnIndex(NeverForgetDatabaseHelper.AUTHOR)),
                    cursor.getString(cursor.getColumnIndex(NeverForgetDatabaseHelper.TITLE)),
                    cursor.getString(cursor.getColumnIndex(NeverForgetDatabaseHelper.RETURN_DATE)),
                    cursor.getString(cursor.getColumnIndex(NeverForgetDatabaseHelper.LIBRARY))));
        }
        cursor.close();
        db.close();
        return bookArrayList;
    }

    public long insertBook(Book book){
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(NeverForgetDatabaseHelper.AUTHOR,book.getAuthor());
        contentValues.put(NeverForgetDatabaseHelper.TITLE,book.getTitle());
        contentValues.put(NeverForgetDatabaseHelper.RETURN_DATE,book.getReturnDate());
        contentValues.put(NeverForgetDatabaseHelper.LIBRARY, book.getLibrary());
        long id = db.insert(NeverForgetDatabaseHelper.TABLE_NAME,null,contentValues);
        db.close();
        return id;
    }

    public NeverForgetDatabaseAdapter(Context context){
        this.helper = new NeverForgetDatabaseHelper(context);
    }

    public void clearDatabase() {
        SQLiteDatabase db = helper.getWritableDatabase();
        String clearDBQuery = "DELETE FROM "+ NeverForgetDatabaseHelper.TABLE_NAME;
        db.execSQL(clearDBQuery);
        db.close();
    }

    static class NeverForgetDatabaseHelper extends SQLiteOpenHelper{
        private static final String TAG = "DatabaseHelper";
        private static final String DATABASE_NAME = "neverforgetdatabase";
        private static final String TABLE_NAME = "BOOKS";
        private static final String BOOK_ID = "_ID";
        private static final String AUTHOR = "AUTHOR";
        private static final String TITLE = "TITLE";
        private static final String RETURN_DATE = "RETURN_DATE";
        private static final String LIBRARY = "LIBRARY";
        private static final int DATABASE_VERSION = 1;

        private static final String CREATE_TABLE_QUERY =
                "CREATE TABLE "+TABLE_NAME+" ("+
                        BOOK_ID+" INTEGER PRIMARY KEY AUTOINCREMENT,"+
                        AUTHOR+" VARCHAR(255),"+
                        TITLE+" VARCHAR(255),"+
                        RETURN_DATE+" VARCHAR(255),"+
                        LIBRARY+" VARCHAR(255));";
        private static final String DROP_TABLE_QUERY =
                "DROP TABLE IF EXISTS "+TABLE_NAME;

        public NeverForgetDatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            try{
                db.execSQL(CREATE_TABLE_QUERY);
            } catch (SQLException e){
                Log.e(TAG, "Failed to create database table", e);
            }
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            try{
                db.execSQL(DROP_TABLE_QUERY);
                onCreate(db);
            } catch (SQLException e){
                Log.e(TAG, "Failed to drop database table", e);
            }
        }
    }
}
