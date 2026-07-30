package com.creatrix.unicorn.Interface;

public interface IVersionUpdate {
    interface View{
        void onVersionGet(String VersionName);
    }
    interface Presenter{
        void GetActiveVersion();
    }
}
