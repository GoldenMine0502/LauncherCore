package kr.goldenmine.launchercore

import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp
import kr.goldenmine.inuminecraftlauncher.launcher.LauncherDirectories
import kr.goldenmine.launchercore.util.LoopUtil
import net.technicpack.launcher.io.TechnicUserStore
import net.technicpack.minecraftcore.microsoft.auth.MicrosoftAuthenticator
import net.technicpack.minecraftcore.microsoft.auth.MicrosoftUser
import net.technicpack.minecraftcore.microsoft.auth.UserModel
import net.technicpack.utilslib.DesktopUtils
import org.openqa.selenium.By
import org.openqa.selenium.WebElement
import org.openqa.selenium.chrome.ChromeDriver
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.io.IOException

class UserAdministrator(launcherDirectories: LauncherDirectories) {
    private val log: Logger = LoggerFactory.getLogger(UserAdministrator::class.java)

    val microsoftAuthenticator = MicrosoftAuthenticator(File(launcherDirectories.launcherDirectory, "oauth"))
    val users: TechnicUserStore = TechnicUserStore.load(File(launcherDirectories.launcherDirectory, "users.json"))
    private val userModel = UserModel(users, microsoftAuthenticator)

    fun login(username: String?, browser: AuthorizationCodeInstalledApp.Browser): MicrosoftUser {
        log.info(users.users.toString())

        val existingUser = if(username != null) users.getUser(username) else null

        return if (existingUser == null) {
            val microsoftUser = userModel.microsoftAuthenticator.loginNewUser(browser)
            users.addUser(microsoftUser)
            log.info(microsoftUser.toString())
            microsoftUser
        } else { // refresh
            try {
                userModel.microsoftAuthenticator.refreshSession(existingUser as MicrosoftUser)
//                users.save()

                existingUser
            } catch (ex: Exception) {
                log.info("refresh token is invalid.")
                throw ex
            }
        }
    }

}