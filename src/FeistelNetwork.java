/**
 * Classname: FeistelNetwork
 * Version: 1.0
 * Date: 05.03.2016
 * Assignment: 3
 * Author: G4br1el
 * Java Version: 8
 */

import javax.swing.*;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

public class FeistelNetwork {

    /**
     * Main method
     * @param args
     * @throws IOException if something goes wrong with writing the file
     */
    public static void main(String[] args) throws IOException{
        byte[] data = readContentIntoArray();

        byte[] left = Arrays.copyOfRange(data, 0, (data.length / 2));
        byte[] right = Arrays.copyOfRange(data, (data.length / 2), data.length);

        String keyString = JOptionPane.showInputDialog("Encryption key: ");
        String eingabe = JOptionPane.showInputDialog("Number of rounds: ");

        int rounds = Integer.parseInt(eingabe);
        if(rounds % 2 != 0)
            rounds++;

        //bring arrays to the same length
        if(left.length > right.length){
            right = Arrays.copyOf(right, (right.length + 1));
            right[right.length - 1] = 0;
        }else if (left.length < right.length){
            left = Arrays.copyOf(left, (left.length + 1));
            left[left.length - 1] = 0;
        }

        //Encryption
        byte[][] eKey = RoundKeyGen(keyString, right.length, rounds, false);
        byte[] ciphertext = Encryption(left, right, eKey, rounds);

        Path path = Paths.get("eFile.txt");
        Files.write(path, ciphertext);

        //Decryption
        byte[][] dKey = RoundKeyGen(keyString, left.length, rounds, true);
        byte[] Plaintext = Decryption(left, right, dKey, rounds);

        path = Paths.get("dFile.txt");
        Files.write(path, Plaintext);

        JOptionPane.showMessageDialog(null, "Done!");
    }

    /**
     * Reads a file to a Byte Array
     * @return Byte Array
     * @throws IOException if something goes wrong with loading the file
     */
    public static byte[] readContentIntoArray() throws IOException {

        Path path = Paths.get("src/Text.txt");
        byte[] data = Files.readAllBytes(path);
        return data;
    }

    /**
     * Encryption with the Feistel Network
     * @param left is a Byte Array
     * @param right is a Byte Array
     * @param key is a 2D Byte Array
     * @param rounds is a Integer
     * @return Byte Array (Encrypted)
     * @node the left and the right side needs the same length
     */
    public static byte[] Encryption(byte[] left, byte[] right, byte[][] key, int rounds){
        byte[] tmp;

        for (int i = 0; i < rounds; i++){
            for (int a = 0; a < right.length; a++){
                right[a] = (byte) ((right[a] ^ key[i][a])^left[a]);
            }
            if(i < (rounds - 1)){
                tmp = left;
                left = right;
                right = tmp;
            }
        }

        byte[] ciphertext = new byte[left.length + right.length];
        System.arraycopy(left, 0, ciphertext, 0, left.length);
        System.arraycopy(right, 0, ciphertext, left.length, right.length);

        return ciphertext;
    }

    /**
     * Decryption with the Feistel Network
     * @param left is a Byte Array
     * @param right is a Byte Array
     * @param key is a 2D Byte Array
     * @param rounds is a Integer
     * @return Byte Array (Decrypted)
     * @node the left and the right side needs the same length
     */
    public static byte[] Decryption(byte[] left, byte[] right, byte[][] key, int rounds){
        byte[] tmp;

        for (int i = 0; i < rounds; i++){
            for (int a = 0; a < left.length; a++){
                left[a] = (byte) ((left[a] ^ key[i][a]) ^ right[a]);
            }
            tmp = right;
            right = left;
            left = tmp;
        }

        byte[] Plaintext = new byte[left.length + right.length];
        System.arraycopy(left, 0, Plaintext, 0, left.length);
        System.arraycopy(right, 0, Plaintext, left.length, right.length);

        return Plaintext;
    }

    /**
     * Generates a Key for each round with a circular left shift by 1
     * @param Key is a String
     * @param Decryption is a Bool variable (false = Encryption, true = Decryption)
     * @return Round keys in a 2D Byte Array
     */
    public static byte[][] RoundKeyGen(String Key, int Textlength, int Rounds, boolean Decryption){
        byte[][] RoundKeys = new byte[Rounds][];
        byte[] tmp;

        if(Key.length() < Textlength){
            while (Key.length() < Textlength){
                Key = Key + Key;
            }
        }
        Key = Key.substring(0, Textlength);
        RoundKeys[0] = Key.getBytes(Charset.forName("UTF-8"));

        for (int i = 1; i < Rounds; i++){
            tmp = RoundKeys[i - 1];
            BigInteger bigInt = new BigInteger(tmp);
            BigInteger shiftInt = bigInt.shiftLeft(1);
            tmp = shiftInt.toByteArray();
            RoundKeys[i] = tmp.clone();
        }

        if(Decryption == true){
            byte[][] tmp2 = RoundKeys.clone();
            for (int i = 0; i < RoundKeys.length; i++){
                RoundKeys[i] = tmp2[(RoundKeys.length - 1) - i ].clone();
            }
        }
        return RoundKeys;
    }
}