package com.android.settings.compatible;

import android.view.WindowManager;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.json.JSONObject;

import com.android.settings.R;
import com.android.settings.utils.LogTools;

import android.widget.TextView;

import androidx.annotation.NonNull;

public class TipDialog extends Dialog {
    Context context;
    String content;
    TextView txtContent;

    public TipDialog(@NonNull Context context, String content) {
        super(context);
        this.content = content;
        this.context = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_fde_tip_view);
        setLayout();
        txtContent = (TextView) findViewById(R.id.txtContent);
        // txtContent.setText(content);
        LogTools.i("content " + content);
        txtContent.setText(content);
        // parseJson(content);
    }

    private void setLayout() {
        getWindow().setGravity(Gravity.CENTER);
        WindowManager m = getWindow().getWindowManager();
        Display d = m.getDefaultDisplay();
        WindowManager.LayoutParams p = getWindow().getAttributes();
        getWindow().setType(WindowManager.LayoutParams.FIRST_SYSTEM_WINDOW + 24);
        getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        p.x = 0;
        p.y = 0;
        p.width = WindowManager.LayoutParams.WRAP_CONTENT;
        p.height = WindowManager.LayoutParams.WRAP_CONTENT;
        getWindow().setAttributes(p);
    }

}
