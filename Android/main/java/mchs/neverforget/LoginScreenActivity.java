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
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import mchs.neverforget.fragments.ProgressDialogFragment;
import mchs.neverforget.util.Crawler;
import mchs.neverforget.util.ReturnValues;

public class LoginScreenActivity extends AppCompatActivity implements ProgressDialogFragment.OnLoginCompletion {

    public static final String IS_LOGGED_IN = "IS_LOGGED_IN";
    public static final String USERNAME_PREFS = "USERNAME";
    public static final String PASSWORD_PREFS = "PASSWORD";
    public static final String IS_FIRST_LOGIN_PREFS = "IS_FIRST_LOGIN";
    private static final String TAG = "LoginScreenActivity";
    private static final int EMPTY_FIELDS = -1;
    private EditText usernameEditText;
    private EditText passwordEditText;
    ProgressDialogFragment fragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_screen);
        usernameEditText = (EditText) findViewById(R.id.editText_username);
        passwordEditText = (EditText) findViewById(R.id.editText_password);
        Toolbar toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);

        if (savedInstanceState != null) {
            try {
                FragmentManager fragmentManager = getFragmentManager();
                fragment = (ProgressDialogFragment) fragmentManager.
                        getFragment(savedInstanceState, ProgressDialogFragment.TAG);

                Log.d(TAG, "onCreate \"" + ProgressDialogFragment.TAG + "\" found");
            } catch (NullPointerException ignored) {
                Log.e(TAG, "onCreate couldn't find \"" + ProgressDialogFragment.TAG);
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Fragment fragment = getFragmentManager().findFragmentByTag(ProgressDialogFragment.TAG);

        if (fragment != null) {
            Log.d(TAG, "onSaveInstanceState fragment is not null");
            getFragmentManager().putFragment(
                    outState,
                    ProgressDialogFragment.TAG,
                    fragment);
        }
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        boolean isLoggedIn = PreferenceManager.getDefaultSharedPreferences(this)
                .getBoolean(IS_LOGGED_IN, false);

        if (isLoggedIn) {
            Intent neverForgetIntent = new Intent(
                    LoginScreenActivity.this,
                    NeverForgetActivity.class);
            startActivity(neverForgetIntent);
        }
    }

    private void displayInformativeDialog(int flag) {
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(LoginScreenActivity.this);
        if (flag == ReturnValues.LOGIN_FAILURE.getLevelCode()) {
            alertBuilder.setTitle(R.string.dialog_login_failure_title)
                    .setMessage(R.string.dialog_login_failure_message);
        } else if (flag == ReturnValues.RETRIEVE_PAGE_FAILURE.getLevelCode()) {
            alertBuilder.setTitle(R.string.dialog_connection_failure_title)
                    .setMessage(R.string.dialog_connection_failure_message);
        } else if (flag == EMPTY_FIELDS) {
            alertBuilder.setTitle(R.string.dialog_empty_field_title)
                    .setMessage(R.string.dialog_empty_field_message);
        }
        alertBuilder.setCancelable(false)
                .setNeutralButton(
                        R.string.dialog_ok_button,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
        alertBuilder.create().show();
    }

    public void onClickLoginButton(View view) {
        final Crawler crawler = Crawler.getInstance();
        String username = usernameEditText.getText().toString();
        String password = passwordEditText.getText().toString();
        if (username.equals("") || password.equals("")) {
            displayInformativeDialog(EMPTY_FIELDS);
        } else {
            crawler.setUsername(username);
            crawler.setPassword(password);
            fragment = new ProgressDialogFragment();
            fragment.show(getFragmentManager(), ProgressDialogFragment.TAG);
            fragment.executeLoginAsyncTask();
        }
    }

    @Override
    public void loginSuccessful() {
        Log.i(TAG,"loginSuccessful");

        final Crawler crawler = Crawler.getInstance();

        Intent neverForgetIntent = new Intent(
                LoginScreenActivity.this,
                NeverForgetActivity.class);

        PreferenceManager.getDefaultSharedPreferences(LoginScreenActivity.this).edit()
                .putBoolean(IS_FIRST_LOGIN_PREFS, true)
                .putBoolean(IS_LOGGED_IN, true)
                .putString(USERNAME_PREFS, crawler.getUsername())
                .putString(PASSWORD_PREFS, crawler.getPassword())
                .apply();
        startActivity(neverForgetIntent);
    }

    @Override
    public void loginFailed(int result) {
        Log.i(TAG, "loginFailed " + result);
        displayInformativeDialog(result);
    }
}
