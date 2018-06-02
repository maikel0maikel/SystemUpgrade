package com.sinohb.system.upgrade.presenter;

import com.sinohb.system.upgrade.BasePresenter;
import com.sinohb.system.upgrade.BaseView;

public interface DownloadPresenter {

    public interface View extends BaseView<Controller>{

        void downloadProcess(int process);

        void complete();

        void failure();

        void notifyFileName(String fileName);

        void notifyFileSize(float size);

    }


    public interface Controller extends BasePresenter{

        void pause();

        void cancel();

    }

}
