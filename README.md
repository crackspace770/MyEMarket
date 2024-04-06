## E-Market

E-Market app where you can be either seller or buyer. 

## Screenshot
<table style="width:100%">
  <tr>
    <th><img src="https://github.com/crackspace770/MyEMarket/blob/master/screenshot/1.jpeg"/></th>
    <th><img src="https://github.com/crackspace770/MyEMarket/blob/master/screenshot/2.jpeg"/></th>
  
  </tr>

   <tr>
    <th><img src="https://github.com/crackspace770/MyEMarket/blob/master/screenshot/3.jpeg"/></th>
    <th><img src="https://github.com/crackspace770/MyEMarket/blob/master/screenshot/4.jpeg"/></th>
  
  </tr>

</table>


## Tech stack & Open-source libraries
- Minimum SDK level 24
- [Kotlin](https://kotlinlang.org/) based, [Coroutines](https://github.com/Kotlin/kotlinx.coroutines) + [LiveData](https://developer.android.com/topic/libraries/architecture/livedata) for asynchronous.
- Jetpack
  - Lifecycle: Observe Android lifecycles and handle UI states upon the lifecycle changes.
  - ViewModel: Manages UI-related data holder and lifecycle aware. Allows data to survive configuration changes such as screen rotations.
  - DataBinding: Binds UI components in your layouts to data sources in your app using a declarative format rather than programmatically.
  - Room: Constructs Database by providing an abstraction layer over SQLite to allow fluent database access.
  - [Navigation](https://developer.android.com/jetpack/androidx/releases/navigation): Allowing you to navigate each fragment using argument to define data that is being sent to each fragment.
  - [Hilt](https://dagger.dev/hilt/): for dependency injection.
- Architecture
  - MVP Architecture
- [Firebase](https://github.com/firebase/FirebaseUI-Android): Storing and fetching data from Firestore.
  - Auth: Managing authentication for login and registration.
  - Firestore: Let you store, sync, and query data.
  - Storage: Let you store and serve user-generated content, such as photos, videos, and other files, securely in the cloud.
- [Glide](https://github.com/bumptech/glide): [GlidePalette](https://github.com/florent37/GlidePalette): Loading images from network.
- [Circle Image View](https://github.com/hdodenhof/CircleImageView): To transform your image to circle view
- [Color Picker](https://github.com/skydoves/ColorPickerView): Picking color using Hex code.
- [StepView](https://github.com/shuhart/StepView): To animate order process.

## Midtrans Set-Up
1. Set up a sample Midtrans Merchant Server by hosting or local: https://github.com/rizdaprasetya/midtrans-mobile-merchant-server--php-sample-/tree/master
2. Set up SDK : https://docs.midtrans.com/reference/android-sdk
 ```
 implementation ("com.midtrans:uikit:2.0.0-SANDBOX")
```
3. Set up your midtrans account : https://dashboard.sandbox.midtrans.com/
4. Get your Access key on Setting-> Access Key
   <img src="https://github.com/crackspace770/MyEMarket/blob/master/screenshot/3.jpeg"/>
5. Copy your access key inside your code.
  
