package com.mtgprod;

import com.mtgprod.clavier.In;
import jssc.SerialPortEvent;
import jssc.SerialPortException;

public class LoraConfigurator extends LiaisonSerie {
    public LoraConfigurator() {

    }

    public void startConnection() {
        super.listerLesPorts().forEach(System.out::println);

//        In.readString();
//        System.out.println("Valider avec entrée");

        try {
            super.initCom("/dev/ttyS0");

            super.configurerParametres(
                    57600,
                    8,
                    1,
                    0
            );

        } catch (SerialPortException e) {
            System.out.println("Connexion au LoRa impossible");
        }
    }

    @Override
    public void serialEvent(SerialPortEvent event) {
        System.out.println("Retour de la commande lora:");

        var trame_int = event.getEventValue();

        var trame_byte = new byte[trame_int];
        trame_byte = this.lireTrame(trame_int);

        System.out.println(new String(trame_byte));
    }
    // --- 2.3 System Commands ---

    /**
     * 2.3.1 Met le module en veille pour X millisecondes.
     * Note: Le module se réveille s'il reçoit un caractère sur l'UART (break condition).
     */
    public void sysSleep(long lengthMs) {
        String message = "sys sleep " + lengthMs + "\r\n";
        super.ecrire(message.getBytes());
    }

    /**
     * 2.3.2 Reset logiciel du module.
     */
    public void sysReset() {
        super.ecrire("sys reset\r\n".getBytes());
    }

    /**
     * 2.3.4 Reset aux paramètres d'usine (Attention, efface la config !).
     */
    public void sysFactoryReset() {
        super.ecrire("sys factoryRESET\r\n".getBytes());
    }

    /**
     * 2.3.6.1 Récupère la version du firmware.
     */
    public void getSysVer() {
        super.ecrire("sys get ver\r\n".getBytes());
    }

    /**
     * 2.3.6.3 Récupère la tension d'alimentation (en mV).
     */
    public void getVdd() {
        super.ecrire("sys get vdd\r\n".getBytes());
    }

    /**
     * 2.3.6.4 Récupère l'identifiant matériel unique (EUI du chip).
     */
    public void getHweui() {
        super.ecrire("sys get hweui\r\n".getBytes());
    }

    // --- 2.4.8 MAC Set Commands ---

    /**
     * 2.4.8.1 Définit l'Application Key (OTAA).
     * @param appKey Chaîne hexadécimale de 32 caractères.
     */
    public void setAppKey(String appKey) {
        String message = "mac set appkey " + appKey + "\r\n";
        super.ecrire(message.getBytes());
    }

    /**
     * 2.4.8.2 Définit l'Application Session Key (ABP).
     */
    public void setAppSKey(String appSKey) {
        String message = "mac set appskey " + appSKey + "\r\n";
        super.ecrire(message.getBytes());
    }

    /**
     * 2.4.8.8 Définit le Device EUI (Identifiant unique du device).
     * Souvent requis pour l'OTAA si tu n'utilises pas le HWEUI interne.
     */
    public void setDevEui(String devEui) {
        String message = "mac set deveui " + devEui + "\r\n";
        super.ecrire(message.getBytes());
    }

    /**
     * 2.4.8.17 Définit la Network Session Key (ABP).
     */
    public void setNwkSKey(String nwkSKey) {
        String message = "mac set nwkskey " + nwkSKey + "\r\n";
        super.ecrire(message.getBytes());
    }

    /**
     * 2.4.8.7 Définit l'adresse du device (ABP).
     */
    public void setDevAddr(String devAddr) {
        String message = "mac set devaddr " + devAddr + "\r\n";
        super.ecrire(message.getBytes());
    }

    /**
     * 2.4.8.10 Définit le Data Rate (vitesse de transmission / SF).
     * @param dr Index du Data Rate (0-5 généralement pour EU868).
     */
    public void setDataRate(int dr) {
        String message = "mac set dr " + dr + "\r\n";
        super.ecrire(message.getBytes());
    }

    /**
     * 2.4.8.3 Active ou désactive l'Adaptive Data Rate (ADR).
     * @param state "on" ou "off"
     */
    public void setAdr(String state) {
        String message = "mac set ar " + state + "\r\n"; // Attention c'est 'ar' pas 'adr' dans la commande set
        super.ecrire(message.getBytes());
    }

    // --- 2.4 MAC Actions ---

    /**
     * 2.4.3 Rejoint le réseau LoRaWAN.
     * @param mode "otaa" ou "abp"
     */
    public void macJoin(String mode) {
        // Le module répondra d'abord "ok", puis plus tard "accepted" ou "denied".
        String message = "mac join " + mode + "\r\n";
        super.ecrire(message.getBytes());
    }

    /**
     * 2.4.4 Sauvegarde la configuration MAC dans l'EEPROM.
     * Important à faire après un Join réussi pour garder la session après un reboot.
     */
    public void macSave() {
        super.ecrire("mac save\r\n".getBytes());
    }

    /**
     * 2.4.2 Envoie des données (Uplink).
     * @param type "cnf" (confirmé) ou "uncnf" (non confirmé)
     * @param port Port de l'application (1-223)
     * @param data Payload en hexadécimal
     */
    public void macTx(String type, int port, String data) {
        // Syntaxe: mac tx <type> <portno> <data>
        // Le module répond "ok", puis "mac_tx_ok" (ou err) une fois l'envoi fini.
        String message = "mac tx " + type + " " + port + " " + data + "\r\n";
        super.ecrire(message.getBytes());
    }

    /**
     * Surcharge utilitaire pour envoyer du texte brut (converti en Hex).
     */
    public void macTxText(String type, int port, String text) {
        StringBuilder hex = new StringBuilder();
        for (char c : text.toCharArray()) {
            hex.append(String.format("%02X", (int) c));
        }
        macTx(type, port, hex.toString());
    }

    // --- 2.5 Radio Commands (Point-to-Point) ---

    public void macPause() {
        super.ecrire("mac pause\r\n".getBytes());
    }

    public void macResume() {
        super.ecrire("mac resume\r\n".getBytes());
    }

    /**
     * 2.5.2 Envoie un paquet brut LoRa.
     * @param data Payload hexadécimal
     */
    public void radioTx(String data) {
        String message = "radio tx " + data + "\r\n";
        super.ecrire(message.getBytes());
    }

    // JoinEUI sur TTN
    public void getAppEUI() {
        super.ecrire("mac get appeui\r\n".getBytes());
    }

    public void getDevEUI() {
        super.ecrire("mac get deveui\r\n".getBytes());
    }

    public void getMacStatus() {
        super.ecrire("mac get status\r\n".getBytes());
    }

    public void getRadioFrequency() {
        super.ecrire("radio get freq\r\n".getBytes());
    }

    public void setRadio868Mhz() {
        super.ecrire("radio set freq 868000000\r\n".getBytes());
    }

    public void resetWithBand(String band) {
        var message = "mac reset "+band+"\r\n";
        super.ecrire(message.getBytes());
    }
}
