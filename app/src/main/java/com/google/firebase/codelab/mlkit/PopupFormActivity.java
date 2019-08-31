package com.google.firebase.codelab.mlkit;

import android.app.Activity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class PopupFormActivity extends Activity implements AdapterView.OnItemSelectedListener {

    private TextView mLastNumbers;

    private EditText mDistCode, mRTOcode, mName, mAddress, mContact;

    private Button mClearForm, mSaveForm;

    private String statecode="";

    private DatabaseHelper myDb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_popup_form);

        mLastNumbers = (TextView) findViewById(R.id.tv_lastnumbers);

        mDistCode = (EditText) findViewById(R.id.distcode_et);
        mRTOcode = (EditText) findViewById(R.id.rtocode_et);
        mName = (EditText) findViewById(R.id.name_et);
        mAddress = (EditText) findViewById(R.id.address_et);
        mContact = (EditText) findViewById(R.id.contact_et);

        mClearForm = (Button) findViewById(R.id.btn_clearform);
        mSaveForm = (Button) findViewById(R.id.btn_saveform);
        //mSaveForm.setClickable(false);

        myDb = new DatabaseHelper(this);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        int width = displayMetrics.widthPixels;
        int height = displayMetrics.heightPixels;

        getWindow().setLayout((int) (width*0.8), (int) (height*0.8));

        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.gravity = Gravity.CENTER;
        params.x = 0;
        params.y = -20;

        getWindow().setAttributes(params);

        Spinner statecodes_spinner = (Spinner)findViewById(R.id.spinner_statecodes);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.array_statecodes, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        statecodes_spinner.setAdapter(adapter);

        statecodes_spinner.setOnItemSelectedListener(this);

        statecodes_spinner.setSelection(0);

        final String lastnumbers_string = getIntent().getStringExtra("lastnumbers");
        final int int_lastnumbers = Integer.parseInt(lastnumbers_string);

        mLastNumbers.setText(lastnumbers_string);

        mSaveForm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String distcode = mDistCode.getText().toString();
                String RTOcode = mRTOcode.getText().toString().toUpperCase();
                String name = mName.getText().toString();
                String address = mAddress.getText().toString();
                String contact = mContact.getText().toString();

                String s = statecode + distcode + RTOcode + lastnumbers_string;

                String date = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss", Locale.getDefault()).format(new Date());

                if(isEmpty(mDistCode) || isEmpty(mRTOcode) || isEmpty(mName) || isEmpty(mAddress) || isEmpty(mContact) || contact.length()<10){
                    Toast.makeText(getApplicationContext(), "Please fill all details", Toast.LENGTH_SHORT).show();
                }else {

                    myDb.insertData(s, int_lastnumbers, 1, name, address, contact, date);

                    Toast.makeText(getApplicationContext(), "Data saved successfully", Toast.LENGTH_SHORT).show();

                    finish();
                }
            }
        });

        mClearForm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDistCode.setText("");
                mRTOcode.setText("");
                mName.setText("");
                mAddress.setText("");
                mContact.setText("");

            }
        });

    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

        String stateCodeArr[] = getResources().getStringArray(R.array.array_statecodes);

        statecode = String.valueOf(stateCodeArr[i]);

    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    private boolean isEmpty(EditText etText) {
        return etText.getText().toString().trim().length() == 0;
    }
}
