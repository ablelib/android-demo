
# AbleLib Android Demo

Android demo app for AbleLib - the premium mobile BLE library. Check it out at [https://ablelib.com](https://ablelib.com/).
Demo app shows 
This app demonstrates some of the Able SDK capabilities, and can be used as a starting point for project integrating the library.
This project consists of two Apps. One being the **AbleDemo** and the other one being **BlePeripheral** to make testing easier.

## BlePeripheral
**BlePeripheral** is very basic app that turns your phone into simple Ble server. In case you already have Ble device which you plan to use in conjuction with AbleLib, you can go ahead and skip this part.
As mentioned above, this is very basic app that consists of single activity and single button.

![](https://github.com/ablelib/android-demo/blob/develop/screenshots/server_start.jpg?raw=true)

Click on "Start server" will start advertising and server will become visible during our Ble device scans.  Once the server is up and running, it is possible to write characteristics to it. Writing characteristics to it will make server trigger characteristic change and as value it will pass its current time. It is also possible to pass value when writing characteristics with date/time format you want to receive from server. 

## AbleDemo
**AbleDemo** app is split in five sections. Each of the section covers different parts of AbleLib functionality. 
### Scanning and Pairing
![](https://github.com/ablelib/android-demo/blob/develop/screenshots/start_scanning.jpg?raw=true)

The very first section of app is intended for scanning devices around us and pairing with them. Before we get to scanning, we should check if our app has all the permissions to do so. AbleLib provides some utility to make this process easier. To check for permissions we use `AbleManager.handlePermissionRequestIfNotGranted(...)`, providing the `Activity` which will be in charge of results. To check if we were given all results we first override `onRequestPermissionResult`, there we can call AbleLib's `AbleManager.handleRequestPermissionResult` and forward all parameters of overriden method with addition of `PermissionRequestResult` callback. 
```
object: PermissionRequestResult {  
    override fun onAllPermissionsGranted() { ... }    
    override fun onPermissionDenied(permissionDenied: Array<String>) { ... }  
}
```
The callback will let us know if we were given all permisions to proceed or not, if not we also get list of permissions that were denied.

Now that we have permissions sorted out we can start the scanning by clicking the "Start scanning". AbleLib code for this is pretty straightforward.
```
AbleManager.scan(15_000)
```
Using coroutines it takes a single method to start scanning. We can also specify how long do we wish to scan for by providing a parameter to the method. Once the scanning is done, the `scan()` method will provide us with all devices that lib managed to scan, which we then use to populate the list with those devices.

![](https://github.com/ablelib/android-demo/blob/develop/screenshots/scan_results.jpg?raw=true)

Clicking on one of the items from the list will attempt to pair our device with it. This is again handled with just a single method.
```
ableDevice.pair { result ->
	if (result.isSuccess()) { ... }   
}
```
Call to `pair()` method of `AbleDevice` will start the pairing process with that device. AbleLib also provides callback for when pairing is done and result of pairing can be checked with either `result.isSuccess` or `result.isFailure`.

### Storage
Storage tab shows all previously paired devices with our phone. 

![](https://github.com/ablelib/android-demo/blob/develop/screenshots/storage.jpg?raw=true)

Clicking the "Refresh" button will update the list with newly paired devices if there are any. This is done through the `AbleDeviceStorage` class.  We can access all the devices by calling `AbleDeviceStorage.devices`. 
Clicking the "Delete" button next to specific device will remove that device from our phone completely. Again, just a single call to `AbleDeviceStorage.remove(...)`does the job. We can pass either device address or `AbleDevice` object.

### Communication
Communication tab, like storage tab, shows all previously paired devices. It also lets you to connect to those devices and discover their services and characteristics. There are also some tools which you can use to test **BlePeripheral**.

![](https://github.com/ablelib/android-demo/blob/develop/screenshots/comm_list.jpg?raw=true)

Click on "Connect" button next to device will try and connect to that device. This is similar to the pairing. We have to call the `connect()` method on `Comm` from specific `AbleDevice`. This looks like this:
```
val state = ableDevice.comm.connect()  
if (state == BluetoothGattConnectionState.CONNECTED) { ... }
```
As with pairing, `connect()` provides us with result of our connection attempt, in this case `connect()` returns `BluetoothGattConnectionState` which app uses to check if connection attempt was successful. After connection was successful, UI will update and we get to use `Comm` object to handle further communication with device.

![](https://github.com/ablelib/android-demo/blob/develop/screenshots/comm_connected.jpg?raw=true)

UI will now show some of the actions we can do. Let's start with "Discover Services". Clicking on "Discover Services" we will attempt to retrieve all of peripheral services. All this does in this demo app is a call to `ableDevice.comm.discoverServices()` which returns a list of all services from device and then it updates the list accordingly.

![](https://github.com/ablelib/android-demo/blob/develop/screenshots/discover_services.jpg?raw=true)

As `discoverServices()` returns list of `BluetoothGattService`, we can then also easily get the list of characteristics for each of those services. Click on one of these items from the list will update the list with characteristics of that service.

If you are testing this app together with the **BlePeripheral** app you can do that using "Test comm" button at the top when you connect. This will give you access to two buttons which will do communication to the server.

![](https://github.com/ablelib/android-demo/blob/develop/screenshots/test_comm_connected.jpg?raw=true")

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

![](https://github.com/ablelib/android-demo/blob/develop/screenshots/test_comm_time.jpg?raw=true)
### Background Service
![](https://github.com/ablelib/android-demo/blob/develop/screenshots/start_service.jpg?raw=true)

AbleLib provides functionality to scan for devices throught service, which has benefit of being able to work even when our app is in background. To start service in demo, a single click on "Start Service" is required. The code for this consists for several things. First if we wish to have our service running in background on Android, we need to make notification that will let user know it is running. You can make notification yourself or you can use AbleLib's helper method: 
```
AbleService.defaultNotification(  
  context!!,  
  getString(R.string.app_name),  
  getString(R.string.app_is_scanning),  
  R.drawable.ic_launcher_foreground,  
  MainActivity::class.java  
)
``` 
It requires title, description, drawable for notification icon and target class which is activity which will get opened when user clicks on notification.

Once we have notification, we can setup our service:
```
AbleManager.setUpService(  
  serviceClass = AbleService::class.java,  
  notification = defaultNotification,  
  backgroundScanInApp = true,  
  backgroundScanOutsideApp = true  
)
``` 
Here we pass the serviceClass which will take care of scanning logic, we can either pass AbleService or any class which inherits it. We also have to pass parameters for `notification`, `backgroundScanInApp` and `backgroundScanOutsideApp` which are self-explanatory.  After we make any changes to our service we have to call `AbleManager.refreshBackgroundServiceState()` to (re)start our service with latest changes.

Lastly to get scan results we will need to use a receiver. First lets create our receiver:
```
private val receiver = object: BroadcastReceiver() {  
    override fun onReceive(context: Context?, intent: Intent?) {  
        val device = intent?.getParcelableExtra<AbleDevice>(AbleService.DEVICE)
  }  
}
```
Our receiver is in charge of receiving the scan result. Scan result will be a single `AbleDevice` object and it will be in intent as `Parcelable` extra under the `AbleService.DEVICE` key. Here we get device, add it to our current list of devices and update the UI. Now that we have our receiver, all that is left is to register it: `registerReceiver(receiver, IntentFilter(AbleService.ACTION_DEVICE_FOUND))`. All we have to do here is pass `AbleService.ACTION_DEVICE_FOUND` as parameter to `IntentFilter` and we are good to go.

![](https://github.com/ablelib/android-demo/blob/develop/screenshots/service_results.jpg?raw=true)

### Quality of Service
Quality of service tab will show you list of all "Scan configs" which AbleLib can use. 

![](https://github.com/ablelib/android-demo/blob/develop/screenshots/qos.jpg?raw=true)

AbleLib has three different `QualityOfService` objects which can work with by default. Those are `LOW_ENERGY`, `DEFAULT` and `INTENSE`. Each of these differ by scan time, delay bettwen scans and some other scan settings. Clicking on one of the items will update the `QualityOfService` which AbleLib uses. This is done by simply assigning `AbleManager` propery to it: `AbleManager.qualityOfService = LOW_ENERGY`.
If none of those suit your needs, you can just use `QualityOfService(...)` constructor and make `QualityOfService` object with values that matches your needs.
