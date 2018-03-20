package com.example.mycontactapp.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import java.sql.SQLException;

/**
 * Created by milan on 3/17/2018.
 */

public class DataBaseHelper extends OrmLiteSqliteOpenHelper{

        public static String DATABASE_NAME = "myContact.db";
        public static int DATABASE_VERSION = 2;

        private Dao<Contact, Integer> mContactDao = null;
        private Dao<PhoneNumber, Integer> mNumberDao = null;

        //Potrebno je dodati konstruktor zbog pravilne inicijalizacije biblioteke
    public DataBaseHelper(Context context) {
        super(context,
                DATABASE_NAME,
                null,
                DATABASE_VERSION);
    }

        //Prilikom kreiranja baze potrebno je da pozovemo odgovarajuce metode biblioteke
        //prilikom kreiranja moramo pozvati TableUtils.createTable za svaku tabelu koju imamo
        @Override
        public void onCreate(SQLiteDatabase database, ConnectionSource connectionSource) {
        try {
            TableUtils.createTable(connectionSource, Contact.class);
            TableUtils.createTable(connectionSource, PhoneNumber.class);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

        //kada zelimo da izmenomo tabele, moramo pozvati TableUtils.dropTable za sve tabele koje imamo
        @Override
        public void onUpgrade(SQLiteDatabase db, ConnectionSource connectionSource, int oldVersion, int newVersion) {
        try {
            TableUtils.dropTable(connectionSource, Contact.class, true);
            TableUtils.dropTable(connectionSource, PhoneNumber.class, true);
            onCreate(db, connectionSource);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

        //jedan Dao objekat sa kojim komuniciramo. Ukoliko imamo vise tabela
        //potrebno je napraviti Dao objekat za svaku tabelu
        public Dao<Contact, Integer> getContactDao() throws SQLException {
        if (mContactDao == null) {
            mContactDao = getDao(Contact.class);
        }

        return mContactDao;
    }

        public Dao<PhoneNumber, Integer> getNumberDao() throws SQLException {
        if (mNumberDao == null) {
            mNumberDao = getDao(PhoneNumber.class);
        }

        return mNumberDao;
    }

        //obavezno prilikom zatvarnaj rada sa bazom osloboditi resurse
        @Override
        public void close() {
        mContactDao = null;
        mNumberDao = null;

        super.close();
    }
    }
