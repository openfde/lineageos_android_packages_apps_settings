/*
 * Copyright (C) 2008 The Android Open Source Project
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

package com.android.settings.network_fde;

import android.annotation.NonNull;
import android.os.Parcel;
import android.os.Parcelable;

public class NetConfiguration implements Parcelable {
    private static final String TAG = "NetConfiguration";
	
    public String interfaceName = "eth0";
	
    public int ipType;//dhcp: 0; static: 1
    
    public  String ipAddress;
	
	public int networkPrefixLength;

    public String gateway;

    public String dns1;

    public String dns2;

    public NetConfiguration() {
        interfaceName = null;
        ipType = -1;
        ipAddress = null;
        networkPrefixLength = -1;
        gateway = null;
        dns1 = null;
        dns2 = null;
    }
	/** Implement the Parcelable interface {@hide} */
	public int describeContents() {
		return 0;
	}

    @Override
    public String toString() {
        StringBuilder sbuf = new StringBuilder();
        sbuf.append("interfaceName: ").append(this.interfaceName).append(" ipType: ").append(this.ipType).
                append(" ipAddress: ").append(this.ipAddress).
                append(" networkPrefixLength: ").append(this.networkPrefixLength).append(" gateway: ").append(this.gateway)
                .append(" dns1: ").append(this.dns1)
                .append(" dns2: ").append(this.dns2)
                .append('\n');
        return sbuf.toString();
    }

    /** Copy constructor */
    public NetConfiguration(@NonNull NetConfiguration source) {
        if (source != null) {
            interfaceName = source.interfaceName;
            ipType = source.ipType;
            ipAddress = source.ipAddress;
            networkPrefixLength = source.networkPrefixLength;
            gateway = source.gateway;
            dns1 = source.dns1;
            dns2 = source.dns2;
        }
    }

    /** Implement the Parcelable interface {@hide} */
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(interfaceName);
        dest.writeInt(ipType);
        dest.writeString(ipAddress);
        dest.writeInt(networkPrefixLength);
        dest.writeString(gateway);
        dest.writeString(dns1);
        dest.writeString(dns2);
    }

    public static final @android.annotation.NonNull Creator<NetConfiguration> CREATOR =
        new Creator<NetConfiguration>() {
            public NetConfiguration createFromParcel(Parcel in) {
                NetConfiguration config = new NetConfiguration();
                config.interfaceName = in.readString();
                config.ipType = in.readInt();
                config.ipAddress = in.readString();
                config.networkPrefixLength = in.readInt();
                config.gateway = in.readString();
                config.dns1 = in.readString();
                config.dns2 = in.readString();
                return config;
            }

            public NetConfiguration[] newArray(int size) {
                return new NetConfiguration[size];
            }
        };
}
