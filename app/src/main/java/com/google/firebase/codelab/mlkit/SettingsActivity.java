package com.google.firebase.codelab.mlkit;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetManager;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.ajts.androidmads.library.ExcelToSQLite;
import com.ajts.androidmads.library.SQLiteToExcel;
import com.google.firebase.codelab.mlkit.util.Utils;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SettingsActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private final int CHOOSE_FILE_REQUEST_CODE = 0;

    ExcelToSQLite.ImportListener dbQueries;
    //DBQueries dbQueries;

    private Button mdelete;
    private Button mSet;

    private  Button mImport;
    private Button mExport;
    private  Button mBack;


    private String accuracy="";

    private int selected_index=0;

    DatabaseHelper myDb;
    String directory_path2 = Environment.getExternalStorageDirectory().getPath() + "/ALPR/update.xlsx";


    String directory_path = Environment.getExternalStorageDirectory().getPath() + "/ALPR/";




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        int acc = 100;

        //Spinner accuracy_spinner = (Spinner)findViewById(R.id.spinner_accuracy);

        myDb = new DatabaseHelper(getApplicationContext());
  //      assert getSupportActionBar() != null;
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //mSet = (Button) findViewById(R.id.btn_set);

        mExport = (Button) findViewById(R.id.btn_export);
        mdelete=(Button)findViewById(R.id.btn_delete);
        mBack=(Button)findViewById(R.id.btn_back);
        mImport=(Button)findViewById(R.id.btn_import);
//
//        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.accuracy_array, android.R.layout.simple_spinner_item);
//        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
//        accuracy_spinner.setAdapter(adapter);
//
//        accuracy_spinner.setOnItemSelectedListener(this);
//
//        accuracy_spinner.setSelection(selected_index);

//        mSet.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent intent = new Intent();
//                intent.putExtra("accuracy", accuracy);
//                setResult(RESULT_OK, intent);
//                finish();
//            }
//        });
        mImport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
//                File file = new File(directory_path2);
//                if (!file.exists()) {
//                    Utils.showSnackBar(view, "No file");
//                    return;
//                }
//
//
//                SQLiteDatabase db = myDb.getWritableDatabase();
//                ExcelToSQLite excelToSQLite = new ExcelToSQLite(getApplicationContext(), myDb.DATABASE_NAME,true);
//
//                excelToSQLite.importFromAsset(directory_path2, new ExcelToSQLite.ImportListener() {
//                    @Override
//                    public void onStart() {
//
//                    }
//
//                    @Override
//                    public void onCompleted(String dbName) {
//                        Utils.showSnackBar(view, "Excel imported into " + myDb.DATABASE_NAME);
//                    }
//
//                    @Override
//                    public void onError(Exception e) {
//                        Utils.showSnackBar(view, "Error : " + e.getMessage());
//                        System.out.println("abc" + e.getStackTrace()+" tejas "+e.getCause());
//                    }
//                });

                Intent fileintent = new Intent(Intent.ACTION_GET_CONTENT);
                fileintent.setType("gagt/sdf");
                try {
                    startActivityForResult(fileintent, CHOOSE_FILE_REQUEST_CODE);
                } catch (ActivityNotFoundException e) {
                    Toast.makeText(getApplicationContext(), "No activity can handle picking a file. Showing alternatives.", Toast.LENGTH_SHORT).show();
                }

            }
        });
        mExport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                // Export SQLite DB as EXCEL FILE


                SQLiteToExcel sqliteToExcel;
                sqliteToExcel = new SQLiteToExcel(getApplicationContext(), myDb.DATABASE_NAME, directory_path);
                String timedate = new SimpleDateFormat("dd-MM-yyyy hh.mm.ss", Locale.getDefault()).format(new Date());
                sqliteToExcel.exportAllTables(timedate+".xlsx", new SQLiteToExcel.ExportListener() {
                    @Override
                    public void onStart() {

                    }

                    @Override
                    public void onCompleted(String filePath) {
                        Utils.showSnackBar(view, "Successfully Exported");
                    }

                    @Override
                    public void onError(Exception e) {
                        Utils.showSnackBar(view, e.getMessage());
                    }
                });
            }
        });
        mdelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Toast.makeText(getApplicationContext(), "Are you sure you want to Delete all time stamps? This action cannot be reversed Please proceed with caution", Toast.LENGTH_SHORT).show();
                AlertDialog.Builder a_builder = new AlertDialog.Builder(SettingsActivity.this);
                a_builder.setMessage("Are you sure you want to Delete all time stamps? This action cannot be reversed Please proceed with caution")
                        .setCancelable(false)
                        .setPositiveButton("Yes",new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                myDb.emptyColumn("dates");
                                Toast.makeText(getApplicationContext(),"All dates are cleared successfully", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .setNegativeButton("No",new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        }) ;
                AlertDialog alert = a_builder.create();
                alert.setTitle("Alert !!!");
                alert.show();
            }
        });

        mBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (data == null)
            return;
        switch (requestCode) {
            case CHOOSE_FILE_REQUEST_CODE:
                String FilePath = data.getData().getPath();
                try {
                    if (resultCode == RESULT_OK) {
                        AssetManager am = this.getAssets();
                        InputStream inStream;
                        Workbook wb = null;
                        try {
                            inStream = new FileInputStream(FilePath);
                            wb = new HSSFWorkbook(inStream);
                            inStream.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                            Toast.makeText(getApplicationContext(), "First" + e.getMessage().toString(), Toast.LENGTH_SHORT).show();
                        }

                        DatabaseHelper dbAdapter = new DatabaseHelper(this);
                        Sheet sheet1 = wb.getSheetAt(0);

                        if (sheet1 == null) {
                            return;
                        }

                        //dbAdapter.open();
                        //dbAdapter.delete();
                        //dbAdapter.close();
                        //dbAdapter.open();
                        Excel2SQLiteHelper.insertExcelToSqlite(dbAdapter, sheet1);
                        //dbAdapter.close();
                        Toast.makeText(getApplicationContext(), "File imported successfully", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception ex) {
                    Toast.makeText(getApplicationContext(), "Second" + ex.getMessage().toString(), Toast.LENGTH_SHORT).show();

                }
        }

    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

//        int[] arr_accuracy = {100, 75, 50, 25};
//
//        accuracy = String.valueOf(arr_accuracy[i]);
//
//        selected_index = i;

    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }
}
