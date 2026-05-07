import SwiftUI
import ComposeApp
import FirebaseCore

@main
struct iosApp: App {
    init() {
        // Gebruik een controle om te voorkomen dat een ontbrekende plist de app direct laat crashen
        if Bundle.main.path(forResource: "GoogleService-Info", ofType: "plist") != nil {
            FirebaseApp.configure()
        } else {
            print("WAARSCHUWING: GoogleService-Info.plist niet gevonden. Firebase is niet geconfigureerd.")
        }

        do {
            KoinKt.doInitKoin(useMock: false)
        } catch {
            print("FOUT BIJ KOIN INITIALISATIE: \(error)")
        }
    }

    var body: some Scene {
        WindowGroup {
            ZStack {
                Color.red // Als je rood ziet, werkt SwiftUI maar wordt de Compose view niet getoond of is hij transparant
                ComposeView()
                    .ignoresSafeArea(.all, edges: .bottom)
            }
        }
    }
}
