/*      Copyright 2016 Marcello de Paula Ferreira Costa

        Licensed under the Apache License, Version 2.0 (the "License");
        you may not use this file except in compliance with the License.
        You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

        Unless required by applicable law or agreed to in writing, software
        distributed under the License is distributed on an "AS IS" BASIS,
        WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
        See the License for the specific language governing permissions and
        limitations under the License. */
package mchs.neverforget;

import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import mchs.neverforget.fragments.HelpAboutNotificationsFragment;
import mchs.neverforget.fragments.HelpHeadersFragment;
import mchs.neverforget.fragments.HelpHowToRenewFragment;

public class HelpActivity extends AppCompatActivity implements HelpHeadersFragment.OnFragmentInteractionListener {

    private static final String TAG_HEADERS = "TAG_HEADERS";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);
        Toolbar toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(R.string.help_activity_title);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        if (savedInstanceState == null) {
            FragmentManager fragmentManager = getFragmentManager();
            fragmentManager.beginTransaction()
                    .add(R.id.help_fragment_holder, HelpHeadersFragment.newInstance())
                    .commit();
        } else {
            getFragmentManager().getFragment(savedInstanceState, "fragmentInstanceSaved");
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        // this if statement is necessary to navigate through nested and main fragments
        if (getFragmentManager().getBackStackEntryCount() == 0) {
            super.onBackPressed();
        } else {
            getFragmentManager().popBackStack();
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null){
                actionBar.setTitle(R.string.help_activity_title);
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        getFragmentManager().putFragment(
                outState,
                "fragmentInstanceSaved",
                getFragmentManager().findFragmentById(R.id.help_fragment_holder));
    }

    @Override
    public void onFragmentInteraction(int position) {
        FragmentManager fragmentManager = getFragmentManager();
        if (position == 0) {
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null){
                actionBar.setTitle(R.string.howtorenew_string);
            }
            fragmentManager.beginTransaction()
                    .replace(R.id.help_fragment_holder, HelpHowToRenewFragment.newInstance())
                    .addToBackStack(TAG_HEADERS)
                    .commit();
        } else if (position == 1) {
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null){
                actionBar.setTitle(R.string.howtonotification_string);
            }
            fragmentManager.beginTransaction()
                    .replace(R.id.help_fragment_holder, HelpAboutNotificationsFragment.newInstance())
                    .addToBackStack(TAG_HEADERS)
                    .commit();
        }
    }
}
