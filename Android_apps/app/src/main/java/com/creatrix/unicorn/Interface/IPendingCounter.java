package com.creatrix.unicorn.Interface;

import com.creatrix.unicorn.Model.DcrSM;
import com.creatrix.unicorn.Model.OrderDetailSample;
import com.creatrix.unicorn.Model.OrderDetails;
import com.creatrix.unicorn.Model.OrderMaster;
import com.creatrix.unicorn.Model.PrescriptionSM;
import com.creatrix.unicorn.Model.ProductSample;

import java.util.List;

public interface IPendingCounter {
    interface  Presenter{
        void totalDcr();
        void totalPresc();
        void totalSample();
        void totalOrder();
        void totalOrderMaster();

    }
    interface  View{
        void totalDcr(List<DcrSM> dcrList);
        void totalPresc(List<PrescriptionSM> preList);
        void totalSample(List<OrderDetailSample> soList);
        void totalOrder(List<OrderDetails>oList);
        void totalOrderMaster(List<OrderMaster>oList);
    }
}
