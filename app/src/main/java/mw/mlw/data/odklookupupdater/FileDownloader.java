package mw.mlw.data.odklookupupdater;

/**
 * Created by bkapalamula on 22/01/2018.
 */
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class FileDownloader
{

    //private static final String TAG = "Download Task";
    private Context context;

    private String downloadUrl = "", downloadFileName = "", downloadFolder="";
    private ProgressDialog progressDialog;
    final String TAG="Logging B :";
    public FileDownloader(Context context,String downloadUrl)
    {
        this.context = context;

        this.downloadFileName = downloadUrl.substring(downloadUrl.lastIndexOf( '/' ),downloadUrl.length());//Create file name by picking download file name from URL
        this.downloadUrl=downloadUrl;
        String [] folders=this.downloadUrl.split("/");
        this.downloadFolder=folders[folders.length-2];

        //Start Downloading Task
        new DownloadingTask(context).execute();
    }

    private class DownloadingTask extends AsyncTask<Void, Void, Void>
    {

        File storageDirectory = null;
        File outputFile = null;
        Context context;
        public DownloadingTask(Context context)
        {
            this.context=context;
        }
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog=new ProgressDialog(context);
            progressDialog.setMessage("Updating File "+downloadFileName+"\nPlease wait...");
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.show();
        }

        @Override
        protected void onPostExecute(Void result)
        {
            try {
                if (outputFile != null) {
                    progressDialog.dismiss();
                    /*AlertDialog.Builder dlgAlert  = new AlertDialog.Builder(context);
                    dlgAlert.setMessage("Database updated successfully.\n Click ok to refresh the app");
                    dlgAlert.setTitle(R.string.app_name);
                    dlgAlert.setPositiveButton("Ok",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which)
                                {
                                    //dismiss the dialog
                                    Intent i = context.getPackageManager()
                                            .getLaunchIntentForPackage(context.getPackageName() );
                                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    context.startActivity(i);
                                }
                            });
                    dlgAlert.setCancelable(true);
                    dlgAlert.create().show();*/
                    //>>>>>>>>>>Toast.makeText(context, "Downloaded Successfully", Toast.LENGTH_SHORT).show();
                } else {

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {

                        }
                    }, 3000);

                }
            } catch (Exception e) {
                e.printStackTrace();

                //Change button text if exception occurs

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {

                    }
                }, 3000);

            }


            super.onPostExecute(result);
        }

        @Override
        protected Void doInBackground(Void... arg0)
        {
            try 
            {
                URL url = new URL(downloadUrl);//Create Download URl
                HttpURLConnection c = (HttpURLConnection) url.openConnection();//Open Url Connection
                c.setRequestMethod("GET");//Set Request Method to "GET" since we are grtting data
                c.connect();//connect the URL Connection

                //If Connection response is not OK then show Logs
                if (c.getResponseCode() != HttpURLConnection.HTTP_OK)
                {
                    Toast.makeText(context, "Server returned HTTP " + c.getResponseCode()
                            + " " + c.getResponseMessage(), Toast.LENGTH_SHORT).show();
                }
                
                //Environment.getExternalStorageDirectory().toString()+"/mlw";
                storageDirectory = new File(
                        Environment.getExternalStorageDirectory() + "/odk");
                if (!storageDirectory.exists())
                {
                    storageDirectory.mkdir();
                    Log.e("Blessk", "Directory Created1.");
                }
                //forms folder
                storageDirectory = new File(
                        Environment.getExternalStorageDirectory() + "/odk/forms");
                if (!storageDirectory.exists())
                {
                    storageDirectory.mkdir();
                }

                //lookup folder
                storageDirectory = new File(
                        Environment.getExternalStorageDirectory() + "/odk/forms/"+downloadFolder);
                if (!storageDirectory.exists())
                {
                    storageDirectory.mkdir();
                }

                outputFile = new File(storageDirectory, downloadFileName);//Create Output file in Main File

                //Create New File if not present
                if (!outputFile.exists())
                {
                    outputFile.createNewFile();
                }

                FileOutputStream fos = new FileOutputStream(outputFile);//Get OutputStream for NewFile Location

                InputStream is = c.getInputStream();//Get InputStream for connection

                byte[] buffer = new byte[1024];//Set buffer type
                int len1 = 0;//init length
                while ((len1 = is.read(buffer)) != -1) {
                    fos.write(buffer, 0, len1);//Write new file
                }

                //Close all connection after doing task
                fos.close();
                is.close();

            } catch (Exception e) {

                //Read exception if something went wrong
                outputFile = null;
            }

            return null;
        }
    }
}
