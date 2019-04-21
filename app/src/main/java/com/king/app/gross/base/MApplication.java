package com.king.app.gross.base;

import android.app.Application;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.king.app.gross.conf.AppConfig;
import com.king.app.gross.model.entity.DaoMaster;
import com.king.app.gross.model.entity.DaoSession;
import com.king.app.gross.model.entity.GrossDao;
import com.king.app.gross.model.entity.MarketDao;
import com.king.app.gross.model.entity.MarketGrossDao;
import com.king.app.gross.model.entity.MovieDao;
import com.king.app.gross.utils.DebugLog;

import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.query.QueryBuilder;

/**
 * Desc:
 *
 * @author：Jing Yang
 * @date: 2018/6/6 13:46
 */
public class MApplication extends Application {

    private static MApplication instance;

    private DaoSession daoSession;
    private Database database;
    private RHelper helper;

    public static MApplication getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        instance = this;
        super.onCreate();
    }

    /**
     * 程序初始化使用外置数据库
     * 需要由外部调用，如果在onCreate里直接初始化会创建新的数据库
     */
    public void createGreenDao() {
        helper = new RHelper(getApplicationContext(), AppConfig.DB_NAME);
        database = helper.getWritableDb();
        daoSession = new DaoMaster(database).newSession();

        QueryBuilder.LOG_SQL = true;
        QueryBuilder.LOG_VALUES = true;
    }

    public void reCreateGreenDao() {
        daoSession.clear();
        helper.close();
        createGreenDao();
    }

    public Database getDatabase() {
        return database;
    }

    public DaoSession getDaoSession() {
        return daoSession;
    }

    public static class RHelper extends DaoMaster.OpenHelper {

        public RHelper(Context context, String name) {
            super(context, name);
        }

        public RHelper(Context context, String name, SQLiteDatabase.CursorFactory factory) {
            super(context, name, factory);
        }

        @Override
        public void onUpgrade(Database db, int oldVersion, int newVersion) {
            DebugLog.e(" oldVersion=" + oldVersion + ", newVersion=" + newVersion);
            switch (oldVersion) {
                case 1:
                    db.execSQL("ALTER TABLE " + MovieDao.TABLENAME + " ADD COLUMN " + MovieDao.Properties.Debut.columnName + " TEXT");
                    db.execSQL("ALTER TABLE " + MovieDao.TABLENAME + " ADD COLUMN " + MovieDao.Properties.IsReal.columnName + " INTEGER");
                    db.execSQL("ALTER TABLE " + MovieDao.TABLENAME + " ADD COLUMN " + MovieDao.Properties.Year.columnName + " INTEGER");
                case 2:
                    db.execSQL("ALTER TABLE " + GrossDao.TABLENAME + " ADD COLUMN " + GrossDao.Properties.IsTotal.columnName + " INTEGER");
                case 3:
                    db.execSQL("ALTER TABLE " + MovieDao.TABLENAME + " ADD COLUMN " + MovieDao.Properties.Budget.columnName + " INTEGER");
                case 4:
                    db.execSQL("ALTER TABLE " + MovieDao.TABLENAME + " ADD COLUMN " + MovieDao.Properties.MojoId.columnName + " TEXT");
                    MarketDao.createTable(db, true);
                    MarketGrossDao.createTable(db, true);
                    break;
            }
        }
    }
}
