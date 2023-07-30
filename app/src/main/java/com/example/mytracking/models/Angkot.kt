package com.example.mytracking.models

import android.os.Parcel
import android.os.Parcelable

data class Angkot (
    var namaAngkot: String? = null,
    var jurusan: String? = null

) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(namaAngkot)
        parcel.writeString(jurusan)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Angkot> {
        override fun createFromParcel(parcel: Parcel): Angkot {
            return Angkot(parcel)
        }

        override fun newArray(size: Int): Array<Angkot?> {
            return arrayOfNulls(size)
        }
    }
}
