/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package zeroserverpi4j;

import com.pi4j.io.serial.Baud;
import com.pi4j.io.serial.DataBits;
import com.pi4j.io.serial.FlowControl;
import com.pi4j.io.serial.Parity;
import com.pi4j.io.serial.Serial;
import com.pi4j.io.serial.SerialConfig;
import com.pi4j.io.serial.SerialDataEvent;
import com.pi4j.io.serial.SerialDataEventListener;
import com.pi4j.io.serial.SerialFactory;
import com.pi4j.io.serial.SerialPort;
import com.pi4j.io.serial.StopBits;
import com.pi4j.util.Console;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author CMcMichael
 */
public class SerialStuff implements Runnable {
    
    static final byte[] MP_ZERO = {
        (byte) 0x00, (byte) 0x80, (byte) 0x00, (byte) 0x84, (byte) 0x3F, (byte) 0x88, (byte) 0x3F, (byte) 0x8E,
        (byte) 0x00, (byte) 0x90, (byte) 0x00, (byte) 0x94, (byte) 0x3F, (byte) 0x98, (byte) 0x3F, (byte) 0x9E,
        (byte) 0x00, (byte) 0xA0, (byte) 0x00, (byte) 0xA4, (byte) 0x3F, (byte) 0xA8, (byte) 0x3F, (byte) 0xAE,
        (byte) 0x00, (byte) 0xB0, (byte) 0x00, (byte) 0xB4, (byte) 0x3F, (byte) 0xB8, (byte) 0x3F, (byte) 0xBE,
        (byte) 0x00, (byte) 0xC0, (byte) 0x00, (byte) 0xC4, (byte) 0x3F, (byte) 0xC8, (byte) 0x3F, (byte) 0xCE,
        (byte) 0x00, (byte) 0xD0, (byte) 0x00, (byte) 0xD4, (byte) 0x3F, (byte) 0xD8, (byte) 0x3F, (byte) 0xDE,
        (byte) 0x00, (byte) 0xE0, (byte) 0x00, (byte) 0xE4, (byte) 0x3F, (byte) 0xE8, (byte) 0x3F, (byte) 0xEE,
        (byte) 0x00, (byte) 0xF0, (byte) 0x00, (byte) 0xF4, (byte) 0x3F, (byte) 0xF8, (byte) 0x3F, (byte) 0xFE};
    static final byte[] MP_ONE = {
        (byte) 0x11, (byte) 0x80, (byte) 0x11, (byte) 0x84, (byte) 0x06, (byte) 0x88, (byte) 0x06, (byte) 0x8E,
        (byte) 0x11, (byte) 0x90, (byte) 0x11, (byte) 0x94, (byte) 0x06, (byte) 0x98, (byte) 0x06, (byte) 0x9E,
        (byte) 0x11, (byte) 0xA0, (byte) 0x11, (byte) 0xA4, (byte) 0x06, (byte) 0xA8, (byte) 0x06, (byte) 0xAE,
        (byte) 0x11, (byte) 0xB0, (byte) 0x11, (byte) 0xB4, (byte) 0x06, (byte) 0xB8, (byte) 0x06, (byte) 0xBE,
        (byte) 0x11, (byte) 0xC0, (byte) 0x11, (byte) 0xC4, (byte) 0x06, (byte) 0xC8, (byte) 0x06, (byte) 0xCE,
        (byte) 0x11, (byte) 0xD0, (byte) 0x11, (byte) 0xD4, (byte) 0x06, (byte) 0xD8, (byte) 0x06, (byte) 0xDE,
        (byte) 0x11, (byte) 0xE0, (byte) 0x11, (byte) 0xE4, (byte) 0x06, (byte) 0xE8, (byte) 0x06, (byte) 0xEE,
        (byte) 0x11, (byte) 0xF0, (byte) 0x11, (byte) 0xF4, (byte) 0x06, (byte) 0xF8, (byte) 0x06, (byte) 0xFE};
    static final byte[] MP_TWO = {
        (byte) 0x22, (byte) 0x80, (byte) 0x22, (byte) 0x84, (byte) 0x5B, (byte) 0x88, (byte) 0x5B, (byte) 0x8E,
        (byte) 0x22, (byte) 0x90, (byte) 0x22, (byte) 0x94, (byte) 0x5B, (byte) 0x98, (byte) 0x5B, (byte) 0x9E,
        (byte) 0x22, (byte) 0xA0, (byte) 0x22, (byte) 0xA4, (byte) 0x5B, (byte) 0xA8, (byte) 0x5B, (byte) 0xAE,
        (byte) 0x22, (byte) 0xB0, (byte) 0x22, (byte) 0xB4, (byte) 0x5B, (byte) 0xB8, (byte) 0x5B, (byte) 0xBE,
        (byte) 0x22, (byte) 0xC0, (byte) 0x22, (byte) 0xC4, (byte) 0x5B, (byte) 0xC8, (byte) 0x5B, (byte) 0xCE,
        (byte) 0x22, (byte) 0xD0, (byte) 0x22, (byte) 0xD4, (byte) 0x5B, (byte) 0xD8, (byte) 0x5B, (byte) 0xDE,
        (byte) 0x22, (byte) 0xE0, (byte) 0x22, (byte) 0xE4, (byte) 0x5B, (byte) 0xE8, (byte) 0x5B, (byte) 0xEE,
        (byte) 0x22, (byte) 0xF0, (byte) 0x22, (byte) 0xF4, (byte) 0x5B, (byte) 0xF8, (byte) 0x5B, (byte) 0xFE};
    static final byte[] MP_THREE = {
        (byte) 0x33, (byte) 0x80, (byte) 0x33, (byte) 0x84, (byte) 0x4F, (byte) 0x88, (byte) 0x4F, (byte) 0x8E,
        (byte) 0x33, (byte) 0x90, (byte) 0x33, (byte) 0x94, (byte) 0x4F, (byte) 0x98, (byte) 0x4F, (byte) 0x9E,
        (byte) 0x33, (byte) 0xA0, (byte) 0x33, (byte) 0xA4, (byte) 0x4F, (byte) 0xA8, (byte) 0x4F, (byte) 0xAE,
        (byte) 0x33, (byte) 0xB0, (byte) 0x33, (byte) 0xB4, (byte) 0x4F, (byte) 0xB8, (byte) 0x4F, (byte) 0xBE,
        (byte) 0x33, (byte) 0xC0, (byte) 0x33, (byte) 0xC4, (byte) 0x4F, (byte) 0xC8, (byte) 0x4F, (byte) 0xCE,
        (byte) 0x33, (byte) 0xD0, (byte) 0x33, (byte) 0xD4, (byte) 0x4F, (byte) 0xD8, (byte) 0x4F, (byte) 0xDE,
        (byte) 0x33, (byte) 0xE0, (byte) 0x33, (byte) 0xE4, (byte) 0x4F, (byte) 0xE8, (byte) 0x4F, (byte) 0xEE,
        (byte) 0x33, (byte) 0xF0, (byte) 0x33, (byte) 0xF4, (byte) 0x4F, (byte) 0xF8, (byte) 0x4F, (byte) 0xFE};
    static final byte[] MP_FOUR = {
        (byte) 0x44, (byte) 0x80, (byte) 0x44, (byte) 0x84, (byte) 0x66, (byte) 0x88, (byte) 0x66, (byte) 0x8E,
        (byte) 0x44, (byte) 0x90, (byte) 0x44, (byte) 0x94, (byte) 0x66, (byte) 0x98, (byte) 0x66, (byte) 0x9E,
        (byte) 0x44, (byte) 0xA0, (byte) 0x44, (byte) 0xA4, (byte) 0x66, (byte) 0xA8, (byte) 0x66, (byte) 0xAE,
        (byte) 0x44, (byte) 0xB0, (byte) 0x44, (byte) 0xB4, (byte) 0x66, (byte) 0xB8, (byte) 0x66, (byte) 0xBE,
        (byte) 0x44, (byte) 0xC0, (byte) 0x44, (byte) 0xC4, (byte) 0x66, (byte) 0xC8, (byte) 0x66, (byte) 0xCE,
        (byte) 0x44, (byte) 0xD0, (byte) 0x44, (byte) 0xD4, (byte) 0x66, (byte) 0xD8, (byte) 0x66, (byte) 0xDE,
        (byte) 0x44, (byte) 0xE0, (byte) 0x44, (byte) 0xE4, (byte) 0x66, (byte) 0xE8, (byte) 0x66, (byte) 0xEE,
        (byte) 0x44, (byte) 0xF0, (byte) 0x44, (byte) 0xF4, (byte) 0x66, (byte) 0xF8, (byte) 0x66, (byte) 0xFE};
    static final byte[] MP_FIVE = {
        (byte) 0x55, (byte) 0x80, (byte) 0x55, (byte) 0x84, (byte) 0x6D, (byte) 0x88, (byte) 0x6D, (byte) 0x8E,
        (byte) 0x55, (byte) 0x90, (byte) 0x55, (byte) 0x94, (byte) 0x6D, (byte) 0x98, (byte) 0x6D, (byte) 0x9E,
        (byte) 0x55, (byte) 0xA0, (byte) 0x55, (byte) 0xA4, (byte) 0x6D, (byte) 0xA8, (byte) 0x6D, (byte) 0xAE,
        (byte) 0x55, (byte) 0xB0, (byte) 0x55, (byte) 0xB4, (byte) 0x6D, (byte) 0xB8, (byte) 0x6D, (byte) 0xBE,
        (byte) 0x55, (byte) 0xC0, (byte) 0x55, (byte) 0xC4, (byte) 0x6D, (byte) 0xC8, (byte) 0x6D, (byte) 0xCE,
        (byte) 0x55, (byte) 0xD0, (byte) 0x55, (byte) 0xD4, (byte) 0x6D, (byte) 0xD8, (byte) 0x6D, (byte) 0xDE,
        (byte) 0x55, (byte) 0xE0, (byte) 0x55, (byte) 0xE4, (byte) 0x6D, (byte) 0xE8, (byte) 0x6D, (byte) 0xEE,
        (byte) 0x55, (byte) 0xF0, (byte) 0x55, (byte) 0xF4, (byte) 0x6D, (byte) 0xF8, (byte) 0x6D, (byte) 0xFE};
    static final byte[] MP_SIX = {
        (byte) 0x66, (byte) 0x80, (byte) 0x66, (byte) 0x84, (byte) 0x7D, (byte) 0x88, (byte) 0x7D, (byte) 0x8E,
        (byte) 0x66, (byte) 0x90, (byte) 0x66, (byte) 0x94, (byte) 0x7D, (byte) 0x98, (byte) 0x7D, (byte) 0x9E,
        (byte) 0x66, (byte) 0xA0, (byte) 0x66, (byte) 0xA4, (byte) 0x7D, (byte) 0xA8, (byte) 0x7D, (byte) 0xAE,
        (byte) 0x66, (byte) 0xB0, (byte) 0x66, (byte) 0xB4, (byte) 0x7D, (byte) 0xB8, (byte) 0x7D, (byte) 0xBE,
        (byte) 0x66, (byte) 0xC0, (byte) 0x66, (byte) 0xC4, (byte) 0x7D, (byte) 0xC8, (byte) 0x7D, (byte) 0xCE,
        (byte) 0x66, (byte) 0xD0, (byte) 0x66, (byte) 0xD4, (byte) 0x7D, (byte) 0xD8, (byte) 0x7D, (byte) 0xDE,
        (byte) 0x66, (byte) 0xE0, (byte) 0x66, (byte) 0xE4, (byte) 0x7D, (byte) 0xE8, (byte) 0x7D, (byte) 0xEE,
        (byte) 0x66, (byte) 0xF0, (byte) 0x66, (byte) 0xF4, (byte) 0x7D, (byte) 0xF8, (byte) 0x7D, (byte) 0xFE};
    static final byte[] MP_SEVEN = {
        (byte) 0x77, (byte) 0x80, (byte) 0x77, (byte) 0x84, (byte) 0x07, (byte) 0x88, (byte) 0x07, (byte) 0x8E,
        (byte) 0x77, (byte) 0x90, (byte) 0x77, (byte) 0x94, (byte) 0x07, (byte) 0x98, (byte) 0x07, (byte) 0x9E,
        (byte) 0x77, (byte) 0xA0, (byte) 0x77, (byte) 0xA4, (byte) 0x07, (byte) 0xA8, (byte) 0x07, (byte) 0xAE,
        (byte) 0x77, (byte) 0xB0, (byte) 0x77, (byte) 0xB4, (byte) 0x07, (byte) 0xB8, (byte) 0x07, (byte) 0xBE,
        (byte) 0x77, (byte) 0xC0, (byte) 0x77, (byte) 0xC4, (byte) 0x07, (byte) 0xC8, (byte) 0x07, (byte) 0xCE,
        (byte) 0x77, (byte) 0xD0, (byte) 0x77, (byte) 0xD4, (byte) 0x07, (byte) 0xD8, (byte) 0x07, (byte) 0xDE,
        (byte) 0x77, (byte) 0xE0, (byte) 0x77, (byte) 0xE4, (byte) 0x07, (byte) 0xE8, (byte) 0x07, (byte) 0xEE,
        (byte) 0x77, (byte) 0xF0, (byte) 0x77, (byte) 0xF4, (byte) 0x07, (byte) 0xF8, (byte) 0x07, (byte) 0xFE};
    static final byte[] MP_EIGHT = {
        (byte) 0x08, (byte) 0x81, (byte) 0x08, (byte) 0x85, (byte) 0x7F, (byte) 0x88, (byte) 0x7F, (byte) 0x8E,
        (byte) 0x08, (byte) 0x91, (byte) 0x08, (byte) 0x95, (byte) 0x7F, (byte) 0x98, (byte) 0x7F, (byte) 0x9E,
        (byte) 0x08, (byte) 0xA1, (byte) 0x08, (byte) 0xA5, (byte) 0x7F, (byte) 0xA8, (byte) 0x7F, (byte) 0xAE,
        (byte) 0x08, (byte) 0xB1, (byte) 0x08, (byte) 0xB5, (byte) 0x7F, (byte) 0xB8, (byte) 0x7F, (byte) 0xBE,
        (byte) 0x08, (byte) 0xC1, (byte) 0x08, (byte) 0xC5, (byte) 0x7F, (byte) 0xC8, (byte) 0x7F, (byte) 0xCE,
        (byte) 0x08, (byte) 0xD1, (byte) 0x08, (byte) 0xD5, (byte) 0x7F, (byte) 0xD8, (byte) 0x7F, (byte) 0xDE,
        (byte) 0x08, (byte) 0xE1, (byte) 0x08, (byte) 0xE5, (byte) 0x7F, (byte) 0xE8, (byte) 0x7F, (byte) 0xEE,
        (byte) 0x08, (byte) 0xF1, (byte) 0x08, (byte) 0xF5, (byte) 0x7F, (byte) 0xF8, (byte) 0x7F, (byte) 0xFE};
    static final byte[] MP_NINE = {
        (byte) 0x19, (byte) 0x81, (byte) 0x19, (byte) 0x85, (byte) 0x6F, (byte) 0x88, (byte) 0x6F, (byte) 0x8E,
        (byte) 0x19, (byte) 0x91, (byte) 0x19, (byte) 0x95, (byte) 0x6F, (byte) 0x98, (byte) 0x6F, (byte) 0x9E,
        (byte) 0x19, (byte) 0xA1, (byte) 0x19, (byte) 0xA5, (byte) 0x6F, (byte) 0xA8, (byte) 0x6F, (byte) 0xAE,
        (byte) 0x19, (byte) 0xB1, (byte) 0x19, (byte) 0xB5, (byte) 0x6F, (byte) 0xB8, (byte) 0x6F, (byte) 0xBE,
        (byte) 0x19, (byte) 0xC1, (byte) 0x19, (byte) 0xC5, (byte) 0x6F, (byte) 0xC8, (byte) 0x6F, (byte) 0xCE,
        (byte) 0x19, (byte) 0xD1, (byte) 0x19, (byte) 0xD5, (byte) 0x6F, (byte) 0xD8, (byte) 0x6F, (byte) 0xDE,
        (byte) 0x19, (byte) 0xE1, (byte) 0x19, (byte) 0xE5, (byte) 0x6F, (byte) 0xE8, (byte) 0x6F, (byte) 0xEE,
        (byte) 0x19, (byte) 0xF1, (byte) 0x19, (byte) 0xF5, (byte) 0x6F, (byte) 0xF8, (byte) 0x6F, (byte) 0xFE};
    static final byte[] BANK2_ZERO = {
        (byte) 0x00, (byte) 0x90, (byte) 0x00, (byte) 0x94, (byte) 0x3F, (byte) 0x98, (byte) 0x3F, (byte) 0x9E};
    static final byte[] BANK2_ONE = {
        (byte) 0x11, (byte) 0x90, (byte) 0x11, (byte) 0x94, (byte) 0x06, (byte) 0x98, (byte) 0x06, (byte) 0x9E};
    static final byte[] BANK2_TWO = {
        (byte) 0x22, (byte) 0x90, (byte) 0x22, (byte) 0x94, (byte) 0x5B, (byte) 0x98, (byte) 0x5B, (byte) 0x9E};
    static final byte[] BANK2_THREE = {
        (byte) 0x33, (byte) 0x90, (byte) 0x33, (byte) 0x94, (byte) 0x4F, (byte) 0x98, (byte) 0x4F, (byte) 0x9E};
    static final byte[] BANK2_FOUR = {
        (byte) 0x44, (byte) 0x90, (byte) 0x44, (byte) 0x94, (byte) 0x66, (byte) 0x98, (byte) 0x66, (byte) 0x9E};
    static final byte[] BANK2_FIVE = {
        (byte) 0x55, (byte) 0x90, (byte) 0x55, (byte) 0x94, (byte) 0x6D, (byte) 0x98, (byte) 0x6D, (byte) 0x9E};
    static final byte[] BANK2_SIX = {
        (byte) 0x66, (byte) 0x90, (byte) 0x66, (byte) 0x94, (byte) 0x7D, (byte) 0x98, (byte) 0x7D, (byte) 0x9E};
    static final byte[] BANK2_SEVEN = {
        (byte) 0x77, (byte) 0x90, (byte) 0x77, (byte) 0x94, (byte) 0x07, (byte) 0x98, (byte) 0x07, (byte) 0x9E};
    static final byte[] BANK2_EIGHT = {
        (byte) 0x08, (byte) 0x91, (byte) 0x08, (byte) 0x95, (byte) 0x7F, (byte) 0x98, (byte) 0x7F, (byte) 0x9E};
    static final byte[] BANK2_NINE = {
        (byte) 0x19, (byte) 0x91, (byte) 0x19, (byte) 0x95, (byte) 0x6F, (byte) 0x98, (byte) 0x6F, (byte) 0x9E};
    static ArrayList<byte[]> mpDigits;
    static int mpIndex = 10;
    boolean keepGoing = true;
    int mpNumberInput = -1;

    @Override
    public void run() {
        mpDigits = new ArrayList<byte[]>();
        /*mpDigits.add(MP_ZERO);
        mpDigits.add(MP_ONE);
        mpDigits.add(MP_TWO);
        mpDigits.add(MP_THREE);
        mpDigits.add(MP_FOUR);
        mpDigits.add(MP_FIVE);
        mpDigits.add(MP_SIX);
        mpDigits.add(MP_SEVEN);
        mpDigits.add(MP_EIGHT);
        mpDigits.add(MP_NINE);*/
        mpDigits.add(BANK2_ZERO);
        mpDigits.add(BANK2_ONE);
        mpDigits.add(BANK2_TWO);
        mpDigits.add(BANK2_THREE);
        mpDigits.add(BANK2_FOUR);
        mpDigits.add(BANK2_FIVE);
        mpDigits.add(BANK2_SIX);
        mpDigits.add(BANK2_SEVEN);
        mpDigits.add(BANK2_EIGHT);
        mpDigits.add(BANK2_NINE);
        
        final Console console = new Console();
        console.title("RS232 Communications Prototype Program", "Digit Flipper");
        console.promptForExit();
        
        final Serial serial = SerialFactory.createInstance();
        serial.addListener(new SerialDataEventListener() {
            @Override
            public void dataReceived(SerialDataEvent event) {
                // Prevent the receive buffer from expanding by reading
                try {
                    console.println("[HEX DATA]   " + event.getHexByteString());
                    console.println("[ASCII DATA] " + event.getAsciiString());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        try {
            SerialConfig config = new SerialConfig();
            // MP protocol = 2400 Baud, 8 Data Bits, No Parity, 1 Stop Bit, No Flow Control
            config.device(SerialPort.getDefaultPort())
                  .baud(Baud._2400)
                  .dataBits(DataBits._8)
                  .parity(Parity.NONE)
                  .stopBits(StopBits._1)
                  .flowControl(FlowControl.NONE);
            serial.open(config);
            console.println(" Connecting to: " + config.toString(),
                    " Sending a new digit to each MP address once per second.",
                    " Any data received on serial port will be displayed below.");

            while(console.isRunning() && keepGoing) {
                // This is the version of the loop which writes to Bank 2 only
                if (mpNumberInput != -1) {
                    try{
                        serial.write(nextDigit(mpNumberInput));
                        StringBuilder outputString = new StringBuilder();
                        outputString.append("Index = ").append(mpNumberInput).append("  Data =");
                        for (int i = 0; i < mpDigits.get(mpNumberInput).length; i++) {
                            outputString.append( mpDigits.get(mpNumberInput)[i] + " ");
                        }
                        console.print(outputString);
                    } catch(IllegalStateException ex) {
                        ex.printStackTrace();
                    }
                    mpNumberInput = -1;
                }
                // This is the version of the loop which writes to all 8 Banks
                /*
                if (--mpIndex < 0) {
                    mpIndex = 9;
                }
                try {
                    serial.write(nextDigit(mpIndex));
                    //serial.writeln("Data looks clean this way.");
                }
                catch(IllegalStateException ex){
                    ex.printStackTrace();
                }
                StringBuilder outputString = new StringBuilder();
                outputString.append("Index = ").append(mpIndex).append("  Data =");
                for (int i = 0; i < mpDigits.get(mpNumberInput).length; i++) {
                    outputString.append( mpDigits.get(mpIndex)[i] + " ");
                }
                console.print(outputString);
                */
                Thread.sleep(50);
            }
        }
        catch(IOException ex) {
            System.out.println(" ==>> SERIAL SETUP FAILED : " + ex.getMessage());
            return;
        } catch (InterruptedException ex) {
            Logger.getLogger(SerialStuff.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
     public static byte[] nextDigit(int i) {
        if (i <= 9 && i >= 0) {
            return mpDigits.get(i);
        } else {
           return MP_ZERO;
        }
    }
     
     public void stopIt() {
         keepGoing = false;
     }
     
     public void mpNumber(int number) {
         mpNumberInput = number;
     }
    
}
