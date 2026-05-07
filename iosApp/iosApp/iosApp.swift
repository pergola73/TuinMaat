import SwiftUI
import ComposeApp
import FirebaseCore

@main
struct iosApp: App {
    init() {
        // Initialiseer Firebase voordat Koin of andere diensten starten
        FirebaseApp.configure()

        do {
            // Start Koin
            KoinKt.doInitKoin(useMock: false)
        } catch {
            print("FOUT BIJ KOIN INITIALISATIE: \(error)")
        }
    }

    var body: some Scene {
        WindowGroup {
            ComposeView()
                .ignoresSafeArea(.all, edges: .bottom) // Zorg dat Compose het hele scherm vult
        }
    }
}
