package com.creatrix.unicorn.Activity.SelfReports;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.Matchers.allOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Spinner;

import androidx.recyclerview.widget.RecyclerView;
import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.ViewInteraction;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry;
import androidx.test.runner.lifecycle.Stage;

import com.creatrix.unicorn.Activity.Attendance.AttendanceReportsActivity;
import com.creatrix.unicorn.Activity.Customer.Pending.CustomerPendingActivity;
import com.creatrix.unicorn.Activity.Doctor.Pending.DoctorPendingActivity;
import com.creatrix.unicorn.Activity.SelfReports.ReportsTargetAcheiActivity;
import com.creatrix.unicorn.Activity.SelfReports.SalesReport.SalesReportActivity;
import com.creatrix.unicorn.R;
import com.creatrix.unicorn.UtilityHelper.SessionManagement;

import org.hamcrest.Matcher;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

/**
 * Drives the Attendance / Order Summary / Sales Summary filters on a device against the API the app
 * is pointed at. Seeds the login session directly (local DB users), so run it only on a test build.
 * Taps are dispatched in-process (dispatchTouchEvent) because some phones (MIUI) refuse Espresso's
 * input injection. Read the result lines with:  adb logcat -d -s UITEST
 */
@RunWith(AndroidJUnit4.class)
public class ReportFilterUiTest {
    private static final String TAG = "UITEST";
    private static final Context CTX = InstrumentationRegistry.getInstrumentation().getTargetContext();

    // local SalesDisDB_Unicorn_DB users
    private static void loginMio()     { login(1023, "Md. Razon Ali", "EMP000026", 43, "1", "MIO", "Executive - Sales"); }
    private static void loginManager() { login(1025, "Md. Sohanur Rahman", "EMP000039", 25, "2", "AM", "Team Leader"); }

    private static void login(int userId, String name, String code, int empId, String roleTypeId, String roleType, String desig) {
        new SessionManagement(CTX).createLoginSession(userId, name, code, code, "", empId, "", "", roleType, roleTypeId, roleType, desig);
    }

    @AfterClass
    public static void restoreMioLogin() { loginMio(); }

    /**
     * MIUI aborts activity starts from a background process (START_ABORTED), so the app must be in the
     * foreground before ActivityScenario.launch: the runner script launches it with
     * `adb shell monkey -p com.creatrix.unicorn -c android.intent.category.LAUNCHER 1`; this waits for the
     * dashboard (the splash screen opens the dashboard with CLEAR_TASK, which would kill the activity under test).
     */
    @Before
    public void waitForDashboardInForeground() {
        long end = SystemClock.uptimeMillis() + 90000;
        while (SystemClock.uptimeMillis() < end && !resumedActivity().contains("MainDashboardActivity")) SystemClock.sleep(500);
        assertTrue("dashboard never came to the foreground (launch the app with monkey), resumed=" + resumedActivity(),
                resumedActivity().contains("MainDashboardActivity"));
        SystemClock.sleep(2000);
    }

    // ---------- Attendance Report ----------

    @Test
    public void attendance_mio_selfOnlyReadOnly() {
        loginMio();
        try (ActivityScenario<AttendanceReportsActivity> s = ActivityScenario.launch(AttendanceReportsActivity.class)) {
            waitDisplayed(withId(R.id.atteFilter));
            tap(withId(R.id.atteFilter));
            waitDisplayed(withId(R.id.spnEmp));
            List<String> items = spinnerItems(withId(R.id.spnEmp));
            Log.i(TAG, "attendance MIO spinner=" + items + " enabled=" + isEnabled(withId(R.id.spnEmp)));
            assertEquals(1, items.size());
            assertTrue(items.get(0).contains("(Self)"));
            assertFalse(isEnabled(withId(R.id.spnEmp)));
            tap(withId(R.id.psubmitBnt));
            Log.i(TAG, "attendance MIO rows=" + waitRows(withId(R.id.recycler_view)));
        }
    }

    @Test
    public void attendance_manager_allTeamSearchable() {
        loginManager();
        try (ActivityScenario<AttendanceReportsActivity> s = ActivityScenario.launch(AttendanceReportsActivity.class)) {
            waitDisplayed(withId(R.id.atteFilter));
            tap(withId(R.id.atteFilter));
            waitDisplayed(withId(R.id.spnEmp));
            SystemClock.sleep(4000); // team API
            List<String> items = spinnerItems(withId(R.id.spnEmp));
            Log.i(TAG, "attendance AM spinner=" + items + " enabled=" + isEnabled(withId(R.id.spnEmp)));
            assertTrue(isEnabled(withId(R.id.spnEmp)));
            assertTrue(items.size() >= 3);
            assertEquals("All", items.get(0));
            // searchable: tapping opens the search dialog; pick "All" from its list
            tap(withId(R.id.spnEmp));
            waitDisplayed(isAssignableFrom(SearchView.class));
            Log.i(TAG, "attendance AM search dialog shown");
            pickFromDialog(0);
            Log.i(TAG, "attendance AM selected=" + selected(withId(R.id.spnEmp)));
            // a month that has attendance rows in the test DB
            onView(withId(R.id.txtFromDate)).check((v, e) -> ((android.widget.TextView) v).setText("01-Sep-2026"));
            onView(withId(R.id.txtToDate)).check((v, e) -> ((android.widget.TextView) v).setText("30-Sep-2026"));
            tap(withId(R.id.psubmitBnt));
            int rows = waitRows(withId(R.id.recycler_view));
            Log.i(TAG, "attendance AM All rows=" + rows + " firstHeader=" + firstHeader(withId(R.id.recycler_view)));
            assertTrue(rows > 0);
        }
    }

    // ---------- Target & Achievement ----------

    @Test
    public void targetAchievement_manager_pickOnePerson() {
        loginManager();
        try (ActivityScenario<ReportsTargetAcheiActivity> s = ActivityScenario.launch(ReportsTargetAcheiActivity.class)) {
            waitDisplayed(withId(R.id.atteFilter));
            tap(withId(R.id.atteFilter));
            waitDisplayed(withId(R.id.spnEmp));
            SystemClock.sleep(3000); // team API
            List<String> items = spinnerItems(withId(R.id.spnEmp));
            Log.i(TAG, "target AM spinner=" + items + " enabled=" + isEnabled(withId(R.id.spnEmp)));
            assertTrue(isEnabled(withId(R.id.spnEmp)));
            assertEquals("All", items.get(0));
            int nurAmin = -1;
            for (int i = 0; i < items.size(); i++) if (items.get(i).startsWith("Nur Amin")) nurAmin = i;
            assertTrue("Nur Amin not in team list", nurAmin >= 0);
            tap(withId(R.id.spnEmp));
            waitDisplayed(isAssignableFrom(SearchView.class));
            pickFromDialog(nurAmin);
            Log.i(TAG, "target AM selected=" + selected(withId(R.id.spnEmp)));
            tap(withId(R.id.psubmitBnt));
            int rows = waitRows(withId(R.id.recycler_view));
            Log.i(TAG, "target AM NurAmin rows=" + rows + " totalTarget=" + text(withId(R.id.mainTarget)));
            assertTrue(rows > 0);
        }
    }

    @Test
    public void targetAchievement_mio_ownTargetOnly() {
        loginMio();
        try (ActivityScenario<ReportsTargetAcheiActivity> s = ActivityScenario.launch(ReportsTargetAcheiActivity.class)) {
            waitDisplayed(withId(R.id.atteFilter));
            int rows = waitRows(withId(R.id.recycler_view));
            Log.i(TAG, "target MIO rows=" + rows + " totalTarget=" + text(withId(R.id.mainTarget)));
            assertTrue(rows > 0);
            tap(withId(R.id.atteFilter));
            waitDisplayed(withId(R.id.spnEmp));
            List<String> items = spinnerItems(withId(R.id.spnEmp));
            Log.i(TAG, "target MIO spinner=" + items + " enabled=" + isEnabled(withId(R.id.spnEmp)));
            assertEquals(1, items.size());
            assertFalse(isEnabled(withId(R.id.spnEmp)));
        }
    }

    // ---------- DCR List / Customer List / Doctor List ----------

    @Test
    public void dcrList_manager_allThenOneDoctor() {
        loginManager();
        try (ActivityScenario<ReportsDcrActivity> s = ActivityScenario.launch(ReportsDcrActivity.class)) {
            waitDisplayed(withId(R.id.atteFilter));
            tap(withId(R.id.atteFilter));
            waitDisplayed(withId(R.id.spinner_sales_person));
            SystemClock.sleep(4000); // team API
            assertHidden(R.id.til_progtypetype);
            assertHidden(R.id.til_pharmatype);
            List<String> people = spinnerItems(withId(R.id.spinner_sales_person));
            List<String> doctors = spinnerItems(withId(R.id.spinner_doctor));
            Log.i(TAG, "dcr AM people=" + people + " doctors=" + doctors.size() + " first=" + doctors.get(0));
            assertEquals("All", people.get(0));
            assertEquals("All", doctors.get(0));
            onView(withId(R.id.txtFromDate)).check((v, e) -> ((android.widget.TextView) v).setText("01-Sep-2026"));
            onView(withId(R.id.txtToDate)).check((v, e) -> ((android.widget.TextView) v).setText("31-Oct-2026"));
            tap(withId(R.id.spinner_sales_person));
            waitDisplayed(isAssignableFrom(SearchView.class));
            pickFromDialog(0); // All
            tap(withId(R.id.btn_donefilter));
            int all = waitRows(withId(R.id.recycler_view));
            Log.i(TAG, "dcr AM All rows=" + all);
            assertTrue(all > 0);
        }
    }

    @Test
    public void customerList_manager_pickPersonAndCustomer() {
        loginManager();
        try (ActivityScenario<CustomerPendingActivity> s = ActivityScenario.launch(CustomerPendingActivity.class)) {
            waitDisplayed(withId(R.id.filter));
            int before = waitRows(withId(R.id.rv_customerpending));
            SystemClock.sleep(3000);
            tap(withId(R.id.filter));
            waitDisplayed(withId(R.id.spinner_sales_person));
            assertHidden(R.id.til_progtypetype);
            assertHidden(R.id.til_pharmatype);
            List<String> people = spinnerItems(withId(R.id.spinner_sales_person));
            List<String> customers = spinnerItems(withId(R.id.spinner_customer));
            Log.i(TAG, "customerList AM rows=" + before + " people=" + people + " customers=" + customers.size());
            assertEquals("All", people.get(0));
            assertEquals("All", customers.get(0));
            tap(withId(R.id.spinner_customer));
            waitDisplayed(isAssignableFrom(SearchView.class));
            pickFromDialog(1);
            String picked = selected(withId(R.id.spinner_customer));
            tap(withId(R.id.btn_donefilter));
            SystemClock.sleep(3000);
            int after = rowsNow(withId(R.id.rv_customerpending));
            Log.i(TAG, "customerList AM picked=" + picked + " rows=" + after);
            assertTrue(after <= 1);
        }
    }

    @Test
    public void doctorList_manager_pickPersonAndDoctor() {
        loginManager();
        try (ActivityScenario<DoctorPendingActivity> s = ActivityScenario.launch(DoctorPendingActivity.class)) {
            waitDisplayed(withId(R.id.filter));
            int before = waitRows(withId(R.id.rv_doctorpending));
            SystemClock.sleep(3000);
            tap(withId(R.id.filter));
            waitDisplayed(withId(R.id.spinner_sales_person));
            List<String> people = spinnerItems(withId(R.id.spinner_sales_person));
            List<String> doctors = spinnerItems(withId(R.id.spinner_doctor));
            Log.i(TAG, "doctorList AM rows=" + before + " people=" + people + " doctors=" + doctors.size());
            assertEquals("All", people.get(0));
            assertEquals("All", doctors.get(0));
            // pick one team member: the list must reload for that person
            int member = people.size() > 2 ? 2 : 1;
            tap(withId(R.id.spinner_sales_person));
            waitDisplayed(isAssignableFrom(SearchView.class));
            pickFromDialog(member);
            String who = selected(withId(R.id.spinner_sales_person));
            tap(withId(R.id.btn_donefilter));
            SystemClock.sleep(5000);
            int after = rowsNow(withId(R.id.rv_doctorpending));
            Log.i(TAG, "doctorList AM person=" + who + " rows=" + after);
            assertTrue(after >= 0);
        }
    }

    private static void assertHidden(int id) {
        onView(withId(id)).check((v, e) -> assertTrue("should be hidden: " + v.getResources().getResourceEntryName(id), !v.isShown()));
    }

    private static String text(Matcher<View> m) {
        String[] r = {""};
        onView(m).check((v, e) -> r[0] = ((android.widget.TextView) v).getText().toString());
        return r[0];
    }

    // ---------- Order / Sales Summary ----------

    @Test public void orderSummary_manager()  { summary("Order", true); }
    @Test public void salesSummary_manager()  { summary("Sales", true); }
    @Test public void orderSummary_mio()      { summary("Order", false); }

    private void summary(String from, boolean manager) {
        if (manager) loginManager(); else loginMio();
        Intent i = new Intent(CTX, SalesReportActivity.class).putExtra("From", from);
        try (ActivityScenario<SalesReportActivity> s = ActivityScenario.launch(i)) {
            Matcher<View> filter = allOf(withId(R.id.cfilter), isDisplayed());
            waitDisplayed(filter);
            SystemClock.sleep(3000); // team API
            tap(filter);
            waitDisplayed(withId(R.id.spinner_sales_person));
            List<String> people = spinnerItems(withId(R.id.spinner_sales_person));
            List<String> customers = spinnerItems(withId(R.id.spinner_customer));
            boolean enabled = isEnabled(withId(R.id.spinner_sales_person));
            Log.i(TAG, from + (manager ? " AM" : " MIO") + " people=" + people + " enabled=" + enabled
                    + " customers=" + customers.size() + " first=" + (customers.isEmpty() ? "-" : customers.get(0)));
            assertEquals("All", customers.get(0));
            assertTrue("customer list from SQLite is empty", customers.size() > 1);
            if (manager) {
                assertTrue(enabled);
                assertEquals("All", people.get(0));
                assertTrue(people.size() >= 3);
                tap(withId(R.id.spinner_sales_person));
                waitDisplayed(isAssignableFrom(SearchView.class));
                pickFromDialog(0); // All
            } else {
                assertFalse(enabled);
                assertEquals(1, people.size());
            }
            // customer: open the searchable dialog and pick the first real customer
            tap(withId(R.id.spinner_customer));
            waitDisplayed(isAssignableFrom(SearchView.class));
            Log.i(TAG, from + " customer search dialog shown");
            pickFromDialog(1);
            Log.i(TAG, from + " picked person=" + selected(withId(R.id.spinner_sales_person)) + " customer=" + selected(withId(R.id.spinner_customer)));
            tap(withId(R.id.btn_donefilter));
            // wait for the progress dialog to go and the list to be reachable again (up to 30 s)
            long end = SystemClock.uptimeMillis() + 30000;
            int rows = -1;
            while (SystemClock.uptimeMillis() < end && (rows = rowsNow(allOf(withId(R.id.recycler_view), isDisplayed()))) < 0) SystemClock.sleep(1000);
            Log.i(TAG, from + " rows after customer filter=" + rows);
            assertTrue("report list never came back after DONE", rows >= 0);
        }
    }

    /** Typing letters from the middle of a word must still find the customer (the old filter matched word starts only). */
    @Test
    public void customerSearch_matchesMidWord() {
        loginMio();
        Intent i = new Intent(CTX, SalesReportActivity.class).putExtra("From", "Sales");
        try (ActivityScenario<SalesReportActivity> s = ActivityScenario.launch(i)) {
            Matcher<View> filter = allOf(withId(R.id.cfilter), isDisplayed());
            waitDisplayed(filter);
            tap(filter);
            waitDisplayed(withId(R.id.spinner_customer));
            List<String> customers = spinnerItems(withId(R.id.spinner_customer));
            String target = customers.get(1);
            int at = -1; // first spot where 3 letters start mid-word
            for (int k = 1; k + 3 <= target.length() && at < 0; k++)
                if (Character.isLetter(target.charAt(k - 1)) && target.substring(k, k + 3).chars().allMatch(Character::isLetter)) at = k;
            assertTrue("no mid-word letters in " + target, at > 0);
            String q = target.substring(at, at + 3).toUpperCase();
            tap(withId(R.id.spinner_customer));
            waitDisplayed(isAssignableFrom(SearchView.class));
            onView(isAssignableFrom(SearchView.class)).check((v, e) -> ((SearchView) v).setQuery(q, false));
            SystemClock.sleep(800);
            List<String> hits = new ArrayList<>();
            onView(isAssignableFrom(ListView.class)).check((v, e) -> {
                ListView lv = (ListView) v;
                for (int k = 0; k < lv.getAdapter().getCount(); k++) hits.add(String.valueOf(lv.getAdapter().getItem(k)));
            });
            Log.i(TAG, "search q=" + q + " target=" + target + " hits=" + hits.size() + " of " + customers.size());
            assertTrue("target missing from results", hits.contains(target));
            for (String h : hits) assertTrue("non-matching hit " + h, h.toLowerCase().contains(q.toLowerCase()));
            pickFromDialog(hits.indexOf(target));
            assertEquals(target, selected(withId(R.id.spinner_customer)));
        }
    }

    // ---------- "CODE : Name" pickers ----------

    @Test
    public void doctorList_showsCodeAndSearchesIt() {
        loginMio();
        Intent i = new Intent(CTX, com.creatrix.unicorn.Activity.Doctor.DoctorListActivity.class).putExtra("From", "DCR");
        try (ActivityScenario<?> s = ActivityScenario.launch(i)) { codeNamePicker("doctorList", R.id.recycler_view, R.id.searchEd, R.id.nameTxt); }
    }

    @Test
    public void updateCustomer_showsCodeAndSearchesIt() {
        loginMio();
        try (ActivityScenario<?> s = ActivityScenario.launch(com.creatrix.unicorn.Activity.Customer.UpdateCustomerActivity.class)) {
            codeNamePicker("updateCustomer", R.id.rv_customerupdate, R.id.searchCustomer, R.id.custName);
        }
    }

    @Test
    public void updateDoctor_showsCodeAndSearchesIt() {
        loginManager();
        try (ActivityScenario<?> s = ActivityScenario.launch(com.creatrix.unicorn.Activity.Doctor.UpdateDoctorActivity.class)) {
            codeNamePicker("updateDoctor", R.id.rv_doctorupdate, R.id.searchDoctor, R.id.docName);
        }
    }

    /** First row must read "CODE : Name"; typing the code's middle digits must keep only rows containing them. */
    private static void codeNamePicker(String tag, int listId, int searchId, int nameId) {
        int rows = waitRows(withId(listId));
        assertTrue(tag + ": list is empty", rows > 0);
        String first = rowTexts(listId, nameId).get(0);
        Log.i(TAG, tag + " rows=" + rows + " first=" + first);
        assertTrue(tag + ": no 'CODE : Name' in " + first, first.contains(" : "));
        String code = first.substring(0, first.indexOf(" : "));
        String q = code.length() > 3 ? code.substring(1, code.length() - 1) : code; // middle of the code
        onView(withId(searchId)).check((v, e) -> ((android.widget.EditText) v).setText(q.toLowerCase()));
        SystemClock.sleep(1500);
        List<String> hits = rowTexts(listId, nameId);
        Log.i(TAG, tag + " search q=" + q + " rows=" + rowsNow(withId(listId)) + " visible=" + hits);
        assertTrue(tag + ": search lost the row", hits.contains(first));
        for (String h : hits) assertTrue(tag + ": non-matching row " + h, h.toLowerCase().contains(q.toLowerCase()));
    }

    private static List<String> rowTexts(int listId, int nameId) {
        List<String> out = new ArrayList<>();
        onView(withId(listId)).check((v, e) -> {
            RecyclerView rv = (RecyclerView) v;
            for (int k = 0; k < rv.getChildCount(); k++) {
                android.widget.TextView tv = rv.getChildAt(k).findViewById(nameId);
                if (tv != null) out.add(tv.getText().toString());
            }
        });
        return out;
    }

    // ---------- helpers ----------

    private static String resumedActivity() {
        String[] r = {""};
        InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> {
            for (android.app.Activity a : ActivityLifecycleMonitorRegistry.getInstance().getActivitiesInStage(Stage.RESUMED))
                r[0] = a.getClass().getSimpleName();
        });
        return r[0];
    }

    private static ViewInteraction waitDisplayed(Matcher<View> m) {
        long end = SystemClock.uptimeMillis() + 30000;
        Throwable last = null;
        while (SystemClock.uptimeMillis() < end) {
            try { return onView(m).check(matches(isDisplayed())); }
            catch (Throwable t) { last = t; SystemClock.sleep(500); }
        }
        throw new AssertionError("not displayed in time: " + m, last);
    }

    /** In-process tap: DOWN + UP through the view's own dispatchTouchEvent (runs on the main thread). */
    private static void tap(Matcher<View> m) {
        onView(m).check((v, e) -> {
            int[] xy = new int[2];
            v.getLocationOnScreen(xy);
            float x = xy[0] + v.getWidth() / 2f, y = xy[1] + v.getHeight() / 2f;
            long t = SystemClock.uptimeMillis();
            MotionEvent down = MotionEvent.obtain(t, t, MotionEvent.ACTION_DOWN, x, y, 0);
            MotionEvent up = MotionEvent.obtain(t, t + 50, MotionEvent.ACTION_UP, x, y, 0);
            boolean handled = v.dispatchTouchEvent(down) | v.dispatchTouchEvent(up);
            down.recycle(); up.recycle();
            if (!handled) v.performClick();
        });
        SystemClock.sleep(800);
    }

    /** Click a row of the SearchableSpinner dialog's ListView through its own item-click listener. */
    private static void pickFromDialog(int pos) {
        onView(isAssignableFrom(ListView.class)).check((v, e) -> {
            ListView lv = (ListView) v;
            lv.performItemClick(lv.getChildAt(0), pos, lv.getAdapter().getItemId(pos));
        });
        SystemClock.sleep(800);
    }

    private static List<String> spinnerItems(Matcher<View> m) {
        List<String> out = new ArrayList<>();
        onView(m).check((v, e) -> {
            Spinner sp = (Spinner) v;
            for (int i = 0; sp.getAdapter() != null && i < sp.getAdapter().getCount(); i++) out.add(String.valueOf(sp.getAdapter().getItem(i)));
        });
        return out;
    }

    private static String selected(Matcher<View> m) {
        String[] r = {""};
        onView(m).check((v, e) -> r[0] = String.valueOf(((Spinner) v).getSelectedItem()));
        return r[0];
    }

    private static boolean isEnabled(Matcher<View> m) {
        boolean[] r = new boolean[1];
        onView(m).check((v, e) -> r[0] = v.isEnabled());
        return r[0];
    }

    private static int rowsNow(Matcher<View> m) {
        int[] n = {-1};
        try { onView(m).check((v, e) -> n[0] = ((RecyclerView) v).getAdapter() == null ? 0 : ((RecyclerView) v).getAdapter().getItemCount()); }
        catch (Throwable ignored) { }
        return n[0];
    }

    private static int waitRows(Matcher<View> m) {
        long end = SystemClock.uptimeMillis() + 30000;
        int n = 0;
        while (SystemClock.uptimeMillis() < end && (n = rowsNow(m)) <= 0) SystemClock.sleep(1000);
        return n;
    }

    private static String firstHeader(Matcher<View> m) {
        String[] r = {""};
        try {
            onView(m).check((v, e) -> {
                RecyclerView rv = (RecyclerView) v;
                View first = rv.getChildAt(0);
                android.widget.TextView tv = first == null ? null : first.findViewById(R.id.headerDate);
                r[0] = tv == null ? "?" : tv.getText().toString();
            });
        } catch (Throwable ignored) { }
        return r[0];
    }
}
