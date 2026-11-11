package com.creatrix.unicorn.Activity.Doctor.AddDoctor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import android.content.Context;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.creatrix.unicorn.R;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/** The Degree / Speciality / Brand / Special Day column the user can drag into order. */
@RunWith(AndroidJUnit4.class)
public class DoctorFieldOrderTest {

    private static final String DEFAULT_ORDER =
            "degreeSection,specialitySection,brandSection,specialDaySection";

    private LinearLayout box;

    @Before
    public void inflateDoctorForm() {
        Context target = InstrumentationRegistry.getInstrumentation().getTargetContext();
        Context themed = new ContextThemeWrapper(target, R.style.Theme_SalesSolution);
        View form = LayoutInflater.from(themed).inflate(R.layout.activity_doctor, null);
        box = form.findViewById(R.id.reorderSection);
        assertNotNull("reorderSection is missing from activity_doctor", box);
    }

    @Test
    public void fourCardsSitInOneColumn() {
        assertEquals(4, box.getChildCount());
        assertEquals(DEFAULT_ORDER, DoctorActivity.fieldOrderOf(box));
    }

    @Test
    public void everyCardHasADragHandle() {
        for (int i = 0; i < box.getChildCount(); i++) {
            assertNotNull("card " + i + " has no drag handle",
                    box.getChildAt(i).findViewWithTag("dragHandle"));
        }
    }

    @Test
    public void savedOrderMovesNamedCardsToTheFront() {
        DoctorActivity.applyFieldOrder(box, "specialDaySection,brandSection");
        assertEquals("specialDaySection,brandSection,degreeSection,specialitySection",
                DoctorActivity.fieldOrderOf(box));
    }

    @Test
    public void aFullOrderIsRestoredExactly() {
        String wanted = "brandSection,specialDaySection,degreeSection,specialitySection";
        DoctorActivity.applyFieldOrder(box, wanted);
        assertEquals(wanted, DoctorActivity.fieldOrderOf(box));
    }

    @Test
    public void savingThenApplyingIsStable() {
        DoctorActivity.applyFieldOrder(box, "specialitySection,specialDaySection");
        String saved = DoctorActivity.fieldOrderOf(box);
        DoctorActivity.applyFieldOrder(box, saved);
        assertEquals(saved, DoctorActivity.fieldOrderOf(box));
    }

    @Test
    public void unknownOrEmptyOrderLeavesTheFormIntact() {
        DoctorActivity.applyFieldOrder(box, "");
        DoctorActivity.applyFieldOrder(box, "aCardFromAnOlderBuild,anotherOne");
        assertEquals(DEFAULT_ORDER, DoctorActivity.fieldOrderOf(box));
        assertEquals(4, box.getChildCount());
    }
}
