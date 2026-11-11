package com.creatrix.unicorn.DBInfo;

public class DatabaseInfomation {

    private  static  final  String DATABASE_NAME = "SalesLocalDBoct.db";
    private static  final  int DATABASE_VERSION= 12;

    public static String getDatabaseName() {
        return DATABASE_NAME;
    }

    public static int getDatabaseVersion() {
        return DATABASE_VERSION;
    }
}
