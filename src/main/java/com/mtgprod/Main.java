package com.mtgprod;

import com.mtgprod.gavazzi.EM111ModBusClient;
import com.mtgprod.logger.ErrorLogger;
import jssc.SerialPortException;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.Base64;
import java.util.concurrent.ThreadLocalRandom;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) throws InterruptedException, SQLException {
        /*BufferedReader br; String s;
        Path loraConfig = Paths.get("lora-config.txt");
        try {
            br = Files.newBufferedReader(loraConfig, Charset.defaultCharset());

        } catch (IOException e) {
            System.out.println("Error reading lora-config.txt file");
            throw new RuntimeException(e);
        }*/

        ErrorLogger errorLogger = new ErrorLogger();
        LoraConfigurator lora = new LoraConfigurator();

        lora.startConnection();

        // Dois être: 70B3D57050000003
        lora.macGetAppeui();
        Thread.sleep(1000);
        // Dois être: 0004A30B0024038F (correspond a la JoinEUI)
        lora.macGetDeveui();
        Thread.sleep(1000);
        lora.sysGetVer();
        Thread.sleep(1000);
        lora.macSetDr("2");
        Thread.sleep(1000);
        lora.macSave();
        Thread.sleep(3000);
        //lora.macSetDevaddr("006677");
        //lora.macTxText();

        //System.out.println("Setting appkey...");
        //lora.macSetAppkey("1BB24C63509C78D11F05C27104A522F5");
        //Thread.sleep(1000);
        //System.out.println("Setting powerIdx...");
        //lora.macSetPwridx("1");
        //Thread.sleep(1000);
        //lora.macSave();
        //Thread.sleep(1000);
        //lora.macSave();
        //lora.macSetAppkey("1BB24C63509C78D11F05C27104A522F5");
        //lora.macSave();
        //lora.macTx("uncnf", "1", toHexString("le payload mon cousin".getBytes()));
        //Thread.sleep(1000);
        System.out.println("Connecting using OTAA...");
        lora.macJoin("otaa");
        Thread.sleep(30 * 1000);
        //System.out.println("Sending 'Augusto Pascal' over radio...");
        //lora.macTx("uncnf", "1", toHexString("lucasaugusto".getBytes()));
        //Thread.sleep(10000);

        String serialPort = "/dev/ttyUSB0";

        // L'ID esclave par défaut du EM111 à 1
        int modbusSlaveId = 1;

        EM111ModBusClient reader = new EM111ModBusClient(serialPort, modbusSlaveId);

        try {



            reader.connect();

            // Boucle de lecture simple
//            for (int i = 0; i < 10; i++) {
//                reader.readData();
//                Thread.sleep(1000); // Periode d'une Seconde entre chaque mesure
//            }
            while (true){
                float[] realData = reader.readData();

                ByteBuffer lora_buffer = ByteBuffer.allocate(4 * 11);
                lora_buffer.order(ByteOrder.BIG_ENDIAN);

                for (float data : realData) {
                    lora_buffer.putFloat(data);
                }

                var lora_buffer_array = lora_buffer.array();
                var string_payload = toHexString(lora_buffer_array);

                System.out.println(string_payload);

                lora.macTx("uncnf", "1", string_payload);

                Thread.sleep(60000);
            }



        } catch (Exception e) {
            e.printStackTrace();
            errorLogger.log(e.getMessage());
        } finally {
            reader.disconnect();
            errorLogger.log("Liaison série déconnecté");
        }

       /* for (int i = 0; i < 40; i++) {

            float min = 100.0f;
            float max = 3500.0f;

            // Génération du nombre aléatoire
            float randomNum = (float) ThreadLocalRandom.current().nextDouble(min, max);

            final float[] SENSOR_DATA = {
                    230.2f,    // u = Tension
                    16.35f,    // i = Intensité
                    49.98f,    // f = fréquence
                    0.92f,     // k = facteur de puissance
                    randomNum, // p = puissance active
                    1475.09f,   // q = puissance réactive
                    3763.77f,   // s = puissance apparente
                    //44.8f,      // lat
                    //0.0f,       // long
                    372.81f,    // w
                    //0.1765f     // prix
            };

            System.out.println("random kWh:"+randomNum);

            ByteBuffer lora_buffer = ByteBuffer.allocate(4 * 11);
            lora_buffer.order(ByteOrder.BIG_ENDIAN);

            for (float data : SENSOR_DATA) {
                lora_buffer.putFloat(data);
            }

            var lora_buffer_array = lora_buffer.array();
            var string_payload = toHexString(lora_buffer_array);

            System.out.println(string_payload);

            lora.macTx("uncnf", "1", string_payload);

            Thread.sleep(60*1000);
        }*/
    }

    public static String toHexString(byte[] bytes) {
        char[] hexArray = {'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'};
        char[] hexChars = new char[bytes.length * 2];
        int v;
        for ( int j = 0; j < bytes.length; j++ ) {
            v = bytes[j] & 0xFF;
            hexChars[j*2] = hexArray[v/16];
            hexChars[j*2 + 1] = hexArray[v%16];
        }
        return new String(hexChars);
    }
}