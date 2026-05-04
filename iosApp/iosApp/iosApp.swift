import SwiftUI
import ComposeApp

@main
struct iosApp: App {
    init() {
        KoinKt.doInitKoin(useMock:true)
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
