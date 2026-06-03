import SwiftUI
import ComposeApp
import FirebaseCore
import GoogleMobileAds

@main
struct TuinMaatApp: App {
    init() {
        // Gebruik een controle om te voorkomen dat een ontbrekende plist de app direct laat crashen
        if Bundle.main.path(forResource: "GoogleService-Info", ofType: "plist") != nil {
            FirebaseApp.configure()
        } else {
            print("WAARSCHUWING: GoogleService-Info.plist niet gevonden. Firebase is niet geconfigureerd.")
        }

        // Initialize AdMob (v12 style)
        MobileAds.shared.start(completionHandler: nil)

        let plantnetKey = Bundle.main.object(forInfoDictionaryKey: "PLANTNET_API_KEY") as? String ?? ""
        let geminiKey = Bundle.main.object(forInfoDictionaryKey: "GEMINI_API_KEY") as? String ?? ""
        let revenueCatKey = Bundle.main.object(forInfoDictionaryKey: "REVENUECAT_API_KEY") as? String ?? ""

        do {
            KoinKt.doInitKoin(
                useMock: false,
                plantnetApiKey: plantnetKey,
                geminiApiKey: geminiKey,
                revenueCatApiKey: revenueCatKey
            )
            
            // Registreer AdMob views voor de KMP interface
            setupAdMobRegistry()
        } catch {
            print("FOUT BIJ KOIN INITIALISATIE: \(error)")
        }
    }

    private func setupAdMobRegistry() {
        PlatformViewRegistry.shared.bannerFactory = { adUnitId in
            let bannerView = BannerView(adSize: AdSizeBanner)
            bannerView.adUnitID = adUnitId
            
            // Haal de rootViewController op op een veilige manier voor iOS 15+
            if let windowScene = UIApplication.shared.connectedScenes.first as? UIWindowScene,
               let rootVC = windowScene.windows.first?.rootViewController {
                bannerView.rootViewController = rootVC
            }

            bannerView.load(Request())
            return bannerView
        }
        
        PlatformViewRegistry.shared.nativeAdFactory = { adUnitId in
            let nativeAdContainer = UIView()
            // Hier kan de gebruiker later een specifieke GADNativeAdView implementeren
            // Voor nu tonen we een subtiele placeholder die aangeeft dat het werkt
            nativeAdContainer.backgroundColor = UIColor(red: 0.96, green: 0.96, blue: 0.94, alpha: 1.0) // ZachtBeige
            nativeAdContainer.layer.cornerRadius = 20
            
            let label = UILabel()
            label.text = "Advertentie"
            label.font = UIFont.systemFont(ofSize: 10)
            label.textColor = .gray
            label.translatesAutoresizingMaskIntoConstraints = false
            nativeAdContainer.addSubview(label)
            
            NSLayoutConstraint.activate([
                label.centerXAnchor.constraint(equalTo: nativeAdContainer.centerXAnchor),
                label.centerYAnchor.constraint(equalTo: nativeAdContainer.centerYAnchor)
            ])
            
            return nativeAdContainer
        }
    }

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
                KoinHelper().getDeepLinkHandler().handleJoinGarden(gardenId: gid) { _, _ in
                    // Afhandeling gebeurt in de handler zelf via MessageService
                }
            }
        }
    }
}
