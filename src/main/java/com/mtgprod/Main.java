package com.mtgprod;

import jssc.SerialPortException;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) throws InterruptedException {
        /*BufferedReader br; String s;
        Path loraConfig = Paths.get("lora-config.txt");
        try {
            br = Files.newBufferedReader(loraConfig, Charset.defaultCharset());

        } catch (IOException e) {
            System.out.println("Error reading lora-config.txt file");
            throw new RuntimeException(e);
        }*/


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
        lora.macGetDr();
        Thread.sleep(1000);
        //lora.getVdd();
        //lora.macGetDeveui();
        //Thread.sleep(1000);
        //lora.macGetAppeui();
        //Thread.sleep(1000);
        //lora.macGetDeveui();
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

        final float[] SENSOR_DATA = {
                230.2f,    // u
                16.35f,    // i
                49.98f,    // f
                0.92f,     // k
                3492.6684f, // p
                1475.09f,   // q
                3763.77f,   // s
                44.8f,      // lat
                0.0f,       // long
                372.81f,    // w
                0.1765f     // prix
        };

        ByteBuffer lora_buffer = ByteBuffer.allocate(4 * 11);
        lora_buffer.order(ByteOrder.BIG_ENDIAN);

        for (float data : SENSOR_DATA) {
            lora_buffer.putFloat(data);
        }

        var lora_buffer_array = lora_buffer.array();
        var string_payload = toHexString(lora_buffer_array);

        System.out.println(string_payload);

        lora.macTx("uncnf", "1", string_payload);
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