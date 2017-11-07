package pope.tipcalculator;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.IdRes;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.view.View.OnClickListener;
import android.widget.Toast;

import java.text.NumberFormat;

import static android.widget.CompoundButton.*;

public class MainActivity extends AppCompatActivity implements OnEditorActionListener, OnKeyListener, SeekBar.OnSeekBarChangeListener {


    private EditText billEditText;
    private TextView tipPercentNumLabel;
    private TextView PercentNumberLabel;
    private TextView totalTextView;
    private Button  plusButton;
    private Button applyButton;
    private Button   ResetButton;
    private RadioButton noneRadioBtn;
    private RadioButton tipRadioBtn;
    private RadioButton totalRadioBtn;
    private RadioGroup radioGroup;
    private Spinner splitSpinner;
    private TextView perPersonLbl;
    private TextView perPersonTv;
    private SeekBar seekBar;

    //rounding constants
    private final int ROUND_NONE = 0;
    private final int ROUND_TIP = 1;
    private final int ROUND_TOTAL = 2;



    private String billAmountString = "";
    private float percentAmt = 0.0f;
    private SharedPreferences saved;
    private int rounding = ROUND_NONE;
    private int split = 1;
    private float totalAmt;
    private float tipAmount;
    private float billAmt;
    private float splitAmt = 0;
    private int progress = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Get reference to the widget
        billEditText = (EditText) findViewById(R.id.billEditText);
        PercentNumberLabel = (TextView) findViewById(R.id.PercentNumberLabel);
        totalTextView = (TextView) findViewById(R.id.totalTextView);
        tipPercentNumLabel = (TextView) findViewById(R.id.tipPercentNumLabel);
        ResetButton = (Button) findViewById(R.id.ResetButton);
        applyButton = (Button) findViewById(R.id.applyButton);
        radioGroup  = (RadioGroup) findViewById(R.id.radioGroup);
        noneRadioBtn = (RadioButton) findViewById(R.id.noneRadioBtn);
        tipRadioBtn = (RadioButton) findViewById(R.id.tipRadioBtn);
        totalRadioBtn = (RadioButton) findViewById(R.id.totalRadioBtn);
        splitSpinner = (Spinner) findViewById(R.id.splitSpinner);
        perPersonLbl = (TextView) findViewById(R.id.perPersonLbl);
        perPersonTv = (TextView) findViewById(R.id.perPersonTv);
        seekBar = (SeekBar) findViewById(R.id.seekBar);

        // Set the listener
        ClickListener clicked = new ClickListener();
        billEditText.setOnEditorActionListener(this);
        billEditText.setOnKeyListener(this);
        radioGroup.setOnKeyListener(this);
        seekBar.setOnSeekBarChangeListener(this);
        seekBar.setMax(30);
        seekBar.setProgress(15);
        applyButton.setOnClickListener(clicked);
        ResetButton.setOnClickListener(clicked);

        // set array adapter for spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.split_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item);
        splitSpinner.setAdapter(adapter);
        //anonymous class as the listener
        radioGroup.setOnCheckedChangeListener(checkedChangeListener);

        // aonymous inner class as the listener
        splitSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                split = position + 1;
                calculateAndDisplay();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // do nothing
            }
        });

        saved = getSharedPreferences("saved", MODE_PRIVATE);
    }
    public void onPause(){
        SharedPreferences.Editor edit = saved.edit();
        edit.putString("billAmountString", billAmountString);
        super.onPause();
    }
    public void onResume() {
        super.onResume();
        billAmountString = saved.getString(billAmountString, "");
    }
    public boolean onKey(View v, int keyCode, KeyEvent event) {

        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        switch (keyCode) {

            case KeyEvent.KEYCODE_ENTER:
            case KeyEvent.KEYCODE_DPAD_CENTER:
                calculateAndDisplay();
                imm.hideSoftInputFromInputMethod(
                        billEditText.getWindowToken(), 0);
                return true; // consume the event
            case KeyEvent.KEYCODE_DPAD_DOWN:
                calculateAndDisplay();
                imm.hideSoftInputFromInputMethod(
                        billEditText.getWindowToken(), 0);
                break; // don't consume the even
            case KeyEvent.KEYCODE_DPAD_RIGHT:
            case KeyEvent.KEYCODE_DPAD_LEFT:
                if(v.getId() == R.id.PercentNumberLabel) {
                    calculateAndDisplay();
                }

                break; // don't consume the event
        }

        return false; // don't consume the event
    }
    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

        if(actionId == EditorInfo.IME_ACTION_DONE ||
                actionId == EditorInfo.IME_ACTION_UNSPECIFIED) {
            calculateAndDisplay();
        }

        Toast.makeText(getApplicationContext(), "Action ID: " + actionId, Toast.LENGTH_LONG).show();

        return false;
    }
    public void calculateAndDisplay(){
            // Get the amount
            billAmountString = billEditText.getText().toString();
            if (billAmountString.equals("")){
                billAmt = 0.0f;
            }else {
                billAmt = Float.parseFloat(billAmountString);
            }

            // Calculate tip percent and total
            if (rounding == ROUND_NONE){
                tipAmount = billAmt * percentAmt;
                totalAmt = tipAmount + billAmt;
            }
            else if (rounding == ROUND_TIP){
                tipAmount = StrictMath.round(billAmt * percentAmt);
                totalAmt = tipAmount + billAmt;
            }
            else if (rounding == ROUND_TOTAL){
                float tipNotRounded = billAmt * percentAmt;
                totalAmt = StrictMath.round(billAmt + tipNotRounded);
                tipAmount = totalAmt - billAmt;
            }

        if(split == 1) {
            perPersonLbl.setVisibility(View.GONE);
            perPersonTv.setVisibility(View.GONE);
        } else {
            splitAmt = totalAmt / split;
            perPersonLbl.setVisibility(View.VISIBLE);
            perPersonTv.setVisibility(View.VISIBLE);
        }

            //Format and display the percent, tip amount, and total amount
        NumberFormat percent = NumberFormat.getPercentInstance();
        PercentNumberLabel.setText(percent.format(percentAmt));

        NumberFormat currency = NumberFormat.getCurrencyInstance();
        tipPercentNumLabel.setText(currency.format(tipAmount));
        totalTextView.setText(currency.format(totalAmt));
        perPersonTv.setText(currency.format(splitAmt));

    }
    private RadioGroup.OnCheckedChangeListener checkedChangeListener = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
            switch (checkedId) {
                case R.id.noneRadioBtn:
                    rounding = ROUND_NONE;
                    break;
                case R.id.tipRadioBtn:
                    rounding = ROUND_TIP;
                    break;
                case R.id.totalRadioBtn:
                    rounding = ROUND_TOTAL;
                    break;
            }

            calculateAndDisplay();
        }
    };

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        float percentage = progress *.01f;
        PercentNumberLabel.setText(""+ percentage);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    class ClickListener implements OnClickListener {
        @Override
        public void onClick(View v) {

            switch(v.getId()) {
                case R.id.applyButton:
                    int amt = seekBar.getProgress();
                    float percentage = amt * .01f;
                    percentAmt = percentage;
                    calculateAndDisplay();
                    break;
                case R.id.ResetButton:
                    percentAmt = .15f;
                    billEditText.setText("");
                    calculateAndDisplay();
                    break;
            }
        }

    }
}
