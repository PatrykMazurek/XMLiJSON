package com.example.patryyyk21.myapplication;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.JsonReader;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    public ListView lvPerson;
    public TextView tvMessage;
    public List<String> lPerson;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button bJSON = (Button)findViewById(R.id.bJSON);
        Button bXML = (Button)findViewById(R.id.bXML);
        lvPerson = (ListView)findViewById(R.id.listPerson);

        lPerson = new ArrayList<String>();

        final AsyntTastTest[] test = {new AsyntTastTest()};

        final String url1 = "http://pmazurek.sk5.eu/android/xml/zadanie.xml";
        final String url2 = "http://pmazurek.sk5.eu/android/json/zadanie.json";

        bJSON.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (test[0].getStatus().equals(AsyncTask.Status.FINISHED)) {
                    test[0] = new AsyntTastTest();
                }
                if(lPerson.size() > 0) lPerson.clear();
                test[0].execute(url2, "json");
                Toast.makeText(getApplicationContext(), "Pobierannie danych z json-a", Toast.LENGTH_LONG).show();
            }
        });

        bXML.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (test[0].getStatus().equals(AsyncTask.Status.FINISHED)) {
                    test[0] = new AsyntTastTest();
                }
                if(lPerson.size() > 0) lPerson.clear();
                test[0].execute(url1, "xml");
                Toast.makeText(getApplicationContext(), "Pobierannie danych z xml-a", Toast.LENGTH_LONG).show();
            }
        });
    }

    private class AsyntTastTest extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... params) {
            URL url = null;
            try{
                url = new URL(params[0]);
                HttpURLConnection httpConn = (HttpURLConnection)url.openConnection();
                httpConn.setConnectTimeout(15000);
                httpConn.setReadTimeout(15000);
                httpConn.setRequestMethod("POST");
                if(httpConn.getResponseCode() == 200){
                    if(params[1].equals("json")){
                        JsonReader jsonReader = new JsonReader(new InputStreamReader(
                                httpConn.getInputStream()));
                        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(
                                httpConn.getInputStream()));
                        StringBuilder sb = new StringBuilder();
                        String line;
                        while ((line = bufferedReader.readLine()) != null){
                            sb.append(line);
                        }
                        FromJsonToList(sb.toString());
                    }
                    else{
                        InputStream inputStream = httpConn.getInputStream();
                        FromXmlToList(inputStream);
                    }
                }
                else{

                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        @Override
        protected void onPostExecute(String integer) {
            super.onPostExecute(integer);
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(),
                    android.R.layout.simple_list_item_1, lPerson);
            lvPerson.setAdapter(adapter);
        }

        @Override
        protected void onCancelled(String integer) {
            super.onCancelled(integer);
            //Toast.makeText(getApplicationContext(), "Zakończony wątek", Toast.LENGTH_SHORT).show();
        }

        private void FromJsonToList(String jsonStr) {

            try{
                JSONObject jsonObj = new JSONObject(jsonStr);
                JSONArray jsonArray = jsonObj.getJSONArray("person");
                for(int i = 0; i<jsonArray.length(); i++){
                    JSONObject p = jsonArray.optJSONObject(i);
                    lPerson.add(p.getString("id") + " " + p.getString("name")
                            + " " + p.getString("last_name") + " " + p.getString("city"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }

        private void FromXmlToList(InputStream xmlStr) {
            XmlPullParserFactory factory = null;
            XmlPullParser parser = null;
            List<Person> tempPerson = new ArrayList<Person>();
            Person person = new Person();
            String text = "";
            try{
                factory = XmlPullParserFactory.newInstance();
                factory.setNamespaceAware(true);
                parser = factory.newPullParser();

                parser.setInput(xmlStr, null);

                int eventType = parser.getEventType();
                while (eventType != XmlPullParser.END_DOCUMENT) {
                    String tagname = parser.getName();
                    switch (eventType) {
                        case XmlPullParser.START_TAG:
                            if (tagname.equalsIgnoreCase("person")) {
                                person = new Person();
                            }
                            break;

                        case XmlPullParser.TEXT:
                            text = parser.getText();
                            break;

                        case XmlPullParser.END_TAG:
                            if (tagname.equalsIgnoreCase("person")) {
                                tempPerson.add(person);
                            } else if (tagname.equalsIgnoreCase("id")) {
                                person.id = Integer.parseInt(text);
                            } else if (tagname.equalsIgnoreCase("name")) {
                                person.name = text;
                            } else if (tagname.equalsIgnoreCase("lastname")) {
                                person.lastName = text;
                            } else if (tagname.equalsIgnoreCase("city")) {
                                person.city = text;
                            }
                            break;

                        default:
                            break;
                    }
                    eventType = parser.next();
                }

                for(int i = 0; i < tempPerson.size(); i++){
                    Person p = tempPerson.get(i);
                    lPerson.add(p.id + " " + p.name + " " + p.lastName + " " + p.city);
                }
            } catch (XmlPullParserException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
