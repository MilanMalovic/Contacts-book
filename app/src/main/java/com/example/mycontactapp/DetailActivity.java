package com.example.mycontactapp;

import android.Manifest;
import android.app.Dialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.SmsManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mycontactapp.db.Contact;
import com.example.mycontactapp.db.DataBaseHelper;
import com.example.mycontactapp.db.PhoneNumber;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;

import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static android.R.layout.simple_list_item_1;

/**
 * Created by milan on 3/17/2018.
 */




public class DetailActivity extends AppCompatActivity {

    ArrayAdapter adapter;
    private DataBaseHelper databaseHelper;





    public static final String EXTRA_NO = "contactNo";
    public static int MY_PERMISSIONS_REQUEST_CALL = 123;



    private Contact contact = null;
    private List<PhoneNumber> number ;
    private ListView listView = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_detail);
        setSupportActionBar(toolbar);

        listView = (ListView) findViewById(R.id.list_of_phone_numbers);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, final int i, long l) {

                final Dialog dialog = new Dialog(DetailActivity.this);
                dialog.setContentView(R.layout.call_and_sms_dialog);
                dialog.show();

                Button call = dialog.findViewById(R.id.bt_call);
                call.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        PhoneNumber num = number.get(i);
                        Intent intent = new Intent(Intent.ACTION_CALL);

                        intent.setData(Uri.parse("tel:" + num.getmHomeNumber()));
                        if (ActivityCompat.checkSelfPermission(DetailActivity.this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions(DetailActivity.this,
                                    new String[]{Manifest.permission.CALL_PHONE},
                                    MY_PERMISSIONS_REQUEST_CALL);
                            return;
                        }
                        startActivity(intent);
                        dialog.dismiss();
                    }
                });

                Button sms = dialog.findViewById(R.id.bt_sms);
                sms.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        try {
                            PhoneNumber num = number.get(i);
                            SmsManager smsManager = SmsManager.getDefault();
                            smsManager.sendTextMessage(num.getmHomeNumber(), null, "!!!", null, null);
                            dialog.dismiss();
                        } catch (Exception ex) {
                            Toast.makeText(getApplicationContext(),ex.getMessage().toString(),
                                    Toast.LENGTH_LONG).show();
                            ex.printStackTrace();
                        }
                    }
                });
            }
        });




    }

    @Override
    protected void onResume() {
        super.onResume();

        int actorNo = (Integer) getIntent().getExtras().get(EXTRA_NO);

        DataBaseHelper helper = new DataBaseHelper(this);
        Dao<Contact, Integer> contactDao = null;
        try {
            contactDao = helper.getContactDao();
        } catch (SQLException e) {
            e.printStackTrace();
        }


        try {
            contact = contactDao.queryForId(actorNo);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        ImageView contactImage = (ImageView) findViewById(R.id.detail_image);
        if (contact.getmImage() != null) {
            File imgFile = new File(contact.getmImage());
            if (imgFile.exists()) {
                Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                contactImage.setImageBitmap(myBitmap);
            }
        }


        TextView contactName = (TextView) findViewById(R.id.detail_name);
        contactName.setText(contact.getmName());

        TextView contactSurname = (TextView) findViewById(R.id.detail_surname);
        contactSurname.setText(contact.getmSurname());

        TextView contactAdress = (TextView) findViewById(R.id.detail_adress);
        contactAdress.setText(contact.getmAdress());

        listView = (ListView) findViewById(R.id.list_of_phone_numbers);

        ArrayList<String> numberList = new ArrayList<>();
        for (PhoneNumber pn : contact.getmPhoneNumber()) {
            if (pn.getmHomeNumber() != null) {
                numberList.add("Home number: " + pn.getmHomeNumber());
            } else if (pn.getmMobileNumber() != null) {
                numberList.add("Mobile number: " + pn.getmMobileNumber());
            } else if (pn.getmWorkNumber() != null) {
                numberList.add("Work number: " + pn.getmWorkNumber());
            }
        }

        ArrayAdapter<String> listAdapter = new ArrayAdapter<String>(
                this,
                simple_list_item_1,
                numberList) {
        };
        listView.setAdapter(listAdapter);


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //! Add
        getMenuInflater().inflate(R.menu.action_bar_details, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        boolean allowToast = sharedPreferences.getBoolean(getString(R.string.preferences_toast_key), false);
        boolean allowNotification = sharedPreferences.getBoolean(getString(R.string.preferences_notification_key), false);

        switch (item.getItemId()) {
            case R.id.action_add_number:

                if (allowToast == true) {
                    Toast.makeText(getApplicationContext(), "Data about movie will be added", Toast.LENGTH_LONG).show();
                }
                if (allowNotification == true) {

                    String channelId = "actor_channel_id";

                    android.support.v4.app.NotificationCompat.Builder mBuilder =
                            new android.support.v4.app.NotificationCompat.Builder(this, channelId);
                    mBuilder.setSmallIcon(R.drawable.ic_notification_add_number);
                    mBuilder.setContentTitle(getString(R.string.notification_add_number_title));
                    mBuilder.setContentText(getString(R.string.notification_add_number_text));

                    NotificationManager mNotificationManager =
                            (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

                }
                Intent intent = new Intent(DetailActivity.this,
                        AddNumberActivity.class);
                intent.putExtra(DetailActivity.EXTRA_NO, contact.getmId());
                startActivity(intent);
                return true;

            case R.id.action_edit:

                if (allowToast == true) {
                    Toast.makeText(getApplicationContext(), "Data about contact will be edited", Toast.LENGTH_LONG).show();
                }
                if (allowNotification == true) {

                    String channelId = "contact_channel_id";

                    android.support.v4.app.NotificationCompat.Builder mBuilder =
                            new android.support.v4.app.NotificationCompat.Builder(this, channelId);
                    mBuilder.setSmallIcon(R.drawable.ic_notification_edit);
                    mBuilder.setContentTitle(getString(R.string.notification_edit_title));
                    mBuilder.setContentText(getString(R.string.notification_edit_text));

                    NotificationManager mNotificationManager =
                            (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    mNotificationManager.notify(003, mBuilder.build());
                }

                intent = new Intent(DetailActivity.this,
                        AddContactActivity.class);
                intent.putExtra(DetailActivity.EXTRA_NO, contact.getmId());
                startActivity(intent);

                return true;
            case R.id.action_delete:

                if (allowToast == true) {
                    Toast.makeText(getApplicationContext(), "Data about contact will be deleted", Toast.LENGTH_LONG).show();
                }
                if (allowNotification == true) {

                    String channelId = "actor_channel_id";

                    android.support.v4.app.NotificationCompat.Builder mBuilder =
                            new android.support.v4.app.NotificationCompat.Builder(this, channelId);
                    mBuilder.setSmallIcon(R.drawable.ic_notification_delete);
                    mBuilder.setContentTitle(getString(R.string.notification_delete_title));
                    mBuilder.setContentText(getString(R.string.notification_delete_text));

                    NotificationManager mNotificationManager =
                            (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

                    mNotificationManager.notify(001, mBuilder.build());

                }

                int actorNo = (Integer) getIntent().getExtras().get(EXTRA_NO);
                DataBaseHelper helper = new DataBaseHelper(this);

                Dao<Contact, Integer> contactDao = null;
                try {
                    contactDao = helper.getContactDao();
                    contactDao.deleteById(actorNo);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                finish();
                return true;
            case R.id.delete_number:

                try {
                    for (PhoneNumber n : number) {
                        getDatabaseHelper().getNumberDao().delete(n);
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                refresh();
                return true;





            default:
                return super.onOptionsItemSelected(item);
        }
    }



    private void refresh() {

        if (adapter != null) {
            adapter.clear();
            try {
                number = getDatabaseHelper().getNumberDao().queryBuilder()
                        .where()
                        .eq(PhoneNumber.FIELD_NAME_CONTACT, contact.getmId())
                        .query();
                List<String> numberList = new ArrayList<>();
                String[] categories = getResources().getStringArray(R.array.spinner_number_category);

                for (PhoneNumber n : number) {
                    numberList.add(categories[n.getCategory()] + " : " + n.getmHomeNumber());
                }
                adapter.addAll(numberList);

                adapter.notifyDataSetChanged();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

    }
    public DataBaseHelper getDatabaseHelper() {
        if (databaseHelper == null) {
            databaseHelper = OpenHelperManager.getHelper(this, DataBaseHelper.class);
        }
        return databaseHelper;
    }

}


