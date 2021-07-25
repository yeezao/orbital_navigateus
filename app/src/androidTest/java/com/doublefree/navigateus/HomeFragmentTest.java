package com.doublefree.navigateus;

import com.android21buttons.fragmenttestrule.FragmentTestRule;
import com.doublefree.navigateus.favourites.FavouriteDatabase;
import com.doublefree.navigateus.favourites.FavouriteStop;
import com.doublefree.navigateus.ui.home.HomeFragment;

import org.junit.Rule;
import org.junit.Test;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

public class HomeFragmentTest {
    private FavouriteStop favouriteStop;
    private FavouriteDatabase db;

    @Rule
    public FragmentTestRule<?, HomeFragment> fragmentTestRule =
            new FragmentTestRule<>(MainActivity.class, HomeFragment.class);

    @Test
    public void testServiceAlert() throws Exception{
//        FragmentScenario<DirectionsFragment> scenario = FragmentScenario.launchInContainer(DirectionsFragment.class);
//        scenario.moveToState(Lifecycle.State.STARTED);
        onView(withId(R.id.serviceAlerts_label))
                .perform(click());
    }
}
