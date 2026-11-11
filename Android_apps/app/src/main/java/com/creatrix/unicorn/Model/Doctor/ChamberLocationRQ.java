package com.creatrix.unicorn.Model.Doctor;

public class ChamberLocationRQ {
    int ChamberId;
    String Latitude;
    String Longitude;

    public ChamberLocationRQ(int chamberId, String latitude, String longitude) {
        ChamberId = chamberId;
        Latitude = latitude;
        Longitude = longitude;
    }
}
