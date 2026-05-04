import SwiftUI
import ComposeApp

@main
struct iosApp: App {
    init() {
        do {
            // We proberen Koin te starten
            KoinKt.doInitKoin(useMock: true)
        } catch {
            // Als het crasht, printen we het in de console (Appetize kan dit soms opvangen)
            print("FOUT BIJ KOIN INITIALISATIE: \(error)")
        }
    }

    var body: some Scene {
        WindowGroup {
            // Een hele simpele "Hello World" voor de zekerheid
            Text("App gestart")
        }
    }
}
