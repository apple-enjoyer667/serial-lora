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
     * 2.3.1 sys sleep <length>
     * @param length decimal number representing the number of milliseconds the system is put to sleep, from 100 to 4294967296.
     * @return ok after the system gets back from Sleep mode, or invalid_param if the length is not valid.
     */
    public void sysSleep(String length) {
        var message = "sys sleep " + length + "\r\n";
        super.ecrire(message.getBytes());
    }

    /**
     * 2.3.2 sys reset
     * @return RN2483 X.Y.Z MMM DD YYYY HH:MM:SS, where X.Y.Z is firmware version, MMM is month, DD is day, YYYY is year, HH:MM:SS is hour, minutes, seconds.
     */
    public void sysReset() {
        super.ecrire("sys reset\r\n".getBytes());
    }

    /**
     * 2.3.3 sys eraseFW
     * @return no response.
     */
    public void sysEraseFW() {
        super.ecrire("sys eraseFW\r\n".getBytes());
    }

    /**
     * 2.3.4 sys factoryRESET
     * @return RN2483 X.Y.Z MMM DD YYYY HH:MM:SS, where X.Y.Z is firmware version, MMM is month, DD is day, YYYY is year, HH:MM:SS is hour, minutes, seconds.
     */
    public void sysFactoryReset() {
        super.ecrire("sys factoryRESET\r\n".getBytes());
    }

    /**
     * 2.3.5.1 sys set nvm <address> <data>
     * @param address hexadecimal number representing user EEPROM address, from 300 to 3FF.
     * @param data hexadecimal number representing data, from 00 to FF.
     * @return ok if the parameters (address and data) are valid, or invalid_param if the parameters (address and data) are not valid.
     */
    public void sysSetNvm(String address, String data) {
        var message = "sys set nvm " + address + " " + data + "\r\n";
        super.ecrire(message.getBytes());
    }

    /**
     * 2.3.5.2 sys set pindig <pinName> <pinState>
     * @param pinName string representing the pin. Parameter can be: GPIO0 - GPIO13, UART_CTS, UART_RTS, TEST0, TEST1
     * @param pinState decimal number representing the state. Parameter values can be: 0 or 1.
     * @return ok if the parameters (<pinname>, <pinstate>) are valid, or invalid_param if the parameters (<pinname>, <pinstate>) are not valid
     */
    public void sysSetPindig(String pinName, String pinState) {
        var message = "sys set pindig " + pinName + " " + pinState + "\r\n";
        super.ecrire(message.getBytes());
    }

    /**
     * 2.3.5.3 sys set pinmode <pinname> <pinmode>
     * @param pinName string representing the pin. Parameters can be: GPIO0 - GPIO13, UART_CTS, UART_RTS, TEST0, TEST1.
     * @param pinMode string representing the functional mode of the pin. Parameters can be: digout, digin or ana.
     * @return ok if all the parameters are valid, or invalid_param if any of the parameters are not valid.
     */
    public void sysSetPinmode(String pinName, String pinMode) {
        var message = "sys set pinmode " + pinName + " " + pinMode + "\r\n";
        super.ecrire(message.getBytes());
    }

    /**
     * 2.3.6.1 sys get ver
     * @return RN2483 X.Y.Z MMM DD YYYY HH:MM:SS, where X.Y.Z is firmware version, MMM is month, DD is day, YYYY is year, HH:MM:SS is hour, minutes, seconds.
     */
    public void sysGetVer() {
        super.ecrire("sys get ver\r\n".getBytes());
    }

    /**
     * 2.3.6.2 sys get nvm <address>
     * @param address hexadecimal number representing user EEPROM address, from 300 to 3FF.
     * @return 00-FF (hexadecimal value from 00 to FF) if the address is valid, or invalid_param if the address is not valid.
     */
    public void sysGetNvm(String address) {
        var message = "sys get nvm " + address + "\r\n";
        super.ecrire(message.getBytes());
    }

    /**
     * 2.3.6.3 sys get vdd
     * @return 0-3600 (decimal value from 0 to 3600).
     */
    public void sysGetVdd() {
        super.ecrire("sys get vdd\r\n".getBytes());
    }

    /**
     * 2.3.6.4 sys get hweui
     * @return hexadecimal number representing the preprogrammed EUI node address.
     */
    public void sysGetHweui() {
        super.ecrire("sys get hweui\r\n".getBytes());
    }

    /**
     * 2.3.6.5 sys get pindig <pinname>
     * @param pinName string representing the pin. Parameters can be: GPIO0 - GPIO13, UART_CTS, UART_RTS, TEST0, TEST1.
     * @return decimal number representing the state (either 0 or 1)
     */
    public void sysGetPindig(String pinName) {
        var message = "sys get pindig " + pinName + "\r\n";
        super.ecrire(message.getBytes());
    }

    /**
     * 2.3.6.6 sys get pinana <pinname>
     * @param pinName string representing the pin. Parameters can be: GPIO0 - GPIO3, GPIO5-GPIO13.
     * @return decimal number representing the result of the conversion, from 0 to 1023, where 0 represents 0V and 1023 is VDD, the supply voltage of the module.
     */
    public void sysGetPinana(String pinName) {
        var message = "sys get pinana " + pinName + "\r\n";
        super.ecrire(message.getBytes());
    }

    // --- 2.4.8 MAC Set Commands ---

    /**
     * 2.4.1 mac reset <band>
     * @param band decimal number representing the frequency band, either 868 or 433[cite: 463, 464].
     * @return ok if band is valid, or invalid_param if band is not valid[cite: 465, 466].
     */
    public void macReset(String band) {
        var message = "mac reset " + band + "\r\n";
        super.ecrire(message.getBytes());
    }

    /**
     * 2.4.2 mac tx <type> <portno> <data>
     * @param type string representing the uplink payload type, either cnf or uncnf (cnf - confirmed, uncnf - unconfirmed)[cite: 479, 480].
     * @param portno decimal number representing the port number, from 1 to 223[cite: 481].
     * @param data hexadecimal value. The length of <data> bytes capable of being transmitted are dependent upon the set data rate[cite: 481, 482].
     * @return ok if parameters and configurations are valid and the packet was forwarded to the radio transceiver for transmission (followed by a second reply after transmission)[cite: 483, 484, 487].
     */
    public void macTx(String type, String portno, String data) {
        var message = "mac tx " + type + " " + portno + " " + data + "\r\n";
        super.ecrire(message.getBytes());
    }

    /**
     * 2.4.3 mac join <mode>
     * @param mode string representing the join procedure type (case-insensitive), either otaa or abp (otaa - over-the-air activation, abp - activation by personalization)[cite: 533, 534, 535].
     * @return ok - if parameters and configurations are valid and the join request packet was forwarded to the radio transceiver for transmission[cite: 540].
     */
    public void macJoin(String mode) {
        var message = "mac join " + mode + "\r\n";
        super.ecrire(message.getBytes());
    }

    /**
     * 2.4.4 mac save
     * @return ok[cite: 560].
     */
    public void macSave() {
        super.ecrire("mac save\r\n".getBytes());
    }

    /**
     * 2.4.5 mac forceENABLE
     * @return ok[cite: 592].
     */
    public void macForceEnable() {
        super.ecrire("mac forceENABLE\r\n".getBytes());
    }

    /**
     * 2.4.6 mac pause
     * @return 0-4294967295 (decimal number representing the number of milliseconds the mac can be paused)[cite: 602].
     */
    public void macPause() {
        super.ecrire("mac pause\r\n".getBytes());
    }

    /**
     * 2.4.7 mac resume
     * @return ok[cite: 616].
     */
    public void macResume() {
        super.ecrire("mac resume\r\n".getBytes());
    }

    /**
     * 2.4.8.1 mac set appkey <appKey>
     * @param appKey 16-byte hexadecimal number representing the application key[cite: 628, 629].
     * @return ok if key is valid, or invalid_param if key is not valid[cite: 630, 631].
     */
    public void macSetAppkey(String appKey) {
        var message = "mac set appkey " + appKey + "\r\n";
        super.ecrire(message.getBytes());
    }

    /**
     * 2.4.8.2 mac set appskey <appSessKey>
     * @param appSessKey 16-byte hexadecimal number representing the application session key[cite: 640, 641].
     * @return ok if key is valid, or invalid_param if key is not valid[cite: 643, 644].
     */
    public void macSetAppskey(String appSessKey) {
        var message = "mac set appskey " + appSessKey + "\r\n";
        super.ecrire(message.getBytes());
    }

    /**
     * 2.4.8.3 mac set ar <state>
     * @param state string value representing the state, either on or off[cite: 650, 651].
     * @return ok if state is valid, or invalid_param if state is not valid[cite: 652, 653].
     */
    public void macSetAr(String state) {
        var message = "mac set ar " + state + "\r\n";
        super.ecrire(message.getBytes());
    }

    /**
     * 2.4.8.4 mac set bat <level>
     * @param level decimal number representing the level of the battery, from 0 to 255. '0' means external power, '1' means low level, 254 means high level, 255 means the end device was not able to measure the battery level[cite: 662, 663].
     * @return ok if the battery level is valid, or invalid_param if the battery level is not valid[cite: 664, 665].
     */
    public void macSetBat(String level) {
        var message = "mac set bat " + level + "\r\n";
        super.ecrire(message.getBytes());
    }

    /**
     * 2.4.8.5.1 mac set ch freq <channelID> <frequency>
     * @param channelID decimal number representing the channel number, from 3 to 15[cite: 678, 679].
     * @param frequency decimal number representing the frequency, from 863000000 to 870000000 or from 433050000 to 434790000, in Hz[cite: 679].
     * @return ok if parameters are valid, or invalid_param if parameters are not valid[cite: 680, 681].
     */
    public void macSetChFreq(String channelID, String frequency) {
        var message = "mac set ch freq " + channelID + " " + frequency + "\r\n";
        super.ecrire(message.getBytes());
    }

    /**
     * 2.4.8.5.2 mac set ch dcycle <channelID> <dutyCycle>
     * @param channelID decimal number representing the channel number, from 0 to 15[cite: 687, 688].
     * @param dutyCycle decimal number representing the duty cycle, from 0 to 65535[cite: 688].
     * @return ok if parameters are valid, or invalid_param if parameters are not valid[cite: 688, 689].
     */
    public void macSetChDcycle(String channelID, String dutyCycle) {
        var message = "mac set ch dcycle " + channelID + " " + dutyCycle + "\r\n";
        super.ecrire(message.getBytes());
    }

    /**
     * 2.4.8.5.3 mac set ch drrange <channelID> <minRange> <maxRange>
     * @param channelID decimal number representing the channel number, from 0 to 15[cite: 698, 699].
     * @param minRange decimal number representing the minimum data rate, from 0 to 7[cite: 699].
     * @param maxRange decimal number representing the maximum data rate, from 0 to 7[cite: 699].
     * @return ok if parameters are valid, or invalid_param if parameters are not valid[cite: 699, 700].
     */
    public void macSetChDrrange(String channelID, String minRange, String maxRange) {
        var message = "mac set ch drrange " + channelID + " " + minRange + " " + maxRange + "\r\n";
        super.ecrire(message.getBytes());
    }

    /**
     * 2.4.8.5.4 mac set ch status <channelID> <status>
     * @param channelID decimal number representing the channel number, from 0 to 15[cite: 709, 710].
     * @param status string value representing the state, either on or off[cite: 710, 711].
     * @return ok if parameters are valid, or invalid_param if parameters are not valid[cite: 712, 713].
     */
    public void macSetChStatus(String channelID, String status) {
        var message = "mac set ch status " + channelID + " " + status + "\r\n";
        super.ecrire(message.getBytes());
    }

    /**
     * 2.4.8.6 mac set class <class>
     * @param deviceClass A letter representing the LoRaWAN device class, either a or c[cite: 724, 725].
     * @return ok if class is valid, or invalid_param if the class is not valid[cite: 725, 726].
     */
    public void macSetClass(String deviceClass) {
        var message = "mac set class " + deviceClass + "\r\n";
        super.ecrire(message.getBytes());
    }

    /**
     * 2.4.8.7 mac set devaddr <address>
     * @param address 4-byte hexadecimal number representing the device address, from 00000000 - FFFFFFFF[cite: 734, 735, 736].
     * @return ok if address is valid, or invalid_param if address is not valid[cite: 737, 738].
     */
    public void macSetDevaddr(String address) {
        var message = "mac set devaddr " + address + "\r\n";
        super.ecrire(message.getBytes());
    }

    /**
     * 2.4.8.8 mac set deveui <devEUI>
     * @param devEUI 8-byte hexadecimal number representing the device EUI[cite: 744, 745].
     * @return ok if address is valid, or invalid_param if address is not valid[cite: 746, 747].
     */
    public void macSetDeveui(String devEUI) {
        var message = "mac set deveui " + devEUI + "\r\n";
        super.ecrire(message.getBytes());
    }

    /**
     * 2.4.8.9 mac set dnctr <fCntDown>
     * @param fCntDown decimal number representing the value of the downlink frame counter that will be used for the next downlink reception, from 0 to 4294967295[cite: 755, 756, 757].
     * @return ok if parameter is valid, or invalid_param if parameter is not valid[cite: 758, 759].
     */
    public void macSetDnctr(String fCntDown) {
        var message = "mac set dnctr " + fCntDown + "\r\n";
        super.ecrire(message.getBytes());
    }

    /**
     * 2.4.8.10 mac set dr <dataRate>
     * @param dataRate decimal number representing the data rate, from 0 and 7, but within the limits of the data rate range for the defined channels[cite: 764, 765, 766].
     * @return ok if data rate is valid, or invalid_param if data rate is not valid[cite: 767, 768].
     */
    public void macSetDr(String dataRate) {
        var message = "mac set dr " + dataRate + "\r\n";
        super.ecrire(message.getBytes());
    }

    /**
     * 2.4.8.11 mac set linkchk <linkCheck>
     * @param linkCheck decimal number that sets the time interval in seconds for the link check process, from 0 to 65535[cite: 776, 777].
     * @return ok if the time interval is valid, or invalid_param if the time interval is not valid[cite: 778, 779].
     */
    public void macSetLinkchk(String linkCheck) {
        var message = "mac set linkchk " + linkCheck + "\r\n";
        super.ecrire(message.getBytes());
    }

    /**
     * 2.4.8.12 mac set mcast <state>
     * @param state string value representing the state, either on or off[cite: 790, 791].
     * @return ok if state is valid, or invalid_param if the state is not valid[cite: 791, 792].
     */
    public void macSetMcast(String state) {
        var message = "mac set mcast " + state + "\r\n";
        super.ecrire(message.getBytes());
    }

    /**
     * 2.4.8.13 mac set mcastappskey <mcastApplicationSessionkey>
     * @param mcastApplicationSessionkey 16-byte hexadecimal number representing the application session key[cite: 798, 799, 801].
     * @return ok if key is valid, or invalid_param if the key is not valid[cite: 800, 802].
     */
    public void macSetMcastappskey(String mcastApplicationSessionkey) {
        var message = "mac set mcastappskey " + mcastApplicationSessionkey + "\r\n";
        super.ecrire(message.getBytes());
    }

    /**
     * 2.4.8.14 mac set mcastdevaddr <mcastAddress>
     * @param mcastAddress 4-byte hexadecimal number representing the device multicast address, from 00000000 - FFFFFFFF[cite: 807, 808, 809].
     * @return ok if address is valid, or invalid_param if the address is not valid[cite: 810, 811].
     */
    public void macSetMcastdevaddr(String mcastAddress) {
        var message = "mac set mcastdevaddr " + mcastAddress + "\r\n";
        super.ecrire(message.getBytes());
    }

    /**
     * 2.4.8.15 mac set mcastdnctr <fMcastCntDown>
     * @param fMcastCntDown decimal number representing the value of the multicast downlink frame counter from 0 to 4294967295[cite: 819, 820, 821].
     * @return ok if parameter is valid, or invalid_param if the parameter is not valid[cite: 822, 823].
     */
    public void macSetMcastdnctr(String fMcastCntDown) {
        var message = "mac set mcastdnctr " + fMcastCntDown + "\r\n";
        super.ecrire(message.getBytes());
    }

    /**
     * 2.4.8.16 mac set mcastnwkskey <mcastNetworkSessionkey>
     * @param mcastNetworkSessionkey 16-byte hexadecimal number representing the network session key[cite: 828, 829, 831].
     * @return ok if key is valid, or invalid_param if the key is not valid[cite: 830, 832].
     */
    public void macSetMcastnwkskey(String mcastNetworkSessionkey) {
        var message = "mac set mcastnwkskey " + mcastNetworkSessionkey + "\r\n";
        super.ecrire(message.getBytes());
    }

    /**
     * 2.4.8.17 mac set nwkskey <nwkSessKey>
     * @param nwkSessKey 16-byte hexadecimal number representing the network session key[cite: 838, 839].
     * @return ok if key is valid, or invalid_param if key is not valid[cite: 839, 840].
     */
    public void macSetNwkskey(String nwkSessKey) {
        var message = "mac set nwkskey " + nwkSessKey + "\r\n";
        super.ecrire(message.getBytes());
    }

    /**
     * 2.4.8.18 mac set pwridx <pwrIndex>
     * @param pwrIndex decimal number representing the index value for the output power, from 0 to 5 for 433 MHz frequency band and from 1 to 5 for 868 MHz frequency band[cite: 848, 849, 850].
     * @return ok if power index is valid, or invalid_param if power index is not valid[cite: 851, 852].
     */
    public void macSetPwridx(String pwrIndex) {
        var message = "mac set pwridx " + pwrIndex + "\r\n";
        super.ecrire(message.getBytes());
    }

    /**
     * 2.4.8.19 mac set retx <reTxNb>
     * @param reTxNb decimal number representing the number of retransmissions for an uplink confirmed packet, from 0 to 255[cite: 858, 859].
     * @return ok if <retx> is valid, or invalid_param if <retx> is not valid[cite: 860, 861].
     */
    public void macSetRetx(String reTxNb) {
        var message = "mac set retx " + reTxNb + "\r\n";
        super.ecrire(message.getBytes());
    }

    /**
     * 2.4.8.20 mac set rx2 <dataRate> <frequency>
     * @param dataRate decimal number representing the data rate, from 0 to 7[cite: 866].
     * @param frequency decimal number representing the frequency, from 863000000 to 870000000 or from 433050000 to 434790000, in Hz[cite: 867, 868].
     * @return ok if parameters are valid, or invalid_param if parameters are not valid[cite: 869, 870].
     */
    public void macSetRx2(String dataRate, String frequency) {
        var message = "mac set rx2 " + dataRate + " " + frequency + "\r\n";
        super.ecrire(message.getBytes());
    }

    /**
     * 2.4.8.21 mac set rxdelay1 <rxDelay>
     * @param rxDelay decimal number representing the delay between the transmission and the first Reception window in milliseconds, from 0 to 65535[cite: 880, 881].
     * @return ok if <rxDelay> is valid, or invalid_param if <rxDelay> is not valid[cite: 882, 883].
     */
    public void macSetRxdelay1(String rxDelay) {
        var message = "mac set rxdelay1 " + rxDelay + "\r\n";
        super.ecrire(message.getBytes());
    }

    /**
     * 2.4.8.22 mac set sync <synchWord>
     * @param synchWord one byte long hexadecimal number representing the synchronization word for the LoRaWAN communication[cite: 889, 890].
     * @return ok if parameters are valid, or invalid_param if parameter is not valid[cite: 891, 892].
     */
    public void macSetSync(String synchWord) {
        var message = "mac set sync " + synchWord + "\r\n";
        super.ecrire(message.getBytes());
    }

    /**
     * 2.4.8.23 mac set upctr <fCntUp>
     * @param fCntUp decimal number representing the value of the uplink frame counter that will be used for the next uplink transmission, from 0 to 4294967295[cite: 897, 898].
     * @return ok if parameter is valid, or invalid_param if parameter is not valid[cite: 899, 900].
     */
    public void macSetUpctr(String fCntUp) {
        var message = "mac set upctr " + fCntUp + "\r\n";
        super.ecrire(message.getBytes());
    }

    /**
     * 2.4.9.1 mac get adr
     * @return string representing the state of the adaptive data rate mechanism, either on or off[cite: 913, 914].
     */
    public void macGetAdr() {
        super.ecrire("mac get adr\r\n".getBytes());
    }

    /**
     * 2.4.9.2 mac get appeui
     * @return 8-byte hexadecimal number representing the application EUI[cite: 921].
     */
    public void macGetAppeui() {
        super.ecrire("mac get appeui\r\n".getBytes());
    }

    /**
     * 2.4.9.3 mac get ar
     * @return string representing the state of the automatic reply, either on or off[cite: 931].
     */
    public void macGetAr() {
        super.ecrire("mac get ar\r\n".getBytes());
    }

    /**
     * 2.4.9.4.1 mac get ch freq <channelID>
     * @param channelID decimal number representing the channel number, from 0 to 15[cite: 948, 949].
     * @return decimal number representing the frequency of the channel, in Hz[cite: 950, 951].
     */
    public void macGetChFreq(String channelID) {
        var message = "mac get ch freq " + channelID + "\r\n";
        super.ecrire(message.getBytes());
    }

    /**
     * 2.4.9.4.2 mac get ch dcycle <channelID>
     * @param channelID decimal number representing the channel number, from 0 to 15[cite: 956, 957].
     * @return decimal number representing the duty cycle of the channel, from 0 to 65535[cite: 957, 958].
     */
    public void macGetChDcycle(String channelID) {
        var message = "mac get ch dcycle " + channelID + "\r\n";
        super.ecrire(message.getBytes());
    }

    /**
     * 2.4.9.4.3 mac get ch drrange <channelID>
     * @param channelID decimal number representing the channel number, from 0 to 15[cite: 964, 965].
     * @return decimal number representing the minimum and maximum data rates of the channel[cite: 965, 966].
     */
    public void macGetChDrrange(String channelID) {
        var message = "mac get ch drrange " + channelID + "\r\n";
        super.ecrire(message.getBytes());
    }

    /**
     * 2.4.9.4.4 mac get ch status <channelID>
     * @param channelID decimal number representing the channel number, from 0 to 15[cite: 972, 973].
     * @return string representing the state of the channel, either on or off[cite: 973].
     */
    public void macGetChStatus(String channelID) {
        var message = "mac get ch status " + channelID + "\r\n";
        super.ecrire(message.getBytes());
    }

    /**
     * 2.4.9.5 mac get class
     * @return A single letter A or C[cite: 985].
     */
    public void macGetClass() {
        super.ecrire("mac get class\r\n".getBytes());
    }

    /**
     * 2.4.9.6 mac get dcycleps
     * @return decimal number representing the prescaler value, from 0 to 65535[cite: 990].
     */
    public void macGetDcycleps() {
        super.ecrire("mac get dcycleps\r\n".getBytes());
    }

    /**
     * 2.4.9.7 mac get devaddr
     * @return 4-byte hexadecimal number representing the device address, from 00000000 to FFFFFFFF[cite: 997, 998].
     */
    public void macGetDevaddr() {
        super.ecrire("mac get devaddr\r\n".getBytes());
    }

    /**
     * 2.4.9.8 mac get deveui
     * @return 8-byte hexadecimal number representing the device EUI[cite: 1003].
     */
    public void macGetDeveui() {
        super.ecrire("mac get deveui\r\n".getBytes());
    }

    /**
     * 2.4.9.9 mac get dnctr
     * @return decimal number representing the value of the downlink frame counter, from 0 to 4294967295[cite: 1009, 1010].
     */
    public void macGetDnctr() {
        super.ecrire("mac get dnctr\r\n".getBytes());
    }

    /**
     * 2.4.9.10 mac get dr
     * @return decimal number representing the current data rate[cite: 1015].
     */
    public void macGetDr() {
        super.ecrire("mac get dr\r\n".getBytes());
    }

    /**
     * 2.4.9.11 mac get gwnb
     * @return decimal number representing the number of gateways, from 0 to 255[cite: 1021].
     */
    public void macGetGwnb() {
        super.ecrire("mac get gwnb\r\n".getBytes());
    }

    /**
     * 2.4.9.12 mac get mcast
     * @return string representing the Multicast state of the module, either on or off[cite: 1027].
     */
    public void macGetMcast() {
        super.ecrire("mac get mcast\r\n".getBytes());
    }

    /**
     * 2.4.9.13 mac get mcastdevaddr
     * @return 4-byte hexadecimal number representing the device multicast address[cite: 1030, 1031].
     */
    public void macGetMcastdevaddr() {
        super.ecrire("mac get mcastdevaddr\r\n".getBytes());
    }

    /**
     * 2.4.9.14 mac get mcastdnctr
     * @return decimal number representing the value of the downlink frame counter, from 0 to 4294967295[cite: 1035, 1036].
     */
    public void macGetMcastdnctr() {
        super.ecrire("mac get mcastdnctr\r\n".getBytes());
    }

    /**
     * 2.4.9.15 mac get mrgn
     * @return decimal number representing the demodulation margin, from 0 to 255[cite: 1039, 1040].
     */
    public void macGetMrgn() {
        super.ecrire("mac get mrgn\r\n".getBytes());
    }

    /**
     * 2.4.9.16 mac get pwridx
     * @return decimal number representing the current output power index value, from 0 to 5[cite: 1044, 1045].
     */
    public void macGetPwridx() {
        super.ecrire("mac get pwridx\r\n".getBytes());
    }

    /**
     * 2.4.9.17 mac get retx
     * @return decimal number representing the number of retransmissions, from 0 to 255[cite: 1052, 1053].
     */
    public void macGetRetx() {
        super.ecrire("mac get retx\r\n".getBytes());
    }

    /**
     * 2.4.9.18 mac get rx2 <freqBand>
     * @param freqBand decimal number representing the frequency band, either 868 or 433[cite: 1057, 1058].
     * @return decimal number representing the data rate and frequency configured for the second Receive window[cite: 1058, 1059].
     */
    public void macGetRx2(String freqBand) {
        var message = "mac get rx2 " + freqBand + "\r\n";
        super.ecrire(message.getBytes());
    }

    /**
     * 2.4.9.19 mac get rxdelay1
     * @return decimal number representing the interval, in milliseconds, for rxdelay1, from 0 to 65535[cite: 1065, 1066, 1067].
     */
    public void macGetRxdelay1() {
        super.ecrire("mac get rxdelay1\r\n".getBytes());
    }

    /**
     * 2.4.9.20 mac get rxdelay2
     * @return decimal number representing the interval, in milliseconds, for rxdelay2, from 0 to 65535[cite: 1071, 1072, 1073].
     */
    public void macGetRxdelay2() {
        super.ecrire("mac get rxdelay2\r\n".getBytes());
    }

    /**
     * 2.4.9.21 mac get status
     * @return 4-byte hexadecimal number representing the current status of the module[cite: 1078].
     */
    public void macGetStatus() {
        super.ecrire("mac get status\r\n".getBytes());
    }

    /**
     * 2.4.9.22 mac get sync
     * @return one byte long hexadecimal number representing the synchronization word for the LoRaWAN communication[cite: 1084, 1085, 1086].
     */
    public void macGetSync() {
        super.ecrire("mac get sync\r\n".getBytes());
    }

    /**
     * 2.4.9.23 mac get upctr
     * @return decimal number representing the value of the uplink frame counter that will be used for the next uplink transmission, from 0 to 4294967295[cite: 1091, 1092].
     */
    public void macGetUpctr() {
        super.ecrire("mac get upctr\r\n".getBytes());
    }

    // envoie les données au uplink
    public void macTx(String type, int port, String data) {
        // Syntaxe: mac tx <type> <portno> <data>
        // Le module répond "ok", puis "mac_tx_ok" (ou err) une fois l'envoi fini.
        String message = "mac tx " + type + " " + port + " " + data + "\r\n";
        super.ecrire(message.getBytes());
    }

    // envoi du text brute (converti en hexa)
    public void macTxText(String type, int port, String text) {
        StringBuilder hex = new StringBuilder();
        for (char c : text.toCharArray()) {
            hex.append(String.format("%02X", (int) c));
        }
        macTx(type, port, hex.toString());
    }

    // --- 2.5 Commandes radio ---

    /**
     * 2.5.1 radio rx <rxWindowSize>
     * @param rxWindowSize decimal number representing the number of symbols (for LoRa modulation) or time-out (in milliseconds, for FSK modulation) that the receiver will be opened, from 0 to 65535. Set to '0' to enable Continuous Reception mode.
     * @return ok if parameter is valid and the transceiver is configured in Receive mode, invalid_param if parameter is not valid, or busy if the transceiver is currently busy.
     */
    public void radioRx(String rxWindowSize) {
        var message = "radio rx " + rxWindowSize + "\r\n";
        super.ecrire(message.getBytes());
    }

    /**
     * 2.5.2 radio tx <data>
     * @param data hexadecimal value representing the data to be transmitted, from 0 to 255 bytes for LoRa modulation and from 0 to 64 bytes for FSK modulation.
     * @return ok if parameter is valid and the transceiver is configured in Transmit mode, invalid_param if parameter is not valid, or busy if the transceiver is currently busy.
     */
    public void radioTx(String data) {
        var message = "radio tx " + data + "\r\n";
        super.ecrire(message.getBytes());
    }

    /**
     * 2.5.3 radio cw <state>
     * @param state string representing the state of the Continuous Wave (CW) mode, either on or off.
     * @return ok if state is valid, or invalid_param if state is not valid.
     */
    public void radioCw(String state) {
        var message = "radio cw " + state + "\r\n";
        super.ecrire(message.getBytes());
    }

    /**
     * 2.5.4 rxstop
     * @return ok.
     */
    public void radioRxstop() {
        super.ecrire("radio rxstop\r\n".getBytes());
    }

    /**
     * 2.5.5.1 radio set afcbw <autoFreqBand>
     * @param autoFreqBand float representing the automatic frequency correction, in kHz.
     * @return ok if the automatic frequency correction is valid, or invalid_param if the automatic frequency correction is not valid.
     */
    public void radioSetAfcbw(String autoFreqBand) {
        var message = "radio set afcbw " + autoFreqBand + "\r\n";
        super.ecrire(message.getBytes());
    }

    /**
     * 2.5.5.2 radio set bitrate <fskBitRate>
     * @param fskBitRate decimal number representing the FSK bit rate value, from 1 to 300000.
     * @return ok if the bit rate value is valid, or invalid_param if the bit rate value is not valid.
     */
    public void radioSetBitrate(String fskBitRate) {
        var message = "radio set bitrate " + fskBitRate + "\r\n";
        super.ecrire(message.getBytes());
    }

    /**
     * 2.5.5.3 radio set bt <gfBT>
     * @param gfBT string representing the Gaussian baseband data shaping, enabling GFSK modulation. Parameter values can be: none, 1.0, 0.5, 0.3.
     * @return ok if the data shaping is valid, or invalid_param if the data shaping is not valid.
     */
    public void radioSetBt(String gfBT) {
        var message = "radio set bt " + gfBT + "\r\n";
        super.ecrire(message.getBytes());
    }

    /**
     * 2.5.5.4 radio set bw <bandWidth>
     * @param bandWidth decimal representing the operating radio bandwidth, in kHz. Parameter values can be: 125, 250, 500.
     * @return ok if the bandwidth is valid, or invalid_param if the bandwidth is not valid.
     */
    public void radioSetBw(String bandWidth) {
        var message = "radio set bw " + bandWidth + "\r\n";
        super.ecrire(message.getBytes());
    }

    /**
     * 2.5.5.5 radio set cr <codingRate>
     * @param codingRate string representing the coding rate. Parameter values can be: 4/5, 4/6, 4/7, 4/8.
     * @return ok if the coding rate is valid, or invalid_param if the coding rate is not valid.
     */
    public void radioSetCr(String codingRate) {
        var message = "radio set cr " + codingRate + "\r\n";
        super.ecrire(message.getBytes());
    }

    /**
     * 2.5.5.6 radio set crc <crcHeader>
     * @param crcHeader string representing the state of the CRC header, either on or off.
     * @return ok if the state is valid, or invalid_param if the state is not valid.
     */
    public void radioSetCrc(String crcHeader) {
        var message = "radio set crc " + crcHeader + "\r\n";
        super.ecrire(message.getBytes());
    }

    /**
     * 2.5.5.7 radio set fdev <freqDev>
     * @param freqDev decimal number representing the frequency deviation, from 0 to 200000.
     * @return ok if the frequency deviation is valid, or invalid_param if frequency deviation is not valid.
     */
    public void radioSetFdev(String freqDev) {
        var message = "radio set fdev " + freqDev + "\r\n";
        super.ecrire(message.getBytes());
    }

    /**
     * 2.5.5.8 radio set freq <frequency>
     * @param frequency decimal representing the frequency, from 433050000 to 434790000 or from 863000000 to 870000000, in Hz.
     * @return ok if the frequency is valid, or invalid_param if the frequency is not valid.
     */
    public void radioSetFreq(String frequency) {
        var message = "radio set freq " + frequency + "\r\n";
        super.ecrire(message.getBytes());
    }

    /**
     * 2.5.5.9 radio set iqi <iqInvert>
     * @param iqInvert string representing the state of the invert IQ, either on or off.
     * @return ok if the state is valid, or invalid_param if the state is not valid.
     */
    public void radioSetIqi(String iqInvert) {
        var message = "radio set iqi " + iqInvert + "\r\n";
        super.ecrire(message.getBytes());
    }

    /**
     * 2.5.5.10 radio set mod <mode>
     * @param mode string representing the modulation method, either lora or fsk.
     * @return ok if the modulation is valid, or invalid_param if the modulation is not valid.
     */
    public void radioSetMod(String mode) {
        var message = "radio set mod " + mode + "\r\n";
        super.ecrire(message.getBytes());
    }

    /**
     * 2.5.5.11 radio set prlen <preamble>
     * @param preamble decimal number representing the preamble length, from 0 to 65535.
     * @return ok if the preamble length is valid, or invalid_param if the preamble length is not valid.
     */
    public void radioSetPrlen(String preamble) {
        var message = "radio set prlen " + preamble + "\r\n";
        super.ecrire(message.getBytes());
    }

    /**
     * 2.5.5.12 radio set pwr <pwrOut>
     * @param pwrOut signed decimal number representing the transceiver output power, from -3 to 15.
     * @return ok if the output power is valid, or invalid_param if the output power is not valid.
     */
    public void radioSetPwr(String pwrOut) {
        var message = "radio set pwr " + pwrOut + "\r\n";
        super.ecrire(message.getBytes());
    }

    /**
     * 2.5.5.13 radio set rxbw <rxBandwidth>
     * @param rxBandwidth float representing the signal bandwidth, in kHz.
     * @return ok if the signal bandwidth is valid, or invalid_param if signal bandwidth is not valid.
     */
    public void radioSetRxbw(String rxBandwidth) {
        var message = "radio set rxbw " + rxBandwidth + "\r\n";
        super.ecrire(message.getBytes());
    }

    /**
     * 2.5.5.14 radio set sf <spreadingFactor>
     * @param spreadingFactor string representing the spreading factor. Parameter values can be: sf7, sf8, sf9, sf10, sf11 or sf12.
     * @return ok if the spreading factor is valid, or invalid_param if the spreading factor is not valid.
     */
    public void radioSetSf(String spreadingFactor) {
        var message = "radio set sf " + spreadingFactor + "\r\n";
        super.ecrire(message.getBytes());
    }

    /**
     * 2.5.5.15 radio set sync <syncWord>
     * @param syncWord hexadecimal value representing the Sync word used during communication.
     * @return ok if the sync word is valid, or invalid_param if the sync word is not valid.
     */
    public void radioSetSync(String syncWord) {
        var message = "radio set sync " + syncWord + "\r\n";
        super.ecrire(message.getBytes());
    }

    /**
     * 2.5.5.16 radio set wdt <watchDog>
     * @param watchDog decimal number representing the time-out length for the Watchdog Timer, from 0 to 4294967295. Set to '0' to disable this functionality.
     * @return ok if the watchdog time-out is valid, or invalid_param if the watchdog time-out is not valid.
     */
    public void radioSetWdt(String watchDog) {
        var message = "radio set wdt " + watchDog + "\r\n";
        super.ecrire(message.getBytes());
    }

    /**
     * 2.5.6.1 radio get afcbw
     * @return float representing the automatic frequency correction band, in kHz.
     */
    public void radioGetAfcbw() {
        super.ecrire("radio get afcbw\r\n".getBytes());
    }

    /**
     * 2.5.6.2 radio get bitrate
     * @return signed decimal representing the configured bit rate, from 1 to 300000.
     */
    public void radioGetBitrate() {
        super.ecrire("radio get bitrate\r\n".getBytes());
    }

    /**
     * 2.5.6.3 radio get bt
     * @return string representing the configuration for data shaping.
     */
    public void radioGetBt() {
        super.ecrire("radio get bt\r\n".getBytes());
    }

    /**
     * 2.5.6.4 radio get bw
     * @return decimal representing the current operating radio bandwidth, in kHz.
     */
    public void radioGetBw() {
        super.ecrire("radio get bw\r\n".getBytes());
    }

    /**
     * 2.5.6.5 radio get cr
     * @return string representing the current value settings used for the coding rate.
     */
    public void radioGetCr() {
        super.ecrire("radio get cr\r\n".getBytes());
    }

    /**
     * 2.5.6.6 radio get crc
     * @return string representing the status of the CRC header, either on or off.
     */
    public void radioGetCrc() {
        super.ecrire("radio get crc\r\n".getBytes());
    }

    /**
     * 2.5.6.7 radio get fdev
     * @return signed decimal representing the frequency deviation setting, from 0 to 200000.
     */
    public void radioGetFdev() {
        super.ecrire("radio get fdev\r\n".getBytes());
    }

    /**
     * 2.5.6.8 radio get freq
     * @return decimal number representing the frequency, from 433050000 to 434790000 or from 863000000 to 870000000, in Hz.
     */
    public void radioGetFreq() {
        super.ecrire("radio get freq\r\n".getBytes());
    }

    /**
     * 2.5.6.9 radio get iqi
     * @return string representing the status of the Invert IQ functionality, either on or off.
     */
    public void radioGetIqi() {
        super.ecrire("radio get iqi\r\n".getBytes());
    }

    /**
     * 2.5.6.10 radio get mod
     * @return string representing the current mode of operation of the module, either lora or fsk.
     */
    public void radioGetMod() {
        super.ecrire("radio get mod\r\n".getBytes());
    }

    /**
     * 2.5.6.11 radio get prlen
     * @return signed decimal representing the preamble length, from 0 to 65535.
     */
    public void radioGetPrlen() {
        super.ecrire("radio get prlen\r\n".getBytes());
    }

    /**
     * 2.5.6.12 radio get pwr
     * @return signed decimal representing the current power level, from -3 to 15.
     */
    public void radioGetPwr() {
        super.ecrire("radio get pwr\r\n".getBytes());
    }

    /**
     * 2.5.6.13 radio get rssi
     * @return decimal representing the rssi for the last received frame.
     */
    public void radioGetRssi() {
        super.ecrire("radio get rssi\r\n".getBytes());
    }

    /**
     * 2.5.6.14 radio get rxbw
     * @return float representing the signal bandwidth, in kHz.
     */
    public void radioGetRxbw() {
        super.ecrire("radio get rxbw\r\n".getBytes());
    }

    /**
     * 2.5.6.15 radio get sf
     * @return string representing the current spreading factor.
     */
    public void radioGetSf() {
        super.ecrire("radio get sf\r\n".getBytes());
    }

    /**
     * 2.5.6.16 radio get snr
     * @return signed decimal number representing the signal-to-noise ratio (SNR), from -128 to 127.
     */
    public void radioGetSnr() {
        super.ecrire("radio get snr\r\n".getBytes());
    }

    /**
     * 2.5.6.17 radio get sync
     * @return hexadecimal number representing the synchronization word used for radio communication.
     */
    public void radioGetSync() {
        super.ecrire("radio get sync\r\n".getBytes());
    }

    /**
     * 2.5.6.18 radio get wdt
     * @return decimal number representing the length used for the watchdog time-out, from 0 to 4294967295.
     */
    public void radioGetWdt() {
        super.ecrire("radio get wdt\r\n".getBytes());
    }
}
