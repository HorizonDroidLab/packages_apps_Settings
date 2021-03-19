/*
 * Copyright (C) 2021 The Android Open Source Project
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

package com.android.settings.panel;

import static com.google.common.truth.Truth.assertThat;

import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.content.Context;
import android.net.Uri;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.android.settings.network.AirplaneModePreferenceController;
import com.android.settings.network.InternetUpdater;
import com.android.settings.network.ProviderModelSliceHelper;
import com.android.settings.slices.CustomSliceRegistry;
import com.android.settings.testutils.ResourcesUtils;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.util.ArrayList;
import java.util.List;

@RunWith(AndroidJUnit4.class)
public class InternetConnectivityPanelTest {

    public static final String TITLE_INTERNET = ResourcesUtils.getResourcesString(
            ApplicationProvider.getApplicationContext(), "provider_internet_settings");
    public static final String TITLE_APM = ResourcesUtils.getResourcesString(
            ApplicationProvider.getApplicationContext(), "airplane_mode");
    public static final String SUBTITLE_WIFI_IS_TURNED_ON = ResourcesUtils.getResourcesString(
            ApplicationProvider.getApplicationContext(), "wifi_is_turned_on_subtitle");
    public static final String BUTTON_SETTINGS = ResourcesUtils.getResourcesString(
            ApplicationProvider.getApplicationContext(), "settings_button");
    public static final String SUBTITLE_NON_CARRIER_NETWORK_UNAVAILABLE =
            ResourcesUtils.getResourcesString(ApplicationProvider.getApplicationContext(),
                    "non_carrier_network_unavailable");
    public static final String SUBTITLE_ALL_NETWORK_UNAVAILABLE =
            ResourcesUtils.getResourcesString(ApplicationProvider.getApplicationContext(),
                    "all_network_unavailable");

    @Rule
    public final MockitoRule mMocks = MockitoJUnit.rule();
    @Mock
    PanelContentCallback mPanelContentCallback;
    @Mock
    InternetUpdater mInternetUpdater;
    @Mock
    private WifiManager mWifiManager;
    @Mock
    private ProviderModelSliceHelper mProviderModelSliceHelper;

    private Context mContext;
    private InternetConnectivityPanel mPanel;

    @Before
    public void setUp() {
        mContext = spy(ApplicationProvider.getApplicationContext());
        when(mContext.getApplicationContext()).thenReturn(mContext);
        when(mContext.getSystemService(WifiManager.class)).thenReturn(mWifiManager);

        mPanel = InternetConnectivityPanel.create(mContext);
        mPanel.registerCallback(mPanelContentCallback);
        mPanel.mIsProviderModelEnabled = true;
        mPanel.mInternetUpdater = mInternetUpdater;
        mPanel.mProviderModelSliceHelper = mProviderModelSliceHelper;
    }

    @Test
    public void getTitle_apmOff_shouldBeInternet() {
        doReturn(false).when(mInternetUpdater).isAirplaneModeOn();

        assertThat(mPanel.getTitle()).isEqualTo(TITLE_INTERNET);
    }

    @Test
    public void getTitle_apmOn_shouldBeApm() {
        doReturn(true).when(mInternetUpdater).isAirplaneModeOn();

        assertThat(mPanel.getTitle()).isEqualTo(TITLE_APM);
    }

    @Test
    public void getSubTitle_apmOnWifiOff_shouldBeNull() {
        doReturn(true).when(mInternetUpdater).isAirplaneModeOn();
        doReturn(false).when(mInternetUpdater).isWifiEnabled();

        assertThat(mPanel.getSubTitle()).isNull();
    }

    @Test
    public void getSubTitle_apmOnWifiOn_shouldWifiIsTurnedOn() {
        doReturn(true).when(mInternetUpdater).isAirplaneModeOn();
        doReturn(true).when(mInternetUpdater).isWifiEnabled();

        mPanel.updatePanelTitle();

        assertThat(mPanel.getSubTitle()).isEqualTo(SUBTITLE_WIFI_IS_TURNED_ON);
    }

    @Test
    public void getSubTitle_apmOffWifiOnNoWifiListHasCarrierData_NonCarrierNetworkUnavailable() {
        List wifiList = new ArrayList<ScanResult>();
        mockCondition(false, true, true, true, wifiList);

        mPanel.updatePanelTitle();

        assertThat(mPanel.getSubTitle()).isEqualTo(SUBTITLE_NON_CARRIER_NETWORK_UNAVAILABLE);
    }

    @Test
    public void getSubTitle_apmOffWifiOnNoWifiListNoCarrierData_AllNetworkUnavailable() {
        List wifiList = new ArrayList<ScanResult>();
        mockCondition(false, true, false, true, wifiList);

        mPanel.updatePanelTitle();

        assertThat(mPanel.getSubTitle()).isEqualTo(SUBTITLE_ALL_NETWORK_UNAVAILABLE);
    }

    @Test
    public void getSubTitle_apmOffWifiOnTwoWifiItemsNoCarrierData_shouldBeNull() {
        List wifiList = new ArrayList<ScanResult>();
        wifiList.add(new ScanResult());
        wifiList.add(new ScanResult());
        mockCondition(false, true, false, true, wifiList);

        mPanel.updatePanelTitle();

        assertThat(mPanel.getSubTitle()).isNull();
    }

    @Test
    public void getCustomizedButtonTitle_apmOff_shouldBeSettings() {
        doReturn(false).when(mInternetUpdater).isAirplaneModeOn();

        assertThat(mPanel.getCustomizedButtonTitle()).isEqualTo(BUTTON_SETTINGS);
    }

    @Test
    public void getCustomizedButtonTitle_apmOnWifiOff_shouldBeNull() {
        doReturn(true).when(mInternetUpdater).isAirplaneModeOn();
        doReturn(false).when(mInternetUpdater).isWifiEnabled();

        assertThat(mPanel.getCustomizedButtonTitle()).isNull();
    }

    @Test
    public void getCustomizedButtonTitle_apmOnWifiOn_shouldBeSettings() {
        doReturn(true).when(mInternetUpdater).isAirplaneModeOn();
        doReturn(true).when(mInternetUpdater).isWifiEnabled();

        assertThat(mPanel.getCustomizedButtonTitle()).isEqualTo(BUTTON_SETTINGS);
    }

    @Test
    public void getSlices_providerModelDisabled_containsNecessarySlices() {
        mPanel.mIsProviderModelEnabled = false;
        final List<Uri> uris = mPanel.getSlices();

        assertThat(uris).containsExactly(
                AirplaneModePreferenceController.SLICE_URI,
                CustomSliceRegistry.MOBILE_DATA_SLICE_URI,
                CustomSliceRegistry.WIFI_SLICE_URI);
    }

    @Test
    public void getSlices_providerModelEnabled_containsNecessarySlices() {
        final List<Uri> uris = mPanel.getSlices();

        assertThat(uris).containsExactly(
                CustomSliceRegistry.PROVIDER_MODEL_SLICE_URI,
                CustomSliceRegistry.TURN_ON_WIFI_SLICE_URI);
    }

    @Test
    public void getSeeMoreIntent_notNull() {
        assertThat(mPanel.getSeeMoreIntent()).isNotNull();
    }

    @Test
    public void onAirplaneModeOn_apmOff_onTitleChanged() {
        doReturn(false).when(mInternetUpdater).isAirplaneModeOn();
        clearInvocations(mPanelContentCallback);

        mPanel.onAirplaneModeChanged(false);

        verify(mPanelContentCallback).onTitleChanged();
    }

    @Test
    public void onAirplaneModeOn_apmOnWifiOff_onTitleChanged() {
        doReturn(true).when(mInternetUpdater).isAirplaneModeOn();
        doReturn(false).when(mInternetUpdater).isWifiEnabled();
        clearInvocations(mPanelContentCallback);

        mPanel.onAirplaneModeChanged(true);

        verify(mPanelContentCallback).onTitleChanged();
    }

    @Test
    public void onAirplaneModeOn_apmOnWifiOn_onHeaderChanged() {
        doReturn(true).when(mInternetUpdater).isAirplaneModeOn();
        doReturn(true).when(mInternetUpdater).isWifiEnabled();
        clearInvocations(mPanelContentCallback);

        mPanel.onAirplaneModeChanged(true);

        verify(mPanelContentCallback).onHeaderChanged();
    }

    @Test
    public void onAirplaneModeOn_onCustomizedButtonStateChanged() {
        doReturn(true).when(mInternetUpdater).isAirplaneModeOn();
        clearInvocations(mPanelContentCallback);

        mPanel.onAirplaneModeChanged(true);

        verify(mPanelContentCallback).onCustomizedButtonStateChanged();
    }

    @Test
    public void onWifiEnabledChanged_apmOff_onTitleChanged() {
        doReturn(false).when(mInternetUpdater).isAirplaneModeOn();
        clearInvocations(mPanelContentCallback);

        mPanel.onWifiEnabledChanged(false);

        verify(mPanelContentCallback).onTitleChanged();
    }

    @Test
    public void onWifiEnabledChanged_apmOnWifiOff_onTitleChanged() {
        doReturn(true).when(mInternetUpdater).isAirplaneModeOn();
        doReturn(false).when(mInternetUpdater).isWifiEnabled();
        clearInvocations(mPanelContentCallback);

        mPanel.onWifiEnabledChanged(true);

        verify(mPanelContentCallback).onTitleChanged();
    }

    @Test
    public void onWifiEnabledChanged_apmOnWifiOn_onHeaderChanged() {
        doReturn(true).when(mInternetUpdater).isAirplaneModeOn();
        doReturn(true).when(mInternetUpdater).isWifiEnabled();
        clearInvocations(mPanelContentCallback);

        mPanel.onWifiEnabledChanged(true);

        verify(mPanelContentCallback).onHeaderChanged();
    }

    @Test
    public void onWifiEnabledChanged_onCustomizedButtonStateChanged() {
        doReturn(true).when(mInternetUpdater).isWifiEnabled();
        clearInvocations(mPanelContentCallback);

        mPanel.onWifiEnabledChanged(true);

        verify(mPanelContentCallback).onCustomizedButtonStateChanged();
    }

    private void mockCondition(boolean airplaneMode, boolean hasCarrier,
            boolean isDataSimActive, boolean isWifiEnabled, List<ScanResult> wifiItems) {
        doReturn(airplaneMode).when(mInternetUpdater).isAirplaneModeOn();
        when(mProviderModelSliceHelper.hasCarrier()).thenReturn(hasCarrier);
        when(mProviderModelSliceHelper.isDataSimActive()).thenReturn(isDataSimActive);
        doReturn(isWifiEnabled).when(mInternetUpdater).isWifiEnabled();
        doReturn(wifiItems).when(mWifiManager).getScanResults();
    }
}
