package com.creatrix.unicorn.Interface;

import com.creatrix.unicorn.Model.DistrictVM;
import com.creatrix.unicorn.Model.DivisionVM;
import com.creatrix.unicorn.Model.MarketStructure.StructureTable.Area;
import com.creatrix.unicorn.Model.MarketStructure.StructureTable.Group;
import com.creatrix.unicorn.Model.MarketStructure.StructureTable.Market;
import com.creatrix.unicorn.Model.MarketStructure.StructureTable.Region;
import com.creatrix.unicorn.Model.MarketStructure.StructureTable.SubTeritorry;
import com.creatrix.unicorn.Model.MarketStructure.StructureTable.Teritorry;
import com.creatrix.unicorn.Model.ThanaVM;

import java.util.List;

public interface IBangladesh {
    interface  Presenter{
        void GetDivisionLocal();
        void GetDistrictLocal(int divid);
        void GetThanaLocal(int disid);

    }
    interface View{
        void vDivL(List<DivisionVM> divList);
        void vDisL(List<DistrictVM> disList);
        void vThanaL(List<ThanaVM> thanaList);


    }
}
