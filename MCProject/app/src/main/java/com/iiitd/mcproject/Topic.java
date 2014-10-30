package com.iiitd.mcproject;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.util.Xml;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutionException;


/**
 * Created by shiv on 17/10/14.
 */
public class Topic extends Activity{

    ProgressBar bar ;
    TextView summary;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.topic);
        Button search = (Button)findViewById(R.id.topic_search_button);
        bar = (ProgressBar)findViewById(R.id.topic_search_progressBar);
        bar.setVisibility(View.INVISIBLE);

        summary = (TextView) findViewById(R.id.topic_search_text);
        summary.setVisibility(View.INVISIBLE);

        final EditText search_text = (EditText) findViewById(R.id.topic_search_editText);

        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ConnectivityManager cmgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo networkInfo = cmgr.getActiveNetworkInfo();
                if(networkInfo!=null && networkInfo.isConnected()){
                    Log.d("Topic class", "Starting the asynctask");
                    bar.setVisibility(View.VISIBLE);
                    summary.setVisibility(View.INVISIBLE);
                    try {
                        String text = new KnowledgeGraphTask().execute(search_text.getText().toString()).get();
                        //String text = new KnowledgeGraphTask().execute("Batman").get();
                        Log.d("Topic class" , "the text/summary is : " + text);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    }
                }else{
                    Toast.makeText(getBaseContext() , "No internet connection" , Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private class KnowledgeGraphTask extends AsyncTask<String , Void , String>{

        @Override
        protected String doInBackground(String... param) {
            try {
                return WikiData(param[0]);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (XmlPullParserException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Log.d("Topic class" , "inside onPostExecute : "  + s);
            bar.setVisibility(View.INVISIBLE);
            summary.setText(s);
            summary.setVisibility(View.VISIBLE);
        }

        private String WikiData(String topic) throws IOException, XmlPullParserException {
            InputStream input = null ;
            try {
                String format = "format=xml" ;                         //The format in which I want to get a response
                String action = "action=query" ;                        //The action that I perform(query/search in this case)
                String prop = "prop=revisions";                         //Specifies that we are looking for the latest revision of the article
                String rvprop = "rvprop=content";                       //This parameter tells the web service API that we want the content of the latest revision of the page
                String titles = "titles="+topic ;                       //The topic I want to search

                String root_url = "http://en.wikipedia.org/w/api.php?" ;

                String search_url = root_url+format+"&"+action+"&"+titles+"&"+prop+"&"+rvprop ;
                Log.d("Topic class", "The search url is : " + search_url);
                URL url = new URL(search_url);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.connect();
                input = conn.getInputStream();
                String summary = parse(input , topic);
                Log.d("Topic class", "WikiData working fine");
                return summary;
            }finally{
                input.close();
            }
        }

        private String parse(InputStream input , String topic) throws XmlPullParserException, IOException {
            Log.d("Topic class" , "entering parse");
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(input, null);
            parser.nextTag();
            Log.d("Topic class" , "exiting parse");
            return readFeed(parser , topic);
        }

        private String readFeed(XmlPullParser parser , String topic) throws IOException, XmlPullParserException {

            StringBuilder text = new StringBuilder();
            Log.d("Topic class" , "entering readFeed");
            int eventType = parser.getEventType();
            while(eventType != XmlPullParser.END_DOCUMENT){
                if(eventType==XmlPullParser.START_DOCUMENT){
                    Log.d("Topic class" , "Start document");
                }else if(eventType==XmlPullParser.START_TAG){
                    Log.d("Topic class" , "Start tag");
                }else if(eventType==XmlPullParser.END_TAG){
                    Log.d("Topic class" , "End tag");
                }else if(eventType==XmlPullParser.TEXT){
                    String message = parser.getText();
                    Log.d("Topic class" , "Something else");

                    String[] array = topic.split("_");

                    Log.d("Topic class" ,"Searching for string : " + array[0]);
                    int ind = message.indexOf("'''"+array[0]);

                    int iter = ind;
                    Log.d("Topic class" , "The index is : " + Integer.toString(iter));
                    int number_of_sentences = 1;                //number of sentences to display
                    int sentences_added = 0;
                    while(iter > 0  && iter < message.length()-1 && sentences_added < number_of_sentences){
                        if(Character.toString(message.charAt(iter)).equals("'") || Character.toString(message.charAt(iter)).equals("]") || Character.toString(message.charAt(iter)).equals("[") || Character.toString(message.charAt(iter)).equals("{")|| Character.toString(message.charAt(iter)).equals("}")){   //skip ' ] [ while displaying the summary
                            //Log.d("Topic class" , "not added to text : " + Character.toString(message.charAt(iter)));
                            iter++;
                            continue;
                        }else if(Character.toString(message.charAt(iter)).equals("<")){ //skip HTML tags
                            while(sentences_added < number_of_sentences){
                                iter++;
                                if(Character.toString(message.charAt(iter)).equals("/")){
                                    iter++;
                                    if(Character.toString(message.charAt(iter)).equals(">")){
                                        iter++;
                                        break;
                                    }
                                }else{
                                    continue;
                                }
                            }
                        }else {
                            text.append(message.charAt(iter));
                            iter++;
                            if (Character.toString(message.charAt(iter)).equals("\n")) {     // each . indicates one sentence
                                sentences_added++;
                                Log.d("Topic class" , "sentence added");
                            }
                        }
                    }
                    Log.d("Topic class" , text.toString());
                }
                eventType = parser.next();
            }
            Log.d("Topic class" , "End document");
            Log.d("Topic class" , "exiting readFeed");
            String summary = text.toString();           //the summary of the topic parsed from wiki
            return summary;
        }
    }
}
