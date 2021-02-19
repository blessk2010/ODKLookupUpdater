package mw.mlw.data.odklookupupdater;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputFilter;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class ProjectManager extends AppCompatActivity {

    Button btnProjectAddEdit;
    DatabaseHelper dbHelper;
    String[] init_seting;
    String project_code;
    String server_ip;
    String auth_code;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project_manager);
        getSupportActionBar().setIcon(R.mipmap.logo);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        final EditText project_et=(EditText)findViewById(R.id.project_code);
        project_et.setFilters(new InputFilter[]{new InputFilter.AllCaps()});
        final EditText server_et=(EditText)findViewById(R.id.server_ip);
        final EditText auth_code_et=(EditText)findViewById(R.id.auth_code);

        btnProjectAddEdit = (Button)findViewById(R.id.button);


        btnProjectAddEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                //add edit
                final String new_proj_code=project_et.getText().toString().trim();
                final String new_server_ip=server_et.getText().toString().trim();
                final String new_auth_code=auth_code_et.getText().toString().trim();

                if(new_proj_code.length()>0 && new_server_ip.length()>0 && new_auth_code.length()>0)
                {
                    String results=validateData(new_server_ip,new_proj_code,new_auth_code);
                    if(results!=null && results.equals("1"))
                    {
                        saveSettings(new_proj_code,new_server_ip,new_auth_code,project_code);
                    }
                    else if(results!=null)
                    {
                        AlertDialog.Builder alertDialog= new AlertDialog.Builder(ProjectManager.this);
                        /*alertDialog.setPositiveButton("Save Anyway!",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which)
                                    {
                                        //force saving
                                        saveSettings(new_proj_code,new_server_ip,new_auth_code,project_code);
                                    }
                                });*/
                        alertDialog.setNegativeButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which)
                            {
                                //dismiss
                            } });
                        alertDialog.setCancelable(true);
                        alertDialog.setTitle(getBaseContext().getString(R.string.app_name)+" Error");
                        //alertDialog.setMessage(results+"\n\nClick Save Anyway to force save settings or Cancel to edit.");
                        alertDialog.setMessage(results);
                        alertDialog.create().show();

                    }
                    else
                    {
                        //general error
                    }
                }
                else
                {
                    Toast.makeText(ProjectManager.this, "Fill in the form", Toast.LENGTH_SHORT).show();

                }

            }
        });
        //initialising withdb data

        ProjectManager.this.runOnUiThread(new Runnable() {
            @Override
            public void run()
            {
                dbHelper=new DatabaseHelper(getBaseContext());
                init_seting=dbHelper.getProjectSetting();
                project_code=init_seting[0];
                server_ip=init_seting[1];
                auth_code=init_seting[2];

                if(server_ip!=null)
                    server_et.setText(server_ip);
                if(project_code!=null)
                    project_et.setText(project_code);
                if(auth_code!=null)
                    auth_code_et.setText(auth_code);
            }
        });
    }
    private String validateData(final String server_ip,final String project_code,final String authentication_code)
    {
        CheckNetworkStatus nc=new CheckNetworkStatus();
        final Context context=ProjectManager.this;
        String result=null;
        if(nc.isNetworkAvailable(context))
        {
            try {
                FileUpdater fu = new FileUpdater(context);
                String request_type = "validate";
                result=fu.execute(request_type, server_ip, project_code, authentication_code).get();
            }
            catch (Exception e)
            {
                result="General processing error";
            }
        }
        else
        {
            nc.getNetworkNotAvailableMessage(ProjectManager.this);
        }
        return result;
    }
    private void restartMainActivity()
    {
        //return
        Context c=getBaseContext();
        Intent i = c.getPackageManager()
                .getLaunchIntentForPackage(c.getPackageName() );
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        c.startActivity(i);
    }
    private void saveSettings(String new_proj_code,String new_server_ip,String new_auth_code,String project_code)
    {
        if(project_code!=null)
        {
            //update
            if(dbHelper.editData(new_proj_code,new_server_ip,new_auth_code,project_code))
            {
                //success
                Toast.makeText(ProjectManager.this, "Settings saved successfully", Toast.LENGTH_SHORT).show();
                restartMainActivity();
            }
            else
            {
                //failure
                Toast.makeText(ProjectManager.this, "Unable to save settings", Toast.LENGTH_SHORT).show();
            }
        }
        else
        {
            //add data
            if(dbHelper.insertData(new_proj_code,new_server_ip,new_auth_code))
            {
                //success
                Toast.makeText(ProjectManager.this, "Settings updated successfully", Toast.LENGTH_SHORT).show();
                restartMainActivity();
            }
            else
            {
                //failure
                Toast.makeText(ProjectManager.this, "Unable to update settings", Toast.LENGTH_SHORT).show();
            }

        }
    }
}
