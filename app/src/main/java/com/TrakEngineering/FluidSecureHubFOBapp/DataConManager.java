package com.TrakEngineering.FluidSecureHubFOBapp;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;

import java.lang.reflect.Method;

/**
 * Created by VASP-LAP on 05-05-2016.
 */
public final class DataConManager
{
    private TelephonyManager m_telManager = null;
    private ConnectivityManager m_conManager = null;

    // ------------------------------------------------------
    // ------------------------------------------------------
    public DataConManager(Context context)
    {
        try
        {
            // Get phone and connectivity services
            m_telManager = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
            m_conManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        }
        catch (Exception e)
        {
            m_telManager = null;
            m_conManager = null;
        }
    }

    // ------------------------------------------------------
    // ------------------------------------------------------
    boolean switchState(boolean enable)
    {
        boolean bRes = false;

        // Data Connection mode (only if correctly initialized)
        if (m_telManager != null)
        {
            try
            {
                // Will be used to invoke hidden methods with reflection
                Class cTelMan = null;
                Method getITelephony = null;
                Object oTelephony = null;
                Class cTelephony = null;
                Method action = null;

                // Get the current object implementing ITelephony interface
                cTelMan = m_telManager.getClass();
                getITelephony = cTelMan.getDeclaredMethod("getITelephony");
                getITelephony.setAccessible(true);
                oTelephony = getITelephony.invoke(m_telManager);

                // Call the enableDataConnectivity/disableDataConnectivity method
                // of Telephony object
                cTelephony = oTelephony.getClass();
                if (enable)
                {
                    action = cTelephony.getMethod("enableDataConnectivity");
                }
                else
                {
                    action = cTelephony.getMethod("disableDataConnectivity");
                }
                action.setAccessible(true);
                bRes = (Boolean)action.invoke(oTelephony);
            }
            catch (Exception e)
            {
                bRes = false;
            }
        }

        return bRes;
    }

    // ------------------------------------------------------
    // ------------------------------------------------------
    public boolean isEnabled()
    {
        boolean bRes = false;

        // Data Connection mode (only if correctly initialized)
        if (m_conManager != null)
        {
            try
            {
                // Get Connectivity Service state
                NetworkInfo netInfo = m_conManager.getNetworkInfo(0);

                // Data is enabled if state is CONNECTED
                bRes = (netInfo.getState() == NetworkInfo.State.CONNECTED);
            }
            catch (Exception e)
            {
                bRes = false;
            }
        }

        return bRes;
    }
}