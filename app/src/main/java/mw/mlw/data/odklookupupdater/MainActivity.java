package mw.mlw.data.odklookupupdater;

import android.Manifest;
import android.content.Intent;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    Button btnUpdater;
    DatabaseHelper dbHelper;
    private TextView project;
    String[] init_seting;
     String project_code;
     String server_ip;
    String auth_code;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().setIcon(R.mipmap.logo);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            ActivityCompat.requestPermissions(this,
                    new String[]{
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.INTERNET,
                            Manifest.permission.ACCESS_NETWORK_STATE

                    }
                    , 1);
        }
        btnUpdater = (Button)findViewById(R.id.updater);
        project = (TextView) findViewById(R.id.project_value);

        btnUpdater.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                if(project_code!=null)
                {
                    updateFileList(server_ip,project_code.toLowerCase(),auth_code);
                }

            }
        });
        MainActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run()
            {
                dbHelper=new DatabaseHelper(getBaseContext());
                init_seting=dbHelper.getProjectSetting();
                project_code=init_seting[0];
                server_ip=init_seting[1];
                auth_code=init_seting[2];
                if(project_code!=null)
                    project.setText(project_code);
                else
                {
                    //start setting activity
                    startActivity(new Intent(MainActivity.this,ProjectManager.class));
                }
            }
        });
    }
    public void updateFileList(final String server_ip,final String project_code,final String authentication_code)
    {
        CheckNetworkStatus nc=new CheckNetworkStatus();
        if(nc.isNetworkAvailable(MainActivity.this))
        {
            new Thread(new Runnable() {
                public void run() {
                    MainActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            FileUpdater fu = new FileUpdater(MainActivity.this);
                            String request_type = "update";
                            fu.execute(request_type, server_ip, project_code, authentication_code);
                        }
                    });
                }
            }).start();
        }
        else
        {
            nc.getNetworkNotAvailableMessage(MainActivity.this);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater=getMenuInflater();
        inflater.inflate(R.menu.menu_main,menu);
        return true;
        //return super.onCreateOptionsMenu(menu);
    }
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle item selection
        switch (item.getItemId())
        {
            case R.id.action_about:
                showAbout();
                return true;
            case R.id.action_settings_update:
                updateSettings();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    public void showAbout()
    {
        startActivity(new Intent(MainActivity.this,About.class));

    }
    public void updateSettings()
    {
        startActivity(new Intent(MainActivity.this,ProjectManager.class));

    }
}
