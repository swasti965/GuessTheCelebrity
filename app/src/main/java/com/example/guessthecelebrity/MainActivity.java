package com.example.guessthecelebrity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    ArrayList<String> imageSource = new ArrayList<>();
    ArrayList<String> names = new ArrayList<>();
    int locationOfCorrectAnswer = 0;
    int chosenCeleb = 0;

    ImageView imageView;
    Button button0;
    Button button1;
    Button button2;
    Button button3;

    public class DownloadURL extends AsyncTask<String, Void, String>{

        @Override
        protected String doInBackground(String... urls) {
            URL url = null;
            try {
                url = new URL(urls[0]);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                InputStream in = urlConnection.getInputStream();
                InputStreamReader reader = new InputStreamReader(in);
                String result = "";
                int data = reader.read();
                while(data != -1){
                    char ch = (char)data;
                    result += ch;
                    data = reader.read();
                }
                return result;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }

        }
    };

    public class DownloadImage extends AsyncTask<String, Void, Bitmap>{

        @Override
        protected Bitmap doInBackground(String... urls) {
            URL url = null;
            try {
                url = new URL(urls[0]);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.connect();
                InputStream in = urlConnection.getInputStream();
                Bitmap quesImage = BitmapFactory.decodeStream(in);
                return quesImage;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
    };

    public void getInfo(String htmlCode){
        try {
            Pattern p = Pattern.compile("img src=\"(.*?)\"");
            Matcher m = p.matcher(htmlCode);

            while (m.find()) {
                imageSource.add(m.group(1));
            }

            p = Pattern.compile("alt=\"(.*?)\"");
            m = p.matcher(htmlCode);

            while (m.find()) {
                names.add(m.group(1));
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }
    public void setQuestion(){
        try {
            Random random = new Random();
            chosenCeleb = random.nextInt(imageSource.size());
            locationOfCorrectAnswer = random.nextInt(4);
            DownloadImage task = new DownloadImage();
            Bitmap quesImage = null;

            quesImage = task.execute(imageSource.get(chosenCeleb)).get();
            imageView.setImageBitmap(quesImage);
            String[] options = new String[4];
            for (int i = 0; i < 4; i++) {
                if (i == locationOfCorrectAnswer) {
                    options[i] = names.get(chosenCeleb);
                }else {
                    int wrongAnswer = random.nextInt(imageSource.size());
                    while(wrongAnswer == chosenCeleb)
                        wrongAnswer = random.nextInt(imageSource.size());
                    options[i] = names.get(wrongAnswer);
                }
            }
            button0.setText(options[0]);
            button1.setText(options[1]);
            button2.setText(options[2]);
            button3.setText(options[3]);
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void celebChosen(View view){
        String chosen = view.getTag().toString();
        if(Integer.valueOf(chosen) == locationOfCorrectAnswer)
            Toast.makeText(getApplicationContext(), "Correct!", Toast.LENGTH_SHORT).show();
        else
            Toast.makeText(getApplicationContext(), "Wrong! It was " + names.get(chosenCeleb), Toast.LENGTH_SHORT).show();
        setQuestion();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = findViewById(R.id.imageView);
        button0 = findViewById(R.id.button0);
        button1 = findViewById(R.id.button1);
        button2 = findViewById(R.id.button2);
        button3 = findViewById(R.id.button3);

        String html="";
        DownloadURL task = new DownloadURL();
        String htmlCode="";
        try {
            htmlCode = task.execute("http://www.posh24.se/kandisar").get();
            String[] splitCode = htmlCode.split("<div class=\"channelListEntry\">");
            getInfo(htmlCode);
            setQuestion();
        } catch (Exception e) {
            e.printStackTrace();
        }



    }
}
