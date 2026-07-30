package com.creatrix.unicorn.Interface;

import com.creatrix.unicorn.Model.OrderMaster;

public interface RecyclerViewActionListener {
    void onEditTextFocusChange(int position,int value);
    boolean onLongClick(int position);
    void OrderSync(OrderMaster orderMaster);
}

