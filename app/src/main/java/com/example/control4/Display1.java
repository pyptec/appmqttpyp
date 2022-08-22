package com.example.control4;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.media.AudioManager;

import android.media.SoundPool;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.Timer;
import java.util.TimerTask;

public class Display1 extends AppCompatActivity {

    boolean mutar = false;
    RelativeLayout ContenedorEnviar;

    LinearLayout Fondo;
    Button BotonUnico;

    final Messenger messenger = new Messenger(new IncomingHandler());//para comunicarse con el servicio
    Messenger enviador;
    boolean bound;

    TextView Mensaje;
    String Contacto;

    int con = 1;
    ScrollView sv;
    LinearLayout ll;
    LinearLayout Arriba;

    Button Mas;

    EditText CajaMsjEnv;

    Button Enviar;
    Button Atras;
    TextView TituloContacto;
    String ContactoAmigo;

    boolean mBound = false;

    String Resultado = "-";
    String Name;

    Timer timer;
    String Nombre;

    SoundPool sonido;
    int iAudio;
    Boolean noguardar = true;

    boolean enfocado = true;

    @Override
    public void onStart(){
        super.onStart();
        bindService(new Intent(this,MyService.class),miMessenger,Context.BIND_AUTO_CREATE);
    }
    public void onStop(){
        super.onStop();
        Message mdf = Message.obtain(null,MyService.APRESENTE,0,0);
        enfocado = false;
        try{
            enviador.send(mdf);
        }catch (RemoteException o){
            Log.e("pista","R: " + o);
        }
        if(bound){
            unbindService(miMessenger);
            bound = false;
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display1);

        ActionBar miBar = getSupportActionBar();
        miBar.setTitle("");
        miBar.setDisplayShowCustomEnabled(true);

        Mensaje =  findViewById(R.id.iMensaje);
        Fondo = findViewById(R.id.iFondo);

        sonido = new SoundPool(1, AudioManager.STREAM_MUSIC,1);
        BotonUnico = findViewById(R.id.iBotonUnico);
        BotonUnico.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mutar == false){
                    String ContMsg = Resultado + "|" + Nombre + "|" +  ContactoAmigo + "|" + "0";
                    Message util = Message.obtain(null,MyService.ENVIAR_MENSAJE,ContMsg);
                    try{
                        enviador.send(util);
                    }catch (RemoteException o){
                        Log.e("pista", "R : " + o);
                    }
                    mutar = true;
                }else{
                    String ContMsg = Resultado + "|" + Nombre + "|" +  ContactoAmigo + "|" + "1";
                    Message util = Message.obtain(null,MyService.ENVIAR_MENSAJE,ContMsg);
                    try{
                        enviador.send(util);
                    }catch (RemoteException o){
                        Log.e("pista", "R : " + o);
                    }
                    mutar = false;
                }
            }
        });
        iAudio = sonido.load(this,R.raw.message,1);

        ContenedorEnviar = findViewById(R.id.iContenedorEnviar);
        ContenedorEnviar.setVisibility(View.GONE);

        Contacto = getIntent().getStringExtra("filtro");//El nombre del Contacto
        Nombre = getIntent().getStringExtra("Nombre");//Mi nombre el que envia
        Name = Nombre;

        Enviar = findViewById(R.id.iEnviar);
        CajaMsjEnv = findViewById(R.id.iCajaMsjEnv);
        Mas = findViewById(R.id.iMas);

        Arriba = findViewById(R.id.iArriba);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("CONTROL");
        actionBar.setDisplayShowHomeEnabled(true);

        LayoutInflater layoutInflater = LayoutInflater.from(this);
        View mCustomView = layoutInflater.inflate(R.layout.bar1, null);
        actionBar.setCustomView(mCustomView);
        actionBar.setDisplayShowCustomEnabled(true);

        Atras = findViewById(R.id.iAtras);
        Atras.setVisibility(View.GONE);
        TituloContacto =  findViewById(R.id.iHeadContacto);

        timer = new Timer();
        timer.schedule(new TraerEvento(), 1000);//este timer temporiza la entrada en servicio el envio de mensajes

        Mas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
        Enviar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String Msg = CajaMsjEnv.getText().toString();
                String ContMsg = Resultado + "|" + Nombre + "|" +  ContactoAmigo + "|" + Msg;
                Message util = Message.obtain(null,MyService.ENVIAR_MENSAJE,ContMsg);
                try{
                    enviador.send(util);
                }catch (RemoteException o){
                    Log.e("pista", "R : " + o);
                }
                noguardar = true;
                CajaMsjEnv.setText("");
            }
        });
        sv = new ScrollView(this);
        ll = new LinearLayout(this);
    }

    private ServiceConnection miMessenger = new ServiceConnection(){
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            enviador =  new Messenger(service);
            try{
                Message msg = Message.obtain(null,MyService.REGISTER);
                msg.replyTo = messenger;
                enviador.send(msg);
               // Toast.makeText(context,"Vinculado",Toast.LENGTH_LONG).show();
            }catch (RemoteException o){
                Log.e("pista","R: " + o);
            }
        }
        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            enviador =  null;
            mBound = false;
        }
    };

    public class TraerEvento extends TimerTask{

        @Override
        public void run() {
            con++;
            duo.sendEmptyMessage(0);
        }
    }
    public Handler duo = new Handler(){
        @Override
        public void handleMessage(Message msg){
            Message dsa = Message.obtain(null,MyService.PRESENTE,Contacto);
            enfocado = true;
            try{
                enviador.send(dsa);
            }catch (RemoteException o){
                Log.e("error", "error: " + o);
            }
        }
    };

    class IncomingHandler extends Handler{
        @Override
        public void handleMessage(Message msg){
            switch(msg.what){
                case MyService.MSG_SAY_HELLO:
                    String Util = msg.obj.toString();
                    break;
                case MyService.CHULIAR:
                    Util = msg.obj.toString();
                    PrenderLeds(Util);
                    break;
                    default:
                        super.handleMessage(msg);
            }
        }
    }
    public void PrenderLeds(String msg){
        Log.i("pista", "PrendeLed" + msg );
        Mensaje.setText("Mensaje : " + msg );
        if(msg.startsWith("1")){
            Fondo.setBackgroundColor(Color.GREEN);
        }
        if(msg.startsWith("0")){

            Fondo.setBackgroundColor(Color.RED);

        }
    }
}
