package org.techtown.withotilla2.DataClass;

import android.os.Parcel;
import android.os.Parcelable;

public class ItemData  implements Parcelable {

    // getset으로 안함
    public String idx = ""; //primary key
    public String reg_user="";
    public String profile = "";
    public String title = "";
    public String image = "";
    public String summary = "";
    public int fire = 0;
    public int reply = 0;
    public String more = "";
    public String reg_date = "";
    public String fire_member="";
    public String email="";


    public ItemData(){}



    public ItemData(String idx, String reg_user,String profile, String title, String image, String summary, int fire, int reply, String more, String reg_date, String fire_member, String email) {
        this.idx = idx;
        this.reg_user = reg_user;
        this.profile = profile;
        this.title = title;
        this.image = image;
        this.summary = summary;
        this.fire = fire;
        this.reply = reply;
        this.more = more;
        this.reg_date = reg_date;
        this.fire_member = fire_member;
        this.email = email;
    }


    @Override
    public int describeContents() { // board
        return 0;
    }

    // CREATOR 상수 정의, Parcel 객체로부터 데이터를 읽어 들여 객체를 생성
    public static final Parcelable.Creator<ItemData> CREATOR = new Parcelable.Creator<ItemData>(){

        @Override
        //Person 생성자를 호출해 Parcel 객체에서 읽기
        public ItemData createFromParcel(Parcel parcel) {
            return new ItemData(parcel);
        }

        @Override
        public ItemData[] newArray(int i) {
            return new ItemData[i];
        }
    };

    //Parcel 객체에서 읽기
    public ItemData(Parcel parcel) {
        idx = parcel.readString();
        reg_user = parcel.readString();
        profile = parcel.readString();
        title = parcel.readString();
        image = parcel.readString();
        summary = parcel.readString();
        fire = parcel.readInt();
        reply = parcel.readInt();
        more = parcel.readString();
        reg_date = parcel.readString();
        fire_member = parcel.readString();
        email = parcel.readString();
    }

    @Override
    // 객체가 가지고 있는 데이터를 Parcel객체로 만들어주는 역할
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(idx);
        parcel.writeString(reg_user);
        parcel.writeString(profile);
        parcel.writeString(title);
        parcel.writeString(image);
        parcel.writeString(summary);
        parcel.writeInt(fire);
        parcel.writeInt(reply);
        parcel.writeString(more);
        parcel.writeString(reg_date);
        parcel.writeString(fire_member);
        parcel.writeString(email);
    }
}
