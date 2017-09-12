package com.example.bartosz.whereismyfriend;

import android.content.Context;
import android.text.TextUtils;
import android.widget.Toast;

/**
 * Created by Bartosz on 12.09.2017.
 */

public class ValidationManager {

    public ValidationManager(){}

    public Boolean IsUserDataCorrect(Context context, String name, String surname, int age, double range){
        if(name.isEmpty()){
            Toast.makeText(context, "Name can not be empty!", Toast.LENGTH_SHORT).show();
            return false;
        }
        if(surname.isEmpty()){
            Toast.makeText(context, "Surname can not be empty!", Toast.LENGTH_SHORT).show();
            return false;
        }
        if(age < 1 && age > 99){
            Toast.makeText(context, "Age must be between 1-99!", Toast.LENGTH_SHORT).show();
            return false;
        }
        if(range < 1.0 || range > 10000.0){
            Toast.makeText(context, "Range must be between 1-10000 meters!", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    public Boolean IsUserRegisterDataCorrect(Context context, String name, String surname, String age){
        if(TextUtils.isEmpty(name))
        {
            Toast.makeText(context, "Please enter name", Toast.LENGTH_SHORT).show();
            return false;
        }
        if(TextUtils.isEmpty(surname))
        {
            Toast.makeText(context, "Please enter surname", Toast.LENGTH_SHORT).show();
            return false;
        }
        if(TextUtils.isEmpty(age))
        {
            Toast.makeText(context, "Please enter age", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }
}
