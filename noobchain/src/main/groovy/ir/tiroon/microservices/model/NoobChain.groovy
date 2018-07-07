package ir.tiroon.microservices.model

import com.fasterxml.jackson.databind.ObjectMapper

class NoobChain {

    public static ArrayList<Block> blockchain = new ArrayList<Block>()
    public static int difficulty = 5

    NoobChain(){
        blockchain.add(new Block("Hi im the first block", "0"))
        System.out.println("Trying to Mine block 1... ")
        blockchain.get(0).mineBlock(difficulty)

        blockchain.add(new Block("Yo im the second block",blockchain.get(blockchain.size()-1).hash))
        System.out.println("Trying to Mine block 2... ")
        blockchain.get(1).mineBlock(difficulty)

        blockchain.add(new Block("Hey im the third block",blockchain.get(blockchain.size()-1).hash))
        System.out.println("Trying to Mine block 3... ")
        blockchain.get(2).mineBlock(difficulty)

        System.out.println("\nBlockchain is Valid: " + isChainValid())


        def om = new ObjectMapper()

        System.out.println("BMD:/n" + om.writeValueAsString(blockchain))

    }

    Boolean isChainValid() {
        Block currentBlock
        Block previousBlock
        String hashTarget = new String(new char[difficulty]).replace('\0', '0')

        //loop through blockchain to check hashes:
        for(int i=1; i < blockchain.size(); i++) {
            currentBlock = blockchain.get(i)
            previousBlock = blockchain.get(i-1)
            //compare registered hash and calculated hash:
            if(!currentBlock.hash.equals(currentBlock.calculateHash()) ){
                System.out.println("Current Hashes not equal")
                false
            }
            //compare previous hash and registered previous hash
            if(!previousBlock.hash.equals(currentBlock.previousHash) ) {
                System.out.println("Previous Hashes not equal")
                false
            }
            //check if hash is solved
            if(!currentBlock.hash.substring( 0, difficulty).equals(hashTarget)) {
                System.out.println("This block hasn't been mined")
                false
            }
        }
        true
    }


}
