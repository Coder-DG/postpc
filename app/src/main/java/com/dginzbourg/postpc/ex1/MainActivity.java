package com.dginzbourg.postpc.ex1;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;


public class MainActivity extends AppCompatActivity {

    static final String textViewStringKey = "1";
    static final String editTextStringKey = "2";
    static String textViewString = "";
    static String editTextString = "";
    EditText editText = null;
    TextView textView = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editText = findViewById(R.id.edit_text);
        editText.setText(editTextString);
        textView = findViewById(R.id.main_text_view);
        textView.setText(textViewString);
        Button button = findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                textViewString = editText.getText().toString();
                textView.setText(textViewString);
                editText.setText("");
            }
        });
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState != null) {
            editTextString = savedInstanceState.getString(editTextStringKey);
            textViewString = savedInstanceState.getString(textViewStringKey);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString(textViewStringKey, textViewString);
        outState.putString(editTextStringKey, editTextString);
    }
}
