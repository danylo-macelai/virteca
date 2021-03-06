package br.com.mobile;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import br.com.mobile.Resource.SessionResource;

public class ApiConnection extends AppCompatActivity {
    private EditText             edtUrl;
    private EditText             edtPort;
    private TextView             txtStatus;
    private ImageView            imgStatus;
    private BottomNavigationView bottomNavigationView;
    private SessionResource      session;
    private MenuItem             menuItem;
    private Menu                 menu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_api_connection);
        session = new SessionResource(this);
        edtUrl = (EditText) findViewById(R.id.edtUrl);
        edtPort = (EditText) findViewById(R.id.edtPort);
        txtStatus = (TextView) findViewById(R.id.txtStatus);
        imgStatus = (ImageView) findViewById(R.id.imgStatus);

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        menu = bottomNavigationView.getMenu();
        menuItem = menu.getItem(3);
        menuItem.setChecked(true);

        bottomNavigationView
                .setOnNavigationItemSelectedListener(
                        new BottomNavigationView.OnNavigationItemSelectedListener() {
                            Intent intent;

                            @Override
                            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

                                switch (menuItem.getItemId()) {
                                case (R.id.search_menu):
                                    intent = new Intent(ApiConnection.this, PesquisaArquivosActivity.class);
                                    if (verificaSessao()) {
                                        startActivity(intent);
                                        overridePendingTransition(0, 0);
                                        finish();
                                    } else {
                                        Toast.makeText(getApplicationContext(), "CONEXÃO NÃO CONFIGURADA!",
                                                Toast.LENGTH_SHORT).show();
                                    }

                                    break;

                                case (R.id.upload_menu):
                                    intent = new Intent(ApiConnection.this, LoginActivity.class);
                                    intent.putExtra("action", 0);
                                    if (verificaSessao()) {
                                        startActivity(intent);
                                        overridePendingTransition(0, 0);
                                        finish();
                                    } else {
                                        Toast.makeText(getApplicationContext(), "CONEXÃO NÃO CONFIGURADA!",
                                                Toast.LENGTH_SHORT).show();
                                    }
                                    break;
                                case (R.id.user_menu):
                                    intent = new Intent(ApiConnection.this, LoginActivity.class);
                                    intent.putExtra("action", R.id.user_menu);
                                    if (verificaSessao()) {
                                        startActivity(intent);
                                        overridePendingTransition(0, 0);
                                        finish();
                                    } else {
                                        Toast.makeText(getApplicationContext(), "CONEXÃO NÃO CONFIGURADA!",
                                                Toast.LENGTH_SHORT).show();
                                    }

                                    break;
                                }

                                return true;
                            }
                        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        verificaStatus();
    }

    public void conectar(View v) {
        String ip = edtUrl.getText().toString();
        String apiPort = edtPort.getText().toString();

        if (!ip.equals("")) {
            session.setPreferencesIP(ip);

            if (apiPort.equals("")) {
                session.setPreferencesApiURL("http://" + session.getPreferencesIP());
            } else {
                session.setPreferencesPortApi(apiPort);
                session.setPreferencesApiURL(
                        "http://" + session.getPreferencesIP() + ":" + session.getPreferencesPortApi());
            }
        }
        verificaStatus();
    }

    private String carregar() {
        return session.getPreferencesApiURL();

    }

    private void verificaStatus() {
        String urlConnection = carregar();
        if (!urlConnection.equals("")) {
            txtStatus.setText("A Conexão com a API foi configurada com sucesso. URL: " + urlConnection);
            imgStatus.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.success_api_connection));
        } else {
            txtStatus.setText("Conexão não configurada!");
            imgStatus.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.error_api_connection));
        }
        txtStatus.setVisibility(View.VISIBLE);
        imgStatus.setVisibility(View.VISIBLE);
    }

    private boolean verificaSessao() {
        boolean status;
        if (session.getPreferencesApiURL().equals("")) {
            status = false;
        } else {
            status = true;
        }
        return status;
    }

}
