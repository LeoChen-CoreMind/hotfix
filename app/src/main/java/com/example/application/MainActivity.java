package com.example.application;
 
import android.app.Activity;
import android.os.Bundle;
import android.widget.Toast;
import java.io.File;

public class MainActivity extends Activity {
     
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
		
		//Toast.makeText(getApplication(), filePath, Toast.LENGTH_SHORT).show();
     // Toast.makeText(getApplication(),df.ad()+sss.ad(), Toast.LENGTH_SHORT).show();
    }
	
}
