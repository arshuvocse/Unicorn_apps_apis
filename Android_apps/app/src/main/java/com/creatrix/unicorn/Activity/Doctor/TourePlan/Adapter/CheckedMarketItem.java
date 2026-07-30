package com.creatrix.unicorn.Activity.Doctor.TourePlan.Adapter;

import com.creatrix.unicorn.Model.Customer;
import com.creatrix.unicorn.Model.MarketStructure.StructureTable.Market;

import java.util.List;

public interface CheckedMarketItem {
    void ckdItemMarketName(List<Market> st, int Pos);
    void unckdItemMarketName(List<Market> st,int Pos);
}
