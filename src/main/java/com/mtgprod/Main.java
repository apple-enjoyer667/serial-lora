package com.mtgprod;

import com.intelligt.modbus.jlibmodbus.exception.ModbusIOException;
import com.intelligt.modbus.jlibmodbus.serial.SerialPortException;
import com.mtgprod.gavazzi.EM111ModBusClient;
import com.mtgprod.logger.ErrorLogger;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.sql.SQLException;


public class Main {
    public static void main(String[] args) throws InterruptedException, SQLException, SerialPortException, ModbusIOException {
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

        System.out.println("Connecting using OTAA...");
        lora.macJoin("otaa");
        Thread.sleep(30 * 1000);

        String em111serialPort = "/dev/ttyUSB0";

        // L'ID esclave par défaut du EM111 à 1
        int modbusSlaveId = 1;
        EM111ModBusClient reader = new EM111ModBusClient(em111serialPort, modbusSlaveId);

        try {
            reader.connect();

            while (true) {
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