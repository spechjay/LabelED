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
        int i=0;
        while (cursor.moveToNext())
        {
            i++;
        }
        Toast.makeText(context, i+"=total", Toast.LENGTH_LONG).show();
        cursor.close();
    }

//    public void delete(String fileName)
//    {
//        SQLiteDatabase database = databaseHelper.getWritableDatabase();
//        database.delete(DatabaseHelper.TABLE_NAME, DatabaseHelper.FILE_NAME+" = ? ",new String[]{fileName});
//    }

    public Cursor getSearch(String search_hint)
    {
        SQLiteDatabase database = databaseHelper.getWritableDatabase();
        //ToDo: standardize and list the Cursor alphabetically
        String query="SELECT "+DatabaseHelper.FILE_NAME+" FROM "+DatabaseHelper.TABLE_NAME+" WHERE "+DatabaseHelper.LABEL_NAME+" LIKE '%"+search_hint+"%';";
        return database.rawQuery(query,null);
    }


    public long insertWordHistory(String word)
    {
        SQLiteDatabase database = databaseHelper.getWritableDatabase();
        Cursor cursor=database.query(DatabaseHelper.HISTORY_TABLE,new String[]{DatabaseHelper.HISTORY_WORDS},DatabaseHelper.HISTORY_WORDS+" = ?",new String[]{word},null,null,null);
        if (cursor!=null&&cursor.getCount()>0)
        {
            cursor.close();
            return -99;
        }
        cursor.close();
        ContentValues contentValues = new ContentValues();
        contentValues.put(DatabaseHelper.HISTORY_WORDS,word);
        return database.insert(DatabaseHelper.HISTORY_TABLE, null, contentValues);
    }
    public Cursor getHistory(String search_hint)
    {
        SQLiteDatabase database = databaseHelper.getWritableDatabase();
        String query="SELECT "+DatabaseHelper.HISTORY_WORDS+" FROM "+DatabaseHelper.HISTORY_TABLE+" WHERE "+DatabaseHelper.HISTORY_WORDS+" LIKE '%"+search_hint+"%';";
        return database.rawQuery(query,null);
    }

    public void removeFiles(String name)
    {
        SQLiteDatabase database = databaseHelper.getWritableDatabase();
        database.delete(DatabaseHelper.TABLE_NAME,DatabaseHelper.FILE_NAME +" = ?",new String[]{name});
    }


//    public void getFileName(String labelName)
//    {
//        SQLiteDatabase database = databaseHelper.getWritableDatabase();
//        String[] columns = {DatabaseHelper.FILE_NAME};
//        String[] selection = {labelName};
//        Cursor cursor = database.query(DatabaseHelper.TABLE_NAME, columns, DatabaseHelper.LABEL_NAME + " = '"+labelName+"' ;", null, null, null, null);
//        String message = "";
//        if (cursor != null)
//        {
//            while (cursor.moveToNext())
//            {
//                message=message+cursor.getString(0)+"\n";
//            }
//            Toast.makeText(context, message, Toast.LENGTH_LONG).show();
//            cursor.close();
//        }
//    }





    static class DatabaseHelper extends SQLiteOpenHelper
    {
        private String logging = getClass().getSimpleName();
        private static final String DATABASE_NAME = "LABELED";
        private static final String TABLE_NAME = "SEARCHING";
        private static final String UID = "_id";
        private static final String FILE_NAME = "FILE";
        private static final String LABEL_NAME = "LABEL";
        private static final String HISTORY_TABLE="HISTORY";
        private static final String HISTORY_WORDS="WORDS";
        private static final int VERSION = 2;
        private final String CREATE_TABLE_SEARCHING = "CREATE TABLE " + TABLE_NAME + " ( " + UID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + FILE_NAME + " VARCHAR(255), " + LABEL_NAME + " VARCHAR(255));";
        private final  String CREATE_SEARCH_HISTORY="CREATE TABLE " + HISTORY_TABLE + " ( "+UID +" INTEGER PRIMARY KEY AUTOINCREMENT, " + HISTORY_WORDS+" TEXT);" ;
        private final String DROP_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;
        private final String DROP_TABLE_HISTORY = "DROP TABLE IF EXISTS " + HISTORY_TABLE;
        public DatabaseHelper(Context context)
        {
            super(context, DATABASE_NAME, null, VERSION);
            Log.d(logging, "Called Constructor");
        }

        @Override
        public void onCreate(SQLiteDatabase db)
        {
            db.execSQL(CREATE_TABLE_SEARCHING);
            db.execSQL(CREATE_SEARCH_HISTORY);
            Log.d(logging, "Called Create");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
        {
            db.execSQL(DROP_TABLE);
            db.execSQL(DROP_TABLE_HISTORY);
            onCreate(db);
        }
    }
}
