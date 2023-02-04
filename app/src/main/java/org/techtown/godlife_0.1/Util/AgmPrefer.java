package org.techtown.withotilla2.Util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class AgmPrefer {


    public SharedPreferences mSharedPref;

    public String mem_email = "mem_email";      //디폴트 값 설정정
    public String mem_nickname= "mem_nickname";
    public String mem_image = "mem_image";
    public String mem_password = "mem_password";


    public AgmPrefer(Context ctx)
    {
        mSharedPref = PreferenceManager.getDefaultSharedPreferences(ctx);
    }

    public void setNickname(String name)
    {
        SharedPreferences.Editor shareEditor = mSharedPref.edit();
        shareEditor.putString(mem_nickname, name);
        shareEditor.commit();
    }



    public String getNickname()
    {
        return mSharedPref.getString(mem_nickname,"");
    }


    public void setEmail(String email)
    {
        SharedPreferences.Editor sharedEdior = mSharedPref.edit();
        sharedEdior.putString(mem_email,email);
        sharedEdior.commit();
    }

    public String getEmail()
    {
        return mSharedPref.getString(mem_email, "");

    }

    public void setPassword(String password)
    {
        SharedPreferences.Editor sharedEdior = mSharedPref.edit();
        sharedEdior.putString(mem_password,password);
        sharedEdior.commit();
    }

    public String getPassword()
    {
        return mSharedPref.getString(mem_password, "");

    }





    public void clear()
    {
        this.setNickname("");
        this.setEmail("");
    }


    public String getProfilImage() {
        return mSharedPref.getString(mem_image, "");
    }

    public void setProfilImage(String image_url)
    {
        SharedPreferences.Editor sharedEdior = mSharedPref.edit();
        sharedEdior.putString(mem_image,image_url);
        sharedEdior.commit();
    }
}
