package mw.mlw.data.odklookupupdater;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by BlessK on 1/9/2018.
 */

public class DatabaseHelper  extends SQLiteOpenHelper
{
    public static String DATABASE_NAME="odk_updater.db";
    public static String TABLE_NAME="project";
    public DatabaseHelper(Context context)
    {
        super(context, DATABASE_NAME, null, 1);
        //SQLiteDatabase db=this.getWritableDatabase();//to create database
    }

    @Override
    public void onCreate(SQLiteDatabase db)
    {
        db.execSQL(
                "CREATE TABLE "+this.TABLE_NAME+"(" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "name VARCHAR(50) UNIQUE,"+
                        "ip VARCHAR(20) UNIQUE,"+
                        "auth_code VARCHAR(20))"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        db.execSQL("DROP TABLE IF EXISTS "+TABLE_NAME);
        onCreate(db);
    }
    public boolean insertData(String name, String ip, String auth_code)
    {
        SQLiteDatabase db=this.getWritableDatabase();
        ContentValues contentValues=new ContentValues();
        contentValues.put("name",name);
        contentValues.put("ip",ip);
        contentValues.put("auth_code",auth_code);
        long result=db.insert(this.TABLE_NAME,null,contentValues);
        db.close();
        if(result==-1)
        {
            return false;
        }
        else
            return true;
    }
    public boolean editData(String name, String ip, String auth_code, String old_name)
    {
        SQLiteDatabase db=this.getWritableDatabase();
        ContentValues contentValues=new ContentValues();
        contentValues.put("name",name);
        contentValues.put("ip",ip);
        contentValues.put("auth_code",auth_code);
        long result=db.update(this.TABLE_NAME,contentValues,"name=?",new String[]{old_name});
        db.close();
        if(result==-1)
        {
            return false;
        }
        else
            return true;
    }
    public String[] getProjectSetting()
    {
        String project=null;
        String ip=null;
        String auth_code=null;
        String selectQuery = "SELECT  name,ip,auth_code FROM "+TABLE_NAME+" limit 1";
        SQLiteDatabase db = this.getReadableDatabase();
        try
        {
            //DB operation
            Cursor cursor = db.rawQuery(selectQuery, null);
            if(cursor.getCount()>0)
            {
                cursor.moveToNext();
                project=cursor.getString(0);
                ip=cursor.getString(1);
                auth_code=cursor.getString(2);

            }
            cursor.close();
        }
        finally
        {
            db.close();
            return new String[]{project,ip,auth_code};
        }

    }
}