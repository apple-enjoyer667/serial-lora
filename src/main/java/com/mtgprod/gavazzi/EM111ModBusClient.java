package com.mtgprod.gavazzi;

import com.intelligt.modbus.jlibmodbus.exception.ModbusIOException;
import com.intelligt.modbus.jlibmodbus.exception.ModbusNumberException;
import com.intelligt.modbus.jlibmodbus.exception.ModbusProtocolException;
import com.intelligt.modbus.jlibmodbus.master.ModbusMaster;
import com.intelligt.modbus.jlibmodbus.master.ModbusMasterFactory;
import com.intelligt.modbus.jlibmodbus.serial.*;
import com.intelligt.modbus.jlibmodbus.serial.SerialPortFactoryJSSC;

public class EM111ModBusClient {

    private ModbusMaster master;
    private final String portName;
    private final int slaveId;

    // Configuration du EM111 pour du ModBus
    private static final int BAUD_RATE = 9600;
    private static final int DATA_BITS = 8;
    private static final int STOP_BITS = 1;
    private static final SerialPort.Parity PARITY = SerialPort.Parity.NONE;

    // Adresses Registres (Hexadécimal)
    private static final int REG_VOLTAGE = 0x00;       // Tension (V)
    private static final int REG_CURRENT = 0x02;       // Courant (A) - RMS
    private static final int REG_ACTIVE_POWER = 0x04;  // Puissance (W)
    private static final int REG_TOTAL_KWH = 0x10;     // Energie Active (kWh)
    private static final int REG_FREQUENCY = 0x0F ;    // Fréquence (Hz)
    private static final int REG_REACT_POWER = 0x08 ;  // Puissance Réactive (VAR)
    private static final int REG_POWER_FACTOR = 0x0E ; // Facteur de puissance

    //Variable -> Valeurs Electriques

    public float voltage ;
    public float currentRMS ;
    public float activePower;
    public float totalKwh ;
    public float frequency ;
    public float reactivePower ;
    public float apparentPower ;
    public float powerFactor ;
    public String quality ; // Qualité lié au facteur de puissance  | Parfait , Excellent , OK/MOYEN , Mauvais

    //TABLEAU DE STOCKAGE DE VALEUR
    public float[] valeurs = new float[8] ;

    public EM111ModBusClient(String portName, int slaveId) {
        this.portName = portName;
        this.slaveId = slaveId;
    }

    public void connect() throws ModbusIOException, SerialPortException {
        // Initialisation du driver série JSSC
        SerialUtils.setSerialPortFactory(new SerialPortFactoryJSSC());

        SerialParameters serialParameters = new SerialParameters();
        serialParameters.setDevice(portName);
        serialParameters.setBaudRate(SerialPort.BaudRate.getBaudRate(BAUD_RATE));
        serialParameters.setDataBits(DATA_BITS);
        serialParameters.setStopBits(STOP_BITS);
        serialParameters.setParity(PARITY);

        master = ModbusMasterFactory.createModbusMasterRTU(serialParameters);
        master.setResponseTimeout(1000);

        try {
            if (!master.isConnected()) {
                master.connect();
                System.out.println(">>> Connecté au port " + portName);
            }
        } catch (ModbusIOException e) {
            System.err.println("Erreur de connexion : " + e.getMessage());
            throw e;
        }
    }

    public void disconnect() {
        try {
            if (master != null && master.isConnected()) {
                master.disconnect();
                System.out.println(">>> Deconnecte.");
            }
        } catch (ModbusIOException e) {
            e.printStackTrace();
        }
    }

    /**
     * @return Voltage ---> [0] en V |
     * Intensité ---> [1] en A |
     * Puissance Active ---> [2] en W |
     * Puissance réactive ---> [3] en VAR |
     * Puissance apparente ---> [4] en VA |
     * Facteur de puissance ---> [5] Ø |
     * Energie Totale ---> [6] en kWh |
     * Frequency ---> [7] en Hz
     */
    public float[] readData() {

        try {
            System.out.println("----------- MESURES EM111 -----------");

            // Lecture avec inversion Little Endian
            voltage = readSwapped32BitValue(REG_VOLTAGE, 10.0f, "Tension");
            currentRMS = readSwapped32BitValue(REG_CURRENT, 1000.0f, "Courant RMS");
            activePower = readSwapped32BitValue(REG_ACTIVE_POWER, 10.0f, "Puissance Active");
            reactivePower = readSwapped32BitValue(REG_REACT_POWER,10.0f,"Puissance Réactive");
            totalKwh = readSwapped32BitValue(REG_TOTAL_KWH, 10.0f, "Total kWh");
            frequency = readSwapped16BitValue(REG_FREQUENCY, 10.0f, "Frequence");
            powerFactor = readSwapped16BitValue(REG_POWER_FACTOR , 1000.0f , "Facteur de puissance") ;

            //Calcul

            apparentPower = (float) Math.sqrt(activePower * activePower + reactivePower * reactivePower) ;

            if (powerFactor == 1){
                quality = "Perfect";
            }else if (powerFactor >= 0.90) {
                quality = "Excellent";
            }else if (powerFactor >= 0.80f) {
                quality = "Ok";
            }else {
                quality = "Bad" ;
            }


            // Ajout des valeurs au tableau

            valeurs[0] = voltage ;  // Tension
            valeurs[1] = currentRMS ; // Intensité
            valeurs[2] = activePower; // Puissance Active
            valeurs[3] = reactivePower; // Puissance Réactive
            valeurs[4] = apparentPower ; // Puissance Apparente
            valeurs[5] = powerFactor ; // Facteur de Puissance
            valeurs[6] = totalKwh ; // Energie totale consommée
            valeurs[7] = frequency ; // Fréquence du courant


            System.out.println("\n--- RESULTATS ---");
            System.out.printf("Tension              : %.2f V%n", voltage);
            System.out.printf("Puissance Active     : %.2f W%n", activePower);
            System.out.printf("Puissance Réactive   : %.2f VAR%n", reactivePower);
            System.out.printf("Puissance Apparente  : %.2f VA%n", apparentPower);
            System.out.printf("Courant (Brut)       : %.3f A  <-- Valeur physique reelle (RMS)%n", currentRMS);
            System.out.printf("Total kWh            : %.1f kWh%n", totalKwh);
            System.out.printf("Frequence            : %.2f Hz%n", frequency);
            System.out.printf("Facteur de puissance : %.2f %n" + " Quality ---> " + quality, powerFactor);

            System.out.println("\n\n-------------------------------------");

        } catch (Exception e) {
            System.err.println("Erreur lecture : " + e.getMessage());
            e.printStackTrace();
        }
        return valeurs ;
    }

    private float readSwapped32BitValue(int startAddress, float divisor, String label)
            throws ModbusProtocolException, ModbusNumberException, ModbusIOException {

        int[] registers = master.readInputRegisters(slaveId, startAddress, 2);

        if (registers.length < 2) {
            throw new ModbusNumberException("Pas assez de donnees recues");
        }

        // --- INVERSION (Little Endian Swap) ---
        long lowWord = registers[0] & 0xFFFF;
        long highWord = registers[1] & 0xFFFF;

        // Reconstitution : HighWord << 16 | LowWord
        int rawValue = (int) ((highWord << 16) | lowWord);

        float finalValue = rawValue / divisor;

        return finalValue;
    }
    private float readSwapped16BitValue(int startAddress, float divisor, String label)
            throws ModbusProtocolException, ModbusNumberException, ModbusIOException {

        int[] registers = master.readInputRegisters(slaveId, startAddress, 1);



        // --- INVERSION (Little Endian Swap) ---
        long lowWord = registers[0] & 0xFFFF;


        // Reconstitution : HighWord << 16 | LowWord
        int rawValue = (int) (lowWord);

        float finalValue = rawValue / divisor;

        return finalValue;
    }

}