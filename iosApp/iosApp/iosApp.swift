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
        }
    }
}
