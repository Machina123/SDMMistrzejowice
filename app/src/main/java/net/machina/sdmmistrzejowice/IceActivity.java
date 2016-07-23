package net.machina.sdmmistrzejowice;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class IceActivity extends AppCompatActivity implements View.OnClickListener{

    protected Button btnCallEmergency, btnCallPolice, btnCallParamedics, btnCallFiredept, btnCallSafetyline;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ice);

        btnCallEmergency = (Button) findViewById(R.id.btnCallEmergency);
        btnCallFiredept = (Button) findViewById(R.id.btnCallFiredept);
        btnCallParamedics = (Button) findViewById(R.id.btnCallParamedics);
        btnCallPolice = (Button) findViewById(R.id.btnCallPolice);
        btnCallSafetyline = (Button) findViewById(R.id.btnCallSafetyline);

        btnCallEmergency.setOnClickListener(this);
        btnCallFiredept.setOnClickListener(this);
        btnCallParamedics.setOnClickListener(this);
        btnCallPolice.setOnClickListener(this);
        btnCallSafetyline.setOnClickListener(this);

    }

    public void call(String phoneNo) {
        Uri action = Uri.parse("tel:" + phoneNo);
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(action);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }

    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.btnCallEmergency:
                call("112");
                break;
            case R.id.btnCallFiredept:
                call("998");
                break;
            case R.id.btnCallParamedics:
                call("999");
                break;
            case R.id.btnCallPolice:
                call("997");
                break;
            case R.id.btnCallSafetyline:
                call("+48608599999");
                break;
        }
    }
}
