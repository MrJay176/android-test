package ng.riby.androidtest.models;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "locations_two")
public class LocationStore  implements Parcelable {
    private @PrimaryKey @NonNull
    String id;
    public double latitude;
    public double longitude;

    public LocationStore(){
    }

    public void setId(@NonNull String id) {
        this.id = id;
    }



    protected LocationStore(Parcel in) {
        id = in.readString();
        latitude = in.readDouble();
        longitude = in.readDouble();
    }

    public static final Creator<LocationStore> CREATOR = new Creator<LocationStore>() {
        @Override
        public LocationStore createFromParcel(Parcel in) {
            return new LocationStore(in);
        }

        @Override
        public LocationStore[] newArray(int size) {
            return new LocationStore[size];
        }
    };

    public String getId() {
        return id;
    }

    public String getNum() {
        return id;
    }

    public void setNum(String id) {
        this.id = id;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(id);
        parcel.writeDouble(latitude);
        parcel.writeDouble(longitude);
    }
}
