package mchs.neverforget.fragments;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import mchs.neverforget.R;
import mchs.neverforget.util.Crawler;
import mchs.neverforget.util.ReturnValues;

public class ProgressDialogFragment extends DialogFragment {
    public static final String TAG = "ProgressDialogFragment";
    private OnLoginCompletion listener;
    private ProgressDialogFragment fragment;

    public interface OnLoginCompletion {
        void loginSuccessful();
        void loginFailed(int result);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        fragment = this;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onAttach(Context context) {
        Log.d(TAG, "onAttach Context");
        super.onAttach(context);
        if (context instanceof OnLoginCompletion) {
            Log.i(TAG, "Activity implemented OnLoginCompletion");
            listener = (OnLoginCompletion) context;
        } else {
            Log.e(TAG, "Activity did not implement OnLoginCompletion");
            throw new RuntimeException(context.toString()
                    + " must implement OnLoginCompletion");
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onAttach(Activity activity) {
        Log.d(TAG, "onAttach Activity");
        super.onAttach(activity);
        if (activity instanceof OnLoginCompletion) {
            Log.i(TAG, "Activity implemented OnLoginCompletion");
            listener = (OnLoginCompletion) activity;
        } else {
            Log.e(TAG, "Activity did not implement OnLoginCompletion");
            throw new RuntimeException(activity.toString()
                    + " must implement OnLoginCompletion");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Log.d(TAG, "onCreateDialog");
        ProgressDialog progressDialog = new ProgressDialog(getActivity());
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage(getString(R.string.trying_connection));
        return progressDialog;
    }

    @Override
    public void onDetach() {
        Log.d(TAG, "onDetach " + this);
        super.onDetach();
        listener = null;
    }

    @Override
    public void onDestroyView() {
        if (getDialog() != null && getRetainInstance()) {
            getDialog().setDismissMessage(null);
        }
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        Log.d(TAG,"onDestroy");
        super.onDestroy();
    }

    public void executeLoginAsyncTask(){
        new Login().execute();
    }

    private class Login extends AsyncTask<Void, Void, Integer> {
        private static final String TAG = "Login AsyncTask";

        @Override
        protected Integer doInBackground(Void... params) {
            Log.d(TAG, "doInBackground");
            Crawler crawler = Crawler.getInstance();
            return crawler.login();
        }

        @Override
        protected void onPostExecute(Integer result) {
            Log.d(TAG, "onPostExecute " + this);
            int count = 0;
            if (result == ReturnValues.LOGIN_SUCCESSFUL.getLevelCode()) {
                while (listener == null && count < 10) {
                    try {
                        Thread.sleep(500);
                        count++;
                    } catch (InterruptedException e) {
                        Log.e(TAG, "Thread.sleep Failed");
                    }
                }
                if (listener != null) {
                    listener.loginSuccessful();
                }
            } else {
                while (listener == null && count < 10) {
                    try {
                        Thread.sleep(500);
                        count++;
                    } catch (InterruptedException e) {
                        Log.e(TAG, "Thread.sleep Failed");
                    }
                }
                if (listener != null) {
                    listener.loginFailed(result);
                }
            }
            fragment.dismiss();
        }
    }
}





