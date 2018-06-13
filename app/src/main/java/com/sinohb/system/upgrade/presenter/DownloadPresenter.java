package com.sinohb.system.upgrade.presenter;

import com.sinohb.system.upgrade.BasePresenter;
import com.sinohb.system.upgrade.BaseView;
import com.sinohb.system.upgrade.entity.UpgradeEntity;

public interface DownloadPresenter {

    public interface View extends BaseView<Controller> {

        void downloadProcess(int process);

        void complete();

        void failure();

        void notifyFileName(String fileName);

        void notifyFileSize(String size);

        void notifyTaskCanceled();

        void notifyUpgradeInfo(UpgradeEntity entity);

        void notifyMD5(String md5);

        void updateDirectly();

        void destroy();
    }


    public interface Controller extends BasePresenter {

        void pause();

        void resume();

        void cancel();

        void download(String url);

        void taskCanceled();

        boolean isTaskStart();

        boolean isPause();

        void update();
    }

}
