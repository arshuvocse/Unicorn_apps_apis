package com.creatrix.unicorn.Interface;

import com.creatrix.unicorn.Model.Doctor.Brand;
import com.creatrix.unicorn.Model.Doctor.ContactTypeVM;
import com.creatrix.unicorn.Model.Doctor.DoctorCategory;
import com.creatrix.unicorn.Model.Doctor.DoctorChamberTypeVM;
import com.creatrix.unicorn.Model.Doctor.DoctorDegreeViewModel;
import com.creatrix.unicorn.Model.Doctor.DoctorDesignation;
import com.creatrix.unicorn.Model.Doctor.DoctorSM;
import com.creatrix.unicorn.Model.Doctor.DoctorSpecialityViewModel;
import com.creatrix.unicorn.Model.Doctor.DoctorTypeVM;
import com.creatrix.unicorn.Model.Doctor.ProgramType;
import com.creatrix.unicorn.Model.Doctor.SpecialDay;
import com.creatrix.unicorn.Model.InstitutionVM;
import com.creatrix.unicorn.Model.ModelProviderType;
import com.creatrix.unicorn.Model.ModelSMCType;

import java.util.List;

public interface IDoctor {
    interface Presenter{
        void GetDoctorDesignation(int id);
        void GetDoctorType(int id);
        void GetDegree(int doctypeId);
        void GetSpeciality();
        void GetInstitute();
        void GetBrand();
        void GetDoctorCategory(int id);
        void GetChamber(int id);
        void GetContactType(int id);
        void GetProviderType(int id);
        void GetSMCType(int id);
        void GetSpecialType(int id);
        void SaveDoctor(DoctorSM doctorSM,String type);
    }

    interface View{
        void onDoctorDesignationGet(List<DoctorDesignation> aList);
        void onDoctorTypeReceived(List<DoctorTypeVM> aList);
        void onDegreeReceived(List<DoctorDegreeViewModel> aList);
        void onSpecialityReceived(List<DoctorSpecialityViewModel> aList);
        void onInstituteReceived(List<InstitutionVM> aList);
        void onBrandReceived(List<Brand> aList);
        void onDocCategoryReceived(List<DoctorCategory> aList);
        void onChamberReceived(List<DoctorChamberTypeVM> aList);
        void onContactTypeReceived(List<ContactTypeVM> aList);
/*        void onProgramTypeReceived(List<ProgramType> aList);*/
        void onProviderTypeReceived(List<ModelProviderType> aList);
        void onSMCTypeReceived(List<ModelSMCType> aList);
        void onSpecialTypeReceived(List<SpecialDay> aList);
        void onSubmitSuccess(String mesg);
        void onSubmitError(String mesg);

    }


}
