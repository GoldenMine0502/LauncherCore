package kr.goldenmine.launchercore

import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp
import kr.goldenmine.inuminecraftlauncher.launcher.DefaultLauncherDirectories
import net.technicpack.minecraftcore.microsoft.auth.MicrosoftAuthenticator
import net.technicpack.utilslib.DesktopUtils
import org.junit.jupiter.api.Test
import java.io.File

class LoginAutomatic {
    @Test
    fun openBrowser() {
        val browser = AuthorizationCodeInstalledApp.Browser { url: String? -> DesktopUtils.browseUrl(url) }

        val directories = DefaultLauncherDirectories(File("test"))
        val userAdministrator = UserAdministrator(directories)

        userAdministrator.microsoftAuthenticator.loginNewUser(browser)
    }
}