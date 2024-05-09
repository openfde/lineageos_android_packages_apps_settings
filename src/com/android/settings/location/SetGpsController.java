package com.android.settings.location;

import android.view.View;
import android.content.Context;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.TextView;

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
import java.lang.reflect.Field;

import android.provider.Settings;
import android.graphics.PorterDuff;
import android.widget.PopupWindow;
import android.annotation.SuppressLint;
import android.graphics.drawable.ColorDrawable;
import android.view.MotionEvent;
import androidx.core.widget.PopupWindowCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.WindowManager;
import android.widget.FrameLayout;

public class SetGpsController {
    public static final String REGION_URI = "content://com.boringdroid.systemuiprovider.region";

    ImageView imgSave;

    TextView txtCountry;
    TextView txtProvince;
    TextView txtCity;

    List<String> listCountrys;
    List<String> listProvinces;
    List<String> listCitys;

    List<String> listCityGps;

    Context context;

    String gpsValue;

    int indexCountry = 0;
    int indexProvince = 0;
    int indexCity = 0;
    boolean isChineseLanguage;

    SimpleAdapter adapterCountry;
    SimpleAdapter adapterProvince;
    SimpleAdapter adapterCity;

    public SetGpsController(Context context, View rootView) {
        this.context = context;
        initView(rootView);
        isChineseLanguage = CompUtils.isChineseLanguage(context);
        initData();
    }

    private void initView(View rootView) {
        imgSave = (ImageView) rootView.findViewById(R.id.imgSave);
        txtCountry = (TextView) rootView.findViewById(R.id.txtCountry);
        txtProvince = (TextView) rootView.findViewById(R.id.txtProvince);
        txtCity = (TextView) rootView.findViewById(R.id.txtCity);
        // imgSave.setColorFilter(R.color.blue, PorterDuff.Mode.SRC_IN);
        txtCountry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                initPopWindow(txtCountry, adapterCountry);
            }
        });

        txtProvince.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                initPopWindow(txtProvince, adapterProvince);
            }
        });

        txtCity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                initPopWindow(txtCity, adapterCity);
            }
        });
    }

    private void initData() {
        listCountrys = new ArrayList<>();
        listProvinces = new ArrayList<>();
        listCitys = new ArrayList<>();

        adapterCountry = new SimpleAdapter(context, listCountrys, new SimpleAdapter.ItemClick() {
            @Override
            public void setOnItemClick(int pos) {
                txtCountry.setText(listCountrys.get(pos));
                popWindow.dismiss();
                listProvinces.clear();
                List<String> tempPList = queryProvincesByCountry(listCountrys.get(pos));
                if (tempPList != null) {
                    listProvinces.addAll(tempPList);
                    adapterProvince.notifyDataSetChanged();

                    if (pos != indexCountry) {
                        txtProvince.setText(listProvinces.get(0));
                    }
                    indexCountry = pos;
                    List<String> tempCList = queryCitysByProvince(listProvinces.get(0));
                    if (tempCList != null) {
                        listCitys.clear();
                        listCitys.addAll(tempCList);
                        adapterCity.notifyDataSetChanged();
                    }
                }
            }
        });
        adapterProvince = new SimpleAdapter(context, listProvinces, new SimpleAdapter.ItemClick() {
            @Override
            public void setOnItemClick(int pos) {
                txtProvince.setText(listProvinces.get(pos));
                popWindow.dismiss();
                listCitys.clear();
                List<String> tempCList = queryCitysByProvince(listProvinces.get(pos));
                if (tempCList != null) {
                    listCitys.addAll(tempCList);
                    adapterCity.notifyDataSetChanged();
                    if (pos != indexProvince) {
                        txtCity.setText(listCitys.get(0));
                    }
                    indexProvince = pos;
                }
            }
        });
        adapterCity = new SimpleAdapter(context, listCitys, new SimpleAdapter.ItemClick() {
            @Override
            public void setOnItemClick(int pos) {
                txtCity.setText(listCitys.get(pos));
                popWindow.dismiss();
                gpsValue = listCityGps.get(pos);
                indexCity = pos;
            }
        });

        imgSave.setOnClickListener(view -> {
            // String locationGps = spCountry.getSelectedItemId() + "~" +
            // spProvince.getSelectedItemId() + "~"
            // + spCity.getSelectedItemId();
            String locationGps = indexCountry + "~" + indexProvince + "~"
                    + indexCity;
            Settings.Global.putString(context.getContentResolver(), "locationGps", locationGps);
            setGps(gpsValue);
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

        try {
            LogTools.i("queryAllCountry  indexCountry: " + indexCountry + ",indexProvince: " + indexProvince
                    + ",indexCity:"
                    + indexCity);
            List<String> tempList = queryAllCountry();
            if (tempList != null) {
                listCountrys.addAll(queryAllCountry());
                adapterCountry.notifyDataSetChanged();

                if (tempList.size() > 0) {
                    List<String> tempPList = queryProvincesByCountry(listCountrys.get(indexCountry));
                    if (tempPList != null) {
                        listProvinces.addAll(tempPList);
                        adapterProvince.notifyDataSetChanged();

                        List<String> tempCList = queryCitysByProvince(listProvinces.get(indexProvince));
                        if (tempCList != null) {
                            listCitys.addAll(tempCList);
                            adapterCity.notifyDataSetChanged();
                            txtCountry.setText(listCountrys.get(indexCountry));
                            txtProvince.setText(listProvinces.get(indexProvince));
                            txtCity.setText(listCitys.get(indexCity));
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private PopupWindow popWindow;

    @SuppressLint("WrongConstant")
    private void initPopWindow(View v, SimpleAdapter simpleAdapter) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_popip, null, false);
        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
        LinearLayoutManager lm = new LinearLayoutManager(context);
        lm.setOrientation(RecyclerView.VERTICAL);
        recyclerView.setLayoutManager(lm);
        recyclerView.setAdapter(simpleAdapter);

        if (null == popWindow) {
            popWindow = new PopupWindow(view,
                    200, FrameLayout.LayoutParams.WRAP_CONTENT, true);
            popWindow.setFocusable(false);// 底部导航消失
            popWindow.setSoftInputMode(popWindow.INPUT_METHOD_NEEDED);
            popWindow.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
            popWindow.setTouchable(true);
            popWindow.setTouchInterceptor(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    return false;
                }
            });
            //
            popWindow.setOutsideTouchable(true);//
            popWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {// 消失后的处理
                @Override
                public void onDismiss() {
                    popWindow = null;
                }
            });
            // 要为popWindow设置一个背景才有效
            popWindow.setBackgroundDrawable(new ColorDrawable(0x00000000));
            PopupWindowCompat.showAsDropDown(popWindow, v, 0, 0, Gravity.START);
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
        listCityGps = new ArrayList<>();

        try {
            ContentResolver contentResolver = context.getContentResolver();
            cursor = contentResolver.query(uri, null, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                list = new ArrayList<>();
                do {
                    String CITY_ID = cursor.getString(cursor.getColumnIndex("CITY_ID"));
                    String GPS = cursor.getString(cursor.getColumnIndex("GPS"));
                    listCityGps.add(GPS);
                    if (isChineseLanguage) {
                        String CITY_NAME = cursor.getString(cursor.getColumnIndex("CITY_NAME"));
                        list.add(CITY_NAME);
                    } else {
                        String CITY_NAME_EN = cursor.getString(cursor.getColumnIndex("CITY_NAME_EN"));
                        list.add(CITY_NAME_EN);
                    }

                } while (cursor.moveToNext());
            }
            gpsValue = listCityGps.get(0);
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
        value = value.replace("\n", "").trim();
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
