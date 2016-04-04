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
package mchs.neverforget.preferences;

import android.content.Context;
import android.content.res.TypedArray;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TimePicker;

import mchs.neverforget.R;

@SuppressWarnings("deprecation") // Suppresses getCurrentHour and getCurrentMinute
public class TimePickerPreference extends DialogPreference {
    private static final String DEFAULT_VALUE = "12:00";
    private String currentValue;
    TimePicker timePicker = null;

    public TimePickerPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setDialogLayoutResource(R.layout.pref_time_picker);
        setPositiveButtonText(android.R.string.ok);
        setNegativeButtonText(android.R.string.cancel);
        setDialogIcon(null);
    }

    @Override
    protected View onCreateDialogView() {
        timePicker = new TimePicker(getContext());
        timePicker.setIs24HourView(true);
        return timePicker;
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        if(positiveResult){
            currentValue = timePicker.getCurrentHour() + ":" + timePicker.getCurrentMinute();
            persistString(currentValue);
        }
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);

        String[] hourAndMinute = currentValue.split(":");
        int currentHour = Integer.parseInt(hourAndMinute[0]);
        int currentMinute = Integer.parseInt(hourAndMinute[1]);
        timePicker.setCurrentHour(currentHour);
        timePicker.setCurrentMinute(currentMinute);
    }

    @Override
    protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
        if (restorePersistedValue) {
            // restore existing state
            currentValue = this.getPersistedString(DEFAULT_VALUE);
        } else {
            // set default state from the XML attribute
            currentValue = (String) defaultValue;
            persistString(currentValue);
        }
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getString(index);
    }
}
