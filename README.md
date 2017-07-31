# SlotNSlot_Android
SlotNSlot Android application

## Geth Client
The project includes customized geth client for supporting the features that is not implemented by the official client yet(i.e. pending tx handling). The customized geth client will be replaced by the official client as soon as it supports the features.  
It is placed in `libs/geth.aar`.  

## Network Connection
The application connects [rinkeby](https://www.rinkeby.io/) test network by default. You can configure network parameter to connect other networks.  
After starting the application, you need to wait to sync ethereum block headers. This process takes about 20 to 30 minutes.

## Contract
[SlotNSlot contract](https://github.com/SlotNSlot/SlotNSlot_Contract) wrapping classes are located in `contract` directory.  
If you need more information about SlotNSlot contracts, please see [the api document](https://github.com/SlotNSlot/SlotNSlot_Contract/blob/master/ContractAPI.md)

## Library
The project uses following libraries:
- [web3j](https://github.com/web3j/web3j) for encoding/decoding abi
- [butterknife](https://github.com/JakeWharton/butterknife) for binding view
- [gradle-retrolambda](https://github.com/evant/gradle-retrolambda) for java8 feature
- [RxJava2](https://github.com/ReactiveX/RxJava) for reactive programing
- [RxLifecycle](https://github.com/trello/RxLifecycle) for managing lifecycle
- [RxBinding](https://github.com/JakeWharton/RxBinding)

## License
Licensed under the GNU GENERAL PUBLIC LICENSE (Version 3, 29th June 2007)

## Contact
The team is open to any debates in any channel for improving the service. If you have any concerns and suggestions about the service, please contact the team with [Github](https://github.com/SlotNSlot/SlotNSlot), [Twitter](https://twitter.com/slotnslot), [Hipchat](https://www.hipchat.com/gIUbFZBvh) or any other communication channels.
