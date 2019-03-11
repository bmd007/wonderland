package ir.tiroon.microservices.model

import ir.tiroon.microservices.StringUtil

import java.time.LocalDateTime
import java.time.ZoneId

class Block {

    String hash
    String previousHash
    private String data //our data will be a simple message.
    private long timeStamp //as number of milliseconds since 1/1/1970.
    private int nonce

    Block(String data,String previousHash ) {
        this.data = data
        this.previousHash = previousHash
        this.timeStamp = new Date().getTime()
        this.hash = calculateHash()
    }

    String calculateHash() {
        String calculatedHash = StringUtil.applySha256(previousHash + Long.toString(timeStamp) + data)
        calculatedHash
    }


    //what the hell??
    void mineBlock(int difficulty) {
        String target = new String(new char[difficulty]).replace('\0', '0'); //Create a string with difficulty * "0"
        while(!hash.substring( 0, difficulty).equals(target)) {
            nonce ++;
            hash = calculateHash();
        }
        System.out.println("Block Mined!!! : " + hash);
    }

//    In reality each miner will start iterating from a random point. Some miners may even try random numbers for nonce. Also it’s worth noting that at the harder difficulties solutions may require more than integer.MAX_VALUE, miners can then try changing the timestamp.
}