/*COMENTARIOS*/
/*Debe tener permisos de acceso a internet en el AndroidManifest
La App inicia preguntando si existe una base datos llamada Credenciales, en ella se encuetra
el Nombre de quien instala la App.
Para que el servicio funcione tiene que tener permiso FOREGROUND_SERVICE

Sí la base no existe abre el una ventana para configurar esos parametros. En mqq se necesitan
mas párametros pero se obian para este proposito. Ejemplo TopicSubscriber y TopicPublish. En mqtt
se necesita un TopicSubcriber para poder recibir los mensajes y el TopicPublish para

Deberia tener un servicio de aviso de terremotos.

Junio 18 de 2022 Funciona.

 */

package com.example.control4;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.ActionBar;
import androidx.core.app.ActivityCompat;
//import android.app.ActionBar;
import android.Manifest;
import android.app.ActivityManager;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;

import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.view.LayoutInflater;

import android.view.View;
import android.widget.LinearLayout;

import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    final Messenger miConexion = new Messenger(new MainActivity.IncomingHandler());//para comunicarse con el servicio
    Messenger lanzador;
    boolean mBound =  false;

    LinearLayout Marco;
    boolean iniciado = false;

    String Nombre = "";

    String Contenido = "";
    String Conte1 = "";
    int Inscrito = 0;

    TextView Conexion;

    @Override
    public void onStop() {
        super.onStop();
            unbindService(connection);
    }

    @Override
    public void onStart(){
        super.onStart();
        bindService(new Intent(this,MyService.class),connection,Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Marco =  findViewById(R.id.iMarco);

        ActionBar miBar = getSupportActionBar();
        miBar.setTitle("");
        miBar.setDisplayShowCustomEnabled(true);

        LayoutInflater layoutInflater = LayoutInflater.from(this);
        View miView = layoutInflater.inflate(R.layout.bar, null);
        miBar.setCustomView(miView);
        miBar.setDisplayShowCustomEnabled(true);
        ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.VIBRATE},1);

        Conexion =  findViewById(R.id.iConexion);
        Conexion.setVisibility(View.VISIBLE);//oculta el icono de conexion

        SQLiteDatabase DbCredenciales = openOrCreateDatabase("TablaCredenciales",MODE_PRIVATE,null);
        try {
            Cursor receptor = DbCredenciales.rawQuery("SELECT  *FROM Credenciales", null);
            receptor.moveToFirst();
            Contenido = receptor.getString(0);
            Conte1 = Contenido;

            if (!isMyServiceRunning(MyService.class)) {
                final Intent intent = new Intent(this, MyService.class);
                intent.putExtra("filtro", Conte1);
                startService(intent);
            }
            Inscrito = 1;
            String [] Param = Contenido.split("\\|");
            Nombre = Param[1];
            receptor.close();

        }catch (SQLException e) {
            Inscrito = 0;
            Intent Inscribirme = new Intent(this, Inscripcion.class);
            startActivity(Inscribirme);
        }
        DbCredenciales.close();
        NotificationManager parar1 = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            parar1.cancelAll();
        }else{
            parar1.cancelAll();
        }
        Inflar("","Tarjeta");//los parametros se ingresan en rom
    }

    public void Inflar(String Cnt,String Nombre){
        iniciado = true;
        Intent MostrarAmigo =  new Intent(this, Display1.class);
        MostrarAmigo.putExtra("filtro",Cnt);
        MostrarAmigo.putExtra("Nombre",Nombre);
        startActivity(MostrarAmigo);

    }
    private  ServiceConnection connection =  new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            lanzador =  new Messenger(service);
            try{
                Message msg = Message.obtain(null,MyService.REGISTER);
                msg.replyTo = miConexion;
                lanzador.send(msg);
            }catch (RemoteException o){
                Log.e("pista","R: " + o);
            }
        }
        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            lanzador =  null;
            mBound = false;
        }
    };

    private boolean isMyServiceRunning(Class<?> serviceClass) {

        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (android.app.ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg){
            switch(msg.what){
                case MyService.CONECTADO:
                    Conexion.setVisibility(View.INVISIBLE);
                    break;
                case MyService.DESCONECTADO:
                    Conexion.setVisibility(View.VISIBLE);
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }
    public Handler duo = new Handler(){
        @Override
        public void handleMessage(Message msg){
            Message dsa = Message.obtain(null,MyService.REGISTRO,"hola");
            //enfocado = true;
            try{
                lanzador.send(dsa);
            }catch (RemoteException o){
                Log.e("error", "error: " + o);
            }
        }

    };
}
