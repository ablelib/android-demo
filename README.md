
# AbleLib Android Demo

Android demo app for AbleLib - the premium mobile BLE library. Check it out at [https://ablelib.com](https://ablelib.com/).
This app demonstrates some of the Able SDK capabilities, and can be used as a starting point for integrating the library into your project.
This project consists of two apps:
* AbleDemo - the actual library demo.
* BlePeripheral - a helper app to make testing easier.

## BlePeripheral
**BlePeripheral** is basic app that allows for initialization of a local GATT server (i.e, peripheral mode) and an L2CAP server socket. The server hosts a service and a characteristic via which the PSM of the L2CAP channel can be obtained by client sockets.

Click on "Start server" to start advertising and server will become visible during BLE device scans. Once the server is up and running, it is possible to read its characteristic to obtain the PSM of its hosted L2CAP channel. Client sockets can then connect to the server socket, after which you can send text in either direction, effectively forming an instant chat service over BLE.

## AbleDemo
**AbleDemo** app is split in five sections. Each of the section covers different parts of AbleLib functionality. 
### Scanning and Pairing
![](https://github.com/ablelib/android-demo/blob/develop/screenshots/start_scanning.jpg?raw=true)

The  first section of app is intended for scanning devices around us and pairing with them. Before we get to scanning, we should check if our app has all the permissions to do so. AbleLib provides some utilities to make this process easier. To check for permissions we use `AbleManager.handlePermissionRequestIfNotGranted(...)`, providing the `Activity` which will be in charge of results. To check if we were given all results we first override `onRequestPermissionResult`, there we can call AbleLib's `AbleManager.handleRequestPermissionResult` and forward all parameters of overriden method with addition of `PermissionRequestResult` callback. 
```kotlin
object : PermissionRequestResult {  
    override fun onAllPermissionsGranted() { ... }    
    override fun onPermissionDenied(permissionDenied: Array<String>) { ... }  
}
```
The callback will let us know if we were given all permisions to proceed or not, if not we also get list of permissions that were denied.

Now that we have permissions sorted out we can start the scanning by clicking the "Start scanning". AbleLib code for this is pretty straightforward.
```kotlin
AbleManager.scan(15_000)
```
Using coroutines it takes a single method to start scanning. We can also specify how long do we wish to scan for by providing a parameter to the method. Once the scanning is done, the `scan()` method will provide us with all devices that lib managed to scan, which we then use to populate the list with those devices.

![](https://github.com/ablelib/android-demo/blob/develop/screenshots/scan_results.jpg?raw=true)

Clicking on one of the items from the list will attempt to pair our device with it. This is again handled with just a single method.
```kotlin
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
Communication tab, like storage tab, shows all previously paired devices. It also lets you to connect to those devices and discover their services and characteristics.

![](https://github.com/ablelib/android-demo/blob/develop/screenshots/comm_list.jpg?raw=true)

Click on "Connect" button next to device will try and connect to that device. This is similar to the pairing. We have to call the `connect()` method on `Comm` from specific `AbleDevice`. This looks like this:
```kotlin
val state = ableDevice.comm.connect()  
if (state == BluetoothGattConnectionState.CONNECTED) { ... }
```
As with pairing, `connect()` provides us with result of our connection attempt, in this case `connect()` returns `BluetoothGattConnectionState` which app uses to check if connection attempt was successful. After connection was successful, UI will update and we get to use `Comm` object to handle further communication with device.

![](https://github.com/ablelib/android-demo/blob/develop/screenshots/comm_connected.jpg?raw=true)

UI will now show some of the actions we can do. Let's start with "Discover Services". Clicking on "Discover Services" we will attempt to retrieve all of peripheral services. All this does in this demo app is a call to `ableDevice.comm.discoverServices()` which returns a list of all services from device and then it updates the list accordingly.

![](https://github.com/ablelib/android-demo/blob/develop/screenshots/discover_services.jpg?raw=true)

As `discoverServices()` returns list of `BluetoothGattService`, we can then also easily get the list of characteristics for each of those services. Click on one of these items from the list will update the list with characteristics of that service.

### Background Service
![](https://github.com/ablelib/android-demo/blob/develop/screenshots/start_service.jpg?raw=true)

AbleLib provides functionality to scan for devices throught service, which has benefit of being able to work even when our app is in background. To start service in demo, a single click on "Start Service" is required. The code for this consists for several things. First if we wish to have our service running in background on Android, we need to make notification that will let user know it is running. You can make notification yourself or you can use AbleLib's helper method: 
```kotlin
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
```kotlin
AbleManager.setUpService(  
  serviceClass = AbleService::class.java,  
  notification = defaultNotification,  
  backgroundScanInApp = true,  
  backgroundScanOutsideApp = true  
)
``` 
Here we pass the serviceClass which will take care of scanning logic, we can either pass AbleService or any class which inherits it. We also have to pass parameters for `notification`, `backgroundScanInApp` and `backgroundScanOutsideApp` which are self-explanatory.  After we make any changes to our service we have to call `AbleManager.refreshBackgroundServiceState()` to (re)start our service with latest changes.

Lastly to get scan results we will need to use a receiver. First lets create our receiver:
```kotlin
private val receiver = object: BroadcastReceiver() {  
    override fun onReceive(context: Context?, intent: Intent?) {  
        val device = intent?.getParcelableExtra<AbleDevice>(AbleService.DEVICE)
  }  
}
```
Our receiver is in charge of receiving the scan result. Scan result will be a single `AbleDevice` object and it will be in intent as `Parcelable` extra under the `AbleService.DEVICE` key. Here we get device, add it to our current list of devices and update the UI. Now that we have our receiver, all that is left is to register it: `registerReceiver(receiver, IntentFilter(AbleService.ACTION_DEVICE_FOUND))`. All we have to do here is pass `AbleService.ACTION_DEVICE_FOUND` as parameter to `IntentFilter` and we are good to go.

![](https://github.com/ablelib/android-demo/blob/develop/screenshots/service_results.jpg?raw=true)

### Sockets
 This tab allows you to test **client socket communication**. It allows for scanning for a peripheral that supports L2CAP server socket, and connecting to it in client mode. After a socket connection is obtained, you can send text in either direction, effectively forming an instant chat service over BLE.

### Quality of Service
Quality of service tab will show you list of all "Scan configs" which AbleLib can use. 

![](https://github.com/ablelib/android-demo/blob/develop/screenshots/qos.jpg?raw=true)

AbleLib has three different `QualityOfService` objects which can work with by default. Those are `LOW_ENERGY`, `DEFAULT` and `INTENSE`. Each of these differ by scan time, delay bettwen scans and some other scan settings. Clicking on one of the items will update the `QualityOfService` which AbleLib uses. This is done by simply assigning `AbleManager` propery to it: `AbleManager.qualityOfService = LOW_ENERGY`.
If none of those suit your needs, you can just use `QualityOfService(...)` constructor and make `QualityOfService` object with values that matches your needs.
