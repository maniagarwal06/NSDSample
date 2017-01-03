package com.mani.nsdsample;

import android.content.Context;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.net.InetAddress;

public class NSDActivity extends AppCompatActivity {
    NsdManager mNsdManager;
    String mServiceName, TAG = "mani";
    String serviceType = "_custom_chating._tcp.";
    TextView serviceDetailsView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nsd);
        mNsdManager = (NsdManager) getSystemService(Context.NSD_SERVICE);
        serviceDetailsView = (TextView) findViewById(R.id.serviceDetails);
    }

    public void registerService(View v) {
        NsdServiceInfo serviceInfo = new NsdServiceInfo();
        serviceInfo.setServiceName("Chating");
        serviceInfo.setServiceType(serviceType);
        serviceInfo.setPort(666);
        mNsdManager.registerService(
                serviceInfo, NsdManager.PROTOCOL_DNS_SD, mRegistrationListener);
    }

    public void unregisterService(View v) {
        mNsdManager.unregisterService(mRegistrationListener);
        mNsdManager.stopServiceDiscovery(mDiscoveryListener);
    }

    public void discoverService(View v) {
        mNsdManager.discoverServices(
                serviceType, NsdManager.PROTOCOL_DNS_SD, mDiscoveryListener);
    }

    NsdManager.RegistrationListener mRegistrationListener = new NsdManager.RegistrationListener() {

        @Override
        public void onServiceRegistered(NsdServiceInfo NsdServiceInfo) {
            // Save the service name.  Android may have changed it in order to
            // resolve a conflict, so update the name you initially requested
            // with the name Android actually used.

            mServiceName = NsdServiceInfo.getServiceName();
            Toast.makeText(NSDActivity.this, "Serivce get registered : " + mServiceName, Toast.LENGTH_LONG).show();
            Log.d(TAG, "onServiceRegistered : " + NsdServiceInfo);
        }

        @Override
        public void onRegistrationFailed(NsdServiceInfo serviceInfo, int errorCode) {
            // Registration failed!  Put debugging code here to determine why.
            Log.d(TAG, "onRegistrationFailed : " + serviceInfo);
            Log.d(TAG, "errorCode : " + errorCode);
        }

        @Override
        public void onServiceUnregistered(NsdServiceInfo serviceInfo) {
            // Service has been unregistered.  This only happens when you call
            // NsdManager.unregisterService() and pass in this listener.
            Log.d(TAG, "onServiceUnregistered : " + serviceInfo);
        }

        @Override
        public void onUnregistrationFailed(NsdServiceInfo serviceInfo, int errorCode) {
            // Unregistration failed.  Put debugging code here to determine why.
            Log.d(TAG, "onUnregistrationFailed : " + serviceInfo);
            Log.d(TAG, "errorCode : " + errorCode);
        }
    };
    NsdManager.DiscoveryListener mDiscoveryListener = new NsdManager.DiscoveryListener() {

        //  Called as soon as service discovery begins.
        @Override
        public void onDiscoveryStarted(String regType) {
            Log.d(TAG, "Service discovery started");
        }

        @Override
        public void onServiceFound(NsdServiceInfo service) {
            // A service was found!  Do something with it.
            Log.d(TAG, "Service discovery success" + service);
            if (!service.getServiceType().equals(serviceType)) {
                // Service type is the string containing the protocol and
                // transport layer for this service.
                Log.d(TAG, "Unknown Service Type: " + service.getServiceType());
            }
//            else if (service.getServiceName().equals(mServiceName)) {
//                // The name of the service tells the user what they'd be
//                // connecting to. It could be "Bob's Chat App".
//                Log.d(TAG, "Same machine: " + mServiceName);
//                mNsdManager.resolveService(service, mResolveListener);
//            }
            else if (service.getServiceName().contains("Chating")) {
                mNsdManager.resolveService(service, mResolveListener);
            }
        }

        @Override
        public void onServiceLost(NsdServiceInfo service) {
            // When the network service is no longer available.
            // Internal bookkeeping code goes here.
            Log.e(TAG, "service lost" + service);
        }

        @Override
        public void onDiscoveryStopped(String serviceType) {
            Log.i(TAG, "Discovery stopped: " + serviceType);
        }

        @Override
        public void onStartDiscoveryFailed(String serviceType, int errorCode) {
            Log.e(TAG, "Discovery failed: Error code:" + errorCode);
            mNsdManager.stopServiceDiscovery(this);
        }

        @Override
        public void onStopDiscoveryFailed(String serviceType, int errorCode) {
            Log.e(TAG, "Discovery failed: Error code:" + errorCode);
            mNsdManager.stopServiceDiscovery(this);
        }
    };

    private NsdServiceInfo mService;

    NsdManager.ResolveListener mResolveListener = new NsdManager.ResolveListener() {

        @Override
        public void onResolveFailed(NsdServiceInfo serviceInfo, int errorCode) {
            // Called when the resolve fails.  Use the error code to debug.
            Log.e(TAG, "Resolve failed" + errorCode);
        }

        @Override
        public void onServiceResolved(NsdServiceInfo serviceInfo) {
            Log.e(TAG, "Resolve Succeeded. " + serviceInfo);

            if (serviceInfo.getServiceName().equals(mServiceName)) {
                Log.d(TAG, "Same IP.");
                return;
            }
            mService = serviceInfo;
            final int port = mService.getPort();
            final InetAddress host = mService.getHost();
            Log.d(TAG, "Port : " + port + " host:  " + host);
            Runnable UI = new Runnable() {
                @Override
                public void run() {
                    serviceDetailsView.setText("service name : " + mService.getServiceName() + "\n Port : " + port + " \n host : " + host);
                }
            };
            runOnUiThread(UI);
        }
    };
}
