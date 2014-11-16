package com.dreamteam.trafficmonitor.customdialog;

import java.text.DecimalFormat;
import java.util.List;

import com.dreamteam.trafficmonitor.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class CustomAdapter extends BaseAdapter {
    
    private List<TrafficInfo> mlistAppInfo = null;  
      
    LayoutInflater infater = null;
    
    public static final long B = 1;
    public static final long KB = B * 1024;
    public static final long MB = KB * 1024;
    public static final long GB = MB * 1024;
    
    public CustomAdapter(Context context,  List<TrafficInfo> apps) {
        infater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);  
        mlistAppInfo = apps ;  
    }
    @Override  
    public int getCount() {  
        // TODO Auto-generated method stub  
        System.out.println("size" + mlistAppInfo.size());  
        return mlistAppInfo.size();  
    }  
    @Override  
    public Object getItem(int position) {  
        // TODO Auto-generated method stub  
        return mlistAppInfo.get(position);  
    }  
    @Override  
    public long getItemId(int position) {  
        // TODO Auto-generated method stub  
        return 0;  
    }  
    @Override  
    public View getView(int position, View convertview, ViewGroup arg2) {
        View view = null;  
        ViewHolder holder = null;  
        if (convertview == null || convertview.getTag() == null) {  
            view = infater.inflate(R.layout.trafficitem, null);  
            holder = new ViewHolder(view);
            view.setTag(holder);
        }   
        else{  
            view = convertview ;  
            holder = (ViewHolder) convertview.getTag() ;  
        }  
        TrafficInfo appInfo = (TrafficInfo) getItem(position);  
        holder.appIcon.setImageDrawable(appInfo.getAppicon());  
        holder.Label.setText(appInfo.getAppName());
		holder.gprs.setText(size2string(appInfo.getGPRS()));  
        holder.wlan.setText(size2string(appInfo.getWIFI()));
        return view;  
    }  
  
    class ViewHolder {  
        ImageView appIcon;  
        TextView Label;  
        TextView gprs;
        TextView wlan;
  
        public ViewHolder(View view) {  
            this.appIcon = (ImageView) view.findViewById(R.id.ItemImage);  
            this.Label = (TextView) view.findViewById(R.id.Lable);
            this.gprs = (TextView) view.findViewById(R.id.gprs);
            this.wlan = (TextView) view.findViewById(R.id.wlan);
            
        }  
    }
    
    private String size2string(long size){  
    	  DecimalFormat df = new DecimalFormat("0.00");  
    	  String mysize = "";  
    	  if( size > 1024*1024){  
    	    mysize = df.format( size / 1024f / 1024f ) +"M";  
    	  }else if( size > 1024 ){  
    	    mysize = df.format( size / 1024f ) +"K";  
    	  }else{  
    	    mysize = size + "B";  
    	  }  
    	  return mysize;  
    	}  
}  
