package com.totalboron.jay.labeled;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by Jay on 10/05/16.
 */
public class DatabaseAdapter
{
    private DatabaseHelper databaseHelper;
    private Context context;

    public DatabaseAdapter(Context context)
    {
        databaseHelper = new DatabaseHelper(context);
        this.context = context;
    }

    public long insertData(String label, String fileName)
    {
        SQLiteDatabase database = databaseHelper.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(DatabaseHelper.LABEL_NAME, label);
        contentValues.put(DatabaseHelper.FILE_NAME, fileName);
        return database.insert(DatabaseHelper.TABLE_NAME, null, contentValues);
    }


    public void displayAll()
    {
        SQLiteDatabase database = databaseHelper.getWritableDatabase();
        String[] columns = {DatabaseHelper.UID, DatabaseHelper.FILE_NAME, DatabaseHelper.LABEL_NAME};
        Cursor cursor = database.query(DatabaseHelper.TABLE_NAME, columns, null, null, null, null, null);
        String message = "";
        int i=0;
        while (cursor.moveToNext())
        {
            i++;
        }
        Toast.makeText(context, i+"=total", Toast.LENGTH_LONG).show();
        cursor.close();
    }
    public void delete(String fileName)
    {
        SQLiteDatabase database = databaseHelper.getWritableDatabase();
        database.delete(DatabaseHelper.TABLE_NAME, DatabaseHelper.FILE_NAME+" = ? ",new String[]{fileName});
    }

    public void getSearch(String search_hint)
    {
        SQLiteDatabase database = databaseHelper.getWritableDatabase();
        String[] columns = {DatabaseHelper.FILE_NAME};
        String query="SELECT "+DatabaseHelper.FILE_NAME+" FROM "+DatabaseHelper.TABLE_NAME+" WHERE "+DatabaseHelper.LABEL_NAME+" LIKE '%"+search_hint+"%';";
        Cursor cursor=database.rawQuery(query,null);
        String message = "";
        if (cursor != null)
        {
            while (cursor.moveToNext())
            {
                message=message+cursor.getString(0)+"\n";
            }
            Toast.makeText(context, message, Toast.LENGTH_LONG).show();
            cursor.close();
        }
    }


    public void getFileName(String labelName)
    {
        SQLiteDatabase database = databaseHelper.getWritableDatabase();
        String[] columns = {DatabaseHelper.FILE_NAME};
        String[] selection = {labelName};
        Cursor cursor = database.query(DatabaseHelper.TABLE_NAME, columns, DatabaseHelper.LABEL_NAME + " = '"+labelName+"' ;", null, null, null, null);
        String message = "";
        if (cursor != null)
        {
            while (cursor.moveToNext())
            {
                message=message+cursor.getString(0)+"\n";
            }
            Toast.makeText(context, message, Toast.LENGTH_LONG).show();
            cursor.close();
        }
    }


    static class DatabaseHelper extends SQLiteOpenHelper
    {
        private String logging = getClass().getSimpleName();
        private static final String DATABASE_NAME = "LABELED";
        private static final String TABLE_NAME = "SEARCHING";
        private static final String UID = "_id";
        private static final String FILE_NAME = "FILE";
        private static final String LABEL_NAME = "LABEL";
        private static final int VERSION = 1;
        private final String CREATE_TABLE_SEARCHING = "CREATE TABLE " + TABLE_NAME + " ( " + UID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + FILE_NAME + " VARCHAR(255), " + LABEL_NAME + " VARCHAR(255));";
        private final String DROP_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;
        public DatabaseHelper(Context context)
        {
            super(context, DATABASE_NAME, null, VERSION);
            Log.d(logging, "Called Constructor");
        }

        @Override
        public void onCreate(SQLiteDatabase db)
        {
            db.execSQL(CREATE_TABLE_SEARCHING);
            Log.d(logging, "Called Create");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
        {
            db.execSQL(DROP_TABLE);
            onCreate(db);
        }
    }
}
