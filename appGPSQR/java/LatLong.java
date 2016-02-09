package com.npi.appgpsqr;

import android.os.Parcel;
import android.os.Parcelable;

/*  This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    See <http://www.gnu.org/licenses/> for a copy of the GNU General
    Public License.

    Autores: Jacinto Carrasco Castillo, Anabel Gómez Ríos.
    Fecha de la última modificación: 10/02/2016.
 */
public class LatLong implements Parcelable {
    Float lat;
    Float lng;

    public static final Parcelable.Creator<LatLong> CREATOR
            = new Parcelable.Creator<LatLong>() {
        public LatLong createFromParcel(Parcel in) {
            return new LatLong(in);
        }

        public LatLong[] newArray(int size) {
            return new LatLong[size];
        }
    };

    public LatLong(Parcel source) {
        lat = source.readFloat();
        lng = source.readFloat();
    }

    public LatLong() {

    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeFloat(lat);
        dest.writeFloat(lng);
    }
};


