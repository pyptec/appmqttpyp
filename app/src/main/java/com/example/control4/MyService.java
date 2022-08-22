package com.example.control4;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import org.eclipse.paho.android.service.MqttAndroidClient;

import org.eclipse.paho.client.mqttv3.DisconnectedBufferOptions;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.io.UnsupportedEncodingException;

import java.util.Timer;
import java.util.TimerTask;


import static android.app.Notification.DEFAULT_SOUND;


public class MyService extends Service {

    static final int MSG_SAY_HELLO = 1;
    static  final int PRESENTE = 2;
    static final int APRESENTE = 3;
    static final int ENVIAR_MENSAJE = 4;
    static final int REGISTER = 5;
    static final int CHULIAR = 6;

    static final int REGISTRO = 12;
    static final  int CONECTADO = 10;
    static final int DESCONECTADO = 11;

/////////////////////////////////////////////////////

    MqttHelper mqttHelper;
    public static final String CHANNEL_ID6= "Foreground Service Channel2";

    public static final int NOTIFICACION_ID = 60;
    String mmm = "";
    String serverUri = "tcp://mqtt.pypteclonuevoeniot.com:1883";

    String clientId = "";

    String subscriptionTopic = "";

   String username = "";

    String password = "";

    String ClienteEnLinea;

    boolean conectado = false;

    Messenger messenger = new Messenger(new IncomingHandler());

    Timer timerAvisarConetado;//cuando la gui principal se conecta se activa este timer e indica si esta conectado o no
    Timer timerresubscribirme;//espera un tiempo para resusbscribirme

    class IncomingHandler extends Handler{
        @Override
        public void handleMessage(Message msg){
            String ContMsg;
            switch(msg.what){
                case REGISTER:
                    messenger = msg.replyTo;
                    ActivarTimer();
                    break;
                case ENVIAR_MENSAJE:
                    String IdPendiente= GrabarDatos(msg.obj.toString());//recoge el id de la transaccion y se la entrega al publicador
                    ContMsg = msg.obj.toString();
                    String[] Ayuda = new String[4];
                    Ayuda = ContMsg.split("\\|");
                    int largo = Ayuda.length;
                    if(largo == 4){
                        String Name = Ayuda[1];
                        String Destino = Ayuda[2];
                        String Mensa = Ayuda[3];
                        mqttHelper.publicar(Name,Destino,Mensa,IdPendiente,"COD1");//le envio el id de la transacion de la base de datos
                    }
                    break;
                case PRESENTE:
                    ContMsg = msg.obj.toString();
                    Presentarse(ContMsg);
                    break;
                case APRESENTE:
                    DesVincularse();
                    break;
                    default:
                        super.handleMessage(msg);
            }
        }
    }
    public MyService() {
    }
    @Override
    public IBinder onBind(Intent intent) {
        return messenger.getBinder();

    }
    @Override
    public void onCreate(){

    }
    @Override
    public int onStartCommand(Intent intent, int flag, int startId) {
        super.onStartCommand(intent, flag, startId);

        try{
            SQLiteDatabase db = openOrCreateDatabase("TablaCredenciales",MODE_PRIVATE,null);
            Cursor datos = db.rawQuery("SELECT *FROM Credenciales",null);
            datos.moveToFirst();
            String contenido = datos.getString(0);//accede a la primer dato de la lista
            datos.close();
            String []aux = contenido.split("\\|");
            int large = aux.length;
            if(large > 3){
                username = aux[1];
                subscriptionTopic = "v1/devices/cliente1/#";//aux[2];//me sucribo manualmente
                clientId = aux[3];
                password = aux[4];
            }
            db.close();
        }catch (SQLException o){
            String Tomo;
            Tomo = intent.getStringExtra("filtro");//recojo
            String[] Dt1 =  Tomo.split("\\|");
            int LargoDt1 =  Dt1.length;
            if(LargoDt1 > 3){
                username = Dt1[1];
                subscriptionTopic = Dt1[2];
                clientId = Dt1[3];
                password = Dt1[4];
            }
        }
        startMqtt();
        return START_STICKY;
    }

    private void startMqtt() {
        mqttHelper = new MqttHelper(getApplicationContext());
        mqttHelper.mqttAndroidClient.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean b, String s) {
                Log.w("Debug", "Connected");
                conectado = true;
                EnviarMensajeGuiPrincipal("hola");
            }
            @Override
            public void connectionLost(Throwable throwable) {
                Log.w("Conexion", "Perdida");
                EnviarMensajeGuiPrincipal1("nohola");
                ActivarResubscripcion();
                conectado = false;

            }
            @Override
            public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
                Log.w("Debug", mqttMessage.toString());

                mmm = mqttMessage.toString();

                String [] Tem = mmm.split("\\|");
                Message uso = null;
                uso = Message.obtain(null,MyService.CHULIAR,mmm);//envia mensaje al

                if(topic.contains("v1/devices/cliente1/tarjeta") | topic.contains("v1/devices/cliente1/celular") ){
                    Timbre(mmm);
                }

                try {
                    messenger.send(uso);
                }catch (RemoteException o){
                    Log.e("msg","Fallo envio");
                }
            }
            @Override
            public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
                try {
                    MqttMessage Loro = iMqttDeliveryToken.getMessage();
                }catch (MqttException l){
                }
            }
        });
    }
    public class MqttHelper {
        public MqttAndroidClient mqttAndroidClient;

        public MqttHelper(Context context) {

            mqttAndroidClient = new MqttAndroidClient(context, serverUri, clientId);
            mqttAndroidClient.setCallback(new MqttCallbackExtended() {

                @Override
                public void connectComplete(boolean b, String s) {
                    Log.w("mqtt", s);
                }
                @Override
                public void connectionLost(Throwable throwable) {
                }
                @Override
                public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
                    Log.w("mqtt", mqttMessage.toString());
                }
                @Override
                public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
                }
            });
            connect();
        }

        private void connect() {
            MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
            mqttConnectOptions.setAutomaticReconnect(true);
            mqttConnectOptions.setCleanSession(false);
            mqttConnectOptions.setUserName(username);
            mqttConnectOptions.setPassword(password.toCharArray());

            try {
                mqttAndroidClient.connect(mqttConnectOptions, null, new IMqttActionListener() {
                    @Override
                    public void onSuccess(IMqttToken asyncActionToken) {

                        DisconnectedBufferOptions disconnectedBufferOptions = new DisconnectedBufferOptions();
                        disconnectedBufferOptions.setBufferEnabled(true);
                        disconnectedBufferOptions.setBufferSize(300);
                        disconnectedBufferOptions.setPersistBuffer(false);
                        disconnectedBufferOptions.setDeleteOldestMessages(false);
                        mqttAndroidClient.setBufferOpts(disconnectedBufferOptions);
                        subscribeToTopic();
                    }
                    @Override
                    public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                        Log.w("Mqtt", "Failed to connect to: " + serverUri + exception.toString());
                    }
                });

            } catch (MqttException ex) {
                ex.printStackTrace();
            }
        }
        private void subscribeToTopic() {
            try {
                mqttAndroidClient.subscribe(subscriptionTopic, 1, null, new IMqttActionListener() {
                    @Override
                    public void onSuccess(IMqttToken asyncActionToken) {
                        Log.w("Mqtt", "Subscribed!");
                    }
                    @Override
                    public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                        Log.w("Mqtt", "Subscribed fail!");
                        ActivarResubscripcion();
                    }
                });

            } catch (MqttException ex) {
                System.err.println("Exceptionst subscribing");
                ex.printStackTrace();
            }
        }

        public void publicar(String Nombre,String Destino,String Mensaje,String IdPendiente,String Head) {

            String msg = Mensaje;
            Log.e("mqtt_enviado ",msg);

            Destino = "v1/devices/cliente1/tarjeta";
            Log.w("pista","enviado a" + Destino + msg);
            byte[] encodedPayload = new byte[0];
            try {
                encodedPayload = msg.getBytes("UTF-8");
            } catch (UnsupportedEncodingException e) {
                Log.e("Excep","R: " + e);
            }
            final MqttMessage message = new MqttMessage(encodedPayload);
            message.setId(320);
            message.setQos(0);
            try {
                mqttAndroidClient.publish(Destino,message);

            } catch (MqttException e) {
                Log.e("pista", "exception publish");
            }

        }
    }
    public void Timbre(String wwx) {

        Log.i("pista","Timbre");
        String nombre = "algo";
        String mensaje = wwx;

            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(intent.FLAG_ACTIVITY_CLEAR_TASK);//Hace que la actividad no abra dos veces
            intent.putExtra("tubo", nombre);//Este codigo le avisa a mainActivity para
            String ID_NOTI = "minoti";

            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel notificationChannel = new NotificationChannel(ID_NOTI, "name",
                        NotificationManager.IMPORTANCE_HIGH);

                long[] pattern = {0, 200, 200};
                Notification notification = new Notification.Builder(getApplicationContext(), ID_NOTI)
                        .setContentTitle("Mensaje de : " + "tarjeta")
                        .setActions()
                        .setContentText(mensaje)
                        .setVibrate(pattern)
                        .setAutoCancel(true)
                        .setSmallIcon(R.drawable.hole)
                        .setContentIntent(pendingIntent)//esta funcion abre la app
                        .build();
                NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.createNotificationChannel(notificationChannel);
                notificationManager.notify(31, notification);
                Log.i("pista","Notificaciones");
            } else {
                long[] pattern = {0, 200, 200};
                Notification otra = new NotificationCompat.Builder(this, CHANNEL_ID6)
                        .setContentTitle("Mensaje de : " + nombre)
                        .setContentText(mensaje)
                        .setVibrate(pattern)
                        .setAutoCancel(true)
                        .setSmallIcon(R.drawable.hole)
                        .setDefaults(DEFAULT_SOUND)
                        .setContentIntent(pendingIntent)
                        .build();
                NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(this);
                notificationManagerCompat.notify(NOTIFICACION_ID, otra);
            }
    }

    public void Presentarse(String Mensaje){
        ClienteEnLinea = Mensaje;
    }
    public void DesVincularse(){
        ClienteEnLinea = "";
    }

    public void EstaConectadoOdesconectado(){
        if(conectado){
            EnviarMensajeGuiPrincipal("algo");
        }else{
            EnviarMensajeGuiPrincipal1("algo");
        }
    }

    public void EnviarMensajeGuiPrincipal(String reto) {
        Message gop = Message.obtain(null, MyService.CONECTADO, reto);
        try {
            messenger.send(gop);
            Log.w("pista", "enviaPrincipal");
        } catch (RemoteException o) {
            Log.e("msg","Envio CONECTADO falló");
        }
    }
    public void EnviarMensajeGuiPrincipal1(String reto) {
        Message gop = Message.obtain(null, MyService.DESCONECTADO, reto);
        try {
            messenger.send(gop);
            Log.w("pista", "enviaPrincipal1");
        } catch (RemoteException o) {
            Log.e("msg","Envio DESCONECTADO falló");
        }
    }
    public String GrabarDatos(String Datos){//devuelve el id de la transacción

        return "0";
    }

    public void ActivarTimer() {
        timerAvisarConetado = new Timer();
        timerAvisarConetado.schedule(new IniciarTimer(),1000);
    }
    public class IniciarTimer extends TimerTask{
        @Override
        public void run(){
            EstaConectadoOdesconectado();
        }
    }
    public void ActivarResubscripcion(){
        timerresubscribirme = new Timer();
        timerresubscribirme.schedule(new temporizarResubscripcion(),3000);
    }
    public class temporizarResubscripcion extends TimerTask{
        @Override
        public void run() {
            mqttHelper.subscribeToTopic();
        }
    }
}