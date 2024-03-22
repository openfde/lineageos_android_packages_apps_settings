package com.android.settings.compatible;

import android.content.ContentResolver;
import android.net.Uri;
import android.database.Cursor;
import android.content.ContentValues;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;

public class CompatibleConfig {
    public static final String COMPATIBLE_URI = "content://com.boringdroid.systemuiprovider";
    public static final String KEY_CODE_IS_ALLOW_SCREENSHOT_AND_RECORD = "isAllowScreenshotAndRecord";
    public static final String KEY_CODE_IS_ALLOW_HIDE_DECOR_CAPTION = "isAllowHideDecorCaption";

    public static Map<String, Object> queryMapValueData(Context context, String packageName, String keycode) {
        Uri uri = Uri.parse(COMPATIBLE_URI + "/COMPATIBLE_VALUE");
        Cursor cursor = null;
        Map<String, Object> result = null;
        String selection = "PACKAGE_NAME = ? AND KEY_CODE = ? AND IS_DEL != 1 ";
        String[] selectionArgs = { packageName, keycode };
        try {
            ContentResolver contentResolver = context.getContentResolver();
            cursor = contentResolver.query(uri, null, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                int _ID = cursor.getInt(cursor.getColumnIndex("_ID"));
                String PACKAGE_NAME = cursor.getString(cursor.getColumnIndex("PACKAGE_NAME"));
                String KEY_CODE = cursor.getString(cursor.getColumnIndex("KEY_CODE"));
                String VALUE = cursor.getString(cursor.getColumnIndex("VALUE"));
                String EDIT_DATE = cursor.getString(cursor.getColumnIndex("EDIT_DATE"));
                result = new HashMap<>();
                result.put("_ID", _ID);
                result.put("PACKAGE_NAME", PACKAGE_NAME);
                result.put("KEY_CODE", KEY_CODE);
                result.put("VALUE", VALUE);
                result.put("EDIT_DATE", EDIT_DATE);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return result;
    }

    public static Map<String, Object> queryMapValueDataHasDel(Context context, String packageName, String keycode) {
        Uri uri = Uri.parse(COMPATIBLE_URI + "/COMPATIBLE_VALUE");
        Cursor cursor = null;
        Map<String, Object> result = null;
        String selection = "PACKAGE_NAME = ? AND KEY_CODE = ? ";
        String[] selectionArgs = { packageName, keycode };
        try {
            ContentResolver contentResolver = context.getContentResolver();
            cursor = contentResolver.query(uri, null, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                int _ID = cursor.getInt(cursor.getColumnIndex("_ID"));
                String PACKAGE_NAME = cursor.getString(cursor.getColumnIndex("PACKAGE_NAME"));
                String KEY_CODE = cursor.getString(cursor.getColumnIndex("KEY_CODE"));
                String VALUE = cursor.getString(cursor.getColumnIndex("VALUE"));
                String EDIT_DATE = cursor.getString(cursor.getColumnIndex("EDIT_DATE"));
                result = new HashMap<>();
                result.put("_ID", _ID);
                result.put("PACKAGE_NAME", PACKAGE_NAME);
                result.put("KEY_CODE", KEY_CODE);
                result.put("VALUE", VALUE);
                result.put("EDIT_DATE", EDIT_DATE);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return result;
    }

    public static String queryValueData(Context context, String packageName, String keycode) {
        Uri uri = Uri.parse(COMPATIBLE_URI + "/COMPATIBLE_VALUE");
        Cursor cursor = null;
        String result = null;
        String selection = "PACKAGE_NAME = ? AND KEY_CODE = ?  AND IS_DEL != 1";
        String[] selectionArgs = { packageName, keycode };
        try {
            ContentResolver contentResolver = context.getContentResolver();
            cursor = contentResolver.query(uri, null, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                result = cursor.getString(cursor.getColumnIndex("VALUE"));
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return result;
    }

    public static void insertValueData(Context context, String appName, String packageName, String keycode,
            String value) {
        try {
            Uri uri = Uri.parse(COMPATIBLE_URI + "/COMPATIBLE_VALUE");
            ContentValues values = new ContentValues();
            values.put("PACKAGE_NAME", packageName);
            values.put("KEY_CODE", keycode);
            values.put("VALUE", value);
            values.put("EDIT_DATE", getCurDateTime());
            values.put("FIELDS1", appName);
            values.put("IS_DEL", "0");
            Uri resUri = context.getContentResolver()
                    .insert(uri, values);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void insertUpdateValueData(Context context, String appName, String packageName, String keycode,
            String value) {
        Map<String, Object> result = queryMapValueDataHasDel(context, packageName, keycode);
        if (result == null) {
            insertValueData(context, appName, packageName, keycode, value);
        } else {
            updateValueData(context, appName, packageName, keycode, value);
        }
    }

    public static int updateValueData(Context context, String appName, String packageName, String keycode,
            String newValue) {
        try {
            Uri uri = Uri.parse(COMPATIBLE_URI + "/COMPATIBLE_VALUE");
            ContentValues values = new ContentValues();
            values.put("VALUE", newValue);
            values.put("FIELDS1", appName);
            values.put("IS_DEL", "0");
            values.put("EDIT_DATE", getCurDateTime());
            String selection = "PACKAGE_NAME = ? AND KEY_CODE = ?";
            String[] selectionArgs = { packageName, keycode };
            int res = context.getContentResolver()
                    .update(uri, values, selection,
                            selectionArgs);
            return res;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    public static void deleteValueData(Context context, String packageName, String keycode) {
        try {
            Uri uri = Uri.parse(COMPATIBLE_URI + "/COMPATIBLE_VALUE");
            String selection = "PACKAGE_NAME = ? AND KEY_CODE = ?";
            String[] selectionArgs = { packageName, keycode };
            int res = context.getContentResolver().delete(uri, selection, selectionArgs);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static int deleteValueData(Context context, String packageName) {
        // try {
        // Uri uri = Uri.parse(COMPATIBLE_URI + "/COMPATIBLE_VALUE");
        // String selection = "PACKAGE_NAME = ?";
        // String[] selectionArgs = { packageName };
        // int res = context.getContentResolver().delete(uri, selection, selectionArgs);
        // } catch (Exception e) {
        // e.printStackTrace();
        // }

        try {
            Uri uri = Uri.parse(COMPATIBLE_URI + "/COMPATIBLE_VALUE");
            ContentValues values = new ContentValues();
            values.put("IS_DEL", "1");
            values.put("EDIT_DATE", getCurDateTime());
            String selection = "PACKAGE_NAME = ? ";
            String[] selectionArgs = { packageName };
            int res = context.getContentResolver()
                    .update(uri, values, selection,
                            selectionArgs);
            return res;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    public static void cleanValueData(Context context) {
        try {
            Uri uri = Uri.parse(COMPATIBLE_URI + "/COMPATIBLE_VALUE");
            String selection = null;
            String[] selectionArgs = null;
            int res = context.getContentResolver().delete(uri, selection, selectionArgs);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static List<Map<String, Object>> queryValueListData(Context context, String keycode) {
        Uri uri = Uri.parse(COMPATIBLE_URI + "/COMPATIBLE_VALUE");
        List<Map<String, Object>> list = null;
        Cursor cursor = null;
        String selection = " KEY_CODE = ?  AND IS_DEL != 1";
        String[] selectionArgs = { keycode };
        try {

            ContentResolver contentResolver = context.getContentResolver();
            cursor = contentResolver.query(uri, null, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                list = new ArrayList<>();
                do {
                    int _ID = cursor.getInt(cursor.getColumnIndex("_ID"));
                    String PACKAGE_NAME = cursor.getString(cursor.getColumnIndex("PACKAGE_NAME"));
                    String KEY_CODE = cursor.getString(cursor.getColumnIndex("KEY_CODE"));
                    String EDIT_DATE = cursor.getString(cursor.getColumnIndex("EDIT_DATE"));
                    String VALUE = cursor.getString(cursor.getColumnIndex("VALUE"));
                    String NOTES = cursor.getString(cursor.getColumnIndex("NOTES"));
                    String FIELDS1 = cursor.getString(cursor.getColumnIndex("FIELDS1"));
                    Map<String, Object> mp = new HashMap<>();
                    mp.put("_ID", _ID);
                    mp.put("PACKAGE_NAME", PACKAGE_NAME);
                    mp.put("VALUE", VALUE);
                    mp.put("KEY_CODE", KEY_CODE);
                    mp.put("NOTES", NOTES);
                    mp.put("FIELDS1", FIELDS1);
                    mp.put("EDIT_DATE", EDIT_DATE);
                    list.add(mp);
                } while (cursor.moveToNext());
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return list;
    }

    public static List<Map<String, Object>> queryListData(Context context) {
        Uri uri = Uri.parse(COMPATIBLE_URI + "/COMPATIBLE_LIST");
        List<Map<String, Object>> list = null;
        Cursor cursor = null;
        String selection = null;
        String[] selectionArgs = null;
        try {

            ContentResolver contentResolver = context.getContentResolver();
            cursor = contentResolver.query(uri, null, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                list = new ArrayList<>();
                do {
                    int _ID = cursor.getInt(cursor.getColumnIndex("_ID"));
                    String KEY_CODE = cursor.getString(cursor.getColumnIndex("KEY_CODE"));
                    String KEY_DESC = cursor.getString(cursor.getColumnIndex("KEY_DESC"));
                    String CREATE_DATE = cursor.getString(cursor.getColumnIndex("CREATE_DATE"));
                    String DEFAULT_VALUE = cursor.getString(cursor.getColumnIndex("DEFAULT_VALUE"));
                    String OPTION_JSON = cursor.getString(cursor.getColumnIndex("OPTION_JSON"));
                    String INPUT_TYPE = cursor.getString(cursor.getColumnIndex("INPUT_TYPE"));
                    String NOTES = cursor.getString(cursor.getColumnIndex("NOTES"));
                    Map<String, Object> mp = new HashMap<>();
                    mp.put("_ID", _ID);
                    mp.put("DEFAULT_VALUE", DEFAULT_VALUE);
                    mp.put("OPTION_JSON", OPTION_JSON);
                    mp.put("KEY_CODE", KEY_CODE);
                    mp.put("KEY_DESC", KEY_DESC);
                    mp.put("NOTES", NOTES);
                    mp.put("CREATE_DATE", CREATE_DATE);
                    mp.put("INPUT_TYPE", INPUT_TYPE);
                    list.add(mp);
                } while (cursor.moveToNext());
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return list;
    }

    public static void recoveryValueData(Context context, String packageName, String keycode) {
        try {
            Uri uri = Uri.parse(COMPATIBLE_URI + "/RECOVERY_VALUE");
            ContentValues values = new ContentValues();
            values.put("PACKAGE_NAME", packageName);
            values.put("KEY_CODE", keycode);
            values.put("IS_DEL", "0");
            Uri resUri = context.getContentResolver()
                    .insert(uri, values);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getCurDateTime() {
        LocalDateTime currentTime = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String formattedTime = currentTime.format(formatter);
        return formattedTime;
    }

}