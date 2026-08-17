import SwiftUI
import ComposeApp
import FirebaseCore
#if canImport(FirebaseMessaging)
import FirebaseMessaging
#endif
import GoogleMobileAds
import UserNotifications

#if canImport(FirebaseMessaging)
class AppDelegate: NSObject, UIApplicationDelegate, UNUserNotificationCenterDelegate, MessagingDelegate {
    func application(_ application: UIApplication,
                     didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey : Any]? = nil) -> Bool {
        
        if Bundle.main.path(forResource: "GoogleService-Info", ofType: "plist") != nil {
            FirebaseApp.configure()
        }
        
        // Push Notificaties Registratie
        UNUserNotificationCenter.current().delegate = self
        let authOptions: UNAuthorizationOptions = [.alert, .badge, .sound]
        UNUserNotificationCenter.current().requestAuthorization(options: authOptions) { _, _ in }
        application.registerForRemoteNotifications()
        
        // Messaging Delegate
        Messaging.messaging().delegate = self
        
        MobileAds.shared.start(completionHandler: nil)
        
        setupKoin()
        setupAdMobRegistry()
        
        return true
    }
    
    private func setupKoin() {
        let plantnetKey = Bundle.main.object(forInfoDictionaryKey: "PLANTNET_API_KEY") as? String ?? ""
        let geminiKey = Bundle.main.object(forInfoDictionaryKey: "GEMINI_API_KEY") as? String ?? ""
        let revenueCatKey = Bundle.main.object(forInfoDictionaryKey: "REVENUECAT_API_KEY") as? String ?? ""

        KoinKt.doInitKoin(
            useMock: false,
            plantnetApiKey: plantnetKey,
            geminiApiKey: geminiKey,
            revenueCatApiKey: revenueCatKey
        )
    }

    private func setupAdMobRegistry() {
        PlatformViewRegistry.shared.bannerFactory = { adUnitId in
            let bannerView = BannerView(adSize: AdSizeBanner)
            bannerView.adUnitID = adUnitId
            if let windowScene = UIApplication.shared.connectedScenes.first as? UIWindowScene,
               let rootVC = windowScene.windows.first?.rootViewController {
                bannerView.rootViewController = rootVC
            }
            bannerView.load(Request())
            return bannerView
        }
        
        PlatformViewRegistry.shared.nativeAdFactory = { adUnitId, isMedium in
            let container = NativeAdViewContainer()
            container.setup(adUnitId: adUnitId, isMedium: isMedium.boolValue)
            return container
        }
    }
    
    // MessagingDelegate: Ontvang de FCM Token
    func messaging(_ messaging: Messaging, didReceiveRegistrationToken fcmToken: String?) {
        print("Firebase registration token: \(String(describing: fcmToken))")
    }
    
    func userNotificationCenter(_ center: UNUserNotificationCenter,
                                willPresent notification: UNNotification,
                                withCompletionHandler completionHandler: @escaping (UNNotificationPresentationOptions) -> Void) {
        completionHandler([[.banner, .list, .sound]])
    }
}
#else
class AppDelegate: NSObject, UIApplicationDelegate, UNUserNotificationCenterDelegate {
    func application(_ application: UIApplication,
                     didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey : Any]? = nil) -> Bool {
        
        if Bundle.main.path(forResource: "GoogleService-Info", ofType: "plist") != nil {
            FirebaseApp.configure()
        }
        
        MobileAds.shared.start(completionHandler: nil)
        
        setupKoin()
        setupAdMobRegistry()
        
        return true
    }
    
    private func setupKoin() {
        let plantnetKey = Bundle.main.object(forInfoDictionaryKey: "PLANTNET_API_KEY") as? String ?? ""
        let geminiKey = Bundle.main.object(forInfoDictionaryKey: "GEMINI_API_KEY") as? String ?? ""
        let revenueCatKey = Bundle.main.object(forInfoDictionaryKey: "REVENUECAT_API_KEY") as? String ?? ""

        KoinKt.doInitKoin(
            useMock: false,
            plantnetApiKey: plantnetKey,
            geminiApiKey: geminiKey,
            revenueCatApiKey: revenueCatKey
        )
    }

    private func setupAdMobRegistry() {
        PlatformViewRegistry.shared.bannerFactory = { adUnitId in
            let bannerView = BannerView(adSize: AdSizeBanner)
            bannerView.adUnitID = adUnitId
            if let windowScene = UIApplication.shared.connectedScenes.first as? UIWindowScene,
               let rootVC = windowScene.windows.first?.rootViewController {
                bannerView.rootViewController = rootVC
            }
            bannerView.load(Request())
            return bannerView
        }
        
        PlatformViewRegistry.shared.nativeAdFactory = { adUnitId, isMedium in
            let container = NativeAdViewContainer()
            container.setup(adUnitId: adUnitId, isMedium: isMedium.boolValue)
            return container
        }
    }
}
#endif

@main
struct TuinMaatApp: App {
    @UIApplicationDelegateAdaptor(AppDelegate.self) var delegate

    var body: some Scene {
        WindowGroup {
            ComposeView()
                .ignoresSafeArea(.all, edges: .bottom)
                .onOpenURL { url in
                    handleDeepLink(url)
                }
        }
    }

    private func handleDeepLink(_ url: URL) {
        if let components = URLComponents(url: url, resolvingAgainstBaseURL: true) {
            let gardenId = components.queryItems?.first(where: { $0.name == "gardenId" })?.value
            if let gid = gardenId {
                KoinHelper().getDeepLinkHandler().handleJoinGarden(gardenId: gid) { _, _ in }
            }
        }
    }
}

class NativeAdViewContainer: UIView, NativeAdLoaderDelegate {
    private var adLoader: AdLoader?
    private var nativeAdView: NativeAdView?
    private var adUnitId: String = ""
    private var isMedium: Bool = true

    func setup(adUnitId: String, isMedium: Bool) {
        self.adUnitId = adUnitId
        self.isMedium = isMedium
        self.backgroundColor = .clear
        loadAd()
    }

    private func loadAd() {
        guard let windowScene = UIApplication.shared.connectedScenes.first as? UIWindowScene,
              let rootVC = windowScene.windows.first?.rootViewController else { return }

        adLoader = AdLoader(adUnitID: adUnitId, rootViewController: rootVC, adTypes: [.native], options: nil)
        adLoader?.delegate = self
        adLoader?.load(Request())
    }

    func adLoader(_ adLoader: AdLoader, didReceive nativeAd: NativeAd) {
        nativeAdView?.removeFromSuperview()

        let adView = NativeAdView()
        self.addSubview(adView)
        adView.translatesAutoresizingMaskIntoConstraints = false
        adView.backgroundColor = .white
        adView.layer.cornerRadius = 12
        adView.clipsToBounds = true

        NSLayoutConstraint.activate([
            adView.topAnchor.constraint(equalTo: self.topAnchor),
            adView.bottomAnchor.constraint(equalTo: self.bottomAnchor),
            adView.leadingAnchor.constraint(equalTo: self.leadingAnchor),
            adView.trailingAnchor.constraint(equalTo: self.trailingAnchor)
        ])

        let adBadge = UILabel()
        adBadge.text = " Ad "
        adBadge.font = .systemFont(ofSize: 10, weight: .bold)
        adBadge.textColor = .white
        adBadge.backgroundColor = UIColor(red: 1.0, green: 0.65, blue: 0.0, alpha: 1.0)
        adBadge.layer.cornerRadius = 3
        adBadge.clipsToBounds = true
        adBadge.isUserInteractionEnabled = false
        adView.addSubview(adBadge)
        adBadge.translatesAutoresizingMaskIntoConstraints = false

        let adChoicesView = AdChoicesView()
        adChoicesView.isUserInteractionEnabled = false
        adView.addSubview(adChoicesView)
        adChoicesView.translatesAutoresizingMaskIntoConstraints = false
        adView.adChoicesView = adChoicesView

        let iconView = UIImageView()
        iconView.contentMode = .scaleAspectFill
        iconView.layer.cornerRadius = 6
        iconView.clipsToBounds = true
        iconView.isUserInteractionEnabled = false
        adView.addSubview(iconView)
        iconView.translatesAutoresizingMaskIntoConstraints = false
        adView.iconView = iconView

        let headlineLabel = UILabel()
        headlineLabel.font = .boldSystemFont(ofSize: 15)
        headlineLabel.textColor = UIColor(red: 0.18, green: 0.49, blue: 0.20, alpha: 1.0)
        headlineLabel.isUserInteractionEnabled = false
        adView.addSubview(headlineLabel)
        headlineLabel.translatesAutoresizingMaskIntoConstraints = false
        adView.headlineView = headlineLabel

        let bodyLabel = UILabel()
        bodyLabel.font = .systemFont(ofSize: 12)
        bodyLabel.textColor = .darkGray
        bodyLabel.numberOfLines = 1
        bodyLabel.isUserInteractionEnabled = false
        adView.addSubview(bodyLabel)
        bodyLabel.translatesAutoresizingMaskIntoConstraints = false
        adView.bodyView = bodyLabel

        let mediaView = MediaView()
        mediaView.isUserInteractionEnabled = true 
        adView.addSubview(mediaView)
        mediaView.translatesAutoresizingMaskIntoConstraints = false
        adView.mediaView = mediaView

        let ctaButton = UIButton()
        ctaButton.backgroundColor = UIColor(red: 0.48, green: 0.65, blue: 0.36, alpha: 1.0)
        ctaButton.setTitleColor(.white, for: .normal)
        ctaButton.titleLabel?.font = .boldSystemFont(ofSize: 14)
        ctaButton.layer.cornerRadius = 8
        ctaButton.isUserInteractionEnabled = false 
        adView.addSubview(ctaButton)
        ctaButton.translatesAutoresizingMaskIntoConstraints = false
        adView.callToActionView = ctaButton

        NSLayoutConstraint.activate([
            adBadge.topAnchor.constraint(equalTo: adView.topAnchor, constant: 8),
            adBadge.leadingAnchor.constraint(equalTo: adView.leadingAnchor, constant: 8),
            adChoicesView.topAnchor.constraint(equalTo: adView.topAnchor),
            adChoicesView.trailingAnchor.constraint(equalTo: adView.trailingAnchor),
            adChoicesView.widthAnchor.constraint(equalToConstant: 24),
            adChoicesView.heightAnchor.constraint(equalToConstant: 24),
            iconView.topAnchor.constraint(equalTo: adBadge.bottomAnchor, constant: 6),
            iconView.leadingAnchor.constraint(equalTo: adView.leadingAnchor, constant: 8),
            iconView.widthAnchor.constraint(equalToConstant: 40),
            iconView.heightAnchor.constraint(equalToConstant: 40),
            headlineLabel.topAnchor.constraint(equalTo: adBadge.topAnchor),
            headlineLabel.leadingAnchor.constraint(equalTo: adBadge.trailingAnchor, constant: 8),
            headlineLabel.trailingAnchor.constraint(equalTo: adChoicesView.leadingAnchor, constant: -8),
            bodyLabel.topAnchor.constraint(equalTo: headlineLabel.bottomAnchor, constant: 2),
            bodyLabel.leadingAnchor.constraint(equalTo: headlineLabel.leadingAnchor),
            bodyLabel.trailingAnchor.constraint(equalTo: headlineLabel.trailingAnchor),
            mediaView.topAnchor.constraint(equalTo: iconView.bottomAnchor, constant: 8),
            mediaView.leadingAnchor.constraint(equalTo: adView.leadingAnchor),
            mediaView.trailingAnchor.constraint(equalTo: adView.trailingAnchor),
            mediaView.heightAnchor.constraint(equalToConstant: 120),
            ctaButton.topAnchor.constraint(equalTo: mediaView.bottomAnchor, constant: 10),
            ctaButton.leadingAnchor.constraint(equalTo: adView.leadingAnchor, constant: 12),
            ctaButton.trailingAnchor.constraint(equalTo: adView.trailingAnchor, constant: -12),
            ctaButton.bottomAnchor.constraint(equalTo: adView.bottomAnchor, constant: -10),
            ctaButton.heightAnchor.constraint(equalToConstant: 44)
        ])

        (adView.headlineView as? UILabel)?.text = nativeAd.headline
        (adView.bodyView as? UILabel)?.text = nativeAd.body
        (adView.callToActionView as? UIButton)?.setTitle(nativeAd.callToAction, for: .normal)
        (adView.iconView as? UIImageView)?.image = nativeAd.icon?.image
        
        adView.nativeAd = nativeAd
        adView.layoutIfNeeded()
        self.nativeAdView = adView
    }

    func adLoader(_ adLoader: AdLoader, didFailToReceiveAdWithError error: Error) {
        print("Native ad failed to load: \(error.localizedDescription)")
    }
}
