# Smilo-Api
Smilo Api for wallets and block-explorer

This Api is used for external 'light' applications.
The main purpose of this Api is to provide a RESTfull and websocket server to allow quick updates form the blockchain.

## P2p
The peer2peer classes are for the connection to the Smilo Nodes.
This connection retrieves Block, Tx's and messages from (hard-coded) nodes.

## LMDB
Blocks, Tx's and messages are stored in a LMDB Database to allow fast queries.
In the future we have plans to browse contracts, states and source code. 

## Websocket
The websocket is setup for a live Blocks, Tx's and messages.
When a Block, Tx or messages is received through p2p, it will be forwarded to the websockets.

## RESTfull
This is a first setup of needed responses.


### /status
This returns the status of the server:
up and running, blockHeight, synced, version, etc..

```json
{
"height": 1,
"status": "OK",
"ConnectedNodes": 4
}
```

## /asset
Returns a list of assets on the blockchain (10 max)

```json
[
    {
    "assetName": "XSM",
    "assetFullname": "Smilo",
    "assetDescription": "This is the description"
    },
    {
    "assetName": "XSP",
    "assetFullname": "Smilo Pay",
    "assetDescription": "This is the description"
    }
]
```

## /asset/{asset}
Returns all of assets on the blockchain

```json
{
"assetName": "XSM",
"assetFullname": "Smilo",
"assetDescription": "The latest generation hybrid blockchain platform",
"assetType": "Token",
"assetWebsite": "https://smilo.io",
"assetSourceCode": "https://github.com/Smilo-platform",
"assetMaxSupply": "200000000",
"assetCirculatingSupply": "200000000",
"assetOwner": "contractAddress",
"assetCreated": "date of contract"
}
```

### /balance/{address}
Returns the balance for {address}

```json
{
"publicKey": "{address}",
"storedCoins": [
    {
    "currency": "XSM",
    "amount": 5712
    },
    {
    "currency": "XSP",
    "amount": 234
    }
]
}
```

### /address/{address}
Return address info including balance and last 10 transactions

```json
{
"publicKey": "{address}",
"storedCoins": [
    {
    "currency": "XSM",
    "amount": 5712
    },
    {
    "currency": "XSP",
    "amount": 234
    }
],
"transactions": [
    {
    "timestamp": "<timestamp>",
    "txHash": "<txHash>",
    "txFrom": "{address}",
    "txTo": "<otherAddress>",
    "txAmount": 123
    },
    {
    "timestamp": "<timestamp>",
    "txHash": "<txHash>",
    "txFrom": "{address}",
    "txTo": "<otherAddress>",
    "txAmount": 124
    }
]
}
```

### /tx
Return the last 10 transactions

```json
[
    {
    "timestamp": "<timestamp>",
    "txHash": "<txHash>",
    "txFrom": "{address}",
    "txTo": "<otherAddress>",
    "txAmount": 123
    },
    {
    "timestamp": "<timestamp>",
    "txHash": "<txHash>",
    "txFrom": "{address}",
    "txTo": "<otherAddress>",
    "txAmount": 124
    }
]
```

### /tx/{tx}
Returns the tx {tx}

```json
{
"timestamp": "<timestamp>",
"txHash": "<txHash>",
"txFrom": "{address}",
"txTo": "<otherAddress>",
"txAmount": 123
}
```


### /block
Returns the last 10 blocks

```json
[
    {
    "timestamp": "<timestamp>",
    "blockHash": "<blockHash>",
    "blockHeight": 100000,
    "blockTransactions": 3,
    "fee": 0.003
    },
    {
    "timestamp": "<timestamp>",
    "blockHash": "<blockHash>",
    "blockHeight": 99999,
    "blockTransactions": 43,
    "fee": 0.043
    }
]
```

### /block/{block}
Returns block {block}

```json
{
"timestamp": "<timestamp>",
"blockHash": "<blockHash>",
"blockHeight": 100000,
"blockTransactions": 3,
"fee": 0.003
"transactions": [
    {
    "timestamp": "<timestamp>",
    "txHash": "<txHash>",
    "txFrom": "{address}",
    "txTo": "<otherAddress>",
    "txAmount": 123
    },
    {
    "timestamp": "<timestamp>",
    "txHash": "<txHash>",
    "txFrom": "{address}",
    "txTo": "<otherAddress>",
    "txAmount": 124
    }
]
}
```

### /price
Returns the price from CoinMarketCap (if available), will be cached in the Api.
In the first versions this will only contain hardcoded prices.

```json
[
    {
    "currencyFrom": "XSM",
    "currencyTo": "USD",
    "value": 0.25
    },
    {
    "currencyFrom": "XSM",
    "currencyTo": "ETH",
    "value": 0.05
    },
    {
    "currencyFrom": "XSM",
    "currencyTo": "BTC",
    "value": 0.005
    },
    {
    "currencyFrom": "XSP",
    "currencyTo": "XSM",
    "value": 0.2
    }
]
```