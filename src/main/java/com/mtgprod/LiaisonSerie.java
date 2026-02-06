package fr.btsciel.liaisonSerie;


import jssc.*;

import java.util.ArrayList;

/**
 * @author Michael
 */


public class LiaisonSerie implements SerialPortEventListener {

    public SerialPort serialPort;

    public LiaisonSerie() {
    }

    /**
     * Methode qui stocke les noms des ports disponibles dans un ArrayList
     *
     * @return le ArrayList
     */
    public ArrayList<String> listerLesPorts() {
        ArrayList<String> liste = new ArrayList<>();
        String[] listePorts = SerialPortList.getPortNames();
        for (int i = 0; i < listePorts.length; i++) {
            liste.add(listePorts[i].toString());
        }
        return liste;
    }


//******************************************************************************
//******************************************************************************

    @Override
    public void serialEvent(SerialPortEvent event) {
        System.out.println("reception");
    }


//******************************************************************************
//******************************************************************************

    /**
     * Methode qui initialise l'objet serialPort avec en paramétre
     * le portDeTravail
     */

    public void initCom(String portDeTravail) throws SerialPortException {
        this.serialPort = new SerialPort(portDeTravail);

    }


//******************************************************************************
//******************************************************************************

    /**
     * Méthode de configuration et ouvre la liaison série
     *
     * @param vitesse
     * @param donnees
     * @param parite
     * @param stop
     */
    public void configurerParametres(int vitesse, int donnees, int parite, int stop) {

        try {
            this.serialPort.openPort();//ouvre port com
            this.serialPort.setParams(vitesse, donnees, stop, parite);
            int mask = SerialPort.MASK_RXCHAR;
            //this.serialPort.setEventsMask(mask);//Set mask
            this.serialPort.addEventListener(this,mask);//Add SerialPortEventListener
        } catch (SerialPortException ex) {
            System.out.println(ex);
        }
    }

//******************************************************************************
//******************************************************************************

    /**
     * Méthode fermant le port série ouvert et les flux
     */
    public void fermerPort() {
        try {
            // fermeture du port
            if (this.serialPort.isOpened()) {
                this.serialPort.removeEventListener();
                this.serialPort.closePort();
            }
        } catch (SerialPortException ex) {
            System.out.println(ex);
        }
    }

//******************************************************************************
//******************************************************************************
    /**
     * Méthode qui lit dans le buffer de réception le nbs de bytes recus
     *
     */
    public int detecteSiReception() {
        int nbsOctect=0;
        try {
            nbsOctect=this.serialPort.getInputBufferBytesCount();
            //    System.out.println("getInputBufferBytesCount="+nbsOctect);
        } catch (SerialPortException ex) {
            System.out.println(ex);
        }
        return nbsOctect;
    }

//******************************************************************************
//******************************************************************************
    /**
     * Methode qui lit un octet sur le port
     *
     */
    public byte lire()  {
        byte octetLu[];
        try {

            octetLu = this.serialPort.readBytes(1);
            //System.out.println(new String(octetLu));
            return octetLu[0];
        } catch (SerialPortException ex) {
            System.out.println(ex);
        }
        return 0;
    }

//******************************************************************************
//******************************************************************************
    /**
     * Methode qui lit une trame d'octet sur le port
     *
     */
    public byte[] lireTrame(int longeur)  {
        try {
            byte []trameLue = new byte[longeur];
            trameLue=this.serialPort.readBytes(longeur);
            return trameLue;
        } catch (SerialPortException ex) {
            System.out.println(ex);
        }
        return null;
    }

    /******************************************************************************
     //******************************************************************************
     /**
     * Methode qui écrit un octet sur le port
     *
     */
    public void ecrire(byte[] b)  {
        try {
            this.serialPort.writeBytes(b);
        } catch (SerialPortException ex) {
            System.out.println(ex);
        }
    }

    public void ecrire(byte b) {
        try {
            this.serialPort.writeByte(b);
        } catch (SerialPortException ex) {
            System.out.println(ex);
        }
    }


}
