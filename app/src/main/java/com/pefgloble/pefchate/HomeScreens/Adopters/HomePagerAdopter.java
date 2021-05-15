package com.pefgloble.pefchate.HomeScreens.Adopters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.pefgloble.pefchate.HomeScreens.CameraPage;
import com.pefgloble.pefchate.HomeScreens.ProfilePage;
import com.pefgloble.pefchate.fragments.ContactsFragment;
import com.pefgloble.pefchate.fragments.ConversationsFragment;
import com.pefgloble.pefchate.fragments.StoriesFragment;

public class HomePagerAdopter extends FragmentPagerAdapter {


    public HomePagerAdopter(@NonNull FragmentManager fm) {
        super(fm);
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {

        Fragment fragment = null;

        switch (position) {
            case 0:
                fragment = new ConversationsFragment();
                break;

            case 1:
                fragment = new ContactsFragment();
                break;

            case 2:
                fragment = new CameraPage();
                break;

            case 3:
                fragment = new StoriesFragment();
                break;

            case 4:
                fragment = new ProfilePage();
                break;
        }
        assert fragment != null;
        return fragment;

    }

    @Override
    public int getCount() {
        return 5;
    }
}
