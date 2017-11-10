package com.upt.cti.smartwallet;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    // UI.
    private TextView tStatus;
    private EditText eIncome, eExpenses;
    private String currentMonth;
    private Spinner monthSpinner;

    // Firebase connection.
    private DatabaseReference databaseReference;
    private ValueEventListener databaseListener;

    // Data structures.
    final List<MonthlyExpenses> monthlyExpenses = new ArrayList<>();
    final List<String> monthNames = new ArrayList<>();

    // For saving data to shared prederences.
    private final static String PREFS_SETTINGS = "prefs_settings";
    private SharedPreferences prefsUser, prefsApp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tStatus = (TextView) findViewById(R.id.tStatus);
        // eSearch = (EditText) findViewById(R.id.eSearch); removed since version 5.
        eIncome = (EditText) findViewById(R.id.eIncome);
        eExpenses = (EditText) findViewById(R.id.eExpenses);

        // Connect to firebase database.
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        databaseReference = database.getReference();

        // Named preference file.
        prefsUser = getSharedPreferences(PREFS_SETTINGS, Context.MODE_PRIVATE);
        /* Removed since version 5.
        String month = prefsUser.getString("KEY1",null);
        eSearch.setText(month);
        */
        initSpinner();
    }

    private void initSpinner(){
        // Set spinner.
        final ArrayAdapter<String> sAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, monthNames);
        monthSpinner = (Spinner) findViewById(R.id.spinner2);
        sAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        monthSpinner.setAdapter(sAdapter);

        //populate spinner.
        databaseReference.child("calendar").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot monthSnapshot : dataSnapshot.getChildren()) {
                    try {
                        String month = monthSnapshot.getKey();
                        monthNames.add(month);
                    } catch (Exception e) {
                    }
                }

                // notify the spinner that data may have changed
                sAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void createNewDBListener() {
        // remove previous databaseListener
        if (databaseReference != null && currentMonth != null && databaseListener != null){
            databaseReference.child("calendar").child(currentMonth).removeEventListener(databaseListener);
        }

        databaseListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                MonthlyExpenses monthlyExpense = dataSnapshot.getValue(MonthlyExpenses.class);
                if (monthlyExpense == null) {
                    tStatus.setText("No entry found for " + currentMonth);
                    eIncome.setText(null);
                    eExpenses.setText(null);
                    return;
                }
                // explicit mapping of month name from entry key
                monthlyExpense.month = dataSnapshot.getKey();

                eIncome.setText(String.valueOf(monthlyExpense.getIncome()));
                eExpenses.setText(String.valueOf(monthlyExpense.getExpenses()));
                tStatus.setText("Found entry for " + currentMonth);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        // set new databaseListener
        databaseReference.child("calendar").child(currentMonth).addValueEventListener(databaseListener);
    }

    public void clicked(View view) {
        switch (view.getId()) {
                /* Laboratory 4 version of app.

                case R.id.bSearch:
                if (!eSearch.getText().toString().isEmpty()) {
                    // Save text to lower case (all our months are stored online in lower case).
                    currentMonth = eSearch.getText().toString().toLowerCase();

                    tStatus.setText("Searching ...");
                    createNewDBListener();

                    // Write the last month searched in the sharedPref file.
                    prefsUser.edit().putString("KEY1", currentMonth).apply();
                } else {
                    Toast.makeText(this, "Search field may not be empty", Toast.LENGTH_SHORT).show();
                }
                break;
                *
                */
            case R.id.bUpdate: /* Update the expenses and income for a certain month. */

                /* Check if the new expenses and income were set in the fields. */
                if (eExpenses.getText().toString().isEmpty() || eIncome.getText().toString().isEmpty() ){
                    Toast.makeText(this, "Income and expenses fields may not be empty", Toast.LENGTH_SHORT).show();
                    break;
                }

                /* Check if we have a month selected to update. */
                if (currentMonth == null){
                    Toast.makeText(this, "Month not selected, please search for a month.", Toast.LENGTH_LONG).show();
                    break;
                }

                /* Try to parse the values, check if the selected month exists and update it. */
                float expenses, income;
                try{
                    expenses = Float.parseFloat(eExpenses.getText().toString());
                    income = Float.parseFloat(eIncome.getText().toString());

                    String key = databaseReference.child(currentMonth).push().getKey();
                    if (key == null){
                        tStatus.setText("No entry found for " + currentMonth);
                    }
                    MonthlyExpenses newExpenses = new MonthlyExpenses(currentMonth, income, expenses);
                    //databaseReference.updateChildren();
                }catch(NumberFormatException e){
                    Toast.makeText(this, "Income and expenses fields have to be decimal values", Toast.LENGTH_SHORT).show();
                    break;
                }
                break;
        }
    }
}
