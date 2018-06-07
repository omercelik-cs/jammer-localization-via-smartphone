package com.mantz_it.rfanalyzer;

import java.util.ArrayList;
import java.util.Locale;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class DBController{

    private static final String TAG = "DBAdapter"; //used for logging database version changes
    private static DBController mInstance = null;
    // Field Names:
    public static final String KEY_ROWID = "_id";
    public static final String KEY_FREQUENCY = "frequency";
    public static final String KEY_SIGNAL_STRENGTH = "signalStrength";
    public static final String KEY_TIME = "time";
    public static final String KEY_DATE = "date";
    public static final String KEY_LATITUDE = "latitude";
    public static final String KEY_LONGITUDE = "longitude";
    public static final String UPDATE_STATUS = "updateStatus";
   // public static final String UPDATE_STATUS = "updateStatus";
    //private static final String UPDATE_STATUS_COLUMN = UPDATE_STATUS + " INTEGER NOT NULL";

    public static final String[] ALL_KEYS = new String[] {KEY_ROWID, KEY_FREQUENCY, KEY_SIGNAL_STRENGTH,KEY_TIME,KEY_LONGITUDE,KEY_LATITUDE,UPDATE_STATUS};

    // Column Numbers for each Field Name:
    public static final int COL_ROWID = 0;
    public static final int COL_TASK = 1;
    public static final int COL_DATE = 2;
    public static final int COL_UPDATE_STATUS = 3;


    // DataBase info:
    public static final String DATABASE_NAME = "RFAnalyzer";
    public static final String DATABASE_TABLE = "RFTable";
    public static final int DATABASE_VERSION = 17; // The version number must be incremented each time a change to DB structure occurs.

    //SQL statement to create database
    private static final String DATABASE_CREATE_SQL =
            "CREATE TABLE " + DATABASE_TABLE
                    + " (" + KEY_ROWID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + KEY_FREQUENCY + " DOUBLE NOT NULL, "
                    + KEY_SIGNAL_STRENGTH + " DOUBLE NOT NULL, "
                    + KEY_TIME + " TEXT NOT NULL, "
                    + KEY_LATITUDE + " DOUBLE NOT NULL, "
                    + KEY_LONGITUDE + " DOUBLE NOT NULL, "
                    + KEY_DATE + " TEXT NOT NULL, "
                    + UPDATE_STATUS + " TEXT"
                    + ");";

    private final Context context;
    private DatabaseHelper myDBHelper;
    private SQLiteDatabase db;


    public DBController(Context ctx) {
        this.context = ctx;
        myDBHelper = new DatabaseHelper(context);
    }

    // Open the database connection.
    public DBController open() {
        db = myDBHelper.getWritableDatabase();
        return this;
    }

    // Close the database connection.
    public void close() {
        myDBHelper.close();
    }

    // Add a new set of values to be inserted into the database.
    public long insertRow(double frequency, double signalStrength, String time,double latitude, double longitude, String date  )  {
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_FREQUENCY, frequency);
        initialValues.put(KEY_SIGNAL_STRENGTH, signalStrength);
        initialValues.put(KEY_TIME, time);
        initialValues.put(KEY_LATITUDE, latitude);
        initialValues.put(KEY_LONGITUDE, longitude);
        initialValues.put(KEY_DATE, date);
        initialValues.put(UPDATE_STATUS, "no");

        // Insert the data into the database.
        return db.insert(DATABASE_TABLE, null, initialValues);

    }

    // Delete a row from the database, by rowId (primary key)
    public boolean deleteRow(long rowId) {
        String where = KEY_ROWID + "=" + rowId;
        return db.delete(DATABASE_TABLE, where, null) != 0;
    }

    public void deleteAll() {
        Cursor c = getAllRows();
        long rowId = c.getColumnIndexOrThrow(KEY_ROWID);
        if (c.moveToFirst()) {
            do {
                deleteRow(c.getLong((int) rowId));
            } while (c.moveToNext());
        }
        c.close();
    }

    void deleteAllRows()
    {
        db = myDBHelper.getWritableDatabase();
        db.delete(DATABASE_TABLE, null, null);
    }

    // Return all data in the database.
    public Cursor getAllRows() {
        String where = null;
        Cursor c = 	db.query(true, DATABASE_TABLE, ALL_KEYS, where, null, null, null, null, null);
        if (c != null) {
            c.moveToFirst();
        }
        return c;
    }

    // Get a specific row (by rowId)
    public Cursor getRow(long rowId) {
        String where = KEY_ROWID + "=" + rowId;
        Cursor c = 	db.query(true, DATABASE_TABLE, ALL_KEYS,
                where, null, null, null, null, null);
        if (c != null) {
            c.moveToFirst();
        }
        return c;
    }

    // Change an existing row to be equal to new data.
    public boolean updateRow(long rowId, String task, String date) {
        String where = KEY_ROWID + "=" + rowId;
        ContentValues newValues = new ContentValues();
        newValues.put(KEY_FREQUENCY, task);
        newValues.put(UPDATE_STATUS, "no");
        // Insert it into the database.
        return db.update(DATABASE_TABLE, newValues, where, null) != 0;
    }


    public static class DatabaseHelper extends SQLiteOpenHelper
    {
        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase _db) {

            _db.execSQL(DATABASE_CREATE_SQL);
        }

        @Override
        public void onUpgrade(SQLiteDatabase _db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading application's database from version " + oldVersion
                    + " to " + newVersion + ", which will destroy all old data!");

            // Destroy old database:
            _db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE);

            // Recreate new database:
            onCreate(_db);
        }
    }

    public static DBController getInstance(Context ctx)
    {
        if (mInstance == null) {
            mInstance = new DBController(ctx.getApplicationContext());
        }
        return mInstance;
    }

   /* protected static String generateForeignKeyColumn(String fk,
                                                     String refTable, String refColumn) {
        final String format = "FOREIGN KEY(%s) REFERENCES %s(%s)";
        return String.format(Locale.US, format, fk, refTable, refColumn);
    }

    protected static String generateSchema(String tableName,
                                           String... columnDefs) {
        final StringBuilder ret = new StringBuilder();
        // Build beginning of CREATE statement
        ret.append("CREATE TABLE IF NOT EXISTS ");
        ret.append(tableName);
        ret.append('(');

        // Build columns of table
        for (int i = 0; i < columnDefs.length - 1; i++) {
            ret.append(columnDefs[i]);
            ret.append(',');
        }
        if (columnDefs.length > 0)
            ret.append(columnDefs[columnDefs.length - 1]);

        // Build end
        ret.append(')');
        ret.append(';');
        return ret.toString();
    }*/


    public String composeJSONfromSQLite(){
        ArrayList<ContentValues> wordList;
        wordList = new ArrayList<ContentValues>();
        String selectQuery = "SELECT  * FROM RFTable where updateStatus = '"+"no"+"'";
        SQLiteDatabase database = this.myDBHelper.getWritableDatabase();
        Cursor cursor = database.rawQuery(selectQuery, null);
        //Cursor cursor = database.query(Readings.TABLE_NAME,null,selectQuery,null,null,null,null);
        if (cursor.moveToFirst()) {
            do {
                ContentValues cv = new ContentValues();
                cv.put("id", cursor.getLong(cursor.getColumnIndex(KEY_ROWID)));
                cv.put("frequency", cursor.getDouble(cursor.getColumnIndex(KEY_FREQUENCY)));
                cv.put("signalStrength", cursor.getDouble(cursor.getColumnIndex(KEY_SIGNAL_STRENGTH)));
                cv.put("time", cursor.getString(cursor.getColumnIndex(KEY_TIME)));
                cv.put("latitude", cursor.getDouble(cursor.getColumnIndex(KEY_LATITUDE)));
                cv.put("longitude", cursor.getDouble(cursor.getColumnIndex(KEY_LONGITUDE)));
                cv.put("date", cursor.getString(cursor.getColumnIndex(KEY_DATE)));
               // cv.put("sdk", Build.VERSION.SDK_INT);
               // cv.put("manufacturer", Build.MANUFACTURER);
               // cv.put("model", Build.MODEL);
                wordList.add(cv);
            } while (cursor.moveToNext());
        }
        database.close();
        Gson gson = new GsonBuilder().create();
        //Use GSON to serialize Array List to JSON
        return gson.toJson(wordList);
    }

    public String getSyncStatus(){
        String msg = null;
        if(this.dbSyncCount() == 0){
            msg = "SQLite and Remote MySQL DBs are in Sync!";
        }else{
            msg = "DB Sync needed\n";
        }
        return msg;
    }

    public int dbSyncCount(){
        int count = 0;
        String selectQuery = "SELECT  * FROM RFTable where updateStatus = '"+"no"+"'";
        SQLiteDatabase database = this.myDBHelper.getWritableDatabase();
        Cursor cursor = database.rawQuery(selectQuery, null);
        count = cursor.getCount();
        database.close();
        return count;
    }


    // need to fix id stuff
    public void updateSyncStatus(String id, String status){
        Log.d("updateSyncStatus", "id = " + id + " status = " + status);
        SQLiteDatabase database = this.myDBHelper.getWritableDatabase();
        String updateQuery = "Update RFTable set updateStatus = '" + status + "' where _id="+ Integer.valueOf(id);
        Log.d("query",updateQuery);
        database.execSQL(updateQuery);
        database.close();
    }

    // this is for databasemanager
    public ArrayList<Cursor> getData(String Query){
        //get writable database
        SQLiteDatabase sqlDB = this.myDBHelper.getWritableDatabase();
        String[] columns = new String[] { "mesage" };
        //an array list of cursor to save three cursors ec_1 has results from the query
        //other cursor stores error message if any errors are triggered
        ArrayList<Cursor> alc = new ArrayList<Cursor>(2);
        MatrixCursor Cursor2= new MatrixCursor(columns);
        alc.add(null);
        alc.add(null);

        try{
            String maxQuery = Query ;
            //execute the query results will be save in Cursor c
            Cursor c = sqlDB.rawQuery(maxQuery, null);


            //add value to cursor2
            Cursor2.addRow(new Object[] { "Success" });

            alc.set(1,Cursor2);
            if (null != c && c.getCount() > 0) {


                alc.set(0,c);
                c.moveToFirst();

                return alc ;
            }
            return alc;
        } catch(SQLException sqlEx){
            Log.d("printing exception", sqlEx.getMessage());
            //if any exceptions are triggered save the error message to cursor an return the arraylist
            Cursor2.addRow(new Object[] { ""+sqlEx.getMessage() });
            alc.set(1,Cursor2);
            return alc;
        } catch(Exception ex){

            Log.d("printing exception", ex.getMessage());

            //if any exceptions are triggered save the error message to cursor an return the arraylist
            Cursor2.addRow(new Object[] { ""+ex.getMessage() });
            alc.set(1,Cursor2);
            return alc;
        }


    }

}