package com.android.settings.location;

import android.view.View;
import android.content.Context;
import android.widget.Spinner;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Toast;

import android.net.Uri;
import android.database.Cursor;
import android.content.ContentResolver;
import android.content.ContentValues;

import com.android.settings.R;
import com.android.settings.compatible.CompUtils;
import com.android.settings.utils.LogTools;
import com.android.settings.utils.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;

import android.net.LocalSocket;
import android.net.LocalSocketAddress;
import android.net.LocalSocketAddress.Namespace;
import java.io.OutputStream;

import android.provider.Settings;

public class SetGpsController {
    public static final String REGION_URI = "content://com.boringdroid.systemuiprovider.region";

    Spinner spCountry;
    Spinner spProvince;
    Spinner spCity;

    ImageView imgSave;

    ArrayAdapter<String> countryAdapter;
    ArrayAdapter<String> provinceAdapter;
    ArrayAdapter<String> cityAdapter;

    List<String> listCountrys;
    List<String> listProvinces;
    List<String> listCitys;

    List<String> listCityIds;

    Context context;

    String cityId;

    int indexCountry = 0;
    int indexProvince = 0;
    int indexCity = 0;
    boolean isChineseLanguage;

    public SetGpsController(Context context, View rootView) {
        this.context = context;
        initView(rootView);
        isChineseLanguage = CompUtils.isChineseLanguage(context);
        initData();
    }

    private void initView(View rootView) {
        spCountry = (Spinner) rootView.findViewById(R.id.spCountry);
        spProvince = (Spinner) rootView.findViewById(R.id.spProvince);
        spCity = (Spinner) rootView.findViewById(R.id.spCity);
        imgSave = (ImageView) rootView.findViewById(R.id.imgSave);
    }

    private void initData() {
        listCountrys = new ArrayList<>();
        listProvinces = new ArrayList<>();
        listCitys = new ArrayList<>();

        ArrayAdapter<String> countryAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item,
                listCountrys);
        countryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spCountry.setAdapter(countryAdapter);

        provinceAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, listProvinces);
        provinceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spProvince.setAdapter(provinceAdapter);

        cityAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, listCitys);
        cityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spCity.setAdapter(cityAdapter);

        imgSave.setOnClickListener(view -> {
            String locationGps = spCountry.getSelectedItemId() + "~" + spProvince.getSelectedItemId() + "~"
                    + spCity.getSelectedItemId();
            Settings.Global.putString(context.getContentResolver(), "locationGps", locationGps);
            setGps(cityId);
        });

        String locationGps = Settings.Global.getString(context.getContentResolver(), "locationGps");
        LogTools.i("locationGps: " + locationGps);
        if (locationGps != null) {
            String[] arrLocationGps = locationGps.split("~");
            try {
                indexCountry = StringUtils.ToInt(arrLocationGps[0]);
                indexProvince = StringUtils.ToInt(arrLocationGps[1]);
                indexCity = StringUtils.ToInt(arrLocationGps[2]);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        spCountry.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                LogTools.i("spCountry onItemSelected: " + i);
                listProvinces.clear();
                List<String> tempPList = queryProvincesByCountry(listCountrys.get(i));
                if (tempPList != null) {
                    listProvinces.addAll(tempPList);
                    provinceAdapter.notifyDataSetChanged();

                    if (i != indexCountry) {
                        spProvince.setSelection(0);
                    }

                    List<String> tempCList = queryCitysByProvince(listProvinces.get(0));
                    if (tempCList != null) {
                        listCitys.clear();
                        listCitys.addAll(tempCList);
                        cityAdapter.notifyDataSetChanged();
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        spProvince.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                LogTools.i("spProvince onItemSelected: " + i);
                listCitys.clear();
                List<String> tempCList = queryCitysByProvince(listProvinces.get(i));
                if (tempCList != null) {
                    listCitys.addAll(tempCList);
                    cityAdapter.notifyDataSetChanged();
                    if (i != indexProvince) {
                        spCity.setSelection(0);
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        spCity.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                LogTools.i("spCity onItemSelected: " + i);
                cityId = listCityIds.get(i);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        try {
            LogTools.i("queryAllCountry  indexCountry: " + indexCountry + ",indexProvince: " + indexProvince
                    + ",indexCity:"
                    + indexCity);
            List<String> tempList = queryAllCountry();
            if (tempList != null) {
                listCountrys.addAll(queryAllCountry());
                countryAdapter.notifyDataSetChanged();

                if (tempList.size() > 0) {
                    List<String> tempPList = queryProvincesByCountry(listCountrys.get(0));
                    if (tempPList != null) {
                        listProvinces.addAll(tempPList);
                        provinceAdapter.notifyDataSetChanged();

                        List<String> tempCList = queryCitysByProvince(listProvinces.get(0));
                        if (tempCList != null) {
                            listCitys.addAll(tempCList);
                            cityAdapter.notifyDataSetChanged();

                            spCountry.setSelection(indexCountry);
                            spProvince.setSelection(indexProvince);
                            spCity.setSelection(indexCity);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private List<String> queryAllCountry() {
        Uri uri = Uri.parse(REGION_URI + "/REGION_COUNTRY");
        Cursor cursor = null;
        Map<String, Object> result = null;
        String selection = null;
        String[] selectionArgs = null;
        List<String> list = null;
        try {
            ContentResolver contentResolver = context.getContentResolver();
            cursor = contentResolver.query(uri, null, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                list = new ArrayList<>();
                do {
                    if (isChineseLanguage) {
                        String COUNTRY_NAME = cursor.getString(cursor.getColumnIndex("COUNTRY_NAME"));
                        list.add(COUNTRY_NAME);
                    } else {
                        String COUNTRY_NAME_EN = cursor.getString(cursor.getColumnIndex("COUNTRY_NAME_EN"));
                        list.add(COUNTRY_NAME_EN);
                    }

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

    private List<String> queryProvincesByCountry(String countryName) {
        Uri uri = Uri.parse(REGION_URI + "/REGION_PROVINCE");
        Cursor cursor = null;
        Map<String, Object> result = null;
        String selection = "COUNTRY_NAME = ?";
        if (!isChineseLanguage) {
            selection = "COUNTRY_NAME_EN = ?";
        }
        String[] selectionArgs = { countryName };
        List<String> list = null;

        try {
            ContentResolver contentResolver = context.getContentResolver();
            cursor = contentResolver.query(uri, null, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                list = new ArrayList<>();
                do {
                    if (isChineseLanguage) {
                        String PROVINCE_NAME = cursor.getString(cursor.getColumnIndex("PROVINCE_NAME"));
                        list.add(PROVINCE_NAME);
                    } else {
                        String PROVINCE_NAME_EN = cursor.getString(cursor.getColumnIndex("PROVINCE_NAME_EN"));
                        list.add(PROVINCE_NAME_EN);
                    }

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

    private List<String> queryCitysByProvince(String province) {
        Uri uri = Uri.parse(REGION_URI + "/REGION_INFO");
        Cursor cursor = null;
        Map<String, Object> result = null;
        String selection = "PROVINCE_NAME = ?";
        if (!isChineseLanguage) {
            selection = "PROVINCE_NAME_EN = ?";
        }
        String[] selectionArgs = { province };
        List<String> list = null;
        listCityIds = new ArrayList<>();
        listCityIds.clear();

        try {
            ContentResolver contentResolver = context.getContentResolver();
            cursor = contentResolver.query(uri, null, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                list = new ArrayList<>();
                do {
                    String CITY_ID = cursor.getString(cursor.getColumnIndex("CITY_ID"));
                    listCityIds.add(CITY_ID);
                    if (isChineseLanguage) {
                        String CITY_NAME = cursor.getString(cursor.getColumnIndex("CITY_NAME"));
                        list.add(CITY_NAME);
                    } else {
                        String CITY_NAME_EN = cursor.getString(cursor.getColumnIndex("CITY_NAME_EN"));
                        list.add(CITY_NAME_EN);
                    }

                } while (cursor.moveToNext());
            }
            cityId = listCityIds.get(0);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return list;
    }

    private void setGps(String value) {
        LogTools.i("setGps value: " + value);
        String cityValue = value.replace("CI_", "");
        String address = "/tmp/unix.str";
        LocalSocket clientSocket = new LocalSocket();
        LocalSocketAddress locSockAddr = new LocalSocketAddress(address, Namespace.FILESYSTEM);
        OutputStream clientOutStream = null;
        try {
            clientSocket.connect(locSockAddr);
            clientOutStream = clientSocket.getOutputStream();
            clientOutStream.write(value.getBytes());
            clientSocket.shutdownOutput();
            clientSocket.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {

        }
        Toast.makeText(context, context.getString(R.string.set_success),
                Toast.LENGTH_SHORT)
                .show();
    }

}
