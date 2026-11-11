package com.creatrix.unicorn.Activity.Reports;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.creatrix.unicorn.UtilityHelper.NetworkInformation;
import com.creatrix.unicorn.UtilityHelper.SessionManagement;
import com.creatrix.unicorn.UtilityHelper.SnackBarManagement;
import com.creatrix.unicorn.databinding.ActivitySalesOrderPaymentReportBinding;

import java.util.HashMap;

public class SalesOrderPaymentReportActivity extends AppCompatActivity {

    public static final String REPORT_URL = "http://103.244.247.93:215/SalesOrder_PaymentReport_Apps.aspx";

    private ActivitySalesOrderPaymentReportBinding binding;
    private SessionManagement session;
    private ProgressDialog pd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySalesOrderPaymentReportBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        session = new SessionManagement(getApplicationContext());
        HashMap<String, String> user = session.getUserDetails();
        int empId = 0;
        try {
            if (user != null && user.get(SessionManagement.KEY_EmpId) != null) {
                empId = Integer.parseInt(user.get(SessionManagement.KEY_EmpId));
            }
        } catch (Exception ignored) {
        }

        binding.toolbarCustom.setNavigationOnClickListener(v -> finish());
        binding.btnRefresh.setOnClickListener(v -> {
            if (NetworkInformation.isConnected(SalesOrderPaymentReportActivity.this)) {
                if (pd != null && !pd.isShowing()) {
                    pd.show();
                }
                binding.webviewSalesOrderPayment.reload();
            } else {
                SnackBarManagement._error_CustomMessage(binding.getRoot(), "No Internet!!!");
            }
        });

        pd = new ProgressDialog(SalesOrderPaymentReportActivity.this);
        pd.setTitle("Loading");
        pd.setMessage("Please wait...");
        pd.setCancelable(true);

        if (NetworkInformation.isConnected(SalesOrderPaymentReportActivity.this)) {
            viewLoad(empId);
        } else {
            SnackBarManagement._error_CustomMessage(binding.getRoot(), "No Internet!!!");
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void viewLoad(int empId) {
        WebSettings settings = binding.webviewSalesOrderPayment.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);
        settings.setBuiltInZoomControls(true);
        settings.setDisplayZoomControls(false);
        settings.setSupportZoom(true);
        settings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NORMAL);
        binding.webviewSalesOrderPayment.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);

        binding.webviewSalesOrderPayment.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                if (newProgress < 100) {
                    binding.progressBar.setVisibility(View.VISIBLE);
                    binding.progressBar.setProgress(newProgress);
                } else {
                    binding.progressBar.setVisibility(View.GONE);
                }
            }
        });

        binding.webviewSalesOrderPayment.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }

            @Override
            public void onPageFinished(WebView view, final String url) {
                binding.progressBar.setVisibility(View.GONE);
                if (pd != null && pd.isShowing()) {
                    pd.dismiss();
                }
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                binding.progressBar.setVisibility(View.GONE);
                if (pd != null && pd.isShowing()) {
                    pd.dismiss();
                }
                SnackBarManagement._error_CustomMessage(binding.getRoot(), "Something went wrong or server unavailable");
            }

            // onReceivedError does not fire for HTTP 404/500, so a missing report page showed a raw IIS error
            @Override
            public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {
                if (request.isForMainFrame()) {
                    SnackBarManagement._error_CustomMessage(binding.getRoot(), "Report page not available on server (HTTP " + errorResponse.getStatusCode() + ")");
                }
            }
        });

        if (pd != null && !pd.isShowing()) {
            pd.show();
        }
        binding.webviewSalesOrderPayment.loadUrl(REPORT_URL + "?EmpId=" + empId);
    }

    @Override
    public void onBackPressed() {
        if (binding.webviewSalesOrderPayment.canGoBack()) {
            binding.webviewSalesOrderPayment.goBack();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        if (pd != null && pd.isShowing()) {
            pd.dismiss();
        }
        super.onDestroy();
    }
}
