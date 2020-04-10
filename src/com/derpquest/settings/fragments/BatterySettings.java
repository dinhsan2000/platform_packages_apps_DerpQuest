/*
 * Copyright (C) 2020 DerpFest ROM
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.derpquest.settings.fragments;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.os.UserHandle;
import android.provider.SearchIndexableResource;
import android.provider.Settings;
import android.text.format.DateFormat;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceGroup;
import androidx.preference.PreferenceScreen;
import androidx.preference.PreferenceCategory;
import androidx.preference.Preference.OnPreferenceChangeListener;
import androidx.preference.PreferenceFragment;
import androidx.preference.SwitchPreference;

import com.android.internal.logging.nano.MetricsProto;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.R;
import com.android.settings.Utils;
import com.android.settingslib.search.SearchIndexable;

import com.derpquest.settings.preferences.CustomSeekBarPreference;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import net.margaritov.preference.colorpicker.ColorPickerPreference;

@SearchIndexable
public class BatterySettings extends SettingsPreferenceFragment implements
        OnPreferenceChangeListener, Indexable {

    private static final String STATUS_BAR_SHOW_BATTERY_PERCENT = "status_bar_show_battery_percent";
    private static final String STATUS_BAR_BATTERY_TEXT_CHARGING = "status_bar_battery_text_charging";
    private static final String BATTERY_PERCENTAGE_HIDDEN = "0";
    private static final String STATUS_BAR_BATTERY_STYLE = "status_bar_battery_style";
    private static final String STATUS_BAR_BATTERY_TEXT_CHARGING_COLOR = "status_bar_battery_text_charging_color";

    private static final int BATTERY_STYLE_Q = 0;
    private static final int BATTERY_STYLE_DOTTED_CIRCLE = 1;
    private static final int BATTERY_STYLE_CIRCLE = 2;
    private static final int BATTERY_STYLE_TEXT = 3;
    private static final int BATTERY_STYLE_HIDDEN = 4;

    private ListPreference mBatteryPercent;
    private ListPreference mBatteryStyle;
    private SwitchPreference mBatteryCharging;
    private ColorPickerPreference mChargingColor;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        addPreferencesFromResource(R.xml.derpquest_settings_battery);
        PreferenceScreen prefSet = getPreferenceScreen();
        final ContentResolver resolver = getActivity().getContentResolver();

        mBatteryPercent = (ListPreference) findPreference(STATUS_BAR_SHOW_BATTERY_PERCENT);
        mBatteryCharging = (SwitchPreference) findPreference(STATUS_BAR_BATTERY_TEXT_CHARGING);
        mBatteryStyle = (ListPreference) findPreference(STATUS_BAR_BATTERY_STYLE);
        mChargingColor = (ColorPickerPreference) findPreference(STATUS_BAR_BATTERY_TEXT_CHARGING_COLOR);

        int batterystyle = Settings.System.getInt(resolver,
                Settings.System.STATUS_BAR_BATTERY_STYLE, BATTERY_STYLE_Q);
        mBatteryStyle.setValueIndex(batterystyle);
        mBatteryStyle.setOnPreferenceChangeListener(this);

        int batterypercent = Settings.System.getInt(resolver,
                Settings.System.STATUS_BAR_SHOW_BATTERY_PERCENT, 0);
        mBatteryPercent.setValueIndex(batterypercent);
        mBatteryPercent.setOnPreferenceChangeListener(this);

        boolean batterycharging = Settings.System.getInt(resolver,
                Settings.System.STATUS_BAR_BATTERY_TEXT_CHARGING, 1) == 1;
        mBatteryCharging.setChecked(batterycharging);
        mBatteryCharging.setOnPreferenceChangeListener(this);

        int color = Settings.System.getInt(resolver,
                Settings.System.STATUS_BAR_BATTERY_TEXT_CHARGING_COLOR, 0xFFFFFF);
        mChargingColor.setNewPreviewColor(color);
        mChargingColor.setOnPreferenceChangeListener(this);

        updateBatteryOptions();
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object objValue) {
        if (preference == mBatteryStyle) {
            int value = Integer.parseInt((String) objValue);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.STATUS_BAR_BATTERY_STYLE, value);
            updateBatteryOptions();
            return true;
        } else if (preference == mBatteryPercent) {
            int value = Integer.parseInt((String) objValue);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.STATUS_BAR_SHOW_BATTERY_PERCENT, value);
            updateBatteryOptions();
            return true;
        } else if (preference == mBatteryCharging) {
            boolean value = (boolean) objValue;
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.STATUS_BAR_BATTERY_TEXT_CHARGING, value ? 1 : 0);
            updateBatteryOptions();
            return true;
        } else if (preference == mChargingColor) {
            int color = (Integer) objValue;
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.STATUS_BAR_BATTERY_TEXT_CHARGING_COLOR, color);
            return true;
        }
        return false;
    }

    private void updateBatteryOptions() {
        int batterystyle = Settings.System.getInt(getActivity().getContentResolver(),
                Settings.System.STATUS_BAR_BATTERY_STYLE, BATTERY_STYLE_Q);
        int batteryPercent = Settings.System.getInt(getActivity().getContentResolver(),
                Settings.System.STATUS_BAR_SHOW_BATTERY_PERCENT, 0);
        boolean batteryChargingText = Settings.System.getInt(getActivity().getContentResolver(),
                Settings.System.STATUS_BAR_BATTERY_TEXT_CHARGING, 1) == 1;

        boolean isHidden = batterystyle == BATTERY_STYLE_HIDDEN;

        mBatteryStyle.setEnabled(!isHidden);

        if (isHidden) {
            mBatteryPercent.setValue(BATTERY_PERCENTAGE_HIDDEN);
            mBatteryPercent.setSummary(mBatteryPercent.getEntry());
            mBatteryStyle.setSummary(R.string.enable_first);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.STATUS_BAR_SHOW_BATTERY_PERCENT, 0);
            mBatteryStyle.setEnabled(false);
        } else if (batterystyle == BATTERY_STYLE_TEXT) {
            mBatteryCharging.setEnabled(false);
            mBatteryStyle.setSummary(mBatteryStyle.getEntry());
        } else {
            mBatteryCharging.setEnabled(true);
            mBatteryStyle.setSummary(mBatteryStyle.getEntry());
        }

        mBatteryPercent.setEnabled(batterystyle != BATTERY_STYLE_TEXT &&
                batterystyle != BATTERY_STYLE_HIDDEN);

        mChargingColor.setEnabled(batterystyle == BATTERY_STYLE_TEXT ||
                batteryPercent == 2 || batteryChargingText);
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.OWLSNEST;
    }

    public static final SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider() {
                @Override
                public List<SearchIndexableResource> getXmlResourcesToIndex(Context context,
                                                                            boolean enabled) {
                    ArrayList<SearchIndexableResource> result =
                            new ArrayList<SearchIndexableResource>();

                    SearchIndexableResource sir = new SearchIndexableResource(context);
                    sir.xmlResId = R.xml.derpquest_settings_battery;
                    result.add(sir);
                    return result;
                }

                @Override
                public List<String> getNonIndexableKeys(Context context) {
                    List<String> keys = super.getNonIndexableKeys(context);
                    return keys;
                }
            };
}
