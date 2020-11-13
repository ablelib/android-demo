# AbleLib Android Demo

Android demo app for AbleLib - the premium mobile BLE library. Check it out at [https://ablelib.com](https://ablelib.com/).
Demo app shows 
This app demonstrates some of the Able SDK capabilities, and can be used as a starting point for project integrating the library.
This project consists of two Apps. One being the **AbleDemo** and the other one being **BlePeripheral** to make testing easier.

## BlePeripheral
**BlePeripheral** is very basic app that turns your phone into simple Ble server. In case you already have Ble device which you plan to use in conjuction with AbleLib, you can go ahead and skip this part.
As mentioned above, this is very basic app that consists of single activity and single button.
</br><img src="https://github.com/ablelib/android-demo/blob/develop/screenshots/server_start.jpg?raw=true" width="107" height="217"></br>
Click on "Start server" will start advertising and server will become visible during our Ble device scans.  Once the server is up and running, it is possible to write characteristics to it. Writing characteristics to it will make server trigger characteristic change and as value it will pass its current time. It is also possible to pass value when writing characteristics with date/time format you want to receive from server. 

## AbleDemo
**AbleDemo** app is split in five sections. Each of the section covers different parts of AbleLib functionality. 
### Scanning and Pairing
</br><img src="https://github.com/ablelib/android-demo/blob/develop/screenshots/start_scanning.jpg?raw=true" width="107" height="217"></br>
The very first section of app is intended for scanning devices around us and pairing with them. If we have all permissions, clicking the "Start scanning" will initiate the scanning process. AbleLib code for this is pretty straightforward.
```
AbleManager.scan(15_000, object: AbleNearbyScanListener {  
    override fun onStart() { ... }  
    override fun onDeviceFound(device: AbleDevice) { ... }  
    override fun onStop() { ... }  
    override fun onError(e: Exception) { ... }  
})
```
A single method call is needed to initate scanning. AbleLib also provides callback with all of the cases we want to cover as scanning goes on. For example this demo app uses `onStart()` and `onStop()` to show or hide list and `onDeviceFound` comes handy when it needs to update the list as it provides all details about found device.

As devices get scanned, list will get updated with items.
</br><img src="https://github.com/ablelib/android-demo/blob/develop/screenshots/scanning.jpg?raw=true" width="107" height="217"></br>
Clicking on one of the items from the list will attempt to pair our device with it. This is again handled with just a single method.
```
ableDevice.pair { result ->
	if (result.isSuccess()) { ... }   
}
```
Call to `pair()` method of `AbleDevice` will start the pairing process with that device. AbleLib also provides callback for when pairing is done and result of pairing can be checked with either `result.isSuccess` or `result.isFailure`.

### Storage
Storage tab shows all previously paired devices with our phone. 
</br><img src="https://github.com/ablelib/android-demo/blob/develop/screenshots/storage.jpg?raw=true" width="107" height="217"></br>
Clicking the "Refresh" button will update the list with newly paired devices if there are any. This is done through the `AbleDeviceStorage` class.  We can access all the devices by calling `AbleDeviceStorage.devices`. 
Clicking the "Delete" button next to specific device will remove that device from our phone completely. Again, just a single call to `AbleDeviceStorage.remove(...)`does the job. We can pass either device address or `AbleDevice` object.

### Communication
Communication tab, like storage tab, shows all previously paired devices. It also lets you to connect to those devices and discover their services and characteristics. There are also some tools which you can use to test **BlePeripheral**.
</br><img src="https://github.com/ablelib/android-demo/blob/develop/screenshots/comm_list.jpg?raw=true" width="107" height="217"></br>
Click on "Connect" button next to device will try and connect to that device. This is similar to the pairing. We have to call the `connect()` method on `Comm` from specific `AbleDevice`. This looks like this:
```
val state = ableDevice.comm.connect()  
if (state == BluetoothGattConnectionState.CONNECTED) { ... }
```
As with pairing, `connect()` provides us with result of our connection attempt, in this case `connect()` returns `BluetoothGattConnectionState` which app uses to check if connection attempt was successful. After connection was successful, UI will update and we get to use `Comm` object to handle further communication with device.
</br><img src="https://github.com/ablelib/android-demo/blob/develop/screenshots/comm_connected.jpg?raw=true" width="107" height="217"></br>
UI will now show some of the actions we can do. Let's start with "Discover Services". Clicking on "Discover Services" we will attempt to retrieve all of peripheral services. All this does in this demo app is a call to `ableDevice.comm.discoverServices()` which returns a list of all services from device and then it updates the list accordingly.
</br><img src="https://github.com/ablelib/android-demo/blob/develop/screenshots/discover_services.jpg?raw=true" width="107" height="217"></br>
As `discoverServices()` returns list of `BluetoothGattService`, we can then also easily get the list of characteristics for each of those services. Click on one of these items from the list will update the list with characteristics of that service.

If you are testing this app together with the **BlePeripheral** app you can do that using "Test comm" button at the top when you connect. This will give you access to two buttons which will do communication to the server.
</br><img src="https://github.com/ablelib/android-demo/blob/develop/screenshots/test_comm_connected.jpg?raw=true" width="107" height="217"></br>
To let server notify us about characteristic changes we first need to write to descriptor with `BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE` as value. AbleLib makes this part easier too. First we need to get the descriptor we wish to write to:
```
services = deviceComm.discoverServices()
val timeService = services.find { service -> service.uuid == TIME_SERVICE }  
val timeChar = timeService.getCharacteristic(TIME_CHARACTERISTIC)
val configDescriptor = timeChar.getDescriptor(CONFIG_DESCRIPTOR)
```
After that, on `Comm` object we can call the following:
`deviceComm.writeDescriptor(timeChar, configDescriptor, false)`
Last parameter is for 'isRACP', if we set it to false here, AbleLib will automatically set the `ENABLE_NOTIFICATION_VALUE` for us.

When we are done with that we can proceed to "Write Characteristic" button. This is pretty much the same process. First we obtain the service we want, and then we get characteristic from that service we wish to write to. Once we have that we can write to characteristic:
`val time = deviceComm.writeCharacteristic(timeChar, "h:mm a".toByteArray())`
We just provide the characteristic we want to write to and value. As I mentioned above in **BlePeripheral** section, we can use date time format as a value to let server format time for us. `writeCharacteristics` will wait for result from peripheral and return it to us, which we then use to show it on screen.
</br><img src="https://github.com/ablelib/android-demo/blob/develop/screenshots/test_comm_time.jpg?raw=true" width="107" height="217"></br>
### Quality of Service
Quality of service tab will show you list of all "Scan configs" which AbleLib can use. 
</br><img src="https://github.com/ablelib/android-demo/blob/develop/screenshots/qos.jpg?raw=true" width="107" height="217"></br>

AbleLib has three different `QualityOfService` objects which can work with by default. Those are `LOW_ENERGY`, `DEFAULT` and `INTENSE`. Each of these differ by scan time, delay bettwen scans and some other scan settings. Clicking on one of the items will update the `QualityOfService` which AbleLib uses. This is done by simply assigning `AbleManager` propery to it: `AbleManager.qualityOfService = LOW_ENERGY`.
If none of those suit your needs, you can just use `QualityOfService(...)` constructor and make `QualityOfService` object with values that matches your needs.

### Background Service
TODO
