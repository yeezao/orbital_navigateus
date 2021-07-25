package com.doublefree.navigateus;

import com.android21buttons.fragmenttestrule.FragmentTestRule;
import com.doublefree.navigateus.ui.stops_services.StopsServicesFragment;

import org.junit.Rule;
import org.junit.Test;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

public class StopsAndServicesFragmentTest {
    @Rule
    public FragmentTestRule<?, StopsServicesFragment> fragmentTestRule =
            new FragmentTestRule<>(MainActivity.class, StopsServicesFragment.class);

    @Test
    public void testExpandableListView() throws Exception{
//        FragmentScenario<DirectionsFragment> scenario = FragmentScenario.launchInContainer(DirectionsFragment.class);
//        scenario.moveToState(Lifecycle.State.STARTED);
        onView(withId(R.id.expandable_listview_nus_stops))
                .perform(click());
    }
}
