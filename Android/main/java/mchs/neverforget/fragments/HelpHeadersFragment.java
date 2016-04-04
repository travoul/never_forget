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
package mchs.neverforget.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import mchs.neverforget.R;

public class HelpHeadersFragment extends Fragment {
    private static final String TAG = "HelpHeadersFragment";
    private OnFragmentInteractionListener listener;
    ListView headersListView;

    public HelpHeadersFragment() {
    }

    public static HelpHeadersFragment newInstance() {
        return new HelpHeadersFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_header, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        headersListView = (ListView) view.findViewById(R.id.headers_list_view);

        if (headersListView != null) {
            headersListView.setAdapter(new ArrayAdapter<>(
                    getActivity(),
                    R.layout.help_row,
                    R.id.help_textview_row,
                    getResources().getStringArray(R.array.help_headers)
            ));
            headersListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    listener.onFragmentInteraction(position);
                }
            });
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            Log.i(TAG, "Activity implemented onFragmentInteractionListener");
            listener = (OnFragmentInteractionListener) context;
        } else {
            Log.e(TAG, "Activity did not implement onFragmentInteractionListener");
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof OnFragmentInteractionListener) {
            Log.i(TAG, "Activity implemented onFragmentInteractionListener");
            listener = (OnFragmentInteractionListener) activity;
        } else {
            Log.e(TAG, "Activity did not implement onFragmentInteractionListener");
            throw new RuntimeException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(int position);
    }
}
