# Actions to send to the connection:

## Send message to other devices

### Type
    Broadcast

### Action
    com.example.ss18.msp.lmu.CONNECTION_MESSAGE_ACTION

### Extra
    * "DATA": string # Arbitrary message. Recommended: Json - String.

# Connection events:

## Endpoint disconnected

### Type
    Broadcast

### Action
    com.example.ss18.msp.lmu.CONNECTION_ENDPOINT_DISCONNECTED

### Extras
    * "DATA": string -> ConnectionEndpointJson

## Endpoint found

### Type
    Broadcast

### Action
    com.example.ss18.msp.lmu.CONNECTION_ENDPOINT_FOUND

### Extras
    * "DATA": string -> ConnectionEndpointJson

## Endpoint lost

### Type
    Broadcast

### Action
    com.example.ss18.msp.lmu.CONNECTION_ENDPOINT_LOST

### Extras
    * "DATA": string -> ConnectionEndpointJson

# JSON Objects

## ConnectionEndpointJson
    {
        "id": string,
        "displayName": string
    }