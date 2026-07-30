import sys

file_path = r'd:\CSTL_Projects\Unicorn\Android_apps\app\src\main\java\com\creatrix\unicorn\Presenter\OrderManagementPresenter.java'

with open(file_path, 'r', encoding='utf-8') as f:
    content = f.read()

bad_text = """    public void draftOrder(OrderMaster aOrder) {
        SQLiteDatabase database = dbHelperMain.getWritableDatabase();
        int orderId = 0;
        String MioCode = "";
        String CustomerCode = "";
        String CustomerName = "";
        String SubmissionDate = "";
        try {
            String insertQuery = "Insert into tblOrderMaster(EmpId,ComUnitId,MIOCode,CustomerCode,CustomerMasterId,CustomerName,CustomerAdress,SubmissionDate,CollectionDate,DeliveryDate,Remarks,PaymentType) " +
                    "values('" + aOrder.getEmpId() + "','" + aOrder.getComunitId() + "','" + aOrder.getMioCode() + "','" + aOrder.getCustomer().getCustomerCode() + "','" + aOrder.getCustomer().getCustomerMasterId() + "','" + aOrder.getCustomer().getCustomerName() + "','" + aOrder.getCustomer().getAddress() + "','" + aOrder.getSubmittedDate() + "','" + aOrder.getCollectionDate() + "','" + aOrder.getDeliveryDate() + "','" + aOrder.getRemarks() + "','" + aOrder.getPaymentType() + "')";
            database.execSQL(insertQuery);
            Cursor cursor = database.rawQuery("SELECT OrderIdLocal FROM tblOrderMaster ORDER BY OrderIdLocal DESC LIMIT 1", null);
            int id = 0;
            Call<ResultInfo> call = service.MakeOrder(aOrder);
            call.enqueue(new Callback<ResultInfo>() {
                @Override
                public void onResponse(Call<ResultInfo> call, Response<ResultInfo> response) {
                    progressDoalog.dismiss();
                    ResultInfo info = response.body();
                    Log.e("Error", response.body().toString());
                    String st = "false";
                    if (info.getSuccess() == true) {
                        st = "Order Has Been Synced Successfully";
                        dbCrudHelper.UpdateOrderSynchInfo(localOrderId);
                        view.onOrderSuccess(st,"Sync");
                    }
                }

                @Override
                public void onFailure(Call<ResultInfo> call, Throwable t) {
                    progressDoalog.dismiss();
                    if (t instanceof SocketTimeoutException) {
                        view.onOrderError("Slow Connection Detected. Please try again");
                    } else {
                        view.onOrderError("Something went wrong... Please try again");
                    }
                }
            });

        } catch (Exception ex) {
            progressDoalog.dismiss();
            String str = ex.toString();
            Log.e("Exception", str);
            view.onOrderError(ex.toString());
        }
    }"""

good_text = """    public void draftOrder(OrderMaster aOrder) {
        SQLiteDatabase database = dbHelperMain.getWritableDatabase();
        int orderId = 0;
        String MioCode = "";
        String CustomerCode = "";
        String CustomerName = "";
        String SubmissionDate = "";
        try {
            String insertQuery = "Insert into tblOrderMaster(EmpId,ComUnitId,MIOCode,CustomerCode,CustomerMasterId,CustomerName,CustomerAdress,SubmissionDate,CollectionDate,DeliveryDate,Remarks,PaymentType) " +
                    "values('" + aOrder.getEmpId() + "','" + aOrder.getComunitId() + "','" + aOrder.getMioCode() + "','" + aOrder.getCustomer().getCustomerCode() + "','" + aOrder.getCustomer().getCustomerMasterId() + "','" + aOrder.getCustomer().getCustomerName() + "','" + aOrder.getCustomer().getAddress() + "','" + aOrder.getSubmittedDate() + "','" + aOrder.getCollectionDate() + "','" + aOrder.getDeliveryDate() + "','" + aOrder.getRemarks() + "','" + aOrder.getPaymentType() + "')";
            database.execSQL(insertQuery);
            Cursor cursor = database.rawQuery("SELECT OrderIdLocal FROM tblOrderMaster ORDER BY OrderIdLocal DESC LIMIT 1", null);
            int id = 0;
            if (cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    id = cursor.getInt(cursor.getColumnIndex("OrderIdLocal"));
                }
            }
            int ProductId = 0;
            String ProductCode = "";
            String ProductName = "";
            int Quantity = 0;
            String PackSize = "";
            String UnitPrice = "";
            String VatPercentage = "";
            String VatAmountPerunit = "";
            for (int i = 0; i < aOrder.getOrderDetails().size(); i++) {
                Product aInfo = aOrder.getOrderDetails().get(i);
                ProductId = aInfo.getProductId();
                ProductName = aInfo.getProductName();
                Quantity = aInfo.getQuantity();
                UnitPrice = aInfo.getUnitPrice().toString();
                VatPercentage = aInfo.getVatPercentage().toString();
                String insertDetal = "Insert into tblOrderDetails(OrderIdLocal,ProductId,ProductName,Quantity,UnitPrice,VatPercentage) " +
                        "values('" + id + "','" + ProductId + "','" + ProductName + "','" + Quantity + "','" + UnitPrice + "','" + VatPercentage + "')";
                database.execSQL(insertDetal);
            }
            view.onOrderDraftSuccess("This Order has been Drafted Successfully");
            database.close();
        } catch (Exception exception) {
            database.close();
            view.onOrderError("Some Error Occurred Drafting Current Order.. Please try again after some times");
            Log.e("EX", exception.toString());

        }


    }

    @Override
    public void SyncOrder(OrderMaster aOrder, int localOrderId) {
        progressDoalog = new ProgressDialog((Context) view);
        progressDoalog.setMessage("Order is Processing.... Please wait");
        progressDoalog.show();
        progressDoalog.setCanceledOnTouchOutside(false);
        try {
            GetDataService service = RetrofitClientInstance.getRetrofitInstance().create(GetDataService.class);
            Call<ResultInfo> call = service.MakeOrder(aOrder);
            call.enqueue(new Callback<ResultInfo>() {
                @Override
                public void onResponse(Call<ResultInfo> call, Response<ResultInfo> response) {
                    progressDoalog.dismiss();
                    ResultInfo info = response.body();
                    Log.e("Error", response.body().toString());
                    String st = "false";
                    if (info.getSuccess() == true) {
                        st = "Order Has Been Synced Successfully";
                        dbCrudHelper.UpdateOrderSynchInfo(localOrderId);
                        view.onOrderSuccess(st,"Sync");
                    }
                }

                @Override
                public void onFailure(Call<ResultInfo> call, Throwable t) {
                    progressDoalog.dismiss();
                    if (t instanceof SocketTimeoutException) {
                        view.onOrderError("Slow Connection Detected. Please try again");
                    } else {
                        view.onOrderError("Something went wrong... Please try again");
                    }
                }
            });

        } catch (Exception ex) {
            progressDoalog.dismiss();
            String str = ex.toString();
            Log.e("Exception", str);
            view.onOrderError(ex.toString());
        }
    }"""

if bad_text in content:
    content = content.replace(bad_text, good_text)
    with open(file_path, 'w', encoding='utf-8') as f:
        f.write(content)
    print('SUCCESS')
else:
    print('bad_text not found in content!')
