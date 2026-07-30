package com.creatrix.unicorn.Activity.Approval;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.creatrix.unicorn.Activity.Approval.DA.TeamDAListActivity;
import com.creatrix.unicorn.Activity.Approval.DCR.DcrApprovalListActivity;
import com.creatrix.unicorn.Activity.Approval.Leave.LeaveApprovalListActivity;
import com.creatrix.unicorn.Activity.Approval.Order.OrderApprovalListActivity;
import com.creatrix.unicorn.Activity.Approval.Prescription.PrescriptionApprovalListActivity;
import com.creatrix.unicorn.Activity.Approval.VisitPlan.ActivityDash;
import com.creatrix.unicorn.Activity.Attendance.TeamAttendancReportActivity;
import com.creatrix.unicorn.Activity.Customer.Approval.CustomerApprovalListActivity;
import com.creatrix.unicorn.Activity.DashboardActivity;
import com.creatrix.unicorn.Activity.Doctor.Approval.DoctorApprovalListActivity;
import com.creatrix.unicorn.Activity.Expense.Approval.TeamExpClaimReportActivity;
import com.creatrix.unicorn.Activity.MileageClaim.Report.TeamMileageClaimActivity;
import com.creatrix.unicorn.R;
import com.creatrix.unicorn.UtilityHelper.SessionManagement;
import com.creatrix.unicorn.databinding.FragmentApprovalPanelBinding;

import java.util.HashMap;
import java.util.Objects;


public class ApprovalPanelFragment extends Fragment {

FragmentApprovalPanelBinding binding;
    //for role
    SessionManagement session;
    HashMap<String, String> userInfo = new HashMap<>();
    String role;

    public ApprovalPanelFragment() {
        // Required empty public constructor
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentApprovalPanelBinding.inflate(getLayoutInflater());
        session = new SessionManagement(getActivity());
        userInfo = session.getUserDetails();
        role = userInfo.get(SessionManagement.KEY_EmpRoleType);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        switch (role) {
            case "MIO":
                binding.crdTeamAtteClick.setVisibility(View.GONE);
                binding.crdTeamExpense.setVisibility(View.GONE);
                binding.crdTeammileage.setVisibility(View.GONE);
                binding.crdTeamDcr.setVisibility(View.GONE);
                binding.crdTeamPresc.setVisibility(View.GONE);
                binding.crdTeamLeave.setVisibility(View.GONE);
                binding.doctorApprovalClick.setVisibility(View.GONE);
                binding.CustomerApprovalClick.setVisibility(View.GONE);
                binding.OrderApprovalClick.setVisibility(View.GONE);
                binding.DaApprovalClick.setVisibility(View.GONE);

                break;
            case "AM":
                binding.crdTeammileage.setVisibility(View.GONE);
                binding.crdTeamAtteClick.setVisibility(View.VISIBLE);
                binding.crdTeamExpense.setVisibility(View.GONE);
                binding.crdTeamDcr.setVisibility(View.VISIBLE);
                binding.crdTeamPresc.setVisibility(View.GONE);
                binding.crdTeamLeave.setVisibility(View.VISIBLE);
                binding.doctorApprovalClick.setVisibility(View.VISIBLE);
                binding.CustomerApprovalClick.setVisibility(View.VISIBLE);
                binding.OrderApprovalClick.setVisibility(View.VISIBLE);
                binding.DaApprovalClick.setVisibility(View.VISIBLE);

                break;
            case "DZSM":
                binding.crdTeammileage.setVisibility(View.GONE);
                binding.crdTeamAtteClick.setVisibility(View.VISIBLE);
                binding.crdTeamExpense.setVisibility(View.GONE);
                binding.crdTeamDcr.setVisibility(View.VISIBLE);
                binding.crdTeamPresc.setVisibility(View.GONE);
                binding.crdTeamLeave.setVisibility(View.VISIBLE);
                binding.doctorApprovalClick.setVisibility(View.VISIBLE);
                binding.CustomerApprovalClick.setVisibility(View.VISIBLE);
                binding.OrderApprovalClick.setVisibility(View.VISIBLE);
                binding.DaApprovalClick.setVisibility(View.VISIBLE);
                binding.crdTeammileage.setVisibility(View.GONE);
            case "NSM":
            case "Admin":
                binding.crdTeamAtteClick.setVisibility(View.VISIBLE);
                binding.crdTeamExpense.setVisibility(View.GONE);
                binding.crdTeammileage.setVisibility(View.GONE);
                binding.crdTeamDcr.setVisibility(View.VISIBLE);
                binding.crdTeamPresc.setVisibility(View.GONE);
                binding.crdTeamLeave.setVisibility(View.VISIBLE);
                binding.doctorApprovalClick.setVisibility(View.VISIBLE);
                binding.CustomerApprovalClick.setVisibility(View.VISIBLE);
                break;
        }
        binding.btnTeamAtte.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getActivity(), TeamAttendancReportActivity.class);
                startActivity(i);
                requireActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });
        //TODO:Expense TeamAtten Approve
        binding.btnTeamExpenseClaim.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getActivity(), TeamExpClaimReportActivity.class);
                startActivity(i);
                requireActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });
        binding.btnTeamDcr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // SnackBarManagement._warning_CustomMessage(getView(), "Comming Soon!!");
                Intent i = new Intent(getActivity(), DcrApprovalListActivity.class);
                startActivity(i);
                requireActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });
        binding.crdTeamPresc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getActivity(), PrescriptionApprovalListActivity.class);
                startActivity(i);
                requireActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });
        binding.btnTeamLeave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getActivity(), LeaveApprovalListActivity.class);
                startActivity(i);
                requireActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });
        binding.btnTeamMilageClaim.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getActivity(), TeamMileageClaimActivity.class);
                startActivity(i);
                requireActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });
        binding.doctorApprovalClick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getActivity(), DoctorApprovalListActivity.class);
                startActivity(i);
                requireActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });
        binding.CustomerApprovalClick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getActivity(), CustomerApprovalListActivity.class);
                startActivity(i);
                requireActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });

        binding.OrderApprovalClick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getActivity(), OrderApprovalListActivity.class);
                startActivity(i);
                requireActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });
        binding.DaApprovalClick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getActivity(), TeamDAListActivity.class);
                startActivity(i);
                requireActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });

    }
}