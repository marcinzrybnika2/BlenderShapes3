package com.android.blendershape3.util;



import android.content.Context;
import android.content.res.Resources;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class TextResourceReader {



    public static String readTextFileFromResource(Context context, int resourceID){
        StringBuilder body=new StringBuilder();
        try{
            InputStream iS=context.getResources().openRawResource(resourceID);
            InputStreamReader iSR=new InputStreamReader(iS);
            BufferedReader bR=new BufferedReader(iSR);

            String nextLine;
            while((nextLine=bR.readLine())!=null){
                body.append(nextLine);
                body.append("\n");
            }
        }catch(IOException e){
            throw new RuntimeException("Could not open Resource: "+resourceID,e);
        }catch (Resources.NotFoundException nfe){
            throw new RuntimeException("Resources not found: "+resourceID,nfe);
        }
        return body.toString();
    }



}
