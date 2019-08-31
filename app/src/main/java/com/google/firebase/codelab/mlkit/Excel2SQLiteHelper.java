package com.google.firebase.codelab.mlkit;

import android.content.ContentValues;
import android.util.Log;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

import java.util.Iterator;

public class Excel2SQLiteHelper {



    public static void insertExcelToSqlite(DatabaseHelper dbAdapter, Sheet sheet) {

        for (Iterator<Row> rit = sheet.rowIterator(); rit.hasNext(); ) {
            Row row = rit.next();

            ContentValues contentValues = new ContentValues();
            row.getCell(0, Row.CREATE_NULL_AS_BLANK).setCellType(Cell.CELL_TYPE_STRING);
            row.getCell(1, Row.CREATE_NULL_AS_BLANK).setCellType(Cell.CELL_TYPE_STRING);
            row.getCell(2, Row.CREATE_NULL_AS_BLANK).setCellType(Cell.CELL_TYPE_STRING);
            row.getCell(3, Row.CREATE_NULL_AS_BLANK).setCellType(Cell.CELL_TYPE_STRING);
            row.getCell(4, Row.CREATE_NULL_AS_BLANK).setCellType(Cell.CELL_TYPE_STRING);
            row.getCell(5, Row.CREATE_NULL_AS_BLANK).setCellType(Cell.CELL_TYPE_STRING);
            row.getCell(6, Row.CREATE_NULL_AS_BLANK).setCellType(Cell.CELL_TYPE_STRING);

            contentValues.put(dbAdapter.COL_1, row.getCell(0, Row.CREATE_NULL_AS_BLANK).getStringCellValue());
            contentValues.put(dbAdapter.COL_2, row.getCell(1, Row.CREATE_NULL_AS_BLANK).getStringCellValue());
            contentValues.put(dbAdapter.COL_3, row.getCell(2, Row.CREATE_NULL_AS_BLANK).getStringCellValue());
            contentValues.put(dbAdapter.COL_4, row.getCell(3, Row.CREATE_NULL_AS_BLANK).getStringCellValue());
            contentValues.put(dbAdapter.COL_5, row.getCell(4, Row.CREATE_NULL_AS_BLANK).getStringCellValue());
            contentValues.put(dbAdapter.COL_6, row.getCell(5, Row.CREATE_NULL_AS_BLANK).getStringCellValue());
            contentValues.put(dbAdapter.COL_7, row.getCell(6, Row.CREATE_NULL_AS_BLANK).getStringCellValue());

            try {
                if (dbAdapter.insert(dbAdapter.TABLE_NAME, contentValues) < 0) {
                    return;
                }
            } catch (Exception ex) {
                Log.d("Exception in importing", ex.getMessage().toString());
            }
        }
    }
}
