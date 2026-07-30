package com.creatrix.unicorn.Interface;

import com.creatrix.unicorn.Model.MarketViewModel;
import com.creatrix.unicorn.Model.SubMarket;
import com.creatrix.unicorn.Model.TerritoryViewModel;

import java.util.List;

public interface ISeedData {
    interface  Presenter{
        void GetTerritory(int empId);
        void GetMarket(int territoryId);
        void GetSubmarket(int marketId);
    }

    interface View{
        void onTerritoryReceived(List<TerritoryViewModel> aList);
        void onMarketReceived(List<MarketViewModel> aList);
        void onSubMarketReceived(List<SubMarket> aList);
    }

}
