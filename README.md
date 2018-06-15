# Smilo-Api
Smilo Api for wallets and block-explorer

This Api is used for external light applications.
The main purpose of this Api is to provide a RESTfull and websocket server to allow quick updates form the blockchain.

## P2p
The peer2peer classes are for the connection to the Smilo Nodes.
This connection retrieves Block, Tx's and messages from (hard-coded) nodes.

## LMDB
Blocks, Tx's and messages are stored in a LMDB Database to allow fast queries.
In the future we have plans to browse contracts, states and source code. 

## Websocket
The websocket is setup for a live Blocks, Tx's and messages.
When a Block, Tx or messages is recieved through p2p, it will be forwarded to the websockets.

## RESTfull

### /status
This returns the status of the server:
up and running, blockHeight, synced, version, etc..

### /tx
Return the last 10 transactions

### /tx/{tx}
Returns the tx {tx}

### /block
Returns the last 10 blocks

### /block/{block}
Returns block {block}