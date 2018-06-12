package com.sinohb.system.upgrade.view;

import android.app.Dialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.sinohb.logger.SystemApplication;
import com.sinohb.system.upgrade.R;
import com.sinohb.system.upgrade.presenter.DownloadPresenter;

public class UpgradeDialog extends Dialog implements View.OnClickListener {
    private TextView newest_version_tv;
    private TextView version_size_tv;
    private TextView update_content_tv;
    private TextView update_tv;
    private TextView update_later_tv;
    private DownloadPresenter.Controller controller;
    public UpgradeDialog(@NonNull Context context,DownloadPresenter.Controller controller) {
        this(context,R.style.themeDialog);
        initDialog();
        this.controller = controller;
    }
    public UpgradeDialog(@NonNull Context context) {
        this(context,R.style.themeDialog);
        initDialog();
    }

    public UpgradeDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
        initDialog();
    }

    protected UpgradeDialog(@NonNull Context context, boolean cancelable, @Nullable OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
        initDialog();
    }

    private void initDialog() {
        View rootView = getLayoutInflater().inflate(R.layout.activity_upgrade, null, false);
        setContentView(rootView);
        getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        initView(rootView);
    }

    private void initView(View rootView) {
        newest_version_tv = (TextView) rootView.findViewById(R.id.newest_version_tv);
        version_size_tv = (TextView) rootView.findViewById(R.id.version_size_tv);
        update_content_tv = (TextView) rootView.findViewById(R.id.update_content_tv);
        update_tv = (TextView) rootView.findViewById(R.id.update_tv);
        update_later_tv = (TextView) rootView.findViewById(R.id.update_later_tv);
        update_tv.setOnClickListener(this);
        update_later_tv.setOnClickListener(this);
        update_content_tv.setMovementMethod(ScrollingMovementMethod.getInstance());
    }

    public void setVersion(String version) {
        if (newest_version_tv != null) {
            newest_version_tv.setText(SystemApplication.getContext().getResources().getString(R.string.dialog_newest_version) + version);
        }
    }

    public void setVersionSize(String size) {
        if (version_size_tv != null) {
            version_size_tv.setText(SystemApplication.getContext().getResources().getString(R.string.dialog_newest_file_size) + size);
        }
    }
    public void setUpdateContent(String updateContent){
        if (update_content_tv!=null){
            update_content_tv.setText(updateContent);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.update_tv:
                controller.update();
                break;
            case R.id.update_later_tv:
                break;
        }
        dismiss();
    }
}
