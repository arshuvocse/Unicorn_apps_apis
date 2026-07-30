package com.creatrix.unicorn.Setting;

import com.creatrix.unicorn.UtilityHelper.StringUtils;

public class AppSetting {
    ////Demo Server - smc
    public static String AppName = "Unicorn";
    //Demo server
    public static String testingBASE_URL = "http://194.233.66.180:247";
    // public static String testingBASE_URL = "http://182.163.127.238:791";
    // public static String testingOrderProcess_URL = "http://182.163.127.238:790";

    //live Server
    //public static String newBASE_URL = "http://103.129.247.83:81";
  public static String newBASE_URL = "http://103.244.247.93:217";
    //    public static String newBASE_URL = "http://103.198.137.179:984";


    //   public static String TPBASE_URL = "http://103.198.137.179:983";
   public static String TPBASE_URL = "http://103.244.247.93:217";
    public static String newOrderProcess_URL = "http://103.244.247.93:216";
    //     public static String newOrderProcess_URL = "http://103.198.137.179:982";

   // test
  //  public static String newOrderProcess_URL = "http://194.233.66.180:422";

    public static boolean isEmptyString(String str) {
        return StringUtils.isEmpty(str);
    }
    public static boolean isNotEmptyString(String str) {
        return StringUtils.isNotEmpty(str);
    }
    public static boolean containsIgnoreCase(String str, String searchStr) {
        return StringUtils.containsIgnoreCase(str, searchStr);
    }

}
