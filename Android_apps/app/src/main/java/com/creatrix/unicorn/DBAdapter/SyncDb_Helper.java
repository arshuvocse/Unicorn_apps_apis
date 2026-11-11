package com.creatrix.unicorn.DBAdapter;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.creatrix.unicorn.Activity.Doctor.DCR.NonEffectiveReason;
import com.creatrix.unicorn.DBInfo.DBHelperMain;
import com.creatrix.unicorn.Model.DistrictVM;
import com.creatrix.unicorn.Model.DivisionVM;
import com.creatrix.unicorn.Model.Doctor.ContactTypeVM;
import com.creatrix.unicorn.Model.Customer;
import com.creatrix.unicorn.Model.CustomerType;
import com.creatrix.unicorn.Model.Doctor.Brand;
import com.creatrix.unicorn.Model.Doctor.DoctorBrand;
import com.creatrix.unicorn.Model.Doctor.DoctorCategory;
import com.creatrix.unicorn.Model.Doctor.DoctorChamberName;
import com.creatrix.unicorn.Model.Doctor.DoctorChamberTypeVM;
import com.creatrix.unicorn.Model.Doctor.DoctorDegreeViewModel;
import com.creatrix.unicorn.Model.Doctor.DoctorDesignation;
import com.creatrix.unicorn.Model.Doctor.DoctorListViewModel;
import com.creatrix.unicorn.Model.Doctor.DoctorSpecialityViewModel;
import com.creatrix.unicorn.Model.Doctor.DoctorTypeVM;
import com.creatrix.unicorn.Model.Doctor.ProgramType;
import com.creatrix.unicorn.Model.Doctor.SpecialDay;
import com.creatrix.unicorn.Model.ExpenseTypeMaster;
import com.creatrix.unicorn.Model.Gift;
import com.creatrix.unicorn.Model.InstitutionVM;
import com.creatrix.unicorn.Model.LeaveTypeInfo;
import com.creatrix.unicorn.Model.MarketStructure.AssignEmpTable.ASM;
import com.creatrix.unicorn.Model.MarketStructure.AssignEmpTable.MIO;
import com.creatrix.unicorn.Model.MarketStructure.AssignEmpTable.NSM;
import com.creatrix.unicorn.Model.MarketStructure.AssignEmpTable.RSM;
import com.creatrix.unicorn.Model.MarketStructure.StructureTable.Area;
import com.creatrix.unicorn.Model.MarketStructure.StructureTable.Group;
import com.creatrix.unicorn.Model.MarketStructure.StructureTable.Market;
import com.creatrix.unicorn.Model.MarketStructure.StructureTable.Region;
import com.creatrix.unicorn.Model.MarketStructure.StructureTable.SubTeritorry;
import com.creatrix.unicorn.Model.MarketStructure.StructureTable.Teritorry;
import com.creatrix.unicorn.Model.ModelProviderType;
import com.creatrix.unicorn.Model.ModelSMCType;
import com.creatrix.unicorn.Model.PrescriptionTYpe;
import com.creatrix.unicorn.Model.Product;
import com.creatrix.unicorn.Model.ProductSample;
import com.creatrix.unicorn.Model.StationType;
import com.creatrix.unicorn.Model.ThanaVM;
import com.creatrix.unicorn.Model.TourPurposeViewModel;
import com.creatrix.unicorn.Model.TourTypeViewModel;
import com.creatrix.unicorn.Model.Transport;
import com.creatrix.unicorn.Model.UserByRole;
import com.creatrix.unicorn.Model.UserRole;

import java.util.List;

public class SyncDb_Helper {
    DBHelperMain dbHelperMain;
    Context context;

    public SyncDb_Helper(Context context) {
        this.context = context;
        dbHelperMain = new DBHelperMain(context);
    }

    public boolean InsertOtherEmpList(List<com.creatrix.unicorn.Model.OtherEmpListModel> aList) {
        boolean isTrue = true;
        try {
            String tableName = "tblOtherEmpList";
            _deleteAllRecordsFromaTable(tableName);
            SQLiteDatabase database = dbHelperMain.getWritableDatabase();
            database.beginTransaction();
            for (int i = 0; i < aList.size(); i++) {
                com.creatrix.unicorn.Model.OtherEmpListModel aInfo = aList.get(i);
                String insertQuery = "Insert into tblOtherEmpList(EmpInfoId,EmpMasterCode) " +
                        "values(" + aInfo.getEmpInfoId() + ",'" + aInfo.getEmpMasterCode() + "')";
                database.execSQL(insertQuery);
            }
        
            database.setTransactionSuccessful();
            database.endTransaction();
        } catch (Exception exception) {

            isTrue = false;
            Log.e("DBEX", exception.toString());
        }
        return isTrue;
    }


  //Area section Insert
    public boolean InsertDivision(List<DivisionVM> aList) {
        boolean isTrue = true;
        try {
            String tableName = "tblDivisionInfo";
            _deleteAllRecordsFromaTable(tableName);
            SQLiteDatabase database = dbHelperMain.getWritableDatabase();
            database.beginTransaction();
            for (int i = 0; i < aList.size(); i++) {
                DivisionVM aInfo = aList.get(i);
                String insertQuery = "Insert into tblDivisionInfo(DivisionId,DivisionName) " +
                        "values('" + aInfo.getDivisionId() + "','" + aInfo.getDivisionName() + "')";
                database.execSQL(insertQuery);
            }
        
            database.setTransactionSuccessful();
            database.endTransaction();
        } catch (Exception exception) {

            isTrue = false;

            Log.e("DBEX", exception.toString());
        }
        return isTrue;
    }

    public boolean InsertDistrict(List<DistrictVM> aList) {
        boolean isTrue = true;
        try {
            String tableName = "tblDistrictInfo";
            _deleteAllRecordsFromaTable(tableName);
            SQLiteDatabase database = dbHelperMain.getWritableDatabase();
            database.beginTransaction();
            for (int i = 0; i < aList.size(); i++) {
                DistrictVM aInfo = aList.get(i);
                String insertQuery = "Insert into tblDistrictInfo(DistrictId,DistrictName,DivisionId) " +
                        "values('" + aInfo.getDistrictId() + "','" + aInfo.getDistrictName() + "','" + aInfo.getDivisionId() + "')";
                database.execSQL(insertQuery);
                // << database.close(); এখানে নয় >>
            }
            database.close(); // লুপের বাইরে, সব শেষ হলে ক্লোজ করুন
        
            database.setTransactionSuccessful();
            database.endTransaction();
        } catch (Exception exception) {

            isTrue = false;
            Log.e("DBEX", exception.toString());
        }
        return isTrue;
    }


    public boolean InsertThana(List<ThanaVM> aList) {
        boolean isTrue = true;
        try {
            String tableName = "tblThanaInfo";
            _deleteAllRecordsFromaTable(tableName);
            SQLiteDatabase database = dbHelperMain.getWritableDatabase();
            database.beginTransaction();
            for (int i = 0; i < aList.size(); i++) {
                ThanaVM aInfo = aList.get(i);
                String insertQuery = "Insert into tblThanaInfo(ThanaId,ThanaName,district_id) " +
                        "values('" + aInfo.getThanaId() + "','" + aInfo.getThanaName() + "','" + aInfo.getDistrict_id() + "')";
                database.execSQL(insertQuery);
            }
        
            database.setTransactionSuccessful();
            database.endTransaction();
        } catch (Exception exception) {

            isTrue = false;

            Log.e("DBEX", exception.toString());
        }
        return isTrue;
    }

    //TODO:Area
    /*public boolean InsertArea(List<Area> aList){
        boolean isTrue = true;
        try{

            String tableName = "tblProviderInfo";
            DeleteRecordsOfTable(tableName);
            SQLiteDatabase database = dbHelperMain.getWritableDatabase();
            database.beginTransaction();
            for (int i=0;i<aList.size();i++){
                DoctorListViewModel aInfo = aList.get(i);
                String insertQuery = "Insert into tblProviderInfo(ProviderinfoID,ThanaId,Union_Pourashava,ProviderCode,Village,NameofProvider,MobileNo,NameofOutlet," +
                        "Address,ThanaName,DistrictName,DivisionName,ProviderType) " +
                        "values('"+aInfo.getProviderinfoID()+"','"+aInfo.getThanaId()+"','"+aInfo.getUnion_Pourashava()+"'," +
                        "'"+ aInfo.getProviderCode()+"','"+ aInfo.getVillage()+"','"+ aInfo.getNameofProvider()+"','"+ aInfo.getMobileNo()+"','"+ aInfo.getNameofOutlet()+"'," +
                        "'"+ aInfo.getAddress()+"','"+aInfo.getThanaName()+"','"+aInfo.getDistrictName()+"','"+aInfo.getDivisionName()+"','"+aInfo.getProviderType()+"')";
                database.execSQL(insertQuery);
            }

        
            database.setTransactionSuccessful();
        } finally {
            database.endTransaction();
        }catch (Exception exception){
            isTrue = false;
            Log.e("DBEX",exception.toString());

        }
        return isTrue;
    }*/
    //TODO:Insert Master DATA

    public void deleteAllFromTable(String tableName, SQLiteDatabase database) {
        try {
            String query = "DELETE FROM '" + tableName + "';";
            database.execSQL(query);
        } catch (Exception ex) {
            Log.e("recordDelError", ex.toString());
        }
    }

    public void deleteAllMasterTables() {
        SQLiteDatabase database = dbHelperMain.getWritableDatabase();
        database.beginTransaction();

        try {
            deleteAllFromTable("tbl_Group", database);
            deleteAllFromTable("tblRegion", database);
            deleteAllFromTable("tblArea", database);
            deleteAllFromTable("tblTerritory", database);
            deleteAllFromTable("tblSubTerritory", database);
            deleteAllFromTable("tblMarket", database);
            deleteAllFromTable("tblNSMInfo", database);
            deleteAllFromTable("tblRSMInfo", database);
            deleteAllFromTable("tblASMInfo", database);
            deleteAllFromTable("tblMIOInfo", database);

            database.setTransactionSuccessful();
        } catch (Exception ex) {
            Log.e("deleteAllError", ex.toString());
        } finally {
            database.endTransaction();
            database.close();
        }
    }

    public boolean InsertGroup(List<Group> gList) {
        boolean isTrue = true;
        String tableName = "tbl_Group";
        SQLiteDatabase database = dbHelperMain.getWritableDatabase();
        boolean inTransaction = false;
        try {
            database.beginTransaction();
            inTransaction = true;
            for (Group m : gList) {
                ContentValues cv = new ContentValues();
                cv.put("GroupId", m.getGroupId());
                cv.put("GroupName", m.getGroupName() != null ? m.getGroupName().replace("'", "''") : "");
                database.insert(tableName, null, cv);
            }
            database.setTransactionSuccessful();
        } catch (Exception exception) {
            isTrue = false;
            Log.e("DBEX", exception.toString());
        } finally {
            if (inTransaction) {
                database.endTransaction();
            }
            database.close();
        }
        return isTrue;
    }

    public boolean InsertRegion(List<Region> rList) {
        boolean isTrue = true;
        String tableName = "tblRegion";
        SQLiteDatabase database = dbHelperMain.getWritableDatabase();
        boolean inTransaction = false;
        try {
            database.beginTransaction();
            inTransaction = true;
            for (Region m : rList) {
                ContentValues cv = new ContentValues();
                cv.put("RegionId", m.getRegionId());
                cv.put("RegionName", m.getRegionName() != null ? m.getRegionName().replace("'", "''") : "");
                cv.put("GroupId", m.getGroupId());
                database.insert(tableName, null, cv);
            }
            database.setTransactionSuccessful();
        } catch (Exception exception) {
            isTrue = false;
            Log.e("DBEX", exception.toString());
        } finally {
            if (inTransaction) {
                database.endTransaction();
            }
            database.close();
        }
        return isTrue;
    }

    public boolean InsertArea(List<Area> arList) {
        boolean isTrue = true;
        String tableName = "tblArea";
        SQLiteDatabase database = dbHelperMain.getWritableDatabase();
        boolean inTransaction = false;
        try {
            database.beginTransaction();
            inTransaction = true;
            for (Area m : arList) {
                ContentValues cv = new ContentValues();
                cv.put("AreaId", m.getAreaId());
                cv.put("AreaName", m.getAreaName() != null ? m.getAreaName().replace("'", "''") : "");
                cv.put("RegionId", m.getRegionId());
                database.insert(tableName, null, cv);
            }
            database.setTransactionSuccessful();
        } catch (Exception exception) {
            isTrue = false;
            Log.e("DBEX", exception.toString());
        } finally {
            if (inTransaction) {
                database.endTransaction();
            }
            database.close();
        }
        return isTrue;
    }

    public boolean InsertTeritorry(List<Teritorry> tList) {
        boolean isTrue = true;
        String tableName = "tblTerritory";
        SQLiteDatabase database = dbHelperMain.getWritableDatabase();
        boolean inTransaction = false;
        try {
            database.beginTransaction();
            inTransaction = true;
            for (Teritorry m : tList) {
                ContentValues cv = new ContentValues();
                cv.put("TerritoryId", m.getTerritoryId());
                cv.put("TerritoryName", m.getTerritoryName() != null ? m.getTerritoryName().replace("'", "''") : "");
                cv.put("AreaId", m.getAreaId());
                database.insert(tableName, null, cv);
            }
            database.setTransactionSuccessful();
        } catch (Exception exception) {
            isTrue = false;
            Log.e("DBEX", exception.toString());
        } finally {
            if (inTransaction) {
                database.endTransaction();
            }
            database.close();
        }
        return isTrue;
    }

    public boolean InsertSubTeritorry(List<SubTeritorry> stList) {
        boolean isTrue = true;
        String tableName = "tblSubTerritory";
        SQLiteDatabase database = dbHelperMain.getWritableDatabase();
        boolean inTransaction = false;
        try {
            database.beginTransaction();
            inTransaction = true;
            for (SubTeritorry m : stList) {
                ContentValues cv = new ContentValues();
                cv.put("SubTerritoryId", m.getSubTerritoryId());
                cv.put("SubTerritoryName", m.getSubTerritoryName() != null ? m.getSubTerritoryName().replace("'", "''") : "");
                cv.put("TerritoryId", m.getTerritoryId());
                database.insert(tableName, null, cv);
            }
            database.setTransactionSuccessful();
        } catch (Exception exception) {
            isTrue = false;
            Log.e("DBEX", exception.toString());
        } finally {
            if (inTransaction) {
                database.endTransaction();
            }
            database.close();
        }
        return isTrue;
    }

    public boolean InsertMarket(List<Market> mList) {
        boolean isTrue = true;
        String tableName = "tblMarket";
        SQLiteDatabase database = dbHelperMain.getWritableDatabase();
        boolean inTransaction = false;
        try {
            database.beginTransaction();
            inTransaction = true;
            for (Market m : mList) {
                ContentValues cv = new ContentValues();
                cv.put("MarketId", m.getMarketId());
                cv.put("MarketName", m.getMarketName() != null ? m.getMarketName().replace("'", "''") : "");
                cv.put("SubTerritoryId", m.getSubTerritoryId());
                database.insert(tableName, null, cv);
            }
            database.setTransactionSuccessful();
        } catch (Exception exception) {
            isTrue = false;
            Log.e("DBEX", exception.toString());
        } finally {
            if (inTransaction) {
                database.endTransaction();
            }
            database.close();
        }
        return isTrue;
    }

    //TODO:Insert MasterEmp
    public boolean InsertNSMInfo(List<NSM> nList) {
        boolean isTrue = true;
        try {

            String tableName = "tblNSMInfo";
           // _deleteAllRecordsFromaTable(tableName);
            SQLiteDatabase database = dbHelperMain.getWritableDatabase();
            database.beginTransaction();
            for (int i = 0; i < nList.size(); i++) {
                NSM nInfo = nList.get(i);
                String insertQuery = "Insert into tblNSMInfo(NSMEmpId,NSMId,EmpMasterCode,EmpName,GroupId) " +
                        "values('" + nInfo.getNSMEmpId() + "','" + nInfo.getNSMId() + "','" + nInfo.getEmpMasterCode() + "','" + nInfo.getEmpName() + "','" + nInfo.getGroupId() + "')";
                database.execSQL(insertQuery);
            }

        
            database.setTransactionSuccessful();
            database.endTransaction();
        } catch (Exception exception) {

            isTrue = false;
            Log.e("DBEX", exception.toString());

        }
        return isTrue;
    }
    public boolean InsertRSMInfo(List<RSM> rList) {
        boolean isTrue = true;
        try {

            String tableName = "tblRSMInfo";
            _deleteAllRecordsFromaTable(tableName);
            SQLiteDatabase database = dbHelperMain.getWritableDatabase();
            database.beginTransaction();
            for (int i = 0; i < rList.size(); i++) {
                RSM nInfo = rList.get(i);
                String insertQuery = "Insert into tblRSMInfo(RSMEmpId,RSMId,EmpMasterCode,EmpName,RegionId) " +
                        "values('" + nInfo.getRSMEmpId() + "','" + nInfo.getRSMId() + "','" + nInfo.getEmpMasterCode() + "','" + nInfo.getEmpName().replace("'", "''") + "','" + nInfo.getRegionId() + "')";
                database.execSQL(insertQuery);
            }

        
            database.setTransactionSuccessful();
            database.endTransaction();
        } catch (Exception exception) {

            isTrue = false;
            Log.e("DBEX", exception.toString());

        }
        return isTrue;
    }
    public boolean InsertASMInfo(List<ASM> aList) {
        boolean isTrue = true;
        try {

            String tableName = "tblASMInfo";
            _deleteAllRecordsFromaTable(tableName);
            SQLiteDatabase database = dbHelperMain.getWritableDatabase();
            database.beginTransaction();
            for (int i = 0; i < aList.size(); i++) {
                ASM nInfo = aList.get(i);
                String insertQuery = "Insert into tblASMInfo(ASMEmpId,ASMId,EmpMasterCode,EmpName,AreaId) " +
                        "values('" + nInfo.getASMEmpId() + "','" + nInfo.getASMId() + "','" + nInfo.getEmpMasterCode() + "','" + nInfo.getEmpName() + "','" + nInfo.getAreaId() + "')";
                database.execSQL(insertQuery);
            }

        
            database.setTransactionSuccessful();
            database.endTransaction();
        } catch (Exception exception) {

            isTrue = false;
            Log.e("DBEX", exception.toString());

        }
        return isTrue;
    }
    public boolean InsertMIOInfo(List<MIO> mList) {
        boolean isTrue = true;
        try {
            String tableName = "tblMIOInfo";
            _deleteAllRecordsFromaTable(tableName);
            SQLiteDatabase database = dbHelperMain.getWritableDatabase();
            database.beginTransaction();
            for (int i = 0; i < mList.size(); i++) {
                MIO nInfo = mList.get(i);
                String insertQuery = "Insert into tblMIOInfo(MIOEmpId,MIOId,EmpMasterCode,EmpName,TerritoryId) " +
                        "values('" + nInfo.getMIOEmpId() + "','" + nInfo.getMIOId() + "','" + nInfo.getEmpMasterCode() + "','" + nInfo.getEmpName() + "','" + nInfo.getTerritoryId() + "')";
                database.execSQL(insertQuery);
            }

        
            database.setTransactionSuccessful();
            database.endTransaction();
        } catch (Exception exception) {

            isTrue = false;
            Log.e("DBEX", exception.toString());

        }
        return isTrue;
    }

/*    public boolean InsertTeritory(List<TerritoryViewModel> aList) {
        boolean isTrue = true;
        try {

            String tableName = "tblTeritory";
            _deleteAllRecordsFromaTable(tableName);
            SQLiteDatabase database = dbHelperMain.getWritableDatabase();
            database.beginTransaction();
            String TerritoryName = "";
            for (int i = 0; i < aList.size(); i++) {
                TerritoryViewModel aInfo = aList.get(i);
                try {
                    if (aInfo.getTerritoryName().equals("")) {
                        TerritoryName = "";
                    } else {
                        TerritoryName = aInfo.getTerritoryName().replace("'", "''");
                    }
                
            database.setTransactionSuccessful();
            database.endTransaction();
        } catch (Exception exception) {

                    //exception.printStackTrace();
                }
                String insertQuery = "Insert into tblTeritory(TerritoryId,TerritoryName,TerritoryCode) " +
                        "values('" + aInfo.getTerritoryId() + "','" + TerritoryName*//*aInfo.getTerritoryName().replace("'","''")*//* + "','" + aInfo.getTerritoryCode() + "')";
                database.execSQL(insertQuery);
            }

        } catch (Exception exception) {
            // isTrue = false;
            Log.e("DBEX", exception.toString());

        }
        return isTrue;
    }*/
    //TODO:Market
    /*public boolean InsertMarket(List<DoctorListViewModel> aList){
        boolean isTrue = true;
        try{

            String tableName = "tblProviderInfo";
            _deleteAllRecordsFromaTable(tableName);
            SQLiteDatabase database = dbHelperMain.getWritableDatabase();
            database.beginTransaction();
            for (int i=0;i<aList.size();i++){
                DoctorListViewModel aInfo = aList.get(i);
                String insertQuery = "Insert into tblProviderInfo(ProviderinfoID,ThanaId,Union_Pourashava,ProviderCode,Village,NameofProvider,MobileNo,NameofOutlet," +
                        "Address,ThanaName,DistrictName,DivisionName,ProviderType) " +
                        "values('"+aInfo.getProviderinfoID()+"','"+aInfo.getThanaId()+"','"+aInfo.getUnion_Pourashava()+"'," +
                        "'"+ aInfo.getProviderCode()+"','"+ aInfo.getVillage()+"','"+ aInfo.getNameofProvider()+"','"+ aInfo.getMobileNo()+"','"+ aInfo.getNameofOutlet()+"'," +
                        "'"+ aInfo.getAddress()+"','"+aInfo.getThanaName()+"','"+aInfo.getDistrictName()+"','"+aInfo.getDivisionName()+"','"+aInfo.getProviderType()+"')";
                database.execSQL(insertQuery);
            }

        
            database.setTransactionSuccessful();
        } finally {
            database.endTransaction();
        }catch (Exception exception){
            isTrue = false;
            Log.e("DBEX",exception.toString());

        }
        return isTrue;
    }*/
    //Customer section Insert
    public boolean InsertCustomerList(List<Customer> aList) {
        boolean isTrue = true;
      /*  try {
            String tableName = "tblCustomerInfo";
            _deleteAllRecordsFromaTable(tableName);

            SQLiteDatabase database = dbHelperMain.getWritableDatabase();
            database.beginTransaction();
            for (int i = 0; i < aList.size(); i++) {
                Customer aInfo = aList.get(i);
                String insertQuery = "Insert into tblCustomerInfo(CustomerMasterId,CustomerName,CustomerCode,CustomerAdress,CustomerType," +
                        "CustomerSubgroup,CustomerCell,CustomerBalance,CustomerCreditlimit,Market) " +
                        "values('" + aInfo.getCustomerMasterId() + "','" + aInfo.getCustomerName() + "','" + aInfo.getCustomerCode() + "','" + aInfo.getAddress() + "'," +
                        "'" + aInfo.getCustomerType() + "','" + aInfo.getCustomerStation() + "','" + aInfo.getCellNo() + "','" + aInfo.getBalance() + "','" + aInfo.getCreditLimit() + "','" + aInfo.getMarketName() + "')";

                database.execSQL(insertQuery);
            }
        
            database.setTransactionSuccessful();
            database.endTransaction();
        } catch (Exception exception) {

            Log.e("DBEX", exception.toString());
        }*/

        try {
            String tableName = "tblCustomerInfo";
            _deleteAllRecordsFromaTable(tableName);

            SQLiteDatabase database = dbHelperMain.getWritableDatabase();
            database.beginTransaction();
            String CustomerName = "";
            String CustomerAddress = "";
            String CustomerType = "";
            String MarketName = "";
            for (int i = 0; i < aList.size(); i++) {
                Customer aInfo = aList.get(i);
                try {
                    if (aInfo.getCustomerName().equals("")) {
                        CustomerName = "";
                    } else {
                        CustomerName = aInfo.getCustomerName().replace("'", "''");
                    }
                } catch (Exception exception) {
                    //exception.printStackTrace();
                }
                try {
                    if (aInfo.getAddress().equals("")) {
                        CustomerAddress = "";
                    } else {
                        CustomerAddress = aInfo.getAddress().replace("'", "''");
                    }
                } catch (Exception exception) {
                    //exception.printStackTrace();
                }
                try {
                    if (aInfo.getCustomerType().equals("")) {
                        CustomerType = "";
                    } else {
                        CustomerType = aInfo.getCustomerType().replace("'", "''");
                    }
                } catch (Exception exception) {
                    //exception.printStackTrace();
                }
                try {
                    if (aInfo.getMarketName().equals("")) {
                        MarketName = "";
                    } else {
                        MarketName = aInfo.getMarketName().replace("'", "''");
                    }
                } catch (Exception exception) {
                    //exception.printStackTrace();
                }

                String insertQuery = "Insert into tblCustomerInfo(CustomerMasterId,CustomerName,CustomerCode,CustomerAdress,CustomerType," +
                        "CustomerCell,CustomerBalance,CustomerCreditlimit,Market,MarketCode,GroupId,RegionId,AreaId,TerritoryId,SubTerritoryId,MarketId,Note,CustomerCheck,CustomerTypeId,ProgramTypeId,SMCTypeId) " +
                        "values('" + aInfo.getCustomerMasterId() + "','" + CustomerName + "','" + aInfo.getCustomerCode() + "','" + CustomerAddress + "'," +
                        "'" + CustomerType + "','" + aInfo.getCellNo() + "','" + aInfo.getBalance() + "','" + aInfo.getCreditLimit() + "','" + MarketName + "','" + aInfo.getMarketCode() + "','" + aInfo.getGroupId() + "','" + aInfo.getRegionId() + "','" + aInfo.getAreaId() + "','" + aInfo.getTerritoryId() + "','" + aInfo.getSubTerritoryId() + "','" + aInfo.getMarketId() + "','" + aInfo.getNote() + "'," +
                        "'" + aInfo.getCustomerCheck() + "','" + aInfo.getCustomerTypeId() + "','" + aInfo.getProgramTypeId() + "','" + aInfo.getSMCTypeId() + "')";
                database.execSQL(insertQuery);

            }
            database.setTransactionSuccessful();
            database.endTransaction();
        } catch (Exception exception) {
            isTrue = false;
            Log.e("DBEX", exception.toString());
        }
        return isTrue;
    }

    public boolean InsertCustomerStation(List<StationType> aList) {
        boolean isTrue = true;
        try {

            String tableName = "tblCustomer_Station";
            _deleteAllRecordsFromaTable(tableName);
            SQLiteDatabase database = dbHelperMain.getWritableDatabase();
            database.beginTransaction();
            for (int i = 0; i < aList.size(); i++) {
                StationType aInfo = aList.get(i);
                String insertQuery = "Insert into tblCustomer_Station(StationTypeId,StationTypeName) " +
                        "values('" + aInfo.getStationTypeId() + "','" + aInfo.getStationTypeName().replace("'", "''") + "')";
                database.execSQL(insertQuery);
            }

        
            database.setTransactionSuccessful();
            database.endTransaction();
        } catch (Exception exception) {

            isTrue = false;
            Log.e("DBEX", exception.toString());

        }
        return isTrue;
    }

    //TODO:Doctor section Insert
    public boolean InsertDoctorList(List<DoctorListViewModel> aList) {
        boolean isTrue = true;
        SQLiteDatabase database = null;
        try {
            _deleteAllRecordsFromaTable("tblDoctorBrand");
            _deleteAllRecordsFromaTable("tblDoctorInfo");

            database = dbHelperMain.getWritableDatabase();
            database.beginTransaction();

            for (int i = 0; i < aList.size(); i++) {
                DoctorListViewModel aInfo = aList.get(i);
                if (aInfo == null) continue;

                String chemberName = "";
                if (aInfo.getChemberName() != null && !aInfo.getChemberName().isEmpty()) {
                    chemberName = aInfo.getChemberName();
                }

                String marketName = "";
                if (aInfo.getMarketName() != null && !aInfo.getMarketName().isEmpty()) {
                    marketName = aInfo.getMarketName();
                }

                String streetAddress = "";
                if (aInfo.getStreetAddress() != null) {
                    streetAddress = aInfo.getStreetAddress();
                }

                String latitude = "";
                if (aInfo.getLatitude() != null) {
                    latitude = aInfo.getLatitude();
                }

                String longitude = "";
                if (aInfo.getLongitude() != null) {
                    longitude = aInfo.getLongitude();
                }

                ContentValues cv = new ContentValues();
                cv.put("DoctorId", aInfo.getDoctorId());
                cv.put("DoctorCode", aInfo.getDoctorCode());
                cv.put("DoctorName", aInfo.getDoctorName());
                cv.put("ChemberName", chemberName);
                cv.put("DocContact", aInfo.getDocContact());
                cv.put("DocTPDetailsId", aInfo.getDocTPDetailsId());
                cv.put("DoctorTypeName", aInfo.getDoctorTypeName());
                cv.put("ProgramTypeName", aInfo.getProgramTypeName());
                cv.put("GroupId", aInfo.getGroupId());
                cv.put("RegionId", aInfo.getRegionId());
                cv.put("AreaId", aInfo.getAreaId());
                cv.put("TerritoryId", aInfo.getTerritoryId());
                cv.put("SubTerritoryId", aInfo.getSubTerritoryId());
                cv.put("MarketId", aInfo.getMarketId());
                cv.put("MarketName", marketName);
                cv.put("MarketCode", aInfo.getMarketCode());
                cv.put("ProgramTypeId", aInfo.getProgramTypeId());
                cv.put("DoctorTypeId", aInfo.getDoctorTypeId());
                cv.put("SMCTypeId", aInfo.getSMCTypeId());
                cv.put("SMCType", aInfo.getSMCType());
                cv.put("Latitude", latitude);
                cv.put("Longitude", longitude);
                cv.put("StreetAddress", streetAddress);

                database.insert("tblDoctorInfo", null, cv);

                if (aInfo.getDoctorBrand() != null) {
                    for (int j = 0; j < aInfo.getDoctorBrand().size(); j++) {
                        DoctorBrand dbi = aInfo.getDoctorBrand().get(j);
                        if (dbi == null) continue;

                        String brandName = "";
                        if (dbi.getBrandName() != null && !dbi.getBrandName().isEmpty()) {
                            brandName = dbi.getBrandName();
                        }

                        ContentValues brandCv = new ContentValues();
                        brandCv.put("BrandId", dbi.getBrandId());
                        brandCv.put("BrandName", brandName);
                        brandCv.put("DoctorId", dbi.getDoctorId());

                        database.insert("tblDoctorBrand", null, brandCv);
                    }
                }
            }

            database.setTransactionSuccessful();
        } catch (Exception exception) {
            isTrue = false;
            Log.e("DBEX", exception.toString());
        } finally {
            if (database != null) {
                try {
                    database.endTransaction();
                    database.close();
                } catch (Exception e) {
                    Log.e("DBEX", "Error ending transaction", e);
                }
            }
        }
        return isTrue;
    }

    public boolean InsertDoctorDesig(List<DoctorDesignation> aList) {
        boolean isTrue = true;
        try {

            String tableName = "tblDoctor_Desig";
            _deleteAllRecordsFromaTable(tableName);
            SQLiteDatabase database = dbHelperMain.getWritableDatabase();
            for (int i = 0; i < aList.size(); i++) {
                DoctorDesignation aInfo = aList.get(i);
                String insertQuery = "Insert into tblDoctor_Desig(DesignationId,DesignationName) " +
                        "values('" + aInfo.getDesignationId() + "','" + aInfo.getDesignationName().replace("'", "''") + "')";
                database.execSQL(insertQuery);
            }

        } catch (Exception exception) {
            isTrue = false;
            Log.e("DBEX", exception.toString());

        }
        return isTrue;
    }

    public boolean InsertDoctorDegree(List<DoctorDegreeViewModel> aList) {
        boolean isTrue = true;
        try {

            String tableName = "tblDoctor_Degree";
            _deleteAllRecordsFromaTable(tableName);
            SQLiteDatabase database = dbHelperMain.getWritableDatabase();
            database.beginTransaction();
            for (int i = 0; i < aList.size(); i++) {
                DoctorDegreeViewModel aInfo = aList.get(i);
                String insertQuery = "Insert into tblDoctor_Degree(DegreeId,DegreeName,DoctorTypeId) " +
                        "values('" + aInfo.getDegreeId() + "','" + aInfo.getDegreeName().replace("'", "''") + "','" + aInfo.getDoctorTypeId() + "')";
                database.execSQL(insertQuery);
            }

        
            database.setTransactionSuccessful();
            database.endTransaction();
        } catch (Exception exception) {

            isTrue = false;
            Log.e("DBEX", exception.toString());

        }
        return isTrue;
    }

    public boolean InsertDoctorSpecility(List<DoctorSpecialityViewModel> aList) {
        boolean isTrue = true;
        try {

            String tableName = "tblDoctor_Speciality";
            _deleteAllRecordsFromaTable(tableName);
            SQLiteDatabase database = dbHelperMain.getWritableDatabase();
            database.beginTransaction();
            for (int i = 0; i < aList.size(); i++) {
                DoctorSpecialityViewModel aInfo = aList.get(i);
                String insertQuery = "Insert into tblDoctor_Speciality(SpecialityId,SpecialityName) " +
                        "values('" + aInfo.getSpecialityId() + "','" + aInfo.getSpecialityName().replace("'", "''") + "')";
                database.execSQL(insertQuery);
            }

        
            database.setTransactionSuccessful();
            database.endTransaction();
        } catch (Exception exception) {

            isTrue = false;
            Log.e("DBEX", exception.toString());

        }
        return isTrue;
    }

    public boolean InsertDoctorSpecialDay(List<SpecialDay> aList) {
        boolean isTrue = true;
        try {

            String tableName = "tblDoctor_Specialday";
            _deleteAllRecordsFromaTable(tableName);
            SQLiteDatabase database = dbHelperMain.getWritableDatabase();
            database.beginTransaction();
            for (int i = 0; i < aList.size(); i++) {
                SpecialDay aInfo = aList.get(i);
                String insertQuery = "Insert into tblDoctor_Specialday(SpecialDayId,SpecialDay) " +
                        "values('" + aInfo.getSpecialDayId() + "','" + aInfo.getSpecialDay().replace("'", "''") + "')";
                database.execSQL(insertQuery);
            }

        
            database.setTransactionSuccessful();
            database.endTransaction();
        } catch (Exception exception) {

            isTrue = false;
            Log.e("DBEX", exception.toString());

        }
        return isTrue;
    }

    public boolean InsertDoctorInstitution(List<InstitutionVM> aList) {
        boolean isTrue = true;
        try {

            String tableName = "tblDoctor_Institution";
            _deleteAllRecordsFromaTable(tableName);
            SQLiteDatabase database = dbHelperMain.getWritableDatabase();
            database.beginTransaction();
            for (int i = 0; i < aList.size(); i++) {
                InstitutionVM aInfo = aList.get(i);
                String insertQuery = "Insert into tblDoctor_Institution(InstitutionId,Institution) " +
                        "values('" + aInfo.getInstitutionId() + "','" + aInfo.getInstitution().replace("'", "''") + "')";
                database.execSQL(insertQuery);
            }

        
            database.setTransactionSuccessful();
            database.endTransaction();
        } catch (Exception exception) {

            isTrue = false;
            Log.e("DBEX", exception.toString());

        }
        return isTrue;
    }

    public boolean InsertDoctorChembar(List<DoctorChamberTypeVM> aList) {
        boolean isTrue = true;
        try {

            String tableName = "tblDoctor_Chembar";
            _deleteAllRecordsFromaTable(tableName);
            SQLiteDatabase database = dbHelperMain.getWritableDatabase();
            database.beginTransaction();
            for (int i = 0; i < aList.size(); i++) {
                DoctorChamberTypeVM aInfo = aList.get(i);
                String insertQuery = "Insert into tblDoctor_Chembar(ChamberTypeId,ChamberTypeName) " +
                        "values('" + aInfo.getChamberTypeId() + "','" + aInfo.getChamberTypeName().replace("'", "''") + "')";
                database.execSQL(insertQuery);
            }

            database.setTransactionSuccessful();
            database.endTransaction();
        } catch (Exception exception) {

            isTrue = false;
            Log.e("DBEX", exception.toString());

        }
        return isTrue;
    }

    public boolean InsertDoctorChembarName(List<DoctorChamberName> aList) {
        boolean isTrue = true;
        try {

            String tableName = "tblDoctor_ChembarName";
            _deleteAllRecordsFromaTable(tableName);
            SQLiteDatabase database = dbHelperMain.getWritableDatabase();
            database.beginTransaction();
            for (int i = 0; i < aList.size(); i++) {
                DoctorChamberName aInfo = aList.get(i);
                String cName = aInfo.getChemberName() != null ? aInfo.getChemberName().replace("'", "''") : "";
                String shift = aInfo.getShift() != null ? aInfo.getShift().replace("'", "''") : "";
                String sittingTime = aInfo.getSittingTime() != null ? aInfo.getSittingTime().replace("'", "''") : "";
                String address = aInfo.getAddress() != null ? aInfo.getAddress().replace("'", "''") : "";
                String institutionId = aInfo.getInstitutionId() != null ? String.valueOf(aInfo.getInstitutionId()) : "0";
                String institutionName = aInfo.getInstitutionName() != null ? aInfo.getInstitutionName().replace("'", "''") : "";
                String lat = aInfo.getLat() != null ? aInfo.getLat().replace("'", "''") : "";
                String lon = aInfo.getLon() != null ? aInfo.getLon().replace("'", "''") : "";
                String insertQuery = "Insert into tblDoctor_ChembarName" +
                        "(ChemberId,ChemberName,DoctorId,Shift,SittingTime,Address,InstitutionId,InstitutionName,Lat,Lon) " +
                        "values('" + aInfo.getChemberId() + "','" + cName + "','" + aInfo.getDoctorId() + "'," +
                        "'" + shift + "','" + sittingTime + "','" + address + "','" + institutionId + "','" + institutionName + "','" + lat + "','" + lon + "')";
                database.execSQL(insertQuery);
            }

            database.setTransactionSuccessful();
            database.endTransaction();
        } catch (Exception exception) {

            isTrue = false;
            Log.e("DBEX", exception.toString());

        }
        return isTrue;
    }

    //TODO:Type section Insert
    public boolean InsertDoctorType(List<DoctorTypeVM> aList) {
        boolean isTrue = true;
        try {

            String tableName = "tblDoctor_Type";
            _deleteAllRecordsFromaTable(tableName);
            SQLiteDatabase database = dbHelperMain.getWritableDatabase();
            database.beginTransaction();
            for (int i = 0; i < aList.size(); i++) {
                DoctorTypeVM aInfo = aList.get(i);
                String insertQuery = "Insert into tblDoctor_Type(DoctorTypeId,DoctorTypeName) " +
                        "values('" + aInfo.getDoctorTypeId() + "','" + aInfo.getDoctorTypeName().replace("'", "''") + "')";
                database.execSQL(insertQuery);
            }

        
            database.setTransactionSuccessful();
            database.endTransaction();
        } catch (Exception exception) {

            isTrue = false;
            Log.e("DBEX", exception.toString());

        }
        return isTrue;
    }

    public boolean InsertRole(List<UserRole> aList) {
        boolean isTrue = true;
        try {

            String tableName = "tbl_UserRole";
            _deleteAllRecordsFromaTable(tableName);
            SQLiteDatabase database = dbHelperMain.getWritableDatabase();
            database.beginTransaction();
            for (int i = 0; i < aList.size(); i++) {
                UserRole aInfo = aList.get(i);
                String insertQuery = "Insert into tbl_UserRole(UserRoleID,RoleName) " +
                        "values('" + aInfo.getUserRoleID() + "','" + aInfo.getRoleName().replace("'", "''") + "')";
                database.execSQL(insertQuery);
            }

        
            database.setTransactionSuccessful();
            database.endTransaction();
        } catch (Exception exception) {

            isTrue = false;
            Log.e("DBEX", exception.toString());

        }
        return isTrue;
    }
    public boolean InsertRoleUser(List<UserByRole> aList) {
        boolean isTrue = true;
        try {


            String tableName = "tbl_UserByRole";
            _deleteAllRecordsFromaTable(tableName);
            SQLiteDatabase database = dbHelperMain.getWritableDatabase();
            database.beginTransaction();
            for (int i = 0; i < aList.size(); i++) {
                UserByRole aInfo = aList.get(i);
                String insertQuery = "Insert into tbl_UserByRole(EmpInfoId,EmpName,EmpMasterCode,UserRoleID) " +
                        "values('" + aInfo.getEmpInfoId() + "','" + aInfo.getEmpName() + "','" + aInfo.getEmpMasterCode() + "','" + aInfo.getUserRoleID() + "')";
                database.execSQL(insertQuery);
            }

            database.setTransactionSuccessful();
            database.endTransaction();
        } catch (Exception exception) {

            isTrue = false;
            Log.e("DBEX", exception.toString());

        }
        return isTrue;
    }

    public boolean InsertProgramType(List<ProgramType> aList) {
        boolean isTrue = true;
        try {

            String tableName = "tblProgram_Type";
            _deleteAllRecordsFromaTable(tableName);
            SQLiteDatabase database = dbHelperMain.getWritableDatabase();
            database.beginTransaction();
            for (int i = 0; i < aList.size(); i++) {
                ProgramType aInfo = aList.get(i);
                String insertQuery = "Insert into tblProgram_Type(ProgramTypeId,ProgramType) " +
                        "values('" + aInfo.getProgramTypeId() + "','" + aInfo.getProgramType().replace("'", "''") + "')";
                database.execSQL(insertQuery);
            }

        
            database.setTransactionSuccessful();
            database.endTransaction();
        } catch (Exception exception) {

            isTrue = false;
            Log.e("DBEX", exception.toString());

        }
        return isTrue;
    }

    public boolean InsertProviderType(List<ModelProviderType> aList) {
        boolean isTrue = true;
        try {

            String tableName = "tblProvider_Type";
            _deleteAllRecordsFromaTable(tableName);
            SQLiteDatabase database = dbHelperMain.getWritableDatabase();
            database.beginTransaction();
            for (int i = 0; i < aList.size(); i++) {
                ModelProviderType aInfo = aList.get(i);
                String insertQuery = "Insert into tblProvider_Type(ProviderTypeId,ProviderType,forCustomer,forDoctor) " +
                        "values('" + aInfo.getProviderTypeId() + "','" + aInfo.getProviderType().replace("'", "''") + "','" + aInfo.getForCustomer() + "','" + aInfo.getForDoctor() + "')";
                database.execSQL(insertQuery);
                isTrue = true;
            }

        
            database.setTransactionSuccessful();
            database.endTransaction();
        } catch (Exception exception) {

            isTrue = false;
            Log.e("DBEX", exception.toString());

        }
        return isTrue;
    }

    public boolean InsertSMCType(List<ModelSMCType> aList) {
        boolean isTrue = true;
        try {
            String tableName = "tblSMCType";
            _deleteAllRecordsFromaTable(tableName);
            SQLiteDatabase database = dbHelperMain.getWritableDatabase();
            database.beginTransaction();
            for (int i = 0; i < aList.size(); i++) {
                ModelSMCType aInfo = aList.get(i);
                String insertQuery = "Insert into tblSMCType(SMCTypeId,SMCType,forCustomer,forDoctor) " +
                        "values('" + aInfo.getSMCTypeId() + "','" + aInfo.getSMCType().replace("'", "''") + "','" + aInfo.getForCustomer() + "','" + aInfo.getForDoctor() + "')";
                database.execSQL(insertQuery);
                isTrue = true;
            }

        
            database.setTransactionSuccessful();
            database.endTransaction();
        } catch (Exception exception) {

            isTrue = false;
            Log.e("DBEX", exception.toString());

        }
        return isTrue;
    }
    public boolean InsertBrand(List<Brand> aList) {
        boolean isTrue = true;
        try {
            String tableName = "tblBrandInfo";
            _deleteAllRecordsFromaTable(tableName);
            SQLiteDatabase database = dbHelperMain.getWritableDatabase();
            database.beginTransaction();
            for (int i = 0; i < aList.size(); i++) {
                Brand nInfo = aList.get(i);
                String insertQuery = "Insert into tblBrandInfo(ProductBrandId,ProductSQName,MaxValue) " +
                        "values('" + nInfo.getProductBrandId() + "','" + nInfo.getProductSQName().replace("'", "''") + "','" + nInfo.getMaxValue() + "')";
                database.execSQL(insertQuery);
                isTrue = true;
            }

        
            database.setTransactionSuccessful();
            database.endTransaction();
        } catch (Exception exception) {

            isTrue = false;
            Log.e("DBEX", exception.toString());

        }
        return isTrue;
    }
    public boolean InsertDoctorCategory(List<DoctorCategory> aList) {
        boolean isTrue = true;
        try {

            String tableName = "tblDoctorCategory";
            _deleteAllRecordsFromaTable(tableName);
            SQLiteDatabase database = dbHelperMain.getWritableDatabase();
            database.beginTransaction();
            for (int i = 0; i < aList.size(); i++) {
                DoctorCategory nInfo = aList.get(i);
                String insertQuery = "Insert into tblDoctorCategory(CategoryId,CategoryName) " +
                        "values('" + nInfo.getCategoryId() + "','" + nInfo.getCategoryName().replace("'", "''") + "')";
                database.execSQL(insertQuery);
            }

        
            database.setTransactionSuccessful();
            database.endTransaction();
        } catch (Exception exception) {

            isTrue = false;
            Log.e("DBEX", exception.toString());

        }
        return isTrue;
    }
    public boolean InsertVisitType(List<TourTypeViewModel> aTpp) {
        boolean isTrue = true;
        try {
            String tableName = "tblVisit_Type";
            _deleteAllRecordsFromaTable(tableName);

            SQLiteDatabase databases = dbHelperMain.getWritableDatabase();
            databases.beginTransaction();
            for (int i = 0; i < aTpp.size(); i++) {
                TourTypeViewModel aInfo = aTpp.get(i);
                String insertQuery = "Insert into tblVisit_Type(TourTypeId,TourTypeName) " +
                        "values('" + aInfo.getTourTypeId() + "','" + aInfo.getTourTypeName().replace("'", "''") + "')";
                databases.execSQL(insertQuery);
            }
        
            databases.setTransactionSuccessful();
            databases.endTransaction();
        } catch (Exception exception) {

            isTrue = false;
            Log.e("DBEX", exception.toString());
        }

        return isTrue;
    }
    public boolean InsertCustomerType(List<CustomerType> aList) {
        boolean isTrue = true;
        try {
            String tableName = "tblCustomer_Type";
            _deleteAllRecordsFromaTable(tableName);
            SQLiteDatabase database = dbHelperMain.getWritableDatabase();
            database.beginTransaction();
            for (int i = 0; i < aList.size(); i++) {
                CustomerType aInfo = aList.get(i);
                String insertQuery = "Insert into tblCustomer_Type(CustomerTypeId,CustomerType) " +
                        "values('" + aInfo.getCustomerTypeId() + "','" + aInfo.getCustomerType().replace("'", "''") + "')";
                database.execSQL(insertQuery);
            }

        
            database.setTransactionSuccessful();
            database.endTransaction();
        } catch (Exception exception) {

            isTrue = false;
            Log.e("DBEX", exception.toString());

        }
        return isTrue;
    }
    public boolean InsertContType(List<ContactTypeVM> aList) {
        boolean isTrue = true;
        try {

            String tableName = "tblContact_Type";
            _deleteAllRecordsFromaTable(tableName);
            SQLiteDatabase database = dbHelperMain.getWritableDatabase();
            database.beginTransaction();
            for (int i = 0; i < aList.size(); i++) {
                ContactTypeVM aInfo = aList.get(i);
                String insertQuery = "Insert into tblContact_Type(ContactTypeId,ContactType) " +
                        "values('" + aInfo.getContactTypeId() + "','" + aInfo.getContactType() + "')";
                database.execSQL(insertQuery);
            }

        
            database.setTransactionSuccessful();
            database.endTransaction();
        } catch (Exception exception) {

            isTrue = false;
            Log.e("DBEX", exception.toString());

        }
        return isTrue;
    }
    public boolean InsertExpType(List<ExpenseTypeMaster> aList) {
        boolean isTrue = true;
        try {

            String tableName = "tblExpense_Type";
            _deleteAllRecordsFromaTable(tableName);
            SQLiteDatabase database = dbHelperMain.getWritableDatabase();
            database.beginTransaction();
            for (int i = 0; i < aList.size(); i++) {
                ExpenseTypeMaster aInfo = aList.get(i);
                String insertQuery = "Insert into tblExpense_Type(ExpenseTypeId,ExpenseTypeName) " +
                        "values('" + aInfo.getExpenseTypeId() + "','" + aInfo.getExpenseTypeName().replace("'", "''") + "')";
                database.execSQL(insertQuery);
            }

        
            database.setTransactionSuccessful();
            database.endTransaction();
        } catch (Exception exception) {

            isTrue = false;
            Log.e("DBEX", exception.toString());

        }
        return isTrue;
    }
    public boolean InsertLeaveType(List<LeaveTypeInfo> aList) {
        boolean isTrue = true;
        try {

            String tableName = "tblLeave_Type";
            _deleteAllRecordsFromaTable(tableName);
            SQLiteDatabase database = dbHelperMain.getWritableDatabase();
            database.beginTransaction();
            for (int i = 0; i < aList.size(); i++) {
                LeaveTypeInfo aInfo = aList.get(i);
                String insertQuery = "Insert into tblLeave_Type(LeaveBalanceId,LeaveTypeName,YearlyLeaveBalance) " +
                        "values('" + aInfo.getLeaveBalanceId() + "','" + aInfo.getLeaveTypeName().replace("'", "''") + "','" + aInfo.getYearlyLeaveBalance() + "')";
                database.execSQL(insertQuery);
            }
        
            database.setTransactionSuccessful();
            database.endTransaction();
        } catch (Exception exception) {

            isTrue = false;
            Log.e("DBEX", exception.toString());
        }
        return isTrue;
    }
    public boolean InsertPrescripType(List<PrescriptionTYpe> aList) {
        boolean isTrue = true;
        try {
            String tableName = "tblPrescrip_Type";
            _deleteAllRecordsFromaTable(tableName);

            SQLiteDatabase database = dbHelperMain.getWritableDatabase();
            database.beginTransaction();
            for (int i = 0; i < aList.size(); i++) {
                PrescriptionTYpe aInfo = aList.get(i);
                String insertQuery = "Insert into tblPrescrip_Type(PrescriptionTypeId,PrescriptionType) " + "values('" + aInfo.getPrescriptionTypeId() + "','" + aInfo.getPrescriptionType().replace("'", "''") + "')";
                database.execSQL(insertQuery);
            }
        
            database.setTransactionSuccessful();
            database.endTransaction();
        } catch (Exception exception) {

            isTrue = false;
            Log.e("DBEX", exception.toString());
        }
        return isTrue;
    }
    public boolean InsertNonEffectiveReason(List<NonEffectiveReason> nList) {
        boolean isTrue = true;
        try {
            String tableName = "tblNonEffectiveReason";
            _deleteAllRecordsFromaTable(tableName);

            SQLiteDatabase database = dbHelperMain.getWritableDatabase();
            database.beginTransaction();
            for (int i = 0; i < nList.size(); i++) {
                NonEffectiveReason ninfo = nList.get(i);
                String insertQuery = "Insert into tblNonEffectiveReason(ReasonId,ReasonName) " + "values('" + ninfo.getReasonId() + "','" + ninfo.getReasonName().replace("'", "''") + "')";
                database.execSQL(insertQuery);
            }
        
            database.setTransactionSuccessful();
            database.endTransaction();
        } catch (Exception exception) {

            isTrue = false;
            Log.e("DBEX", exception.toString());
        }
        return isTrue;
    }
    public boolean InsertTransport(List<Transport> aList) {
        boolean isTrue = true;
        try {
            String tableName = "tblTransportInfo";
            _deleteAllRecordsFromaTable(tableName);

            SQLiteDatabase database = dbHelperMain.getWritableDatabase();
            database.beginTransaction();
            for (int i = 0; i < aList.size(); i++) {
                Transport aInfo = aList.get(i);
                String insertQuery = "Insert into tblTransportInfo(TransportId,TransportName) " +
                        "values('" + aInfo.getTransportId() + "','" + aInfo.getTransportName().replace("'", "''") + "')";
                database.execSQL(insertQuery);
            }
        
            database.setTransactionSuccessful();
            database.endTransaction();
        } catch (Exception exception) {

            isTrue = false;
            Log.e("DBEX", exception.toString());
        }
        return isTrue;
    }
    public boolean InsertProducts(List<Product> aList) {
        boolean isTrue = true;
        try {
            String tableName = "tbl_ProductInfo";
            _deleteAllRecordsFromaTable(tableName);

            SQLiteDatabase database = dbHelperMain.getWritableDatabase();
            database.beginTransaction();
            String sql = "INSERT INTO tbl_ProductInfo(" +
                    "ProductId, ProductName, ProductCode, ProductDes, " +
                    "PackSize, UnitPrice, QuotedPrice, VatPercentage, " +
                    "VatAmountPerunit, CustomerMasterId) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

            for (Product aInfo : aList) {
                database.execSQL(sql, new Object[]{
                        aInfo.getProductId(),
                        aInfo.getProductName(),
                        aInfo.getProductCode(),
                        aInfo.getProductDes(),
                        aInfo.getPackSize(),
                        aInfo.getUnitPrice(),
                        aInfo.getQuotedPrice(),
                        aInfo.getVatPercentage(),
                        aInfo.getVatAmountPerunit(),
                        aInfo.getCustomerMasterId()
                });
            }
        
            database.setTransactionSuccessful();
            database.endTransaction();
        } catch (Exception exception) {

            isTrue = false;
            Log.e("DBEX", exception.toString());
        }
        return isTrue;
    }
    public boolean InsertProductSample(List<ProductSample> aProduct) {
        boolean isTrue = true;
        try {
            String tableName = "tbl_ProductSampleInfo";
            _deleteAllRecordsFromaTable(tableName);

            SQLiteDatabase databases = dbHelperMain.getWritableDatabase();
            databases.beginTransaction();
            for (int i = 0; i < aProduct.size(); i++) {
                ProductSample aInfo = aProduct.get(i);
                String insertQuery = "Insert into tbl_ProductSampleInfo(ProductId,ProductName,ProductCode) " +
                        "values('" + aInfo.getProductId() + "','" + aInfo.getProductName().replace("'", "''") + "','" + aInfo.getProductCode() + "')";
                databases.execSQL(insertQuery);
            }

            databases.setTransactionSuccessful();
            databases.endTransaction();
        } catch (Exception exception) {

            isTrue = false;
            Log.e("DBEX", exception.toString());
        }

        return isTrue;
    }
    public boolean InsertProductGift(List<Gift> aProduct) {
        boolean isTrue = true;
        try {
            String tableName = "tbl_ProductGiftInfo";
            _deleteAllRecordsFromaTable(tableName);

            SQLiteDatabase databases = dbHelperMain.getWritableDatabase();
            databases.beginTransaction();
            for (int i = 0; i < aProduct.size(); i++) {
                Gift aInfo = aProduct.get(i);
                String insertQuery = "Insert into tbl_ProductGiftInfo(ProductId,ProductName,ProductCode) " +
                        "values('" + aInfo.getProductId() + "','" + aInfo.getProductName().replace("'", "''") + "','" + aInfo.getProductCode() + "')";
                databases.execSQL(insertQuery);
                isTrue = true;
            }

            databases.setTransactionSuccessful();
            databases.endTransaction();
        } catch (Exception exception) {

            isTrue = false;
            Log.e("DBEX", exception.toString());
        }

        return isTrue;
    }
    public boolean InsertTourPlanPurpose(List<TourPurposeViewModel> aTpp) {
        boolean isTrue = true;
        try {
            String tableName = "tblTourPlanPurpose";
            _deleteAllRecordsFromaTable(tableName);

            SQLiteDatabase databases = dbHelperMain.getWritableDatabase();
            databases.beginTransaction();
            for (int i = 0; i < aTpp.size(); i++) {
                TourPurposeViewModel aInfo = aTpp.get(i);
                String insertQuery = "Insert into tblTourPlanPurpose(TPId,TPName,IsMarketVisit,IsOtherVisit) " +
                        "values('" + aInfo.getTPId() + "','" + aInfo.getTPName().replace("'", "''") + "','" + aInfo.getIsMarketVisit() + "','" + aInfo.getIsOtherVisit() + "')";
                databases.execSQL(insertQuery);
            }
        
            databases.setTransactionSuccessful();
            databases.endTransaction();
        } catch (Exception exception) {

            isTrue = false;
            Log.e("DBEX", exception.toString());
        }

        return isTrue;
    }

    /*public boolean InsertGift(List<> aProduct) {
        boolean isTrue = true;
        try {
            String tableName = "tbl_ProductSampleInfo";
            _deleteAllRecordsFromaTable(tableName);

            SQLiteDatabase databases = dbHelperMain.getWritableDatabase();
            databases.beginTransaction();
            for (int i = 0; i < aProduct.size(); i++) {
                ProductSample aInfo = aProduct.get(i);
                String insertQuery = "Insert into tbl_ProductSampleInfo(ProductId,ProductName,ProductCode) " +
                        "values('" + aInfo.getProductId() + "','" + aInfo.getProductName() + "','" + aInfo.getProductCode() + "')";
                databases.execSQL(insertQuery);
            }
        
            databases.setTransactionSuccessful();
            databases.endTransaction();
        } catch (Exception exception) {

            Log.e("DBEX", exception.toString());
        }

        return isTrue;
    }*/

    //Initial Sync
   /* public void Insert_InitTable(String today, String todaytime) {
        try {
            String tableName = "tblInitTable";
            _deleteAllRecordsFromaTable(tableName);
            SQLiteDatabase database = dbHelperMain.getWritableDatabase();
            database.beginTransaction();

            String insertQuery = "Insert into tblInitTable(IsFirstSyncDone,Date,Time) " +
                    "values(1,'" + today + "','" + todaytime + "')";
            database.execSQL(insertQuery);
        
            database.setTransactionSuccessful();
            database.endTransaction();
        } catch (Exception exception) {

            Log.e("DBEX", exception.toString());
        }
    }*/

    //Initial Sync
    public boolean Insert_InitTableB(String today, String todaytime) {
        boolean isTrue = true;
        try {
            String tableName = "tblInitTable";
            _deleteAllRecordsFromaTable(tableName);
            SQLiteDatabase database = dbHelperMain.getWritableDatabase();
            database.beginTransaction();

            String insertQuery = "Insert into tblInitTable(IsFirstSyncDone,Date,Time) " +
                    "values(1,'" + today + "','" + todaytime + "')";
            database.execSQL(insertQuery);

            database.setTransactionSuccessful();
            database.endTransaction();
        } catch (Exception exception) {
            isTrue = false;
            Log.e("DBEX", exception.toString());
        }
        return isTrue;
    }

    //Check Data in initial Table
    public boolean CheckDataInTable(String tableName) {

        SQLiteDatabase database = dbHelperMain.getWritableDatabase();
        Cursor cursor = database.rawQuery("SELECT * FROM '" + tableName + "'", null);
        int count = cursor.getCount();
        if (count > 0) {
            return true;
        } else {
            return false;
        }

    }

    //Check backup load or not
   /* public void Insert_InitTableLoadBackup() {
        try {
            String tableName = "tblInitTableLoadBackup";
            _deleteAllRecordsFromaTable(tableName);
            SQLiteDatabase database = dbHelperMain.getWritableDatabase();
            database.beginTransaction();
            String insertQuery = "Insert into tblInitTableLoadBackup(IsFirstLoadBackupDone) " +
                    "values(1)";
            database.execSQL(insertQuery);
        
            database.setTransactionSuccessful();
            database.endTransaction();
        } catch (Exception exception) {

            Log.e("DBEX", exception.toString());
        }
    }*/

    public void _deleteAllRecordsFromaTable(String tableName) {
        SQLiteDatabase database = dbHelperMain.getWritableDatabase();
        try {
            String query = "delete from '" + tableName + "';";
            database.execSQL(query);
        } catch (Exception ex) {
            Log.e("recordDelError", ex.toString());
        }
    }


}
