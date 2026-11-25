package com.example.pbl.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.pbl.model.EmergencyContact;
import com.example.pbl.model.Incident;
import com.example.pbl.model.UserProfile;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "golden_hour.db";
    private static final int DATABASE_VERSION = 2;

    private static DatabaseHelper instance;

    // Table names
    private static final String TABLE_PROFILE = "user_profile";
    private static final String TABLE_CONTACTS = "emergency_contacts";
    private static final String TABLE_INCIDENTS = "incidents";

    // User Profile Columns
    private static final String KEY_PROFILE_ID = "id";
    private static final String KEY_PROFILE_NAME = "name";
    private static final String KEY_PROFILE_PHONE = "phone";
    private static final String KEY_PROFILE_BLOOD_GROUP = "blood_group";
    private static final String KEY_PROFILE_ALLERGIES = "allergies";
    private static final String KEY_PROFILE_CONDITIONS = "medical_conditions";
    private static final String KEY_PROFILE_AGE = "age";
    private static final String KEY_PROFILE_HEIGHT_WEIGHT = "height_weight";
    private static final String KEY_PROFILE_MEDICATIONS = "medications";
    private static final String KEY_PROFILE_SURGERIES = "surgeries";
    private static final String KEY_PROFILE_ORGAN_DONOR = "organ_donor";
    private static final String KEY_PROFILE_NOTES = "notes";

    // Contact Columns
    private static final String KEY_CONTACT_ID = "id";
    private static final String KEY_CONTACT_NAME = "name";
    private static final String KEY_CONTACT_PHONE = "phone";
    private static final String KEY_CONTACT_RELATIONSHIP = "relationship";

    // Incident Columns
    private static final String KEY_INCIDENT_ID = "id";
    private static final String KEY_INCIDENT_TYPE = "type";
    private static final String KEY_INCIDENT_TIMESTAMP = "timestamp";
    private static final String KEY_INCIDENT_LOCATION = "location";
    private static final String KEY_INCIDENT_STATUS = "status";
    private static final String KEY_INCIDENT_CRITICAL = "critical";
    private static final String KEY_INCIDENT_DETAILS = "details";

    public static synchronized DatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseHelper(context.getApplicationContext());
        }
        return instance;
    }

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_PROFILE_TABLE = "CREATE TABLE " + TABLE_PROFILE + "("
                + KEY_PROFILE_ID + " INTEGER PRIMARY KEY,"
                + KEY_PROFILE_NAME + " TEXT,"
                + KEY_PROFILE_PHONE + " TEXT,"
                + KEY_PROFILE_BLOOD_GROUP + " TEXT,"
                + KEY_PROFILE_ALLERGIES + " TEXT,"
                + KEY_PROFILE_CONDITIONS + " TEXT,"
                + KEY_PROFILE_AGE + " TEXT,"
                + KEY_PROFILE_HEIGHT_WEIGHT + " TEXT,"
                + KEY_PROFILE_MEDICATIONS + " TEXT,"
                + KEY_PROFILE_SURGERIES + " TEXT,"
                + KEY_PROFILE_ORGAN_DONOR + " TEXT,"
                + KEY_PROFILE_NOTES + " TEXT" + ")";

        String CREATE_CONTACTS_TABLE = "CREATE TABLE " + TABLE_CONTACTS + "("
                + KEY_CONTACT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + KEY_CONTACT_NAME + " TEXT,"
                + KEY_CONTACT_PHONE + " TEXT,"
                + KEY_CONTACT_RELATIONSHIP + " TEXT" + ")";

        String CREATE_INCIDENTS_TABLE = "CREATE TABLE " + TABLE_INCIDENTS + "("
                + KEY_INCIDENT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + KEY_INCIDENT_TYPE + " TEXT,"
                + KEY_INCIDENT_TIMESTAMP + " INTEGER,"
                + KEY_INCIDENT_LOCATION + " TEXT,"
                + KEY_INCIDENT_STATUS + " TEXT,"
                + KEY_INCIDENT_CRITICAL + " INTEGER,"
                + KEY_INCIDENT_DETAILS + " TEXT" + ")";

        db.execSQL(CREATE_PROFILE_TABLE);
        db.execSQL(CREATE_CONTACTS_TABLE);
        db.execSQL(CREATE_INCIDENTS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            try {
                db.execSQL("ALTER TABLE " + TABLE_PROFILE + " ADD COLUMN " + KEY_PROFILE_AGE + " TEXT");
                db.execSQL("ALTER TABLE " + TABLE_PROFILE + " ADD COLUMN " + KEY_PROFILE_HEIGHT_WEIGHT + " TEXT");
                db.execSQL("ALTER TABLE " + TABLE_PROFILE + " ADD COLUMN " + KEY_PROFILE_MEDICATIONS + " TEXT");
                db.execSQL("ALTER TABLE " + TABLE_PROFILE + " ADD COLUMN " + KEY_PROFILE_SURGERIES + " TEXT");
                db.execSQL("ALTER TABLE " + TABLE_PROFILE + " ADD COLUMN " + KEY_PROFILE_ORGAN_DONOR + " TEXT");
                db.execSQL("ALTER TABLE " + TABLE_PROFILE + " ADD COLUMN " + KEY_PROFILE_NOTES + " TEXT");
            } catch (Exception e) {
                db.execSQL("DROP TABLE IF EXISTS " + TABLE_PROFILE);
                db.execSQL("DROP TABLE IF EXISTS " + TABLE_CONTACTS);
                db.execSQL("DROP TABLE IF EXISTS " + TABLE_INCIDENTS);
                onCreate(db);
            }
        }
    }

    // --- User Profile Operations ---
    public void saveUserProfile(UserProfile profile) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_PROFILE_ID, 1);
        values.put(KEY_PROFILE_NAME, profile.getName());
        values.put(KEY_PROFILE_PHONE, profile.getPhone());
        values.put(KEY_PROFILE_BLOOD_GROUP, profile.getBloodGroup());
        values.put(KEY_PROFILE_ALLERGIES, profile.getAllergies());
        values.put(KEY_PROFILE_CONDITIONS, profile.getMedicalConditions());
        values.put(KEY_PROFILE_AGE, profile.getAge());
        values.put(KEY_PROFILE_HEIGHT_WEIGHT, profile.getHeightWeight());
        values.put(KEY_PROFILE_MEDICATIONS, profile.getMedications());
        values.put(KEY_PROFILE_SURGERIES, profile.getSurgeries());
        values.put(KEY_PROFILE_ORGAN_DONOR, profile.getOrganDonor());
        values.put(KEY_PROFILE_NOTES, profile.getNotes());

        db.insertWithOnConflict(TABLE_PROFILE, null, values, SQLiteDatabase.CONFLICT_REPLACE);
    }

    public UserProfile getUserProfile() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_PROFILE, null, KEY_PROFILE_ID + "=?",
                new String[]{"1"}, null, null, null);

        UserProfile profile = null;
        if (cursor != null && cursor.moveToFirst()) {
            profile = new UserProfile(
                    cursor.getString(cursor.getColumnIndexOrThrow(KEY_PROFILE_NAME)),
                    cursor.getString(cursor.getColumnIndexOrThrow(KEY_PROFILE_PHONE)),
                    cursor.getString(cursor.getColumnIndexOrThrow(KEY_PROFILE_BLOOD_GROUP)),
                    cursor.getString(cursor.getColumnIndexOrThrow(KEY_PROFILE_ALLERGIES)),
                    cursor.getString(cursor.getColumnIndexOrThrow(KEY_PROFILE_CONDITIONS)),
                    getSafeString(cursor, KEY_PROFILE_AGE),
                    getSafeString(cursor, KEY_PROFILE_HEIGHT_WEIGHT),
                    getSafeString(cursor, KEY_PROFILE_MEDICATIONS),
                    getSafeString(cursor, KEY_PROFILE_SURGERIES),
                    getSafeString(cursor, KEY_PROFILE_ORGAN_DONOR),
                    getSafeString(cursor, KEY_PROFILE_NOTES)
            );
            cursor.close();
        }
        return profile;
    }

    private String getSafeString(Cursor cursor, String columnName) {
        int index = cursor.getColumnIndex(columnName);
        if (index != -1 && !cursor.isNull(index)) {
            return cursor.getString(index);
        }
        return "";
    }

    // --- Emergency Contacts Operations ---
    public long addContact(EmergencyContact contact) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_CONTACT_NAME, contact.getName());
        values.put(KEY_CONTACT_PHONE, contact.getPhone());
        values.put(KEY_CONTACT_RELATIONSHIP, contact.getRelationship());

        return db.insert(TABLE_CONTACTS, null, values);
    }

    public List<EmergencyContact> getAllContacts() {
        List<EmergencyContact> contactList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_CONTACTS + " ORDER BY " + KEY_CONTACT_NAME + " ASC", null);

        if (cursor.moveToFirst()) {
            do {
                EmergencyContact contact = new EmergencyContact(
                        cursor.getLong(cursor.getColumnIndexOrThrow(KEY_CONTACT_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(KEY_CONTACT_NAME)),
                        cursor.getString(cursor.getColumnIndexOrThrow(KEY_CONTACT_PHONE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(KEY_CONTACT_RELATIONSHIP))
                );
                contactList.add(contact);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return contactList;
    }

    public boolean deleteContact(long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_CONTACTS, KEY_CONTACT_ID + "=?", new String[]{String.valueOf(id)}) > 0;
    }

    // --- Incident Log Operations ---
    public long addIncident(Incident incident) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_INCIDENT_TYPE, incident.getType());
        values.put(KEY_INCIDENT_TIMESTAMP, incident.getTimestamp());
        values.put(KEY_INCIDENT_LOCATION, incident.getLocation());
        values.put(KEY_INCIDENT_STATUS, incident.getStatus());
        values.put(KEY_INCIDENT_CRITICAL, incident.isCritical() ? 1 : 0);
        values.put(KEY_INCIDENT_DETAILS, incident.getDetails());

        return db.insert(TABLE_INCIDENTS, null, values);
    }

    public List<Incident> getAllIncidents() {
        List<Incident> incidentList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_INCIDENTS + " ORDER BY " + KEY_INCIDENT_TIMESTAMP + " DESC", null);

        if (cursor.moveToFirst()) {
            do {
                Incident incident = new Incident(
                        cursor.getLong(cursor.getColumnIndexOrThrow(KEY_INCIDENT_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(KEY_INCIDENT_TYPE)),
                        cursor.getLong(cursor.getColumnIndexOrThrow(KEY_INCIDENT_TIMESTAMP)),
                        cursor.getString(cursor.getColumnIndexOrThrow(KEY_INCIDENT_LOCATION)),
                        cursor.getString(cursor.getColumnIndexOrThrow(KEY_INCIDENT_STATUS)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(KEY_INCIDENT_CRITICAL)) == 1,
                        cursor.getString(cursor.getColumnIndexOrThrow(KEY_INCIDENT_DETAILS))
                );
                incidentList.add(incident);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return incidentList;
    }

    public void updateIncidentStatus(long id, String status) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_INCIDENT_STATUS, status);
        db.update(TABLE_INCIDENTS, values, KEY_INCIDENT_ID + "=?", new String[]{String.valueOf(id)});
    }
}
