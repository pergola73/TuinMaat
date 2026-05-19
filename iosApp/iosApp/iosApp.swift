import SwiftUI
import ComposeApp
import FirebaseCore

@main
struct TuinMaatApp: App {
    init() {
        // Gebruik een controle om te voorkomen dat een ontbrekende plist de app direct laat crashen
        if Bundle.main.path(forResource: "GoogleService-Info", ofType: "plist") != nil {
            FirebaseApp.configure()
        } else {
            print("WAARSCHUWING: GoogleService-Info.plist niet gevonden. Firebase is niet geconfigureerd.")
        }

        let plantnetKey = Bundle.main.object(forInfoDictionaryKey: "PLANTNET_API_KEY") as? String ?? ""
        let geminiKey = Bundle.main.object(forInfoDictionaryKey: "GEMINI_API_KEY") as? String ?? ""

        do {
            KoinKt.doInitKoin(useMock: false, plantnetApiKey: plantnetKey, geminiApiKey: geminiKey)
        } catch {
            print("FOUT BIJ KOIN INITIALISATIE: \(error)")
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
