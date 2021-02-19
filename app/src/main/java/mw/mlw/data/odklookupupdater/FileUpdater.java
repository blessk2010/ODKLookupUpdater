package mw.mlw.data.odklookupupdater;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.speech.tts.Voice;
import android.util.Log;
import android.widget.Toast;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;


/**
 * Created by BlessK on 1/21/2018.
 */

public class FileUpdater extends AsyncTask<String,Void,String>
{
    Context context;
    private ProgressDialog progressDialog;
    private String server_name,file_url, request_type,url_updater;
    FileUpdater(Context context)
    {
        this.context=context;
        this.server_name=null;
        this.file_url=null;
    }

    @Override
    protected String doInBackground(String... params)
    {
        try {
            this.request_type=params[0];
            this.server_name=params[1];
            this.url_updater=server_name+"/odk_lookup_updater/odk_lookup_updater.php";
            String project_code=params[2];
            String auth_code=params[3];

            URL url=new URL(this.url_updater);
            HttpURLConnection conn=(HttpURLConnection)url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setDoInput(true);

            OutputStream os=conn.getOutputStream();
            BufferedWriter br=new BufferedWriter(new OutputStreamWriter(os,"UTF-8"));
            String post_data= URLEncoder.encode("request_type","UTF-8")+"="+URLEncoder.encode(request_type,"UTF-8")+"&"+URLEncoder.encode("project_code","UTF-8")+"="+URLEncoder.encode(project_code,"UTF-8")+"&"+URLEncoder.encode("auth_code","UTF-8")+"="+URLEncoder.encode(auth_code,"UTF-8");
            br.write(post_data);
            br.flush();
            br.close();
            os.close();

            //reading result
            InputStream is=conn.getInputStream();
            BufferedReader bufferedReader=new BufferedReader(new InputStreamReader(is,"iso-8859-1"));
            String result="";
            String line="";
            while((line=bufferedReader.readLine())!=null)
            {
                result+=line;
            }
            bufferedReader.close();
            is.close();
            conn.disconnect();

            if(this.request_type.equals("validate") && (result != null && !result.toLowerCase().startsWith("error")))
            {
                return "1";
                /*if (result != null && !result.toLowerCase().startsWith("error"))
                    return "1";
                else
                    return result;*/
            }
            else
            {
                return result;
            }
        }
        catch (Exception ex)
        {
            String msg=ex.getMessage();
            if(msg.toLowerCase().contains(this.url_updater))
            {
                msg=msg.replace(this.url_updater,"");
                msg+=" Updater service not found on the "+this.server_name+"\n" +
                        "Contact the administrator for help";
            }
            return "Error : "+msg;
        }
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        progressDialog=new ProgressDialog(context);
        progressDialog.setMessage("Getting file paths\nPlease wait...");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();
        //super.onPreExecute();
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        if(this.request_type.equals("update"))
        {
            //process & show results called without get that means caller is updating
            AlertDialog.Builder alertDialog= new AlertDialog.Builder(context);
            alertDialog.setPositiveButton("Ok",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            //dismiss the dialog
                        }
                    });
            alertDialog.setCancelable(true);

            if (result != null && !result.toLowerCase().startsWith("error"))
            {
                String files = "";
                int file_number = 0;
                try {
                    XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                    factory.setNamespaceAware(true);
                    XmlPullParser xpp = factory.newPullParser();

                    xpp.setInput(new StringReader(result));
                    int eventType = xpp.getEventType();
                    while (eventType != XmlPullParser.END_DOCUMENT) {
                        if (eventType == XmlPullParser.TEXT) {
                            progressDialog.setMessage("Copying file " + file_number);
                            this.file_url = server_name + xpp.getText();
                            new FileDownloader(this.context, file_url);
                            files += this.file_url.substring(this.file_url.lastIndexOf('/') + 1, this.file_url.length()) + "\n";
                            file_number++;
                        }
                        eventType = xpp.next();
                    }
                } catch (Exception e) {
                    alertDialog.setTitle(context.getString(R.string.app_name)+" Error");
                    alertDialog.setMessage("Unable to update files\nMake sure you have configured this project for odk media updater");
                    alertDialog.create().show();
                } finally {
                    progressDialog.dismiss();
                    alertDialog.setTitle(context.getString(R.string.app_name)+" Results");
                    alertDialog.setMessage(file_number + " files updated successfully\n"+files);
                    alertDialog.create().show();
                }

            }
            else
            {
                //null values
                progressDialog.dismiss();
                alertDialog.setTitle(context.getString(R.string.app_name)+" Error");
                alertDialog.setMessage("Unable to update files\n\n"+result);
                alertDialog.create().show();
            }
        }
        else
        {
            //do not show the message handled in the caller using get
            //dismis progress dialog
            progressDialog.dismiss();
        }
    }

    @Override
    protected void onProgressUpdate(Void... values)
    {
        super.onProgressUpdate(values);
    }
}
