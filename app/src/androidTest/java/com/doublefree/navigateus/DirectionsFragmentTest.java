package com.doublefree.navigateus;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.android21buttons.fragmenttestrule.FragmentTestRule;
import com.doublefree.navigateus.ui.directions.DirectionsFragment;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

@RunWith(AndroidJUnit4.class)

public class DirectionsFragmentTest {

    @Rule
    public FragmentTestRule<?, DirectionsFragment> fragmentTestRule =
            new FragmentTestRule<>(MainActivity.class, DirectionsFragment.class);

    @Test
    public void testOriginTextView() throws Exception{
        onView(withId(R.id.originInputEditor))
                .perform(typeText("COM2"), closeSoftKeyboard());
        // Check that the text was changed.
        onView(withId(R.id.originInputEditor)).check(matches(withText("COM2")));
    }

    @Test
    public void testDestinationTextView() throws Exception{
        onView(withId(R.id.destInputEditor))
                .perform(typeText("BIZ2"), closeSoftKeyboard());
        // Check that the text was changed.
        onView(withId(R.id.destInputEditor)).check(matches(withText("BIZ2")));
    }

    @Test
    public void testGoButton() throws Exception{
       onView(withId(R.id.button_go))
               .perform(click());
    }

    @Test
    public void testDirectionsResults() throws Exception{
        onView(withId(R.id.resultrecyclerView))
                .check(matches(isDisplayed()));

    }

    @Test
    public void testCheckBox() throws Exception{
        onView(withId(R.id.checkbox_accessible))
                .perform(click());
        onView(withId(R.id.checkbox_sheltered))
                .perform(click());
        onView(withId(R.id.checkbox_walkOnly))
                .perform(click());
    }
}