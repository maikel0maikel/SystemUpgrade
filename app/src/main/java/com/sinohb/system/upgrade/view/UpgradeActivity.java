package com.sinohb.system.upgrade.view;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.TextView;

import com.sinohb.system.upgrade.R;

public class UpgradeActivity extends Activity implements View.OnClickListener {
    private TextView newest_version_tv;
    private TextView version_size_tv;
    private TextView update_content_tv;
    private TextView update_tv;
    private TextView update_later_tv;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upgrade);
        initView();
    }
    private void initView()  {
        newest_version_tv = (TextView) findViewById(R.id.newest_version_tv);
        version_size_tv = (TextView) findViewById(R.id.version_size_tv);
        update_content_tv = (TextView) findViewById(R.id.update_content_tv);
        update_tv = (TextView) findViewById(R.id.update_tv);
        update_later_tv = (TextView) findViewById(R.id.update_later_tv);
        update_tv.setOnClickListener(this);
        update_later_tv.setOnClickListener(this);
    }
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.update_tv:
                break;
            case R.id.update_later_tv:
                break;
        }
        finish();
    }
}
