    package com.example.plantasapi.models

    import android.net.Uri
    import android.os.Parcel
    import android.os.Parcelable

    data class Plant(
        var name: String,
        var waterPeriod: Int = 0,
        var imageUri: Uri = Uri.EMPTY,
        var apiSuggestedName: String? = null,  // Cambiado a nullable
        var probability: Float
    ) : Parcelable {
        constructor(parcel: Parcel) : this(
            parcel.readString() ?: "",  // name
            parcel.readInt(),           // waterPeriod
            parcel.readParcelable(Uri::class.java.classLoader) ?: Uri.EMPTY, // imageUri
            parcel.readString(),        // apiSuggestedName
            parcel.readFloat()          // probability
        )

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            parcel.writeString(name)
            parcel.writeInt(waterPeriod)
            parcel.writeParcelable(imageUri, flags)
            parcel.writeString(apiSuggestedName)  // Escribir el nombre sugerido por la API
            parcel.writeFloat(probability)
        }

        override fun describeContents(): Int = 0

        companion object CREATOR : Parcelable.Creator<Plant> {
            override fun createFromParcel(parcel: Parcel): Plant {
                return Plant(parcel)
            }

            override fun newArray(size: Int): Array<Plant?> {
                return arrayOfNulls(size)
            }
        }
    }
