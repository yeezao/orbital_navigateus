package com.doublefree.navigateus;

import com.android21buttons.fragmenttestrule.FragmentTestRule;
import com.doublefree.navigateus.ui.onboarding.ViewPagerFragment;

import org.junit.Rule;
import org.junit.Test;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

public class OnboardingScreenTest {
    @Rule
    public FragmentTestRule<?, ViewPagerFragment> fragmentTestRule =
            new FragmentTestRule<>(MainActivity.class, ViewPagerFragment.class);


    @Test
    public void onBoardingFirstScreenTest() throws Exception{
        onView(withId(R.id.next))
                .perform(click());
        onView(withId(R.id.next2))
                .perform(click());
    }
}
