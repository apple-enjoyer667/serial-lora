package com.mtgprod;

import jssc.SerialPortException;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) {
        var lora = new LoraConfigurator();

        lora.startConnection();

        //lora.getSystemVersion();
        //lora.getVdd();
        lora.getRadioFrequency();

    }
}